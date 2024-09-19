package syntax;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compilation.Transpiler;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.grammar.IndirectRecursionEliminator;
import syntax.tree.builder.SyntaxTreeBuilder;

public class SCheckProgressTest {

    private Rule createRule(String string) {
        return RuleCreator.createRule(string);
    }

    @Test
    public void gen1() throws IOException, GrammarException {

        String sourceFileContent = "auzuzbb";

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(RuleCreator.createRule("A->B B"));
        ruleList.add(RuleCreator.createRule("A->'a A"));
        ruleList.add(RuleCreator.createRule("B->'u 'z"));
        ruleList.add(RuleCreator.createRule("B->B 'b 'b"));

        Grammarhost gh = new Grammarhost(ruleList);

        System.out.println(gh);

        Transpiler tp = new Transpiler(sourceFileContent, gh);

        Assert.assertNotNull(tp.transpile());

    }

    @Test
    public void gen2() throws IOException, GrammarException {

        String source = "lsaqbbaq";

        List<Rule> ruleList = new LinkedList<>();

        ruleList.add(RuleCreator.createRule("A->'l 's"));
        ruleList.add(RuleCreator.createRule("A->A 'a B"));

        // ruleList.add(RuleCreator.createRule("B->'q 'n"));

        ruleList.add(RuleCreator.createRule("B->'q"));
        ruleList.add(RuleCreator.createRule("B->B 'b 'b"));

        Grammarhost gh = new Grammarhost(ruleList);

        System.out.println("A->'l 's");
        System.out.println("A->A 'a B");
        System.out.println("B->'q 'n");
        System.out.println("B->B 'b 'b");

        SyntaxTreeBuilder stb = new SyntaxTreeBuilder(gh, source);

        stb.build();

    }

    @Test(expected = GrammarException.class)
    public void nextBugSimplify() throws GrammarException {
        boolean r = false;

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("D->D C F"));
        ruleList.add(createRule("D->'b C 'b"));

        ruleList.add(createRule("C->D 'b"));
        ruleList.add(createRule("C->'b"));

        ruleList.add(createRule("F->D"));

