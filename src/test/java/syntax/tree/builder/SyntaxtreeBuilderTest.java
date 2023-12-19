package syntax.tree.builder;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import syntax.Rule;
import syntax.RuleCreator;
import syntax.display.SyntaxTreePic;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;

public class SyntaxtreeBuilderTest {

	
	@Test
	public void fgfg() throws GrammarException {
	
		
		List<Rule> rl=new LinkedList<>();
		
		Rule r1=createRule("A->F G F G");
		rl.add(r1);
		rl.add(createRule("B->'b"));
		rl.add(createRule("B->C B"));

		rl.add(createRule("G->B"));
		rl.add(createRule("D->'bb"));
		rl.add(createRule("C->'a"));
		rl.add(createRule("F->'ab"));
		String source ="abbabb";
		Grammarhost grammarhost = new Grammarhost(rl);

		System.out.println(grammarhost);
		SyntaxTreeBuilder sb=new SyntaxTreeBuilder(grammarhost, source);
		boolean result = sb.build();
		
		SyntaxTreePic pac=new SyntaxTreePic(sb.getTree());	
		System.out.println(pac.getColorizedPic());
		Assert.assertTrue(result);
	}
	
	
	@Test
	public void midRecTestBadSource() throws GrammarException{
		
		List<Rule> rl=new LinkedList<>();
		
		Rule r1=createRule("M->E O M O E");
		rl.add(r1);
		rl.add(createRule("M->'x"));
		
		rl.add(createRule("E->'2"));
		rl.add(createRule("E->'4"));
		rl.add(createRule("E->'6"));
		rl.add(createRule("E->'8"));
		
		rl.add(createRule("O->'1"));
		rl.add(createRule("O->'3"));
		rl.add(createRule("O->'5"));
		rl.add(createRule("O->'7"));
		
		String source ="123x547";
		Grammarhost grammarhost = new Grammarhost(rl);

		System.out.println(grammarhost);
		SyntaxTreeBuilder sb=new SyntaxTreeBuilder(grammarhost, source);
		boolean result = sb.build();
		
		SyntaxTreePic pac=new SyntaxTreePic(sb.getTree());	
		System.out.println(pac.getColorizedPic());
		Assert.assertFalse(result);

	}
	

	private Rule createRule(String string) {
		return RuleCreator.createRule(string);
	}
	
}
