package slow;

import org.junit.Test;

import compilation.TranslationResult;
import compilation.Translator;
import hu.kg.util.StringLoadUtil;
import read.RuleReader;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;

public class Slow {
	
	
	
	
	@Test
	public void kivi() throws GrammarException{
		
		String stt=StringLoadUtil.loadResource("kivi.stt");
		RuleReader rr = new RuleReader(stt);
		stt=rr.getPreprocessed(); 
		String src=StringLoadUtil.loadResource("kivi.src");
	    //src=src.substring(0, 6);
		Grammarhost grammarhost= new Grammarhost(rr.getAllRules());
		SyntaxTreeBuilder stb=new SyntaxTreeBuilder(grammarhost, src);
		//stb.showSyntaxtree();
	//	boolean result = stb.build();
		
		
		
		
 	    Translator tr = new Translator(grammarhost);
		
		TranslationResult trr = tr.translate( src,null); 
		System.out.println(trr.getResult());
		
		
		//SyntaxTreePic pac=new SyntaxTreePic(stb.getTree());
		
		//System.out.println(pac.getPic());
		
	//	Assert.assertTrue(result);
	}
	
}
