package syntax;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import compilation.TranslationResult;
import compilation.Translator;
import hu.kg.util.StringLoadUtil;
import read.RuleReader;
import syntax.grammar.GrammarException;

public class SyntaxValidatorTest {
    
	@Ignore
	@Test
	//not ready
	public void firstTry() throws GrammarException{
		String stt=StringLoadUtil.loadResource("sttValidator.stt");
		RuleReader rr = new RuleReader(stt);
		stt=rr.getPreprocessed(); 
		Translator tr=new Translator(stt);
		
		
		String src=StringLoadUtil.loadResource("test.stt");
		src=new RuleReader(src).getPreprocessed();
		src=src.trim();
			
		TranslationResult result = tr.translate(src, null);
		
		if(! "OK!".equals(result.getResult())) Assert.fail(result.getResult());
			
	}
}
