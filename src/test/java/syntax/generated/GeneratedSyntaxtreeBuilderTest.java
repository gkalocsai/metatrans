package syntax.generated;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.grammar.badeater.BadTerminatorFinder;
import syntax.tree.builder.SyntaxTreeBuilder;

public class GeneratedSyntaxtreeBuilderTest {

	Group[] groups;

	public void createGroups(){
		groups=new Group[5];
		for(int i=0;i<groups.length;i++) {
			groups[i]= new Group(i, groups);
		}
		for (Group group : groups) {
			group.init();
		}
	}
 
	@Ignore
	@Test
	public void all() throws GrammarException{
		int badCount=0;
		int bg=0;
		for(int i=0;i<10000;i++){
			createGroups();

			List<Rule> rl = getAllRules();
			
			Grammarhost gh=new Grammarhost(rl);

			boolean badGrammar=!BadTerminatorFinder.checkBackwardMovingPointed(gh);
			
			if(badGrammar) {
				bg++;
				continue;
			}
			
		
			for(int k=0;k<5;k++){
				RandomSourceGenerator rsg=new RandomSourceGenerator();
				String source = rsg.generate(gh, 100);
		
			
				
				SyntaxTreeBuilder stb=new SyntaxTreeBuilder(gh, source);
			
				
				if(!stb.build() && BadTerminatorFinder.checkBackwardMovingPointed(gh)){
					System.out.println(gh);				
					System.out.println(source);
					System.out.println(BadTerminatorFinder.checkBackwardMovingPointed(gh)) ;							
					badCount++;
				}else if(!BadTerminatorFinder.checkBackwardMovingPointed(gh)){
					
					
					
				}
				
				
				
			}
		}
		
		System.out.println("Bad grammar count:" +bg);
		Assert.assertEquals(0, badCount);
		System.out.println(badCount);
		
		
	}

	private List<Rule> getAllRules() {
		List<Rule> rl= new LinkedList<>();
		for(Group g:groups) {
			for(Rule r: g.getRsa()){
				rl.add(r);
			}
			
		}
		return rl;
	}

}
