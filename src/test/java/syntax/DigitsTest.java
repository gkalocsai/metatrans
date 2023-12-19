package syntax;

import org.junit.Test;

import compilation.Translator;
import read.RuleReader;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class DigitsTest {

	
	@Test 
	public void digits() throws GrammarException {
		
		System.out.println("GOOD:");
		String syntaxFileContent=""
				+ "ds{d>>d;ds d>>*ds d}"
				+ "d{d:\"(0-9)\">>d;}";
	
		String source="123";
		String rootGroup="ds";
			
		process(syntaxFileContent, rootGroup, source);
		
		System.out.println("BAD:");
		
		syntaxFileContent=""
				+ "ds{d>>d;d ds>>d *ds}"
				+ "d{d:\"(0-9)\">>d;}";
	
		process(syntaxFileContent, rootGroup, source);
		
	}

	private void process(String syntaxFileContent, String rootGroup, String source) throws GrammarException {
		RuleReader rr= new RuleReader(syntaxFileContent);
		
		Grammarhost gh=new Grammarhost(rr.getAllRules() , rootGroup);
		
		
		System.out.println(gh.toString()); 
	//	SyntaxTreeBuilder stb=new SyntaxTreeBuilder(gh,source);
		
	//	stb.build();
	
	//	Spacing[] spacings= stb.getTree();
		
	//	SyntaxTreePic stp=new SyntaxTreePic(spacings);
		
	//	System.out.println(stp.getColorizedPic());
		
	
		Translator tr = new Translator(rr.getAllRules(), rootGroup);
		
		System.out.println(tr.translate(source, rootGroup).getResult());
		
		
	}
}
