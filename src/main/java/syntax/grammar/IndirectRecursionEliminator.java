package syntax.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import syntax.Rule;
import syntax.SyntaxElement;



public class IndirectRecursionEliminator {





	public  void eliminate(Map<String, ArrayList<Rule>> grammar, String rootGroupname,boolean strict) throws GrammarException{
		List<Stack<Rule>> branches = IndirectRecursionFinder.find(grammar, rootGroupname);
		while(branches != null){
			pullUpRules(branches,grammar,strict);
			branches = IndirectRecursionFinder.find(grammar, rootGroupname);
		}
	}

	private void pullUpRules(List<Stack<Rule>> branches, Map<String, ArrayList<Rule>> grammar, boolean strict) throws GrammarException {
		Rule low = branches.get(0).pop();
		Rule into= branches.get(0).peek();
		for(SyntaxElement rse:into.getRightside()) {
			String rs=rse.getReferencedGroup();
			boolean condition = rs != null && low.isDirectRecursive() && low.containsRefOnRightSide(into.getGroupname());
			if( condition) {
				throw new GrammarException("Cannot pull "+low+" into "+into+ " because it is already recursive");
			}
		}

		for(SyntaxElement v:low.getRightside()) {
			String refGroup=v.getReferencedGroup();
			if(refGroup!= null && !into.getGroupname().equals(refGroup)){
				List<Stack<Rule>> lowBranches = IndirectRecursionFinder.find(grammar, low.getGroupname());
				for(Stack<Rule> branch:lowBranches){
					for(Rule r:branch) {
						if(r.containsRefOnRightSide(into.getGroupname())) {
							throw new GrammarException("Cannot pull "+low+" into "+into+ " because it is already recursive");
						}
					}
				}
			}
		}

		while(branches!=null && !branches.get(0).isEmpty()) {
			Rule origi=branches.get(0).pop();
			grammar.get(origi.getGroupname()).addAll(pullInGroupExceptLow(origi, low, grammar.get(low.getGroupname())));
			low = RuleIntoRulePuller.pullInto(origi, low);
		}

	}


	private Collection<? extends Rule> pullInGroupExceptLow(Rule origi, Rule low, ArrayList<Rule> pullable) {
		List<Rule> others= new LinkedList<>();
		for(Rule p: pullable) {
			if(p!=low){
				others.add(RuleIntoRulePuller.pullInto(origi.copy(), p));
			}
		}
		return others;
	}



}
