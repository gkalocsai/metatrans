package syntax;

import org.junit.Assert;
import org.junit.Test;

import compilation.Transpiler;

import read.RuleReader;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

import util.StringLoadUtil;

public class OptionalElements {

    @Test
    public void x() {
        String stt = StringLoadUtil.loadResource("applyseq.s2t");
        RuleReader rr = new RuleReader(stt);
        stt = rr.getPreprocessed();
        String src = "CREATE SCHEMA IF NOT EXISTS";
        Grammarhost gh = new Grammarhost(rr.getAllRules());
        Transpiler trp = new Transpiler(src, gh);
        Assert.assertEquals("CREATE SCHEMA IF NOT EXISTS", trp.transpile());
    }
}
