package syntax.compilation.i2p;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import compilation2.Transpiler;
import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class Infix2PostfixTest {




	@Test
	public void exp2() throws IOException, GrammarException{


		String syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp; e1:exp op e2:exp >> *e1 \" \" *e2 \" \" op;}"
				+ "op{\"+\">>\"+\";\"*\">>\"*\";}"
				+ "ds{d >> d;d ds >> d ds;}"
				+ "d{d:\"(0-9)\">>d;}";
		for(int i=0; i<1000 ;i++){
			String sourceFileContent =new ExpGenerator().generate();
			String expected=Infix2PostfixConverter.convertToPostfix(sourceFileContent);

			System.out.println("Source: "+sourceFileContent);
			System.out.println("Expected: "+expected);

			RuleReader rr = new RuleReader(syntaxFileContent);
			List<Rule> ruleList=rr.getAllRules();
			Grammarhost gh=new Grammarhost(ruleList);



			Transpiler trp=new Transpiler(sourceFileContent, syntaxFileContent);
			String x2 = trp.transpile();

			System.out.println(x2);

		}
	}
}
