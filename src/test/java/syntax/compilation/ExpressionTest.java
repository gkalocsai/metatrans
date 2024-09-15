package syntax.compilation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compilation.Transpiler;
import read.RuleReader;
import syntax.Rule;
import syntax.RuleCreator;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;

public class ExpressionTest {

    @Test
    public void expAddition() throws IOException, GrammarException {

        String syntaxFileContent; // = StringLoadUtil.load("/home/kalocsai/expression/expression.stt");

        // String sourceFileContent ="(53+45)*2+19+56+53+1";

        syntaxFileContent = "exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp; e1:exp op e2:exp >> *e1 \" \" *e2 \" \" op;}"
                + "op{\"+\">>\"+\";\"*\">>\"*\";}" + "ds{d >> d;ds d>> ds d;}" + "d{d:\"(0-9)\">>d;}";

        syntaxFileContent = "exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp; e1:exp op e2:exp >> *e1 \" \" *e2 \" \" op;}"
                + "op{\"+\">>\"+\";\"*\">>\"*\";}" + "ds{...d >> d;}" + "d{d:\"(0-9)\">>d;}";

        String sourceFileContent = "2+1+56";

        RuleReader rr = new RuleReader(syntaxFileContent);
        List<Rule> ruleList = rr.getAllRules();
        Grammarhost gh = new Grammarhost(ruleList);

        System.out.println(gh.getApplicationOrderToRuleList());

        Transpiler trp = new Transpiler(sourceFileContent, gh);

        // System.out.print(trp.transpile());
        Assert.assertEquals("2 1 + 56 +", trp.transpile());
    }

    @Test
    public void exp2() throws IOException, GrammarException {

        String syntaxFileContent; // = StringLoadUtil.load("/home/kalocsai/expression/expression.stt");

        syntaxFileContent = "exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp;e1:exp op:\"*\" e2:exp >> *e1 \" \" *e2 \" \" op; e1:exp op:\"+\" e2:exp >> *e1 \" \" *e2 \" \" op;}"
                + "op{\"-\">>\"-\";\"*\">>\"*\";\"+\">>\"+\";}" + "ds{d;ds d>>d ds;}" + "d{d:\"(0-9)\">>d;}";

        String sourceFileContent = "(53+45)*2+19+56+53+1";

//
//		RuleReader rr = new RuleReader(syntaxFileContent);
//		List<Rule> ruleList=rr.getAllRules();
//		Grammarhost gh=new Grammarhost(ruleList);
//		System.out.println(gh);
//
//		Translator tr = new Translator(gh, true);
//
//		TranslationResult trr = tr.translate( sourceFileContent,"exp");
//
//
//    	System.out.print(trr.getResult());
//        Assert.assertEquals("53 45 + 2 * 19 + 56 + 53 + 1 +",trr.getResult());

        Transpiler trp = new Transpiler(sourceFileContent, syntaxFileContent);
        String x2 = trp.transpile();

        System.out.println(x2);
    }

    @Test
    public void exp() throws IOException, GrammarException {

        String syntaxFileContent = "exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp; e1:exp op e2:exp >> *e1 \" \" *e2 \" \" op;}"
                + "op{\"+\">>\"+\";\"*\">>\"*\";}" + "ds{...d>>d;}" + "d{d:\"(0-9)\">>d;}";

        String sourceFileContent = "(5+2)+(5+5)";

        RuleReader rr = new RuleReader(syntaxFileContent);
        List<Rule> ruleList = rr.getAllRules();
        Grammarhost gh = new Grammarhost(ruleList);
        System.out.println(gh);

        Transpiler trp = new Transpiler(sourceFileContent, syntaxFileContent);

        Assert.assertEquals("5 2 + 5 5 + +", trp.transpile());
    }

    @Test
    public void exp3() throws IOException, GrammarException {

        String syntaxFileContent;// = StringLoadUtil.load("/home/kalocsai/expression/expression.stt");

        syntaxFileContent = "exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp;e1:exp op:\"*\" e2:exp >> *e1 \" \" *e2 \" \" op; e1:exp op:\"+\" e2:exp >> *e1 \" \" *e2 \" \" op;}"
                + "op{\"-\">>\"-\";\"*\">>\"*\";\"+\">>\"+\";}" + "ds{d;d ds:ds>>d(ds);}" + "d{d:\"(0-9)\">>d;}";

        String sourceFileContent;// = StringLoadUtil.load("/home/kalocsai/expression/expression.src").trim();

        sourceFileContent = "9*(5)*2";
        // sourceFileContent="9*5*2";

        Transpiler trp = new Transpiler(sourceFileContent, syntaxFileContent);
        Assert.assertEquals("9 5 2 * *", trp.transpile());
    }

