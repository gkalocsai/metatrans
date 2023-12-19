package syntax.compilation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compilation.TranslationResult;
import compilation.Translator;
import read.RuleReader;
import syntax.Rule;
import syntax.RuleCreator;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class TranslatorTest {

	
	
	@Test
	public void expAddition() throws IOException, GrammarException{
		
		
		String syntaxFileContent;           // = StringLoadUtil.load("/home/kalocsai/expression/expression.stt");
		
		
		syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp;e1:exp op:\"*\" e2:exp >> *e1 \" \" *e2 \" \" op; e1:exp op:\"+\" e2:exp >> *e1 \" \" *e2 \" \" op;}"
				+ "op{\"-\">>\"-\";\"*\">>\"*\";\"+\">>\"+\";}"
				+ "ds{d;ds d>>d ds;}"
				+ "d{d:\"(0-9)\">>d;}";
		
		//String sourceFileContent ="(53+45)*2+19+56+53+1";
		String sourceFileContent ="2+1+56";
		
		
		
		
			
		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList=rr.getAllRules();
		Grammarhost gh=new Grammarhost(ruleList);
		System.out.println(gh);
		
		Translator tr = new Translator(gh, true);
		
		TranslationResult trr = tr.translate( sourceFileContent,"exp");
		
    	
    	System.out.print(trr.getResult());
        Assert.assertEquals("2 1 + 56 +",trr.getResult());
	}
	
	
	@Test
	public void exp2() throws IOException, GrammarException{
		
		
		String syntaxFileContent;           // = StringLoadUtil.load("/home/kalocsai/expression/expression.stt");
		
		
		syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp;e1:exp op:\"*\" e2:exp >> *e1 \" \" *e2 \" \" op; e1:exp op:\"+\" e2:exp >> *e1 \" \" *e2 \" \" op;}"
				+ "op{\"-\">>\"-\";\"*\">>\"*\";\"+\">>\"+\";}"
				+ "ds{d;ds d>>d ds;}"
				+ "d{d:\"(0-9)\">>d;}";
		
		String sourceFileContent ="(53+45)*2+19+56+53+1";
		
		
		
			
		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList=rr.getAllRules();
		Grammarhost gh=new Grammarhost(ruleList);
		System.out.println(gh);
		
		Translator tr = new Translator(gh, true);
		
		TranslationResult trr = tr.translate( sourceFileContent,"exp");
		
    	
    	System.out.print(trr.getResult());
        Assert.assertEquals("53 45 + 2 * 19 + 56 + 53 + 1 +",trr.getResult());
	}
	

	@Test
	public void exp() throws IOException, GrammarException{
		
		
		
		String syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp; e1:exp op e2:exp >> *e1 \" \" *e2 \" \" op;}"
				+ "op{\"+\">>\"+\";\"*\">>\"*\";}"
				+ "ds{d;d ds:ds>>d(ds);}"
				+ "d{d:\"(0-9)\">>d;}";
		String sourceFileContent ="(5+2)+(5+5)";
	
		
		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList=rr.getAllRules();
		Grammarhost gh=new Grammarhost(ruleList);
		System.out.println(gh);
		
		Translator tr = new Translator(gh,true);
		
		TranslationResult trr = tr.translate( sourceFileContent,null);
		
    	
    	System.out.print(trr.getResult());
        Assert.assertEquals("5 2 + 5 5 + +",trr.getResult());
	}


	@Test
	public void exp3() throws IOException, GrammarException{
		
		
		String syntaxFileContent;// = StringLoadUtil.load("/home/kalocsai/expression/expression.stt");
		
		
		syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp;e1:exp op:\"*\" e2:exp >> *e1 \" \" *e2 \" \" op; e1:exp op:\"+\" e2:exp >> *e1 \" \" *e2 \" \" op;}"
				+ "op{\"-\">>\"-\";\"*\">>\"*\";\"+\">>\"+\";}"
				+ "ds{d;d ds:ds>>d(ds);}"
				+ "d{d:\"(0-9)\">>d;}";
		
		
		String sourceFileContent;// = StringLoadUtil.load("/home/kalocsai/expression/expression.src").trim();
		
		//sourceFileContent="9*(5)*2";
		sourceFileContent="9*5*2";
		
		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList=rr.getAllRules();
		Grammarhost gh=new Grammarhost(ruleList);
		System.out.println(gh);
		
		Translator tr = new Translator(gh);
		
		TranslationResult trr = tr.translate( sourceFileContent,null);
		
    	
    	System.out.print(trr.getResult());
        Assert.assertEquals("9 5 * 2 *",trr.getResult());
	}
	
	@Test
	public void simpleTranslate() throws GrammarException{

		List<Rule> rl=new LinkedList<>();
		rl.add(RuleCreator.createRule("E->a:A b:B c:C>>*b *c *a"));
		rl.add(RuleCreator.createRule("A->'a>>\"1\""));
		rl.add(RuleCreator.createRule("A->'aa>>\"11\""));
		rl.add(RuleCreator.createRule("B->'b>>\"2\""));
		rl.add(RuleCreator.createRule("C->'c>>\"3\""));
		String source = "aabc";
		Translator tr=new Translator(rl,null);
		TranslationResult x=tr.translate(source,null);
		Assert.assertEquals("2311", x.getResult()); 

	}
	
	@Test
	public void simpleTrans() throws GrammarException {


		List<Rule> rl=new LinkedList<>();

		Rule r1=RuleCreator.createRule("A->F G F G>>*F \"hello\" *G");
		rl.add(r1);
		rl.add(RuleCreator.createRule("B->'b"));
		rl.add(RuleCreator.createRule("B->C B"));
		

		rl.add(RuleCreator.createRule("G->B"));
		rl.add(RuleCreator.createRule("D->'bb"));
		rl.add(RuleCreator.createRule("C->'a"));
		rl.add(RuleCreator.createRule("F->'ab"));
		String source ="abbabb";
		Grammarhost grammarhost = new Grammarhost(rl);

		Translator tr=new Translator(grammarhost);
		TranslationResult x = tr.translate(source, null);
		Assert.assertEquals("hello", x.getResult());
	}


	@Test
	public void leftRecTestGoodSource() throws GrammarException{

		List<Rule> rl=new LinkedList<>();

		Rule r1=RuleCreator.createRule("M->M O E>>M \"\\s\" O E");
		rl.add(r1);
		rl.add(RuleCreator.createRule("M->'x>>\"x\""));

		rl.add(RuleCreator.createRule("E->'2"));
		rl.add(RuleCreator.createRule("E->'4"));
		rl.add(RuleCreator.createRule("E->'6"));
		rl.add(RuleCreator.createRule("E->'8"));

		rl.add(RuleCreator.createRule("O->'1"));
		rl.add(RuleCreator.createRule("O->'3"));
		rl.add(RuleCreator.createRule("O->'5"));
		rl.add(RuleCreator.createRule("O->'7"));

		String source ="x5478";

		Grammarhost grammarhost = new Grammarhost(rl);

		Translator tr=new Translator(grammarhost);
		TranslationResult x = tr.translate(source, null);
		System.out.println(x.getResult());
		Assert.assertEquals("x54 78", x.getResult());		
	}

	
	@Test
	public void midRecTestGoodSource() throws GrammarException{

		List<Rule> rl=new LinkedList<>();

		Rule r1=RuleCreator.createRule("M->e1:E o1:O M o2:O e2:E>>e1 \"\\s\" o1 \"b\" *M \"a\" o2 e2");
		rl.add(r1);
		rl.add(RuleCreator.createRule("M->'x>>\"x\""));

		rl.add(RuleCreator.createRule("E->'2"));
		rl.add(RuleCreator.createRule("E->'4"));
		rl.add(RuleCreator.createRule("E->'6"));
		rl.add(RuleCreator.createRule("E->'8"));

		rl.add(RuleCreator.createRule("O->'1"));
		rl.add(RuleCreator.createRule("O->'3"));
		rl.add(RuleCreator.createRule("O->'5"));
		rl.add(RuleCreator.createRule("O->'7"));

		String source ="4123x5478";

		Grammarhost grammarhost = new Grammarhost(rl);

		System.out.println(grammarhost);

		Translator tr=new Translator(grammarhost,true);
		TranslationResult x = tr.translate(source, null);

		Assert.assertEquals("4 1b2 3bxa54a78", x.getResult());


	}

	
	@Test
	public void rightRecTestGoodSource() throws GrammarException{

		List<Rule> rl=new LinkedList<>();

		Rule r1=RuleCreator.createRule("M->O E M>>O E \"\\s\" *M");
		rl.add(r1);
		rl.add(RuleCreator.createRule("M->'x>>\"h\""));

		rl.add(RuleCreator.createRule("E->'4"));
		rl.add(RuleCreator.createRule("E->'8"));

		rl.add(RuleCreator.createRule("O->'5"));
		rl.add(RuleCreator.createRule("O->'7"));

		String source ="5478x";

		Grammarhost grammarhost = new Grammarhost(rl);

		Translator tr=new Translator(grammarhost,true);
		TranslationResult x = tr.translate(source, null);
		System.out.println(x.getResult());
		Assert.assertEquals("54 78 h", x.getResult());	
	}

	private Rule createRule(String string) {
		return RuleCreator.createRule(string);
	}



}
