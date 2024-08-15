package syntax.grammar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import syntax.Rule;
import syntax.RuleCreator;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class DescriptorPusherTest {
	
	@Test
	public void push() throws GrammarException{

		List<Rule> rList=new LinkedList<>();
		rList.add(RuleCreator.createRule("S->'c"));
		rList.add(RuleCreator.createRule("S->T"));
		rList.add(RuleCreator.createRule("T->'c M"));
		rList.add(RuleCreator.createRule("M->'m"));
		
		
		
		Grammarhost dp=new Grammarhost(rList);
		
		Map<String, ArrayList<Rule>> newgrammar = dp.getGrammar();
		
		
		Assert.assertEquals(6,newgrammar.values().size());
		
		
		
		
	}
}
