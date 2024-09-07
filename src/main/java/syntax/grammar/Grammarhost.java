package syntax.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import descriptor.CharSequenceDescriptor;
import descriptor.GroupName;
import syntax.Rule;
import syntax.SyntaxElement;

public class Grammarhost {

    private Map<String, ArrayList<Rule>> grammar;
    private Rule[] recursiveRefRules;
    private Rule[] nonRecursiveRefRules;

    private boolean strict = true;
    private String rootGroup;
    private Map<Rule, Integer> matchOrder;
    private Map<String, LinkedList<Rule>> applicableStorage;
    private Set<String> repeaterCatchers = new HashSet<String>();

    public List<Rule> getApplicableRuleList(String ref) {
        if (applicableStorage == null) {
            applicableStorage = new HashMap<>();
            for (ArrayList<Rule> alr : grammar.values()) {
                for (Rule r : alr) {
                    if (r.isDirectRecursive()) {
                        String s2 = r.getGroupname();
                        addToApplicableStorage(r, s2);
                    }

                    String s = r.getRightSideRef(r.getRightSideLength() - 1);
                    addToApplicableStorage(r, s);
                }
            }
        }

        List<Rule> result = applicableStorage.get(ref);
        if (result == null) {
            result = new LinkedList<>();
        }
        return result;
    }

    private void addToApplicableStorage(Rule r, String s) {
        if (applicableStorage.get(s) == null) {
            applicableStorage.put(s, new LinkedList<Rule>());
        }
        applicableStorage.get(s).addFirst(r);
    }

    public Grammarhost(List<Rule> rl, boolean strict) throws GrammarException {
        this.strict = strict;
        init(rl, null);

    }

    public Grammarhost(List<Rule> rl) throws GrammarException {
        init(rl, null);
    }

    public Grammarhost(List<Rule> rules, String rootGroup) throws GrammarException {

        init(rules, rootGroup);
    }

    private void init(List<Rule> rules, String rootGroup) throws GrammarException {
        Set<String> allIds = null;
        if (rules == null || rules.isEmpty()) {
            throw new GrammarException("ERROR: No grammar rules!");
        }
        if (rootGroup != null) {
            this.rootGroup = rootGroup;
        } else {
            this.rootGroup = rules.get(0).getGroupname();
        }

        this.grammar = createRuleMap(rules, rootGroup);
        pushDescriptors(rules);
        removeOneLongRecursiveRules();
        if (strict) {
            eliminateIndirectRecursion();
        }

        removeNonReachableRules();
        if (strict) {
            validateReferencesInAllRules();
        }
        if (strict) {
            checkForInvalidRecursions();
        }

        groupRulesByRecursion();
        createMatchOrderMap();
        allIds = getAllIdentifiers();

        for (Rule r : getRefRules()) {
            if (r.isRepeater()) {
                repeaterCatchers.add(r.getGroupRefsAsArray()[0]);
            }
        }

        IdCreator.InSTANCE.addExistingIds(allIds);
    }

    private void createMatchOrderMap() {
        matchOrder = new HashMap<>();
        int order = Integer.MAX_VALUE;
        for (ArrayList<Rule> alr : grammar.values()) {
            for (Rule r : alr) {
                matchOrder.put(r, order);
                order--;
            }
        }

    }

    private void validateReferencesInAllRules() throws GrammarException {

        Set<String> finisherGroupnames = findFinisherGroupnames();
        if (finisherGroupnames.size() != grammar.keySet().size()) {
            throw new GrammarException("Infinite recursion");
        }
    }

    private Set<String> findFinisherGroupnames() {
        Set<String> finisherGroupnames = new HashSet<>();
        for (Rule csd : getCsdRules()) {
            finisherGroupnames.add(csd.getGroupname());
        }
        int oldSize = 0;

        while (oldSize != finisherGroupnames.size()) {
            oldSize = finisherGroupnames.size();
            for (Rule r : getRefRules()) {
                if (finisherGroupnames.containsAll(r.getRightSideAsStrCollection())) {
                    finisherGroupnames.add(r.getGroupname());
                }
            }

        }
        return finisherGroupnames;
    }

    private void groupRulesByRecursion() {
        List<Rule> all = getRefRules();
        ArrayList<Rule> recRefRules = new ArrayList<>();
        ArrayList<Rule> nonRecRefRules = new ArrayList<>();

        for (Rule r : all) {
            if (r.isDirectRecursive()) {
                recRefRules.add(r);
            } else {
                nonRecRefRules.add(r);
            }
        }
        this.recursiveRefRules = new Rule[recRefRules.size()];

        for (int i = 0; i < recursiveRefRules.length; i++) {
            recursiveRefRules[i] = recRefRules.get(i);
        }

        this.nonRecursiveRefRules = new Rule[nonRecRefRules.size()];

        for (int i = 0; i < nonRecursiveRefRules.length; i++) {
            nonRecursiveRefRules[i] = nonRecRefRules.get(i);
        }

    }

    private void removeOneLongRecursiveRules() throws GrammarException {
        for (Rule r : getRefRules()) {
            if (r.getRightSideLength() == 1 && r.isDirectRecursive()) {
                List<Rule> group = grammar.get(r.getGroupname());
                if (group.size() == 1) {
                    grammar.remove(r.getGroupname());
                } else {
                    group.remove(r);
                    // throw new GrammarException("Infinite recursion in rule: "+r);
                }
            }
        }
    }

