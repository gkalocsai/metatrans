package syntax;

import org.junit.Test;

import compilation.Transpiler;
import syntax.grammar.GrammarException;
import util.StringLoadUtil;

public class NumberTest {

    // @Ignore
    @Test
    public void digits() throws GrammarException {

        String sourceFileContent = StringLoadUtil.loadResource("bignumber.txt");
        String syntaxFileContent = StringLoadUtil.loadResource("number.stt");
        Transpiler trp = new Transpiler(sourceFileContent, syntaxFileContent);

        System.out.println(trp.transpile());

    }

}
