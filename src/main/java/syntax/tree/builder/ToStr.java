package syntax.tree.builder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ToStr {

    private static final int RULE_STRING_SIZE = 20;

    public static List<char[]> toCharArrayList(Map<String, List<RuleInterval>> forward, String source) {
        List<char[]> result = new LinkedList<char[]>();
        List<RuleInterval> collected = new LinkedList<RuleInterval>();
        int max = source.length();
        for (String k : forward.keySet())
            for (RuleInterval ri : forward.get(k))
                collected.add(ri);
        char[] sourceLine = new char[(RULE_STRING_SIZE + 5) * max + 2];

        for (int i = 0; i < source.length(); i++)
            sourceLine[i * RULE_STRING_SIZE + RULE_STRING_SIZE / 2] = source.charAt(i);
        result.add(sourceLine);

        for (RuleInterval ri : collected) {
            boolean added = false;
            char[] newLine = null;
            for (char[] ca : result)
                if (add(ca, ri)) {
                    added = true;
                    break;
                }
            if (!added) {
                newLine = new char[(RULE_STRING_SIZE + 5) * max + 2];
                add(newLine, ri);
                result.add(newLine);
            }

        }
        return result;
    }

    private static boolean add(char[] ca, RuleInterval ri) {
        int begin = 1 + ri.getBegin() * RULE_STRING_SIZE;
        int last = 1 + (ri.getLast() + 1) * RULE_STRING_SIZE;
        if (noMoreSpace(ca, begin, last))
            return false;

        int placeLength = (last - begin) - 1;

        char[] r = createShowedRuleString(ri.getRule().toSyntax(), placeLength);

        for (int i = 0; i < r.length && begin + i < ca.length; i++)
            ca[begin + i] = r[i];
        return true;
    }

    private static boolean noMoreSpace(char[] ca, int begin, int last) {
        for (int i = begin; i < last && i < ca.length; i++)
            if (ca[i] != 0)
                return true;
        return false;
    }

    private static char[] createShowedRuleString(String syntax, int placeLength) {
        char[] result = new char[placeLength];
        Arrays.fill(result, '─');
        result[0] = '█';
        result[result.length - 1] = '█';// '▓';

        if (syntax.length() > placeLength)
            syntax = syntax.substring(0, placeLength - 2);
        int beginPos = (placeLength - syntax.length()) / 2;
        for (int i = 0; i < syntax.length(); i++)
            result[beginPos + i] = syntax.charAt(i);
        return result;
    }

}
