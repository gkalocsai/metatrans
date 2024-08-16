
package syntax;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.RuleInterval;
import syntax.tree.builder.STreeBuilder;

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


		StringBuilder sb=new StringBuilder();
		for(RuleInterval k:x.keySet()) {
			RuleInterval[] elem = x.get(k);
			sb.append(k.toString() + " -> " );
			for(RuleInterval ri:elem) {
				sb.append(ri+" ");
			}
			sb.append("\n");
		}


		System.out.println(sb);


	}





}