        String src = "bbbbbbb";
        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);
        r = sc.build() != null;
        Assert.assertTrue(r);
    }

    @Test(expected = GrammarException.class)
    public void good() throws GrammarException {
        boolean r = false;

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("A->F B G"));
        ruleList.add(createRule("B->'b"));
        ruleList.add(createRule("B->C B G"));

        ruleList.add(createRule("G->B"));
        ruleList.add(createRule("D->'bb"));
        ruleList.add(createRule("C->'a"));
        ruleList.add(createRule("F->'ab"));

        String src = "abbabb";
        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);
        sc.setShowTree(true);
        r = sc.build() != null;
        Assert.assertTrue(r);
    }

    @Test
    public void twoGroupsthreeCsdsOnRight() throws GrammarException {
        boolean r = false;

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("S->K 'b 'b"));
        ruleList.add(createRule("K->'b 'b 'b"));

        String src = "bbbbb";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList, false), src);
        r = sc.build() != null;
        Assert.assertTrue(r);
    }

    @Test
    public void threeCsdsOnRight() throws GrammarException {
        boolean r = false;

        List<Rule> rules = new LinkedList<>();

        rules.add(createRule("S->'b 'b 'b"));

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("S->'b 'b 'b"));
        String src = "bbb";
        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(rules), src);
        r = sc.build() != null;
        Assert.assertTrue(r);
    }

    @Test
    public void testEmptyToLeft() throws GrammarException {
        boolean r = false;

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("S->D B E"));
        ruleList.add(createRule("B->'b"));
        ruleList.add(createRule("B->'ab"));
        ruleList.add(createRule("D->'b"));
        ruleList.add(createRule("E->B"));

        String src = "babab";
        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList, false), src);
        r = sc.build() != null;
        Assert.assertTrue(r);
    }

    @Test
    public void progressiveRecRule8() throws GrammarException {
        boolean r = false;

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("T->D M"));
        ruleList.add(createRule("D->'5"));
        ruleList.add(createRule("D->'5 D"));
        ruleList.add(createRule("M->'54"));

        String src = "554";
        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList, false), src);

        r = sc.build() != null;
        Assert.assertTrue(r);

    }

    @Test
    public void tree() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        rl.add(createRule("E->A B C"));

        rl.add(createRule("A->'a"));
        rl.add(createRule("A->'aa"));
        rl.add(createRule("B->'b"));
        rl.add(createRule("C->'c"));

        String src = "aabc";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(rl), src);
        sc.setShowTree(true);
        sc.build();
        boolean r = sc.isReady();

        Assert.assertTrue(r);

    }

    @Test
    public void tree2() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("E->A B C"));

        ruleList.add(createRule("A->'aa"));
        ruleList.add(createRule("B->'b"));
        ruleList.add(createRule("C->'c"));

        String src = "aabc";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);

        sc.build();
        boolean r = sc.isReady();

        Assert.assertTrue(r);

    }

    @Test
    public void nonProgressiveRecursiveRule() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("E->A B C"));

        ruleList.add(createRule("A->A"));
        ruleList.add(createRule("A->'aa"));
        ruleList.add(createRule("B->'b"));
        ruleList.add(createRule("C->'c"));

        String src = "aabc";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);

        sc.build();
        boolean r = sc.isReady();

        Assert.assertTrue(r);

    }

    @Test
    public void progressiveRecRule1() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("D->'5"));
        ruleList.add(createRule("D->D '5"));

        String src = "55";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);

        sc.build();
        boolean r = sc.isReady();

        Assert.assertTrue(r);

    }

    @Test
    public void progressiveRecRule2() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("D->'5"));
        ruleList.add(createRule("D->D '5"));

        String src = "555";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);

        sc.build();
        boolean r = sc.isReady();

        Assert.assertTrue(r);

    }

    @Test
    public void progressiveRecRule5() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("D->'5"));
        ruleList.add(createRule("M->'5"));

        ruleList.add(createRule("D->D M"));

        String src = "555";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);

        sc.build();
        boolean r = sc.isReady();

        Assert.assertTrue(r);

    }

    @Test
    public void progressiveRecRule3() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("D->'5"));
        ruleList.add(createRule("D->'5 D"));

        String src = "55";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);

        sc.build();
        boolean r = sc.isReady();

        Assert.assertTrue(r);

    }

    @Test
    public void progressiveRecRule4() throws GrammarException {
        boolean r = false;

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(RuleCreator.createRule("d->'5"));
        ruleList.add(RuleCreator.createRule("d->'5 d"));

        String src = "555555555";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);
        sc.setShowTree(true);
        r = sc.build() != null;
        Assert.assertTrue(r);

    }

    @Test(expected = GrammarException.class)
    public void expression1() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("e->e owe"));

        ruleList.add(createRule("e->ds"));
        ruleList.add(createRule("e->'\\( e '\\)"));
        ruleList.add(createRule("owe->o e"));

        ruleList.add(createRule("o->'+"));
        ruleList.add(createRule("o->'-"));
        ruleList.add(createRule("ds->d"));
        ruleList.add(createRule("ds->d ds"));
        ruleList.add(createRule("d->'(0-9)"));
        boolean r = false;
        String src = "5555555555555555555555555555555555555555555555555";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);
        r = sc.build() != null;

        Assert.assertTrue(r);
    }

    @Test
    public void exp1() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        rl.add(RuleCreator.createRule("Exp->Exp Operator Exp"));
        rl.add(RuleCreator.createRule("Exp->Digits"));
        rl.add(RuleCreator.createRule("Operator->'+"));
        rl.add(RuleCreator.createRule("Operator->'-"));

        rl.add(RuleCreator.createRule("Digits->Digit Digits"));
        rl.add(RuleCreator.createRule("Digits->Digit"));

        rl.add(RuleCreator.createRule("Digit->'(0-9)"));

        String src = "13+22-4";

        Grammarhost gh = new Grammarhost(rl, "Exp");

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(gh, src);
        sc.setShowTree(true);
        ;
        sc.build();
        boolean r = sc.isReady();

        Assert.assertTrue(r);
    }

    @Test(expected = GrammarException.class)
    public void current() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        rl.add(RuleCreator.createRule("W->O 'b"));
        rl.add(RuleCreator.createRule("O->'b W O"));
        rl.add(RuleCreator.createRule("O->'b"));

        // W->O "bb", O->"b" W O, O->"aa"

        System.out.println(rl);

        Grammarhost gh = new Grammarhost(rl);
        System.out.println(gh);
        new IndirectRecursionEliminator();

        System.out.println(gh);

    }

    @Test
    public void exp2() throws GrammarException {
        List<Rule> rl = new LinkedList<>();

        rl.add(createRule("exp->'q ds 'w"));
        rl.add(createRule("ds->d"));
        rl.add(createRule("ds->d ds"));
        rl.add(createRule("d->'5"));
        String src = "q5w";
        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(rl), src);
        sc.build();
        boolean r = sc.isReady();
        Assert.assertTrue(r);
    }

    @Test
    public void expWithBrackets() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("expression->'\\( digit '\\)"));
        ruleList.add(createRule("digit->'5"));
        boolean r = false;
        String src = "(5)";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList), src);
        r = sc.build() != null;

        Assert.assertTrue(r);
    }

    @Test
    public void holeInMatch() throws GrammarException {

        List<Rule> ruleList = new LinkedList<>();
        ruleList.add(createRule("expression->'\\( digit '\\)"));
        ruleList.add(createRule("digit->'5"));

        boolean r = false;
        String src = "5A(5)";

        SyntaxTreeBuilder sc = new SyntaxTreeBuilder(new Grammarhost(ruleList, false), src);
        sc.build();
        r = sc.isReady();
        Assert.assertFalse(r);
    }

}