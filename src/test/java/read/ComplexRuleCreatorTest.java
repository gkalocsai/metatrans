package read;

import java.util.List;

import org.junit.Test;

import compilation.CompilationElement;
import syntax.Rule;

public class ComplexRuleCreatorTest {

	
	@Test
	public void simpleRule(){
		String syntaxAndComp="?b:\"sds\"?a:\'sd'>>*a b M(*b+a)";
		List<Rule> rl = ComplexRuleCreator.createRules("T", syntaxAndComp);
		
		//TODO assert
		
		
		System.out.println(rl);
		
		
		
	}
	
	
	@Test 
	public void compElementSplit() {
		List<String> x = ComplexRuleCreator.splitBySpaces("\"{\" *s \"}\" \"{\" *o \"}\" \"(2 1 OWNS)\";");
		for(String s:x) {
		    CompilationElement c=new CompilationElement(s);
		    
		    System.out.println(s+" " +c.getType());
			
		}
		
		
		
		
	}
}
