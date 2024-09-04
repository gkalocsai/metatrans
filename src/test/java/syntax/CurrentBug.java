package syntax;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compilation.Transpiler;
import read.RuleReader;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.STreeBuilder;

public class CurrentBug {

    @Test
    public void expx() throws IOException, GrammarException {

        String syntaxFileContent = "exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp; e1:exp op e2:exp >> *e1 \" \" *e2 \" \" op;}"
                + "op{\"+\">>\"+\";\"*\">>\"*\";}" + "ds{d >> d;ds d>> ds d;}" + "d{d:\"(0-9)\">>d;}";

        String sourceFileContent = "(14*(8))";

        // String sourceFileContent = "((22))";
        String expected = "14 8 *";
        RuleReader rr = new RuleReader(syntaxFileContent);
        List<Rule> ruleList = rr.getAllRules();
        Grammarhost gh = new Grammarhost(ruleList);

        Transpiler trp = new Transpiler(sourceFileContent, syntaxFileContent);
        String x2 = trp.transpile();
        if (x2 == null) {
            STreeBuilder stb = new STreeBuilder(gh, sourceFileContent);
            stb.build();

        }

        Assert.assertEquals(expected, x2);

    }
}
