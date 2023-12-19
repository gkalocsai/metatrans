package syntax.grammar;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import syntax.Rule;
import syntax.RuleCreator;

public class GrammarHostTest {

	@Test
	public void oneRuleTwoDirectRecursion() throws GrammarException{
		List<Rule> rl=new LinkedList<>();
		rl.add(RuleCreator.createRule("Y->'b"));
		rl.add(RuleCreator.createRule("Y->Y 'b 'bb"));
		rl.add(RuleCreator.createRule("J->J"));
		rl.add(RuleCreator.createRule("J->Y 'a"));
		
		Grammarhost gh = new Grammarhost(rl);

		System.out.println(gh.getGrammarString());
	}			
}
