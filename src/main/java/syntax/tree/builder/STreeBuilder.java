package syntax.tree.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import descriptor.CharSequenceDescriptor;
import syntax.Rule;
import syntax.grammar.Grammarhost;
import syntax.tree.tools.Deduction;
import syntax.tree.tools.RuleInterval;
import syntax.tree.tools.ToStr;

public class STreeBuilder {

    private final Map<String, List<RuleInterval>> forward = new LinkedHashMap<String, List<RuleInterval>>();
    private final Map<String, List<RuleInterval>> backward = new LinkedHashMap<String, List<RuleInterval>>();

    private final Map<RuleInterval, RuleInterval[]> deduction = new HashMap<RuleInterval, RuleInterval[]>();

    private Set<String> ruleIntervalEquality = new HashSet<String>();

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
        boolean wasChange = true;
        while (wasChange) {
            matched.clear();
            wasChange = false;
            for (String k : forward.keySet()) {
                List<RuleInterval> rl = forward.get(k);
                for (RuleInterval ri : rl)
                    for (Rule r : gh.getRefRules()) {
                        List<Deduction> current = matchFromRuleBegin(ri, r);
                        for (Deduction d : current) {
                            deduction.put(d.getFrom(), d.getTo());
                            matched.add(d);
                        }
                    }
            }
            wasChange = handleNewIntervals();

            if (isReady(gh.getRootGroup(), source.length()))
                return;
        }
    }

    private boolean isReady(String rootGroup, int length) {
        List<RuleInterval> list = forward.get("0");
        if (list == null)
            return false;
        for (RuleInterval ri : list)
            if (ri.getLast() >= length - 1 && ri.getRule().getGroupname().equals(rootGroup))
                return true;
        return false;
    }

    private boolean handleNewIntervals() {
        boolean wasChange = false;
        // a ruleIntervalEquality -re azért van szükség, hogy ne adjuk hozzá ugyanazt a
        // ruleIntervalt,
        // amit esetleg töröltünk az optimalizálás során
        for (Deduction d : matched) {
            RuleInterval ri = d.getFrom();
            String matchString = ri.matchingString();
            if (this.ruleIntervalEquality.contains(matchString))
                continue;
            else {
                wasChange = true;
                ruleIntervalEquality.add(matchString);
                addToMaps(ri);
                if (printOut)
                    System.out.println(ri + "  " + source.substring(ri.getBegin(), ri.getLast() + 1));
            }
        }
        return wasChange;
    }

    private List<Deduction> matchFromRuleBegin(RuleInterval part, Rule pattern) {

        List<Deduction> result = new LinkedList<Deduction>();
        String groupname = part.getRule().getGroupname();
        String[] patternRs = pattern.extractRefGroups();

        // Az első részre mindenképp kell illeszkednie RuleInterval-nak,
        // ez alapján döntünk az illesztésről

        if (patternRs[0].equals(groupname))
            result.addAll(matchAfterGroupIndex(part, pattern, 0));
        return result;
    }

    private List<Deduction> matchAfterGroupIndex(RuleInterval part, Rule pattern, int groupIndexInPattern) {
        List<Deduction> result = new LinkedList<Deduction>();

        String[] patternRs = pattern.extractRefGroups();

        int firstSourceIndex = part.getLast() + 1;

        ruleIntervalMatcher.reInit(patternRs, groupIndexInPattern);

        List<RuleInterval[]> rights = ruleIntervalMatcher.getRigths(firstSourceIndex);

        if (rights == null)
            return result;

        fillResultWithDeductions(part, pattern, result, rights);

        return result;
    }

    private void fillResultWithDeductions(RuleInterval part, Rule pattern, List<Deduction> result,
            List<RuleInterval[]> rights) {
        for (RuleInterval[] ria : rights) {
            RuleInterval[] n = createDeductionValue(part, ria);
            RuleInterval nri = new RuleInterval(pattern, part.getBegin(), n[n.length - 1].getLast());
            result.add(new Deduction(nri, n));

        }
    }

    private RuleInterval[] createDeductionValue(RuleInterval part, RuleInterval[] ria) {
        RuleInterval[] n = new RuleInterval[ria.length + 1];
        n[0] = part;
        int i = 1;
        for (RuleInterval ri : ria)
            n[i++] = ri;
        return n;
    }

    private void addInitialRules() {
        List<Rule> csdRuleList = this.gh.getCsdRules();
        for (int i = 0; i < source.length(); i++)
            for (Rule r : csdRuleList) {
                CharSequenceDescriptor csd = r.getFirstV().getCsd();
                if (csd.matchesInFrom(source, i))
                    addToMaps(new RuleInterval(r, i, i + csd.getDescribedLength() - 1));
            }
    }

    private void addToMaps(RuleInterval ruleInterval) {
        addToMap(forward, "" + ruleInterval.getBegin(), ruleInterval);
        addToMap(backward, "" + ruleInterval.getLast(), ruleInterval);
    }

    private boolean addToMap(Map<String, List<RuleInterval>> map, String key, RuleInterval ri) {
        if (map.get(key) == null)
            map.put(key, new LinkedList<RuleInterval>());
        List<RuleInterval> l = map.get(key);
        for (RuleInterval rio : l)
            if (ri.equals(rio))
                return false;
        l.add(ri);
        return true;
    }

    public RuleInterval getRoot() {
        List<RuleInterval> candidates = forward.get("0");
        if (candidates == null)
            return null;
        for (RuleInterval ri : candidates)
            if (ri.getLast() == source.length() - 1 && ri.getRule().getGroupname().equals(this.rootName))
                return ri;
        return null;

    }

    @Override
    public String toString() {
        List<char[]> x = ToStr.toCharArrayList(forward, source);
        StringBuilder sb = new StringBuilder();
        for (char[] ca : x) {
            for (char c : ca)
                if (c == 0)
                    sb.append(" ");
                else
                    sb.append(c);

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
