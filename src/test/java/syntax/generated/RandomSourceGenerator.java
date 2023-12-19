package syntax.generated;

import java.util.ArrayList;
import java.util.Stack;

import syntax.Rule;
import syntax.V;
import syntax.grammar.Grammarhost;

public class RandomSourceGenerator {

	public String generate(Grammarhost gh, int recursionAllowedLength) {
		String root=gh.getRootGroup();
		StringBuilder sb=new StringBuilder();
		Stack<RightsidePart> stack=new Stack<>();
		boolean recursionAllowed = recursionAllowedLength > 0;
	
		Rule n=getRandomRule(root, gh, recursionAllowed);
		
		stack.push(new RandomSourceGenerator.RightsidePart(n, 0));
		
		while(!stack.isEmpty()) {
			RightsidePart c=stack.pop();
			Rule r=c.getRule();
			int i=c.getRsIndex();
			if(i>=r.getRightSideLength()) continue;
			c.incIndex();
			stack.push(c);
			V v = r.getRightside()[i];
			if(v.isDescriptor()) sb.append(v.getCsd().toString());
			else{
				recursionAllowed = recursionAllowedLength > sb.length();
				Rule n1=getRandomRule(v.getReferencedGroup(), gh, recursionAllowed);
				stack.push(new RightsidePart(n1, 0));
			}
		}
		return sb.toString();
		
	}
	
	
	private static Rule getRandomRule(String group, Grammarhost gh, boolean recursionAllowed) {
		ArrayList<Rule> rl = gh.getGrammar().get(group);
		if(rl == null) {
			System.out.println(group);
		}
		
		int randomIndex=(int)(Math.random()*rl.size());
		Rule rnd=rl.get(randomIndex);
		
		while(rnd.isDirectRecursive() && !recursionAllowed)  {
		    	randomIndex=(int)(Math.random()*rl.size());
				rnd=rl.get(randomIndex);			    	
		}
			
		return rnd;
	}


	class RightsidePart {
		
		private Rule rule;
		private int rsIndex;
		public RightsidePart(Rule r, int rsIndex) {
			super();
			this.rule = r;
			this.rsIndex = rsIndex;
		}
		public Rule getRule() {
			return rule;
		}
		public int getRsIndex() {
			return rsIndex;
		}
		
		public void incIndex(){
			rsIndex++;
		}
		
		
	}
	
}
