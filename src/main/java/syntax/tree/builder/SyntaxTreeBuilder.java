package syntax.tree.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import descriptor.CharSequenceDescriptor;
import syntax.Rule;
import syntax.grammar.Grammarhost;
import syntax.tree.tools.Deduction;
import syntax.tree.tools.RuleInterval;
import syntax.tree.tools.SyntaxTreeBuilderToStr;
import util.StatefulList;

public class SyntaxTreeBuilder {

    private final Map<String, List<RuleInterval>> forward = new HashMap<String, List<RuleInterval>>();
    private final Map<String, List<RuleInterval>> backward = new HashMap<String, List<RuleInterval>>();

    private final Map<RuleInterval, RuleInterval[]> deduction = new HashMap<RuleInterval, RuleInterval[]>();



    private Set<String> ruleIntervalEquality = new HashSet<String>();

    StatefulList<RuleInterval> toCheck = new StatefulList<RuleInterval>();
    LinkedList<RuleInterval> toCheck2 = new LinkedList<RuleInterval>();

    private Grammarhost gh;
    private String source;

    private boolean printOut;



    private final RuleIntervalMatcher ruleIntervalMatcher = new RuleIntervalMatcher(forward, backward);
    private boolean showTree;

    public SyntaxTreeBuilder(Grammarhost gh, String source, boolean printOut) {
        this.gh = gh;
        this.source = source;

        this.printOut = printOut;
    }

    public SyntaxTreeBuilder(Grammarhost gh2, String string) {
        this(gh2, string, false);
    }

    public Map<RuleInterval, RuleInterval[]> build() {
        addInitialRules();
        processForward();
        return deduction;
    }

    private void processForward() {
        List<RuleInterval> toRemove = new LinkedList<RuleInterval>();
        int level = 1;
        List<Rule> rulesOnCurrentLevel = gh.getApplicationOrderToRuleList().get("" + level);
        while (rulesOnCurrentLevel != null) {
            toRemove.clear();
            boolean wasChange = true;
            while (wasChange) {
                wasChange = false;
                for (RuleInterval current : toCheck) {
                    for (Rule r : rulesOnCurrentLevel) {
                        List<Deduction> matches;
                        if (r.isRepeater()) {
                            matches = new LinkedList<Deduction>();
                            Deduction d = repeaterMatch(current, r);
                            if (d != null) matches.add(d);
                        }

                        else matches = getMatches(current, r);
                        for (Deduction d : matches) {
                            if (!gh.getUnsafeToDel().contains(d.getFrom().getRule().getGroupname()))
                                for (RuleInterval may : d.getTo()) {
                                    if (!gh.getUnsafeToDel().contains(may.getRule().getGroupname()))
                                        removeFromWards(may);
                                }
                            RuleInterval ri = d.getFrom();
                            String matchString = ri.matchingString();
                            if (!this.ruleIntervalEquality.contains(matchString)) {
                                ruleIntervalEquality.add(matchString);
                                toCheck2.addFirst(d.getFrom());
                                wasChange = true;
                                addToMaps(ri);
                            }
                        }
                    }
                }
            }
            if (isReadyInner()) return;

            Set<Rule> toKill = gh.getKillOnLevelToRuleList().get("" + (level));
            if (printOut) {
                System.out.println("Rules on level" + gh.getApplicationOrderToRuleList().get("" + level));
                System.out.println("Kill on level: " + gh.getKillOnLevelToRuleList().get("" + level));
            }

            if (toKill != null) {
                toCheck.selectFirstElement();
                StatefulList<RuleInterval>.Entry<RuleInterval> entry = toCheck.getEntry();
                if (entry != null) {
                    while (toKill.contains(entry.getValue().getRule())) {
                        if (entry == null || !toKill.contains(entry.getValue().getRule())) break;
                        toCheck.pop();
                        entry = toCheck.getEntry();
                    }
                }
                StatefulList<RuleInterval>.Entry<RuleInterval> prev = toCheck.getEntry();

                while (toCheck.stepNext()) {

                    if (toKill.contains(toCheck.get().getRule())) {
                        removeFromWards(toCheck.get());
                        toCheck.setEntry(prev);
                        toCheck.removeNext();
                    }
                    prev = toCheck.getEntry();
                }

            }
            if (printOut) System.out.println("Elements after kill: " + toCheck.size());
            for (RuleInterval ri : toCheck2) {
                toCheck.push(ri);
            }
            if (printOut) System.out.println("Elements after add: " + toCheck.size());
            if (showTree) System.out.println(this);

            toCheck2.clear();
            level++;
            if (printOut) System.out.println("Level: " + level);
            rulesOnCurrentLevel = gh.getApplicationOrderToRuleList().get("" + level);

        }
    }

    private void removeFromWards(RuleInterval ri) {
        forward.get("" + ri.getBegin()).remove(ri);
        backward.get("" + ri.getLast()).remove(ri);
    }

