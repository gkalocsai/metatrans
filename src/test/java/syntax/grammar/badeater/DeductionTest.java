package syntax.grammar.badeater;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import descriptor.OneCharDesc;
import syntax.Rule;
import syntax.RuleCreator;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class DeductionTest {

	
	@Test
	public void x() throws GrammarException{
		Rule r=RuleCreator.createRule("S->A B");
		List<Rule> rl=new LinkedList<>();
		rl.add(r);
		rl.add(RuleCreator.createRule("B->C D"));
		
		rl.add(RuleCreator.createRule("B->'a"));
		rl.add(RuleCreator.createRule("A->'a"));
		rl.add(RuleCreator.createRule("C->'c"));
		rl.add(RuleCreator.createRule("D->'d"));
		
		
		
		Grammarhost gh=new Grammarhost(rl);
		
		Deduction d=new Deduction(new PointedRule(r, false), gh.getGrammar());
		
		System.out.println(d);
		
		d.getCurrentOcds();
		d.step();
		List<OneCharDesc> m = d.getCurrentOcds();
		
		System.out.println(m);
		
		
		
		
		
		
		
		
	}
	
	
}
