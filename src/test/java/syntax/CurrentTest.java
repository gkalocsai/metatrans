
package syntax;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;
import syntax.tree.builder2.RuleInterval;
import syntax.tree.builder2.STreeBuilder;

public class CurrentTest {


	 
	/*


bcefefa


	 */
	
	@Test
	public void gen4() throws IOException, GrammarException{
				
		
		String source ="beea";
				
		List<Rule> ruleList=new LinkedList<>();

		
		ruleList.add(RuleCreator.createRule("A->'e 'a"));	
		ruleList.add(RuleCreator.createRule("A->B A"));
		
		ruleList.add(RuleCreator.createRule("B->'b"));	
		ruleList.add(RuleCreator.createRule("B->B 'e"));
		
	
		
		
		
		
		
					
		Grammarhost gh=new Grammarhost(ruleList);
	

	
		
		
		STreeBuilder stb = new STreeBuilder(gh, source);
		
		Map<RuleInterval, RuleInterval[]> x = stb.build();
		
		System.out.println(x);
		
		
	}
	




}
