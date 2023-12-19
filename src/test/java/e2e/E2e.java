package e2e;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compilation.TranslationResult;
import compilation.Translator;
import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;

public class E2e {

	
	@Test
	public void exp3() throws IOException, GrammarException{
		
		
		String syntaxFileContent;
		
		
		syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp;   e1:exp op:\"*\" e2:exp >> *e1 \" \" *e2"
				+ " \" \" op; e1:exp op:\"+\" e2:exp >> *e1 \" \" *e2 \" \" op; e1:exp op:\"-\" e2:exp >> *e1 \" \" *e2 \" \" op;}"
				+ "op{\"-\">>\"-\";\"*\">>\"*\";\"+\">>\"+\";}"
				+ "ds{d>>\"z\";d ds:ds>>\"z\" ds;}"
				+ "d{d:\"(0-9)\">> \"z\";}";
		
		
		String sourceFileContent;
		
		//sourceFileContent="9*(5)*2";
		sourceFileContent="(53+40)*2+19";
		
		
		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList=rr.getAllRules();
		Grammarhost gh=new Grammarhost(ruleList);
		System.out.println(gh);
		
		Translator tr = new Translator(gh,true);
		
		TranslationResult trr = tr.translate( sourceFileContent,null);
		
    	
    	System.out.print(trr.getResult());
        Assert.assertEquals("53 40 + 2 * 19 +",trr.getResult());
	}
	@Test
	public void kivi() throws GrammarException {

		String syntaxFileContent;
		
		
		syntaxFileContent="cs{ c:c >>  *c;cs c>>*cs *c;}\n"+
		"c{m: \"(a á e é i í o ó ö ő u ú ü ű)\" >> m \"v\" m;m: \"(A Á E É I Í O Ó Ö Ő U Ú Ü Ű)\" >> m \"V\" m;m: \"([0]-[65535])\" >>m;}";
	
	
	    String sourceFileContent="NEM vagyok zebra.";
	    //String sourceFileContent=util.StringLoadUtil.loadResource("konyv.txt");
		
	    long startTime=System.currentTimeMillis();
		
		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList=rr.getAllRules();
		Grammarhost gh=new Grammarhost(ruleList);
		System.out.println(gh);
		
		SyntaxTreeBuilder stb=new SyntaxTreeBuilder(gh, sourceFileContent);
		
		boolean syntaxTreeBuilt = stb.build();
		
		
		System.out.println("Time elapsed: " + (System.currentTimeMillis()-startTime)+" " +syntaxTreeBuilt);
		Translator tr = new Translator(gh);
		
		TranslationResult trr = tr.translate( sourceFileContent,null);
		
    	
    	System.out.print(trr.getResult());
    
	
	
	}
	
	
  
}
