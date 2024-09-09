package syntax.tree.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import descriptor.CharSequenceDescriptor;
import syntax.Rule;
import syntax.grammar.Grammarhost;
import syntax.tree.tools.Deduction;
import syntax.tree.tools.RuleInterval;
import syntax.tree.tools.ToStr;

public class STreeBuilder {

    private final Map<String, List<RuleInterval>> forward = new HashMap<String, List<RuleInterval>>();
    private final Map<String, List<RuleInterval>> backward = new HashMap<String, List<RuleInterval>>();

    private final Map<RuleInterval, RuleInterval[]> deduction = new HashMap<RuleInterval, RuleInterval[]>();

    private Set<String> ruleIntervalEquality = new HashSet<String>();

    Stack<RuleInterval> toCheck = new Stack<RuleInterval>();
    List<RuleInterval> toCheck2 = new LinkedList<RuleInterval>();

    private Grammarhost gh;
    private String source;

    private boolean printOut = true;

    private String rootName;
    private List<Deduction> matched = new LinkedList<Deduction>();
    private final RuleIntervalMatcher ruleIntervalMatcher = new RuleIntervalMatcher(forward, backward);

    public STreeBuilder(Grammarhost gh, String source) {
        this.gh = gh;
        this.source = source;
        this.rootName = gh.getRootGroup();
    }

    public Map<RuleInterval, RuleInterval[]> build() {
        addInitialRules();
        processForward();
        return deduction;
    }

    private void processForward() {

        int level = 1;
        List<Rule> rulesOnCurrentLevel = gh.getApplicationOrderToRuleList().get("" + level);
        while (rulesOnCurrentLevel != null) {
            List<RuleInterval> toRemove = new LinkedList<RuleInterval>();
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
                            RuleInterval ri = d.getFrom();
                            String matchString = ri.matchingString();
                            if (!this.ruleIntervalEquality.contains(matchString)) {
                                ruleIntervalEquality.add(matchString);
                                toCheck2.addFirst(d.getFrom()); // a következő szintnek is!
                                wasChange = true;
                                addToMaps(ri);
                            }
                        }
                    }
                }
            }

            Set<Rule> toKill = gh.getKillOnLevelToRuleList().get("" + level);
            if (toKill != null) {
                for (RuleInterval ri : toCheck) {
                    if (toKill.contains(ri.getRule())) toRemove.add(ri);
                }
                toCheck.removeAll(toRemove);
            }

            for (RuleInterval ri : toCheck2) {
                toCheck.insertElementAt(ri, 0);
            }
            // toCheck.addAll(toCheck2);
            level++;
            rulesOnCurrentLevel = gh.getApplicationOrderToRuleList().get("" + level);

        }
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

    private void handleNewIntervals() {
        // a ruleIntervalEquality -re azért van szükség, hogy ne adjuk hozzá ugyanazt a
        // ruleIntervalt,
        // amit esetleg töröltünk az optimalizálás során

        for (Deduction d : matched) {
            RuleInterval ri = d.getFrom();
            String matchString = ri.matchingString();
            if (this.ruleIntervalEquality.contains(matchString)) continue;
            else {
                ruleIntervalEquality.add(matchString);
                toCheck.add(d.getFrom());
                addToMaps(ri);

            }
        }

    }

    private void addInitialRules() {
        List<Rule> csdRuleList = this.gh.getCsdRules();
        for (int i = 0; i < source.length(); i++)
            for (Rule r : csdRuleList) {
                CharSequenceDescriptor csd = r.getFirstV().getCsd();
                if (csd.matchesInFrom(source, i)) {
                    RuleInterval ruleInterval = new RuleInterval(r, i, i + csd.getDescribedLength() - 1);
                    addToMaps(ruleInterval);
                    toCheck.push(ruleInterval);
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
        for (RuleInterval ri : candidates)
            if (ri.getLast() == source.length() - 1 && ri.getRule().getGroupname().equals(this.rootName)) return ri;
        return null;

    }

    @Override
    public String toString() {
        List<char[]> x = ToStr.toCharArrayList(forward, source);
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
}
