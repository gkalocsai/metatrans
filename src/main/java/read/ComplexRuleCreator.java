package read;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import compilation.CompilationElement;
import descriptor.CharSequenceDescriptor;
import descriptor.GroupName;
import syntax.Rule;
import syntax.SyntaxElement;
import util.CharSeqUtil;

public class ComplexRuleCreator {

    public static List<Rule> createRules(String groupname, String otherParts) {
        int afterRightSide = CharSeqUtil.getNonQuotedIndex(otherParts, ">>", 0);

        String syntax = "";
        String compilation = "";

        if (afterRightSide < 0) {
            syntax = otherParts.trim();
        } else {
            syntax = otherParts.substring(0, afterRightSide).trim();
            compilation = otherParts.substring(afterRightSide + 2).trim();
        }
        boolean repeater = false;
        if (syntax.startsWith("...")) {
            repeater = true;
            syntax = syntax.substring(3);
        }

        CompilationElement[] ces = createCompilation(compilation);

        List<String> syntaxPartStrings = splitSyntax(syntax);

        boolean[] optional = new boolean[syntaxPartStrings.size()];
        SyntaxElement[] rightside = new SyntaxElement[syntaxPartStrings.size()];
        String[] label = new String[syntaxPartStrings.size()];

        int currentIndex = 0;
        for (String part : syntaxPartStrings) {
            optional[currentIndex] = isOptional(part);
            label[currentIndex] = getLabel(part);
            rightside[currentIndex] = getV(part);
            currentIndex++;
        }

        ReadRuleWithOptionalVs rr = new ReadRuleWithOptionalVs(groupname, optional, label, rightside, ces);
        return OptionalVConverter.createRules(rr, repeater);
    }

    private static SyntaxElement getV(String part) {
        if (isOptional(part) || part.startsWith(".")) part = part.substring(1).trim();

        int colonIndex = CharSeqUtil.getNonQuotedIndex(part, ":", 0);
        if (colonIndex >= 0) {
            part = part.substring(colonIndex + 1).trim();
        }
        char first = part.charAt(0);
        if (first != '\'' && first != '\"') {
            return new GroupName(part);
        } else {
            CharSequenceDescriptor csd = new CharSequenceDescriptor(part.substring(1, part.length() - 1));
            return csd;
        }
    }

    private static String getLabel(String part) {
        int colonIndex = CharSeqUtil.getNonQuotedIndex(part, ":", 0);
        if (colonIndex < 0) return "";
        if (isOptional(part)) return part.substring(1, colonIndex).trim();
        else return part.substring(0, colonIndex).trim();
    }

    private static boolean isOptional(String part) {
        return part.charAt(0) == '?';
    }

    private static List<String> splitSyntax(String syntax) {
        List<String> result = new LinkedList<>();
        for (int i = 0; i < syntax.length(); i++) {
            int lastCharOfPart = getLastCharOfPart(syntax, i);
            result.add(syntax.substring(i, lastCharOfPart + 1).trim());
            i = lastCharOfPart;
        }
        return result;
    }

    private static int getLastCharOfPart(String syntax, int from) {
        char ch = syntax.charAt(from);
        while (Character.isWhitespace(ch) || ch == '?') {
            from++;
            ch = syntax.charAt(from);
        }

        for (int i = from; i < syntax.length(); i++) {
            ch = syntax.charAt(i);
            if (ch == '\"' || ch == '\'') return CharSeqUtil.getClosingIndexInEscaped(syntax, i);
            if (ch == ' ') {
                char prev = syntax.charAt(i - 1);
                if (prev != '?' && prev != ':') return i - 1;
            }
        }
        return syntax.length() - 1;
    }

    private static CompilationElement[] createCompilation(String compilationString) {

        List<String> compilationElements = splitBySpaces(compilationString);

        CompilationElement[] result = new CompilationElement[compilationElements.size()];
        for (int i = 0; i < compilationElements.size(); i++) {
            String temp = compilationElements.get(i);
            result[i] = new CompilationElement(temp);
        }

        return result;
    }

    public static List<String> splitBySpaces(String compilationString) {
        List<String> result = new ArrayList<>();

        compilationString = compilationString.trim();
        int nextSpaceIndex = 0;
        do {
            nextSpaceIndex = CharSeqUtil.getNonQuotedIndex(compilationString, " ", 0);
            if (nextSpaceIndex < 0) {
                result.add(compilationString);
            } else {
                result.add(compilationString.substring(0, nextSpaceIndex));
                compilationString = compilationString.substring(nextSpaceIndex + 1).trim();
            }
        } while (nextSpaceIndex > 0);
        return result;
    }
}