    private Deduction repeaterMatch(RuleInterval first, Rule parentRuleCandidate) {
        String[] repeatingRefs = parentRuleCandidate.getGroupRefsAsArray();
        if (!first.getRule().getGroupname().equals(parentRuleCandidate.getFirstV().getReferencedGroup())) return null;
        List<RuleInterval> matchList = repeaterMatchesFromLeft(first.getBegin(), repeatingRefs);

        if (matchList.isEmpty()) return null;

        RuleInterval[] to = new RuleInterval[matchList.size()];
        int toIndex = 0;
        for (RuleInterval ri : matchList) {
            to[toIndex] = ri;
            toIndex++;
        }
        RuleInterval np = new RuleInterval(parentRuleCandidate, to[0].getBegin(), to[to.length - 1].getLast());
        deduction.put(np, to);
        return new Deduction(np, to);
    }

    private List<RuleInterval> repeaterMatchesFromLeft(int begin, String[] repeatingRefs) {
        List<RuleInterval> result = new LinkedList<RuleInterval>();

        int current = goBack(begin, repeatingRefs);

        // FIXME : go back first
        for (;;) {
            RuleInterval[] oneRound = matchOneRound(current, repeatingRefs);
            if (oneRound == null) {
                break;
            }

            current = oneRound[oneRound.length - 1].getLast() + 1;
            Collections.addAll(result, oneRound);
        }
        return result;
    }

    private int goBack(int begin, String[] repeatingRefs) {
        int last = begin - 1;

        for (;;) {
            int earlier = goBackOneRound(last, repeatingRefs);
            if (earlier == last) break;
            last = earlier;
        }
        return last + 1;
    }

    // NOT TESTED
    private int goBackOneRound(int last, String[] repeatingRefs) {
        int current = last;
        for (int i = repeatingRefs.length - 1; i >= 0; i--) {
            String groupName = repeatingRefs[i];
            List<RuleInterval> cl = backward.get("" + current);
            if (cl == null) return last;
            boolean found = false;
            for (RuleInterval ri : cl) {
                if (ri.getRule().getGroupname().equals(groupName)) {
                    current = ri.getBegin() - 1;
                    found = true;
                    break;
                }
            }
            if (!found) return last;
        }
        return current;
    }

    private RuleInterval[] matchOneRound(int current, String[] repeatingRefs) {
        RuleInterval[] result = new RuleInterval[repeatingRefs.length];

        for (int i = 0; i < repeatingRefs.length; i++) {
            String groupName = repeatingRefs[i];
            List<RuleInterval> cl = forward.get("" + current);
            if (cl == null) return null;
            for (RuleInterval ri : cl) {
                if (ri.getRule().getGroupname().equals(groupName)) {
                    result[i] = ri;
                    current = ri.getLast() + 1;
                    break;
                }
            }
            if (result[i] == null) return null;
        }
        return result;
    }

    private List<Deduction> getMatches(RuleInterval current, Rule parent) {
        String[] refs = parent.getGroupRefsAsArray();

        int refGroupIndex = parent.getIndexOfRefGroup(current.getRule().getGroupname());

        List<Deduction> result = new LinkedList<Deduction>();
        if (refGroupIndex < 0) return result;

        while (refGroupIndex >= 0) {

            ruleIntervalMatcher.reInit(refs, refGroupIndex);
            List<RuleInterval[]> lefts = ruleIntervalMatcher.getLefts(current.getBegin() - 1);
            if (lefts == null) {
                refGroupIndex = parent.getIndexOfRefGroup(current.getRule().getGroupname(), refGroupIndex);
                continue;
            }

            List<RuleInterval[]> rights = ruleIntervalMatcher.getRigths(current.getLast() + 1);
            if (rights == null) {
                refGroupIndex = parent.getIndexOfRefGroup(current.getRule().getGroupname(), refGroupIndex);
                continue;
            }

            for (RuleInterval[] currentLeft : lefts) {
                for (RuleInterval[] currentRight : rights) {
                    result.add(createDeduction(currentLeft, current, currentRight, parent));
                }
            }
            refGroupIndex = parent.getIndexOfRefGroup(current.getRule().getGroupname(), refGroupIndex);
        }
        return result;
    }

    private Deduction createDeduction(RuleInterval[] currentLeft, RuleInterval current, RuleInterval[] currentRight,
            Rule parentRule) {

        int len = currentLeft.length + 1 + currentRight.length;
        RuleInterval[] to = new RuleInterval[len];
        int toIndex = 0;
        for (RuleInterval ri : currentLeft) {
            to[toIndex] = ri;
            toIndex++;
        }
        to[toIndex] = current;
        toIndex++;
        for (RuleInterval ri : currentRight) {
            to[toIndex] = ri;
            toIndex++;
        }

        RuleInterval parent = new RuleInterval(parentRule, to[0].getBegin(), to[to.length - 1].getLast());
        Deduction deduction2 = new Deduction(parent, to);
        deduction.put(deduction2.getFrom(), deduction2.getTo());
        return deduction2;
    }

