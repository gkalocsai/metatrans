package syntax;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.Spacing;
import syntax.tree.SpacingArrayBuilder;

public class SpacingArrBuilderTest {


	@Test
	public void nextBugSimplifyedRightHand() throws GrammarException {

		List<Rule> rl =new LinkedList<>();
		
		rl.add(createRule("A->A B A"));
		rl.add(createRule("A->B"));

		rl.add(createRule("B->'a"));
		rl.add(createRule("B->'a 'a"));

		String src ="aaa";
		Grammarhost gh = new Grammarhost(rl, false);
		SpacingArrayBuilder pab=new SpacingArrayBuilder(src, gh);

		Spacing[]  ps=pab.getCreatedSpacings();

		System.out.println(ps);



	}
	@Test
	public void good() throws GrammarException {
		List<Rule> rl =new LinkedList<>();
		
		rl.add(createRule("A->F B G"));
		rl.add(createRule("B->'b"));
		rl.add(createRule("B->C B G"));

		rl.add(createRule("G->B"));
		rl.add(createRule("D->'bb"));
		rl.add(createRule("C->'a"));
		rl.add(createRule("F->'ab"));
		String src ="abbabb";
		Grammarhost gh = new Grammarhost(rl,false);
		SpacingArrayBuilder pab=new SpacingArrayBuilder(src, gh);

		Spacing[]  ps=pab.getCreatedSpacings();

		System.out.println(ps);
	}

	private Rule createRule(String string) {
		return RuleCreator.createRule(string);
	}

}
