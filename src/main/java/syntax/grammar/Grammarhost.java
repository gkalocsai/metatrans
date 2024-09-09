package syntax.grammar;

import java.util.ArrayList;
import java.util.Collections;
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

    private Map<String, List<Rule>> levelInSyntaxTreeToRuleList = new HashMap<String, List<Rule>>();
    private Map<String, List<Rule>> killOnLevelToRuleList = new HashMap<String, List<Rule>>();

    private List<Rule> csdRuleList = null;

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

        // removeNonReachableRules();
        if (strict) {
            validateReferencesInAllRules();
        }
        if (strict) {
            checkForInvalidRecursions();
        }

        groupRulesByRecursion();

        allIds = getAllIdentifiers();

        IdCreator.InSTANCE.addExistingIds(allIds);
        fillApplicationOrderToRuleList();
        fillKillLevel();
        System.out.println();

    }

    private void fillKillLevel() {
        Map<String, String> groupName2Level = createGroupName2Level();

        // a szabály key-je milyen más szabályok jobb oldalán szerepel
        // azok szintjének a max-a +1;

        for (String key : grammar.keySet()) {
            ArrayList<Rule> currentGroup = grammar.get(key);
            Set<Rule> rightSides = getRightSideContains(key);
            int max = getMaxLevelOf(rightSides, groupName2Level) + 1;

            if (this.killOnLevelToRuleList.get("" + max) == null) {
                this.killOnLevelToRuleList.put("" + max, new ArrayList<Rule>());
            }
            List<Rule> rl2 = this.killOnLevelToRuleList.get("" + max);
            rl2.addAll(currentGroup);
        }
    }

    private int getMaxLevelOf(Set<Rule> rightSides, Map<String, String> groupName2Level) {
        int max = 0;
        for (Rule r : rightSides) {
            String level = groupName2Level.get(r.getGroupname());
            if (level == null) continue;
            int current = Integer.valueOf(level);
            if (current > max) max = current;
        }
        return max;
    }

    private Set<Rule> getRightSideContains(String groupName) {

        Set<Rule> result = new HashSet<Rule>();

        for (String key : grammar.keySet()) {
            ArrayList<Rule> currentGroup = grammar.get(key);
            for (Rule r : currentGroup) {
                String ar[] = r.getGroupRefsAsArray();
                for (String rs : ar) {
                    if (groupName.equals(rs)) {
                        result.add(r);
                        break;
                    }
                }

            }
        }
        return result;
    }

    private void fillApplicationOrderToRuleList() {

        Map<String, String> groupName2Order = createGroupName2Level();

        for (String key : grammar.keySet()) {
            ArrayList<Rule> currentGroup = grammar.get(key);
            for (Rule r : currentGroup) {
                String order = groupName2Order.get(r.getGroupname());
                List<Rule> ao = levelInSyntaxTreeToRuleList.get(order);
                if (ao == null) {
                    levelInSyntaxTreeToRuleList.put(order, new ArrayList<Rule>());
                }
                ao = levelInSyntaxTreeToRuleList.get(order);
                ao.add(r);
            }
        }
    }

    private Map<String, String> createGroupName2Level() {
        Map<String, String> groupName2Order = new HashMap<String, String>();
        for (Rule r : getCsdRules()) {
            groupName2Order.put(r.getGroupname(), "0");
        }

        boolean wasChange = true;
        while (wasChange) {
            wasChange = false;
            for (String key : grammar.keySet()) {
                if (groupName2Order.keySet().contains(key)) continue;
                ArrayList<Rule> currentGroup = grammar.get(key);
                Set<String> rightSides = getRightSides(currentGroup);
                rightSides.remove(key);
                if (groupName2Order.keySet().containsAll(rightSides)) {
                    int max = getMaxLevel(rightSides, groupName2Order);
                    groupName2Order.put(key, "" + (max + 1));
                    wasChange = true;
                }
            }
        }
        return groupName2Order;
    }

    private int getMaxLevel(Set<String> rightSides, Map<String, String> groupName2Order) {
        int max = 0;
        for (String s : rightSides) {
            int t = Integer.valueOf(groupName2Order.get(s));
            if (t > max) max = t;
        }
        return max;
    }

    private Set<String> getRightSides(ArrayList<Rule> currentGroup) {
        Set<String> result = new HashSet<String>();
        for (Rule r : currentGroup) {
            String[] arr = r.getGroupRefsAsArray();
            Collections.addAll(result, arr);
        }
        return result;
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
        if (csdRuleList != null) return csdRuleList;
        List<Rule> rList = createListFromAllRules();

        csdRuleList = new LinkedList<>();

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

    public Map<String, List<Rule>> getApplicationOrderToRuleList() {
        return levelInSyntaxTreeToRuleList;
    }

    public Map<String, List<Rule>> getKillOnLevelToRuleList() {
        return killOnLevelToRuleList;
    }

}