    public boolean isReady() {
        return getRoot() != null;
    }

    private boolean isReadyInner() {
        if (gh.getRootGroups() == null || gh.getRootGroups().isEmpty())
            return false;
        return getRoot() != null;
    }

    private void addInitialRules() {
        List<Rule> csdRuleList = this.gh.getCsdRules();
        for (int i = 0; i < source.length(); i++)
            for (Rule r : csdRuleList) {
                CharSequenceDescriptor csd = r.getFirstV().getCsd();
                if (csd.matchesInFrom(source, i)) {
                    RuleInterval ruleInterval = new RuleInterval(r, i, i + csd.getDescribedLength() - 1);
                    addToMaps(ruleInterval);
                    toCheck.addLast(ruleInterval);
                }
            }
    }

    private void addToMaps(RuleInterval ruleInterval) {
        addToMap(forward, "" + ruleInterval.getBegin(), ruleInterval);
        addToMap(backward, "" + ruleInterval.getLast(), ruleInterval);
    }

    private boolean addToMap(Map<String, List<RuleInterval>> map, String key, RuleInterval ri) {
        if (map.get(key) == null) map.put(key, new LinkedList<RuleInterval>());
        List<RuleInterval> l = map.get(key);
        for (RuleInterval rio : l)
            if (ri.equals(rio)) return false;
        l.add(ri);
        return true;
    }

    public RuleInterval getRoot() {
        List<RuleInterval> candidates = forward.get("0");
        if (candidates == null) return null;
        RuleInterval riMax = null;
        int maxLevel = -1;
        if (gh.getRootGroups() == null || gh.getRootGroups().isEmpty()) {
            for (RuleInterval ri : candidates) {
                if (ri.getLast() != source.length() - 1) continue;
                Rule current = ri.getRule();
                int currentLevel = gh.getLevel(current);
                if (gh.getLevel(current) > maxLevel) {
                    maxLevel = currentLevel;
                    riMax = ri;
                }
            }
            return riMax;
        }

        for (RuleInterval ri : candidates)
            if (ri.getLast() == source.length() - 1) {
                if (gh.getRootGroups().contains(ri.getRule().getGroupname()))
                    return ri;
            }
        return null;

    }

    @Override
    public String toString() {
        List<char[]> x = SyntaxTreeBuilderToStr.toCharArrayList(forward, source);
        StringBuilder sb = new StringBuilder();
        for (char[] ca : x) {
            for (char c : ca)
                if (c == 0) sb.append(" ");
                else sb.append(c);

            sb.append("\n");
        }
        return sb.toString();
    }

    public boolean isPrintOut() {
        return printOut;
    }

    public void setPrintOut(boolean printOut) {
        this.printOut = printOut;
    }



    public void setShowTree(boolean showTree) {
        this.showTree = showTree;
    }

    public String getState() {
        StringBuilder sb = new StringBuilder();
        boolean recognized = true;
        int unrecBegin = 0;
        for (int i = 0; i < source.length(); i++) {
            List<RuleInterval> current = forward.get(""+i);
            if (current == null || current.isEmpty()) {
                if (recognized) {
                    recognized = false;
                    unrecBegin = i;
                }
            } else {
                if (!recognized) {
                    sb.append("!!!!??????????????????????!!!!!!!!:\n");
                    sourcePartShowString(sb, unrecBegin, i);
                    recognized = true;
                }
                sb.append(
                        "-------------------------------------------------------------------------------------------------------\n");
            }
            List<RuleInterval> recognizedList = getLongestRuleIntervals(current);
            if(!recognizedList.isEmpty()){
                for (RuleInterval ri : recognizedList) {
                    sb.append(ri.toGroupnameAndInterval() + "  ");
                }
                sb.append("\n");
                RuleInterval ri = recognizedList.get(0);
                int recEnd = ri.getLast();
                sourcePartShowString(sb, ri.getBegin(), recEnd + 1);
                i = recEnd;
            }
        }
        return sb.toString();
    }

    private List<RuleInterval> getLongestRuleIntervals(List<RuleInterval> current) {
        List<RuleInterval> result = new LinkedList<>();
        if (current == null)
            return result;
        int maxEnd = -1;
        for (RuleInterval ri : current) {
            if (ri.getLast() > maxEnd)
                maxEnd = ri.getLast();
        }
        for (RuleInterval ri : current) {
            if (ri.getLast() == maxEnd)
                result.add(ri);
        }
        return result;
    }

    public void sourcePartShowString(StringBuilder sb, int unrecBegin, int i) {
        if (i - unrecBegin < 80)
            sb.append(source.substring(unrecBegin, i));
        else {
            sb.append(source.substring(unrecBegin, unrecBegin + 40));
            sb.append("\n    ...     \n");
            sb.append(source.substring(i - 40, i));
        }
        sb.append("\n");
    }

}
