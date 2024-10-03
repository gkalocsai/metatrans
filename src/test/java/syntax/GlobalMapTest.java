package syntax;

import org.junit.Test;

import compilation.Transpiler;
import read.RuleReader;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;
import util.StringLoadUtil;

public class GlobalMapTest {

    @Test
    public void x() {
        String stt = StringLoadUtil.loadResource("globalmap.s2t");
        System.out.println(stt);
        RuleReader rr = new RuleReader(stt);
        stt = rr.getPreprocessed();
        String src = "sdafdrae";
        Grammarhost gh = new Grammarhost(rr.getAllRules());

        System.out.println(gh);
        SyntaxTreeBuilder sb = new SyntaxTreeBuilder(gh, src, true);
        sb.setShowTree(true);

        sb.build();

        System.out.println(sb.getState());
        Transpiler trp = new Transpiler(src, gh);

        System.out.println(trp.transpile());

    }
}
