
package syntax;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;

public class CurrentTest {


	 
	/*


bcefefa


	 */
	@Ignore
	@Test
	public void gen4() throws IOException, GrammarException{
				
		
		String source ="beea";
				
		List<Rule> ruleList=new LinkedList<>();

		
		ruleList.add(RuleCreator.createRule("A->'e 'a"));	
		ruleList.add(RuleCreator.createRule("A->B A"));
		
		ruleList.add(RuleCreator.createRule("B->'b"));	
		ruleList.add(RuleCreator.createRule("B->B 'e"));
		
	
		
		
		
		
		
					
		Grammarhost gh=new Grammarhost(ruleList);
	

	
		
		
		SyntaxTreeBuilder stb = new SyntaxTreeBuilder(gh, source);
		stb.showSyntaxtree();
		
		Assert.assertTrue(stb.build());
		
		
		
	}
	




}
