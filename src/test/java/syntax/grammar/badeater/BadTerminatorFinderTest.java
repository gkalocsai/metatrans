package syntax.grammar.badeater;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import syntax.Rule;
import syntax.RuleCreator;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class BadTerminatorFinderTest {

	//	@Test
	//	public void vv() throws GrammarException{
	//	
	//		
	//		String grammarString = "S->A S"
	//				+ ",S->X"
	//				+ ",S->'a"
	//				+ ",A->'m"
	//				+ ",D->C A"
	//				+ ",C->'c"
	//				+ ",X->D";
	//
	//		
	//		
	//		List<Rule> rl=new LinkedList<>();
	//		rl=RuleCreator.createRuleList(grammarString );
	//		
	//		Grammarhost gh=new Grammarhost(rl);
	//		System.out.println(gh);
	//		
	//		
	//		Boolean result = BadTerminatorFinder.checkBackwardMovingPointed(gh);
	//		
	//		
	//		System.out.println(result);
	//		
	//	}

	@Test
	public void v2() throws GrammarException{

		LinkedList<Rule> rl = RuleCreator.createRuleList("C->C _5 , C->_c5 _e , B->_9 B,B->_c C , A->B C , A->C _f"
				+ ",_f->'db11827731be2ffd4ce2"
				+ ",_9->'a23a8ea139430c6c41a7"
				+ ",_c->'b61be344484259c6eb14"
				+ ",_5->'04fd81244bed1a85a341"
				+ ",_c5->'59112e7c5517f63997ee"
				+ ",_e->'7d2af62c1f5dda3183b3");

		Boolean result = BadTerminatorFinder.checkBackwardMovingPointed(new Grammarhost(rl));

		Assert.assertTrue(result);

	}


	
	@Test
	public void v3() throws GrammarException{

		LinkedList<Rule> rl = RuleCreator.createRuleList("C->_74 _2"
				+ ", C->_5b _23"
				+ ", B->B C"
				+ ", B->_7 _5"
				+ ", A->_d C"
				+ ", A->B B"
				+ ", _d->'ca73b2d6-0bbb-4975-b31f-f2572adbfa51"
				+ ", _7->'77f9a021-d99f-46ea-85f4-8c85239e2a44"
				+ ",_5->'14badc17-aeb7-40d9-80f8-f11b82f119cc"
				+ ", _74->'9a150e35-4ca1-4b4b-8bfb-095501c16bd4"
				+ ", _2->'82fe9633-2126-4f55-b962-e3f5fde10492"
				+ ", _5b->'adbef318-f0dd-466b-93bf-4beebc5b3fe6"
				+ ",_23->'d135aa6c-cc2a-4083-8707-7b5fe1de4170");

		Boolean result = BadTerminatorFinder.checkBackwardMovingPointed(new Grammarhost(rl));

		Assert.assertTrue(result);

	}

}
