package syntax;

import org.junit.Assert;
import org.junit.Test;

import compilation.Transpiler;
import syntax.grammar.GrammarException;
import util.StringLoadUtil;

public class NumberTest {

    @Test
    public void digits() throws GrammarException {

        String sourceFileContent = StringLoadUtil.loadResource("bignumber.txt");
        String syntaxFileContent = StringLoadUtil.loadResource("number.stt");
        Transpiler trp = new Transpiler(sourceFileContent, syntaxFileContent);
        trp.setStrict(false);
        Assert.assertEquals("2443532455252", trp.transpile());
        System.out.println(trp.transpile());

    }

}
