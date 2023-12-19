package syntax.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

import syntax.Rule;
import syntax.RuleCreator;

public class RecursiveBranchFinderTest {

	@Test
	public void find() throws GrammarException{
		
		List<Rule> rl=new LinkedList<>();
		
		rl.add(RuleCreator.createRule("S->A B C"));
		rl.add(RuleCreator.createRule("A->D E"));
		rl.add(RuleCreator.createRule("A->F G"));
		rl.add(RuleCreator.createRule("B->H"));
		rl.add(RuleCreator.createRule("B->I J"));
		rl.add(RuleCreator.createRule("H->N O"));
		rl.add(RuleCreator.createRule("H->X Y"));
		rl.add(RuleCreator.createRule("X->P Q"));
		rl.add(RuleCreator.createRule("P->B Z"));
		rl.add(RuleCreator.createRule("C->K L"));
		
		rl.add(RuleCreator.createRule("N->'n"));
		rl.add(RuleCreator.createRule("O->'o"));
		
		rl.add(RuleCreator.createRule("D->'d"));
		rl.add(RuleCreator.createRule("E->'e"));
		rl.add(RuleCreator.createRule("F->'f"));
		rl.add(RuleCreator.createRule("G->'g"));
		rl.add(RuleCreator.createRule("I->'i"));
		rl.add(RuleCreator.createRule("J->'j"));
		rl.add(RuleCreator.createRule("Y->'y"));
		rl.add(RuleCreator.createRule("Q->'q"));
		rl.add(RuleCreator.createRule("Z->'z"));
		rl.add(RuleCreator.createRule("K->'k"));
		rl.add(RuleCreator.createRule("L->'l"));
		
		Grammarhost gh = new Grammarhost(rl, false);
		
		
		List<Stack<Rule>> recursiveBranch = IndirectRecursionFinder.find(gh.getGrammar(),gh.getRootGroup());
		System.out.println(recursiveBranch);
		Assert.assertNotNull(recursiveBranch);
	}
	
}
