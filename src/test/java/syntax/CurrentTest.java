
package syntax;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import compilation.Transpiler;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;

public class CurrentTest {



	/*


bcefefa


	 */
	
	 


    @Test
    public void simpleTrans() throws GrammarException {

        List<Rule> rl = new LinkedList<>();

        Rule r1 = RuleCreator.createRule("a->f g f g>>*f \"hello\" *g");
        rl.add(r1);
        rl.add(RuleCreator.createRule("b->'b"));
        rl.add(RuleCreator.createRule("b->c b"));

        rl.add(RuleCreator.createRule("g->b"));
        
        
        rl.add(RuleCreator.createRule("d->'bb"));
        rl.add(RuleCreator.createRule("c->'a"));
        rl.add(RuleCreator.createRule("f->'ab"));
        String source = "abbabb";
        Grammarhost grammarhost = new Grammarhost(rl);

        System.out.println(grammarhost);
        
        SyntaxTreeBuilder stb = new SyntaxTreeBuilder(grammarhost, source);
        stb.setPrintOut(true);
        stb.setShowTree(true);
        stb.build();


        Transpiler trp = new Transpiler(source, grammarhost);

        Assert.assertEquals("hello", trp.transpile());
    }






}
