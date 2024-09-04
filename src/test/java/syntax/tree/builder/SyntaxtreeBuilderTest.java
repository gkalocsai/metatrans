package syntax.tree.builder;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compilation.Transpiler;
import syntax.Rule;
import syntax.RuleCreator;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class SyntaxtreeBuilderTest {

    @Test
    public void fgfg() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        Rule r1 = createRule("a->f g f g");
        rl.add(r1);
        rl.add(createRule("b->'b"));
        rl.add(createRule("b->c b"));

        rl.add(createRule("g->b"));
        rl.add(createRule("d->'bb"));
        rl.add(createRule("c->'a"));
        rl.add(createRule("f->'ab"));
        String source = "abbabb";
        Grammarhost grammarhost = new Grammarhost(rl);

        System.out.println(grammarhost);

        Transpiler trp = new Transpiler(source, grammarhost);

        Assert.assertTrue(trp.transpile() != null);
    }

    @Test
    public void midRecTestBadSource() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        Rule r1 = createRule("M->E O M O E");
        rl.add(r1);
        rl.add(createRule("M->'x"));

        rl.add(createRule("E->'2"));
        rl.add(createRule("E->'4"));
        rl.add(createRule("E->'6"));
        rl.add(createRule("E->'8"));

        rl.add(createRule("O->'1"));
        rl.add(createRule("O->'3"));
        rl.add(createRule("O->'5"));
        rl.add(createRule("O->'7"));

        String source = "123x547";
        Grammarhost grammarhost = new Grammarhost(rl);

        System.out.println(grammarhost);

        Transpiler trp = new Transpiler(source, grammarhost);

        Assert.assertTrue(trp.transpile() == null);

    }

    private Rule createRule(String string) {
        return RuleCreator.createRule(string);
    }

}