    private void checkForInvalidRecursions() throws GrammarException {
        for (Rule r : getRefRules()) {
            if (r.isMidRecursive() && (r.isLeftRecursive() || r.isRightRecursive())) {
                throw new GrammarException("Middle recursion must be single recursion. Rule: " + r.toString());
            } else if (r.hasMultipleMidRecursions()) {
                throw new GrammarException("Multiple middle recursion found. Rule: " + r.toString());
            }
        }
    }

    public void removeNonReachableRules() {
        HashSet<String> reachableGroupnames = new HashSet<>();
        reachableGroupnames.add(rootGroup);
        int oldSize = 0;

        while (oldSize != reachableGroupnames.size()) {
            oldSize = reachableGroupnames.size();
            HashSet<String> wave = new HashSet<>();
            for (String key : grammar.keySet()) {
                if (reachableGroupnames.contains(key)) {
                    for (Rule r : grammar.get(key)) {
                        for (String ref : r.getRightSideAsStrCollection()) {
                            if (ref != null) {
                                wave.add(ref);
                            }
                        }
                    }
                }
            }
            reachableGroupnames.addAll(wave);

        }
        Set<String> toDelete = new HashSet<>();
        for (String groupName : grammar.keySet()) {

            if (!reachableGroupnames.contains(groupName)) {
                toDelete.add(groupName);
            }
        }
        for (String key : toDelete) {
            grammar.remove(key);
        }
    }

    void eliminateIndirectRecursion() throws GrammarException {
        new IndirectRecursionEliminator().eliminate(grammar, getRootGroup(), true);

    }

    private static Map<String, ArrayList<Rule>> createRuleMap(List<Rule> rl, String rootGroup) {

        return new RuleMapStorage(rl, rootGroup).getGrammar();

    }

    private void pushDescriptors(List<Rule> rl) {
        for (Rule r : rl) {
            extractAndAddDescriptorRules(r);
        }
    }

    private void extractAndAddDescriptorRules(Rule r) {
        Map<String, CharSequenceDescriptor> freshGroupnames = new LinkedHashMap<>();

        SyntaxElement[] vs = r.getRightside();

        for (int i = 0; i < vs.length; i++) {
            SyntaxElement v = vs[i];
            if (v.isDescriptor()) {
                CharSequenceDescriptor csd = v.getCsd();
                String groupName = null;

                groupName = generateUnusedGroupName(freshGroupnames);

                freshGroupnames.put(groupName, csd);
                ArrayList<Rule> rlist = new ArrayList<>();
                rlist.add(createCsdRule(groupName, csd));
                grammar.put(groupName, rlist);

                vs[i] = new GroupName(groupName);
            }
        }
    }

    private String generateUnusedGroupName(Map<String, CharSequenceDescriptor> freshGroupnames) {
        // allIds = getAllIdentifiers();
        String candidate = IdCreator.InSTANCE.generateYetUnusedId("_");
        while (freshGroupnames.keySet().contains(candidate)) {
            candidate = IdCreator.InSTANCE.generateYetUnusedId("_");
        }
        return candidate;
    }

    private Rule createCsdRule(String groupName, CharSequenceDescriptor csd) {
        return new Rule(groupName, createVArray(csd), null, null, false);
    }

    private SyntaxElement[] createVArray(CharSequenceDescriptor csd) {
        SyntaxElement[] va = new SyntaxElement[1];
        va[0] = csd;
        return va;
    }

    public List<Rule> getRefRules() {
        List<Rule> rList = createListFromAllRules();
        List<Rule> refRuleList = new ArrayList<>();
        for (Rule r : rList) {
            if (!r.getFirstV().isDescriptor() && !"0".equals(r.getGroupname())) {

                refRuleList.add(r);
            }
        }
        return refRuleList;
    }

    public List<Rule> getCsdRules() {
        List<Rule> rList = createListFromAllRules();

        List<Rule> csdRuleList = new LinkedList<>();
        for (Rule r : rList) {
            if (r.getFirstV().isDescriptor()) {
                csdRuleList.add(r);
            }
        }
        return csdRuleList;
    }

    public Set<String> getAllIdentifiers() {
        Set<String> allIdentifiers = new HashSet<>();
        for (ArrayList<Rule> group : grammar.values()) {
            for (Rule r : group) {
                allIdentifiers.addAll(r.getUsedIdentifiers());
            }
        }
        return allIdentifiers;
    }

    public String getGrammarString() {
        return grammar.toString();
    }

    public Map<String, ArrayList<Rule>> getGrammar() {
        return grammar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ArrayList<Rule> x : grammar.values()) {
            for (Rule r : x) {
                sb.append(r.toStringWoCompilation());
                sb.append("\n");
            }

        }
        return sb.toString();
    }

    public List<Rule> createListFromAllRules() {
        List<Rule> all = new ArrayList<>();
        for (ArrayList<Rule> rl : grammar.values()) {
            all.addAll(rl);
        }
        return all;
    }

    public void setRootGroup(String newRoot) {
        this.rootGroup = newRoot;
        if (grammar.get(newRoot) == null) {
            throw new IllegalArgumentException("Group [" + newRoot + "] is not defined in the grammar");
        }

    }

    public String getRootGroup() {
        return this.rootGroup;
    }

    public Rule[] getRecursiveRefRules() {
        return recursiveRefRules;
    }

    public Rule[] getNonRecursiveRefRules() {
        return nonRecursiveRefRules;
    }

    public int getStrength(Rule rule) {
        return matchOrder.get(rule);

    }

    public Set<String> getRepeaterCathcers() {

        return this.repeaterCatchers;
    }

}
