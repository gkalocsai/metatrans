package syntax;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import syntax.tree.builder.RuleIntervalMatcher;
import syntax.tree.tools.RuleInterval;

public class GetLefts {

    @Test
    public void x() {

        Rule r0 = RuleCreator.createRule("M->E O M");
        Rule r1 = RuleCreator.createRule("M->'x");
        Rule r2 = RuleCreator.createRule("E->'2");
        Rule r3 = RuleCreator.createRule("E->'4");

        Rule r6 = RuleCreator.createRule("O->'1");
        Rule r7 = RuleCreator.createRule("O->'3");
        Rule r8 = RuleCreator.createRule("O->'5");
        Rule r9 = RuleCreator.createRule("O->'7");

        RuleInterval ri2 = new RuleInterval(r2, 0, 2);
        RuleInterval ri3 = new RuleInterval(r3, 0, 5);

        RuleInterval ri6 = new RuleInterval(r6, 3, 10);
        RuleInterval ri7 = new RuleInterval(r7, 6, 10);
        RuleInterval ri8 = new RuleInterval(r8, 0, 0);
        RuleInterval ri9 = new RuleInterval(r9, 0, 0);

        Map<String, List<RuleInterval>> backward = new LinkedHashMap<String, List<RuleInterval>>();

        addToBaxckward(backward, ri2);
        addToBaxckward(backward, ri3);
        addToBaxckward(backward, ri6);
        addToBaxckward(backward, ri7);
        addToBaxckward(backward, ri8);
        addToBaxckward(backward, ri9);
        RuleIntervalMatcher rim = new RuleIntervalMatcher(null, backward);
        rim.reInit(r0.extractRefGroups(), 2);

        List<RuleInterval[]> t = rim.getLefts(10);
        System.out.println(t);
    }

    private void addToBaxckward(Map<String, List<RuleInterval>> backward, RuleInterval ri9) {
        addToMap(backward, "" + ri9.getLast(), ri9);

    }

    private boolean addToMap(Map<String, List<RuleInterval>> map, String key, RuleInterval ri) {
        if (map.get(key) == null) map.put(key, new LinkedList<RuleInterval>());
        List<RuleInterval> l = map.get(key);
        for (RuleInterval rio : l)
            if (ri.equals(rio)) return false;
        l.add(ri);
        return true;
    }

}
