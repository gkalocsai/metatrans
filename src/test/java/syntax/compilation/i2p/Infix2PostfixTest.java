package syntax.compilation.i2p;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import compilation.Transpiler;
import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class Infix2PostfixTest {




	@Test
	public void exp2() throws IOException, GrammarException{
        int count =0;

		String syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp; e1:exp op e2:exp >> *e1 \" \" *e2 \" \" op;}"
				+ "op{\"+\">>\"+\";\"*\">>\"*\";}"
				+ "ds{d >> d;ds d>> ds d;}"
				+ "d{d:\"(0-9)\">>d;}";
		for(int i=0; i<100 ;i++){
			String sourceFileContent =new ExpGenerator().generate();

			//String sourceFileContent = "((22))";
			String expected=Infix2PostfixConverter.convertToPostfix(sourceFileContent);


			RuleReader rr = new RuleReader(syntaxFileContent);
			List<Rule> ruleList=rr.getAllRules();
			Grammarhost gh=new Grammarhost(ruleList);



			Transpiler trp=new Transpiler(sourceFileContent, syntaxFileContent);
			String x2 = trp.transpile();
			//System.out.println(x2);
            if(!expected.equals(x2)) {
                 System.out.println(expected+" != "+ x2);
                 System.out.println("Source: "+sourceFileContent);
                 System.out.println("Expected: "+expected);
                 count++;
            }


		}
		System.out.println("COUNT: "+count);
	}
}
