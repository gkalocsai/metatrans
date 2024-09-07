package syntax;

import java.util.LinkedList;
import java.util.List;

import compilation.CompilationElement;
import descriptor.CharSequenceDescriptor;
import descriptor.GroupName;

public class RuleCreator {

    public static LinkedList<Rule> createRuleList(String commaSeparated) {
        LinkedList<Rule> result = new LinkedList<>();
        String[] splitted = commaSeparated.split(",");
        for (String s : splitted) {
            result.add(RuleCreator.createRule(s.trim()));
        }
        return result;
    }

    public static Rule createRule(String ruleWithArrow) {

        String[] sides = ruleWithArrow.split("->");
        String groupname = sides[0];

        String ruleWithoutGroupnameAndArrow = sides[1];
        boolean rep = false;
        if (ruleWithoutGroupnameAndArrow.startsWith("...")) {
            rep = true;
            ruleWithoutGroupnameAndArrow = ruleWithoutGroupnameAndArrow.substring(3);
        }

        String[] rightSideAndCompilation = ruleWithoutGroupnameAndArrow.split(">>");

        List<String> rightSideElementsInList = createrightSideElementsInList(rightSideAndCompilation[0]);

        SyntaxElement[] rightside = createRightsideVs(rightSideElementsInList);
        String[] labels = createLabels(rightSideElementsInList);
        CompilationElement[] compilation;
        if (rightSideAndCompilation.length > 1) {
            String compilationString = rightSideAndCompilation[1];
            compilation = createCompilation(compilationString);
        } else {
            compilation = new CompilationElement[0];
        }
        return new Rule(groupname, rightside, labels, compilation, rep);

    }

    private static List<String> createrightSideElementsInList(String rightSide) {
        List<String> result = new LinkedList<>();
        char last = ' ';
        StringBuilder sb = new StringBuilder();
        boolean insideParenthesis = false;
        for (int i = 0; i < rightSide.length(); i++) {
            char c = rightSide.charAt(i);
            if (c == '(' && last != '\\') insideParenthesis = true;
            if (c == ')' && last != '\\') insideParenthesis = false;
            if (c == ' ' && !insideParenthesis) {
                result.add(sb.toString());
                sb = new StringBuilder();
                continue;
            }
            sb.append(c);
            last = c;
        }
        if (sb.length() != 0) result.add(sb.toString());
        return result;
    }

    private static CompilationElement[] createCompilation(String compilationString) {
        String[] compilationElements = compilationString.split(" ");
        CompilationElement[] result = new CompilationElement[compilationElements.length];
        for (int i = 0; i < compilationElements.length; i++) {
            String temp = compilationElements[i];
            result[i] = new CompilationElement(temp);
        }

        return result;
    }

    private static SyntaxElement[] createRightsideVs(List<String> rightSideElementsInList) {
        SyntaxElement[] vs = new SyntaxElement[rightSideElementsInList.size()];
        int i = 0;
        for (String rse : rightSideElementsInList) {
            String vString;
            int colonIndex = rse.indexOf(":");
            if (colonIndex >= 0 && (colonIndex < rse.indexOf('\'') || rse.indexOf('\'') < 0)) {
                vString = rse.substring(colonIndex + 1);
            } else {
                vString = rse;
            }
            SyntaxElement v;
            if (vString.indexOf('\'') == 0) {
                v = new CharSequenceDescriptor(vString.substring(1));
            } else {
                v = new GroupName(vString);
            }
            vs[i] = v;
            i++;
        }
        return vs;
    }

    private static String[] createLabels(List<String> rightSideElementsInList) {
        String[] labels = new String[rightSideElementsInList.size()];
        int i = 0;
        for (String rse : rightSideElementsInList) {
            String label = "";
            int colonIndex = rse.indexOf(":");
            if (colonIndex >= 0 && (colonIndex < rse.indexOf('\'') || rse.indexOf('\'') < 0)) {
                label = rse.substring(0, colonIndex);
            }
            labels[i] = label;
            i++;
        }
        return labels;
    }
}
