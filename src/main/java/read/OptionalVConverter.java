package read;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import compilation.CompilationElement;
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
            if (digits > ReadRuleWithOptionalVs.MAX_ALLOWED_OPTIONAL_ELEMENTS)
                throw new RuntimeException("Too many optional elements: " + rr);
            int startNumberOfConvertBinaryNumber = 0;
            if (digits == rr.getRightside().length) {
                startNumberOfConvertBinaryNumber = 1;
            }
            for (Integer i : sortByBinaryOnes(digits)) {
                if (startNumberOfConvertBinaryNumber == 1 && i == 0)
                    continue;
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


        Set<String> unneededLabelsInComp = new HashSet<>();

        SyntaxElement[] rv = new SyntaxElement[rightSideLength];
        String[] labels = new String[rightSideLength];
        int rightSideIndex = 0;
        for (int i = 0; i < rr.getRightside().length; i++) {
            boolean condition = rr.getOptional()[i] && !needed[neededIndex++];
            if (condition) {
                unneededLabelsInComp.add(rr.getLabel()[i]);
                continue;
            }

            rv[rightSideIndex] = rr.getRightside()[i];
            labels[rightSideIndex] = rr.getLabel()[i];
            rightSideIndex++;

        }
        CompilationElement[] comp = rr.getCompilation();
        ArrayList<CompilationElement> validElements = new ArrayList<>();
        for (CompilationElement ce : comp) {
            if (!unneededLabelsInComp.contains(ce.getBase())) {
                validElements.add(ce);
            }
        }
        CompilationElement[] x=new CompilationElement[validElements.size()];
        int i=0;
        for(CompilationElement ce:validElements) {
            x[i] = ce;
            i++;
        }
        return new Rule(rr.getGroupname(), rv, labels, x, false);
    }

    public static List<Integer> sortByBinaryOnes(int bits) {

        List<Integer> l = new ArrayList<>();

        for (int i = (int) Math.pow(2, bits) - 1; i >= 0; i--) {
            l.add(i);
        }

        List<Integer> r = sortByBinaryOnes(l);

        Collections.reverse(r);

        return r;
    }

    private static List<Integer> sortByBinaryOnes(List<Integer> arr) {

        Collections.sort(arr);

        List<List<Integer>> v = new ArrayList<>(32);

        for (int i = 0; i < 32; i++) {
            v.add(new ArrayList<>());
        }

        for (int i = 0; i < arr.size(); i++) {
            int x = 0;
            int y = arr.get(i);
            while (y > 0) {
                if ((y & 1) == 1)
                    x++;
                y >>= 1;
            }
            v.get(x).add(arr.get(i));
        }

        List<Integer> ans = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < v.get(i).size(); j++) {
                ans.add(v.get(i).get(j));
            }
        }
        return ans;
    }

}
