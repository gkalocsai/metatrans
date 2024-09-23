package syntax;

import org.junit.Test;

import compilation.Transpiler;
import read.RuleReader;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;
import util.StringLoadUtil;

public class RepeaterTest2 {

    @Test
    public void repeaterBug() throws GrammarException {

        String stt = StringLoadUtil.loadResource("repeaterbug.s2t");
        System.out.println(stt);
        RuleReader rr = new RuleReader(stt);
        stt = rr.getPreprocessed();
        String src = "Da";
        Grammarhost gh = new Grammarhost(rr.getAllRules());

        System.out.println(gh);
        SyntaxTreeBuilder sb = new SyntaxTreeBuilder(gh, src, true);
        sb.setShowTree(true);

        sb.build();

        System.out.println(sb.toString());
        Transpiler trp = new Transpiler(src, gh);

        System.out.println(trp.transpile());


    }
}
