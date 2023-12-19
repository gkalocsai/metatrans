package syntax.display;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import syntax.Rule;
import syntax.RuleCreator;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.Spacing;
import syntax.tree.SpacingArrayBuilder;

public class DisplayTest {

	@Test
	public void good() throws GrammarException {
	
		List<Rule> rules = new LinkedList<>();
		
		rules.add(createRule("A->F B G"));
		rules.add(createRule("B->'b"));
		rules.add(createRule("B->C B"));

		rules.add(createRule("G->B"));
		rules.add(createRule("D->'bb"));
		rules.add(createRule("C->'a"));
		rules.add(createRule("F->'ab"));
		String src ="abbabb";
		
		Grammarhost gh = new Grammarhost(rules);
		
		SpacingArrayBuilder pab=new SpacingArrayBuilder(src, gh);

		Spacing[]  ps=pab.getCreatedSpacings();

		SyntaxTreePic pap=new SyntaxTreePic(ps);
		
		System.out.println(pap.getPic());
		
	}

	private Rule createRule(String string) {
		return RuleCreator.createRule(string);
	}
	
	
}
