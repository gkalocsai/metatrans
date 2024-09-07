package read;

import java.util.LinkedList;
import java.util.List;

import syntax.Rule;
import syntax.SyntaxElement;
import util.Util;

public class OptionalVConverter {

    public static List<Rule> createRules(ReadRuleWithOptionalVs rr, boolean repeater) {
        List<Rule> list = new LinkedList<>();
        if (rr.countOfOptionalElements() == 0) {
            Rule r = new Rule(rr.getGroupname(), rr.getRightside(), rr.getLabel(), rr.getCompilation(), repeater);
            list.add(r);
        } else {
            int digits = rr.countOfOptionalElements();
            int startNumberOfConvertBinaryNumber = 0;
            if (digits == rr.getRightside().length) {
                startNumberOfConvertBinaryNumber = 1;
            }
            for (int i = startNumberOfConvertBinaryNumber; i < Math.pow(2, digits); i++) {
                boolean[] needed = Util.convertBinaryNumber(i, digits);
                Rule r2 = createRule(rr, needed);
                list.add(r2);
            }
        }
        return list;
    }

    private static Rule createRule(ReadRuleWithOptionalVs rr, boolean[] needed) {
        int neededIndex = 0;
        int countOfNeededOptionalElements = Util.countTrue(needed);
        int rightSideLength = (rr.getRightside().length - rr.countOfOptionalElements()) + countOfNeededOptionalElements;

        SyntaxElement[] rv = new SyntaxElement[rightSideLength];
        String[] labels = new String[rightSideLength];
        int rightSideIndex = 0;
        for (int i = 0; i < rr.getRightside().length; i++) {
            boolean condition = rr.getOptional()[i] && !needed[neededIndex++];
            if (condition) {
                continue;
            }

            rv[rightSideIndex] = rr.getRightside()[i];
            labels[rightSideIndex] = rr.getLabel()[i];
            rightSideIndex++;

        }
        return new Rule(rr.getGroupname(), rv, labels, rr.getCompilation(), false);
    }
}
