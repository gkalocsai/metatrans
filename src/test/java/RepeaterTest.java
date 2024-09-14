import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syntax.Rule;
import syntax.RuleCreator;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.STreeBuilder;

public class RepeaterTest {

    @Test
    public void x() {
        Rule r1 = RuleCreator.createRule("a->...'ss 'tt ");

        Assert.assertTrue(r1.isRepeater());

        List<Rule> list = new LinkedList<Rule>();
        list.add(r1);
        Grammarhost gh = new Grammarhost(list, true);

        Assert.assertTrue(gh.getRefRules().get(0).isRepeater());

        STreeBuilder stb = new STreeBuilder(gh, "ssttsstt");

        stb.build();
        Assert.assertTrue(stb.isReadyOuter());

    }

}
