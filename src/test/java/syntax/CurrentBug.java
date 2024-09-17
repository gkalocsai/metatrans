package syntax;

import org.junit.Test;

import compilation.Transpiler;
import read.RuleReader;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;
import util.StringLoadUtil;

public class CurrentBug {

    @Test
    public void currentBug() throws GrammarException {

        String stt = StringLoadUtil.loadResource("ora2postgres.s2t");
        System.out.println(stt);
        RuleReader rr = new RuleReader(stt);
        stt = rr.getPreprocessed();
        String src = "D";
        Grammarhost gh=new Grammarhost(rr.getAllRules());
        System.out.println(gh);
        SyntaxTreeBuilder sb = new SyntaxTreeBuilder(gh, src, true);
        sb.setShowTree(true);

        sb.build();


        Transpiler trp = new Transpiler(src, gh);

        System.out.println(trp.transpile());

    }
}