    @Test
    public void simpleTranslate() throws GrammarException {

        List<Rule> rl = new LinkedList<>();
        rl.add(RuleCreator.createRule("E->a:A b:B c:C>>*b *c *a"));
        rl.add(RuleCreator.createRule("A->'a>>\"1\""));
        rl.add(RuleCreator.createRule("A->'aa>>\"11\""));
        rl.add(RuleCreator.createRule("B->'b>>\"2\""));
        rl.add(RuleCreator.createRule("C->'c>>\"3\""));
        String source = "aabc";
        Transpiler trp = new Transpiler(source, new Grammarhost(rl));

        Assert.assertEquals("2311", trp.transpile());

    }

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
            SyntaxTreeBuilder stb = new SyntaxTreeBuilder(gh, sourceFileContent);
            stb.build();

        }

        Assert.assertEquals(expected, x2);

    }

    @Test
    public void simpleTrans() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        Rule r1 = RuleCreator.createRule("a->f g f g>>*f \"hello\" *g");
        rl.add(r1);
        rl.add(RuleCreator.createRule("b->'b"));
        rl.add(RuleCreator.createRule("b->c b"));

        rl.add(RuleCreator.createRule("g->b"));
        rl.add(RuleCreator.createRule("d->'bb"));
        rl.add(RuleCreator.createRule("c->'a"));
        rl.add(RuleCreator.createRule("f->'ab"));
        String source = "abbabb";
        Grammarhost grammarhost = new Grammarhost(rl);

        SyntaxTreeBuilder stb = new SyntaxTreeBuilder(grammarhost, source);
        stb.setPrintOut(true);
        stb.build();


        Transpiler trp = new Transpiler(source, grammarhost);

        Assert.assertEquals("hello", trp.transpile());
    }

    @Test
    public void leftRecTestGoodSource() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        Rule r1 = RuleCreator.createRule("M->M O E>>M \"X\" O E");
        rl.add(r1);
        rl.add(RuleCreator.createRule("M->'x>>\"x\""));

        rl.add(RuleCreator.createRule("E->'2"));
        rl.add(RuleCreator.createRule("E->'4"));
        rl.add(RuleCreator.createRule("E->'6"));
        rl.add(RuleCreator.createRule("E->'8"));

        rl.add(RuleCreator.createRule("O->'1"));
        rl.add(RuleCreator.createRule("O->'3"));
        rl.add(RuleCreator.createRule("O->'5"));
        rl.add(RuleCreator.createRule("O->'7"));

        String source = "x5478";

        Grammarhost grammarhost = new Grammarhost(rl);
        Transpiler trp = new Transpiler(source, grammarhost);

        Assert.assertEquals("x54X78", trp.transpile());
    }

    @Test
    public void midRecTestGoodSource() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        Rule r1 = RuleCreator.createRule("M->e1:E o1:O M o2:O e2:E>>e1 \"X\" o1 \"b\" *M \"a\" o2 e2");
        rl.add(r1);
        rl.add(RuleCreator.createRule("M->'x>>\"x\""));

        rl.add(RuleCreator.createRule("E->'2"));
        rl.add(RuleCreator.createRule("E->'4"));
        rl.add(RuleCreator.createRule("E->'6"));
        rl.add(RuleCreator.createRule("E->'8"));

        rl.add(RuleCreator.createRule("O->'1"));
        rl.add(RuleCreator.createRule("O->'3"));
        rl.add(RuleCreator.createRule("O->'5"));
        rl.add(RuleCreator.createRule("O->'7"));

        String source = "4123x5478";

        Grammarhost grammarhost = new Grammarhost(rl);

        System.out.println(grammarhost);

        Transpiler trp = new Transpiler(source, grammarhost);
        Assert.assertEquals("4X1b2X3bxa54a78", trp.transpile());

    }

    @Test
    public void rightRecTestGoodSource() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        Rule r1 = RuleCreator.createRule("M->O E M>>O E \"X\" *M");
        rl.add(r1);
        rl.add(RuleCreator.createRule("M->'x>>\"h\""));

        rl.add(RuleCreator.createRule("E->'4"));
        rl.add(RuleCreator.createRule("E->'8"));

        rl.add(RuleCreator.createRule("O->'5"));
        rl.add(RuleCreator.createRule("O->'7"));

        String source = "5478x";

        Grammarhost grammarhost = new Grammarhost(rl);

        Transpiler trp = new Transpiler(source, grammarhost);

        Assert.assertEquals("54X78Xh", trp.transpile());
    }

    private Rule createRule(String string) {
        return RuleCreator.createRule(string);
    }

}
