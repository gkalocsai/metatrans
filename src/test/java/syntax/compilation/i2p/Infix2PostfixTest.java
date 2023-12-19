package syntax.compilation.i2p;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import compilation.TranslationResult;
import compilation.Translator;
import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class Infix2PostfixTest {



    
	@Test
	public void exp2() throws IOException, GrammarException{

		int counter  =0;
		String syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp; e1:exp op e2:exp >> *e1 \" \" *e2 \" \" op;}"
				+ "op{\"+\">>\"+\";\"*\">>\"*\";}"
				+ "ds{d;d ds:ds>>d(ds);}"
				+ "d{d:\"(0-9)\">>d;}";
		for(int i=0; i<1000 ;i++){
			String sourceFileContent =new ExpGenerator().generate();
			String expected=Infix2PostfixConverter.convertToPostfix(sourceFileContent);

			//System.out.println("Source: "+sourceFileContent);
			//System.out.println("Expected: "+expected);

			RuleReader rr = new RuleReader(syntaxFileContent);
			List<Rule> ruleList=rr.getAllRules();
			Grammarhost gh=new Grammarhost(ruleList);
			

			Translator tr = new Translator(gh);
			try {
				TranslationResult trr = tr.translate( sourceFileContent,"exp");
				if(trr.getResult().isEmpty()) {
//					Translator tr2 = new Translator(gh,true);
//					tr2.translate(sourceFileContent, null);
//					
					//System.out.println(counter++ +"  :"+ sourceFileContent);				
				}else {
					System.out.println(counter++ +"  :"+ trr.getResult());
				}
			}
			catch(RuntimeException e){
				System.out.println(counter++ +"  Exception: "+sourceFileContent);
			}

			
		}
		//Assert.assertEquals(expected,trr.getResult());

	}

}
