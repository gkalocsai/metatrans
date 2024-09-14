package syntax.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import syntax.Rule;

public class IndirectRecursionFinder {

    static List<Stack<Rule>> find(Map<String, ArrayList<Rule>> map, String rootGroup) throws GrammarException {
        List<Stack<Rule>> result = new LinkedList<>();
        Map<String, ArrayList<Rule>> grammar = map;
        Map<Rule, Integer> dotIndices = new HashMap<>();
        Map<String, Integer> groupIndices = new HashMap<>();
        ArrayList<Rule> st = new ArrayList<>();

        if (rootGroup != null) {
            Rule rootRule = grammar.get(rootGroup).get(0);
            if (indirectRecCheckWithRoot(result, grammar, dotIndices, groupIndices, st, rootRule)) return result;
        } else {
            for (ArrayList<Rule> dd : map.values()) {
                for (Rule r : dd) {
                    if (indirectRecCheckWithRoot(result, grammar, dotIndices, groupIndices, st, r)) return result;
                    dotIndices = new HashMap<>();
                    groupIndices = new HashMap<>();
                    st = new ArrayList<>();
                }
            }

        }

        return null;
    }

    private static boolean indirectRecCheckWithRoot(List<Stack<Rule>> result, Map<String, ArrayList<Rule>> grammar,
            Map<Rule, Integer> dotIndices, Map<String, Integer> groupIndices, ArrayList<Rule> st, Rule rootRule) {
        st.add(rootRule);
        while (!st.isEmpty()) {
            Rule top = st.get(st.size() - 1);
            int di = getDotIndex(top, dotIndices);
            if (di >= top.getRightSideLength()) {
                st.remove(st.size() - 1);
                if (!result.isEmpty()) return true;
                continue;
            }
            String groupName = top.getRightSideRef(di);
            if (top.getGroupname().equals(groupName)) {
                di = incDotIndex(top, dotIndices);
                continue;
            }

            Rule check = getNextRule(groupName, groupIndices, grammar);
            if (check == null) {
                di = incDotIndex(top, dotIndices);
                continue;
            }
            st.add(check);
            if (recursiveBranch(st, check) && lastTwoElementDiffers(st)) {

                result.add(topOfBranchStack(st));
            }
        }
        return !result.isEmpty();
    }

    private static boolean lastTwoElementDiffers(ArrayList<Rule> st) {

        if (st == null || st.size() < 2) return false;
        int stSize = st.size();
        return st.get(stSize - 1) != st.get(stSize - 2);
    }

    private static boolean recursiveBranch(ArrayList<Rule> st, Rule check) {
        for (String gn : check.getGroupRefsAsArray()) {
            for (Rule stackRule : st) {
                if (stackRule.getGroupname().equals(gn)) {
                    st.add(stackRule);
                    return true;
                }
            }
        }
        return false;
    }

    private static Rule getNextRule(String groupName, Map<String, Integer> groupIndices,
            Map<String, ArrayList<Rule>> grammar) {

        if (groupName == null) return null;
        Integer index = groupIndices.get(groupName);
        if (index == null) {
            groupIndices.put(groupName, 0);

            if (grammar.get(groupName) == null) return null; // TODO CHECKME
            return grammar.get(groupName).get(0);
        } else {
            if (grammar.get(groupName) == null) return null;
            if (grammar.get(groupName).size() > index + 1) {
                groupIndices.put(groupName, index + 1);
                return grammar.get(groupName).get(index + 1);
            } else return null;
        }

    }

    private static int incDotIndex(Rule current, Map<Rule, Integer> dotIndices) {

        dotIndices.put(current, dotIndices.get(current) + 1);
        return dotIndices.get(current);
    }

    private static Stack<Rule> topOfBranchStack(ArrayList<Rule> st) {
        Stack<Rule> result = new Stack<>();
        Rule last = st.get(st.size() - 1);
        for (int index = st.indexOf(last); index < st.size() - 1; index++) {
            result.push(st.get(index));
        }
        return result;
    }

    private static int getDotIndex(Rule current, Map<Rule, Integer> dotIndices) {
        Integer currentIndex = dotIndices.get(current);
        if (currentIndex == null) {
            dotIndices.put(current, 0);
            currentIndex = 0;
        } else {

            dotIndices.put(current, currentIndex);
        }
        return currentIndex;
    }

}
