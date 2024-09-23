package syntax;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;

public class RepeaterTest {

    @Test
    public void x() {
        Rule r1 = RuleCreator.createRule("a->...'ss 'tt ");

        Assert.assertTrue(r1.isRepeater());

        List<Rule> list = new LinkedList<Rule>();
        list.add(r1);
        Grammarhost gh = new Grammarhost(list, true);

        boolean hasRepeater = false;

        for (Rule r : gh.getRefRules()) {
            if (r.isRepeater())
                hasRepeater = true;
        }
        Assert.assertTrue(hasRepeater);

        SyntaxTreeBuilder stb = new SyntaxTreeBuilder(gh, "ssttsstt");

        stb.build();
        Assert.assertTrue(stb.isReady());

    }

}
