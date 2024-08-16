package syntax;

import org.junit.Test;

import compilation.Transpiler;
import syntax.grammar.GrammarException;

public class DigitsTest {


	@Test
	public void digits() throws GrammarException {

		System.out.println("GOOD:");
		String syntaxFileContent=""
				+ "ds{d>>d;ds d>>*ds d}"
				+ "d{d:\"(0-9)\">>d;}";

		String source="1224324424324235453235625662466525656543";
		String rootGroup="ds";

		process(syntaxFileContent, rootGroup, source);

		System.out.println("BAD:");

		syntaxFileContent=""
				+ "ds{d>>d;d ds>>d *ds}"
				+ "d{d:\"(0-9)\">>d;}";

		process(syntaxFileContent, rootGroup, source);

	}

	private void process(String syntaxFileContent, String rootGroup, String source) throws GrammarException {
		Transpiler trp=new Transpiler(source, syntaxFileContent);

		System.out.println(trp.transpile());


	}
}
