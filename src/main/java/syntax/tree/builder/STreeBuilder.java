package syntax.tree.builder;

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

    private Grammarhost gh;
    private String source;

    private boolean printOut;

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
        while (!toCheck.isEmpty()) {
            RuleInterval current = toCheck.pop();
            matched.clear();
            for (Rule r : gh.getRefRules()) {
                // System.out.println(r);
                matched.addAll(getMatches(current, r));
            }
            handleNewIntervals();
            // System.out.println(this);
            if (isReady()) return;
        }
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

    private boolean isReady() {
        return getRoot() != null;
    }

    private void handleNewIntervals() {
        if (matched == null) return;
        // a ruleIntervalEquality -re azért van szükség, hogy ne adjuk hozzá ugyanazt a
        // ruleIntervalt,
        // amit esetleg töröltünk az optimalizálás során
        for (Deduction d : matched) {
            RuleInterval ri = d.getFrom();
            String matchString = ri.matchingString();
            if (this.ruleIntervalEquality.contains(matchString)) continue;
            else {
                ruleIntervalEquality.add(matchString);
                removeFromMaps(d);
                addToMaps(ri);
                if (printOut) System.out.println(ri + "  " + source.substring(ri.getBegin(), ri.getLast() + 1));
            }
        }
    }

    private void removeFromMaps(Deduction d) {
        RuleInterval fr = d.getFrom();
        Rule m = fr.getRule();
        if (!Character.isUpperCase(m.getGroupname().charAt(0))) return;
        if (m.getRightSideLength() < 2) return;
        toCheck.push(fr);
        for (RuleInterval ri : d.getTo()) {
            forward.remove("" + ri.getBegin());
            backward.remove("" + ri.getLast());

        }

    }

    private void addInitialRules() {
        List<Rule> csdRuleList = this.gh.getCsdRules();
        for (int i = 0; i < source.length(); i++)
            for (Rule r : csdRuleList) {
                CharSequenceDescriptor csd = r.getFirstV().getCsd();
                if (csd.matchesInFrom(source, i)) addToMaps(new RuleInterval(r, i, i + csd.getDescribedLength() - 1));
            }
    }

    private void addToMaps(RuleInterval ruleInterval) {
        addToMap(forward, "" + ruleInterval.getBegin(), ruleInterval);
        addToMap(backward, "" + ruleInterval.getLast(), ruleInterval);
        toCheck.addFirst(ruleInterval);
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
