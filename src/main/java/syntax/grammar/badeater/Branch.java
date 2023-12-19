package syntax.grammar.badeater;

import java.util.LinkedList;

public class Branch {
	
	private LinkedList<PointedRule> prList;

	public Branch(PointedRule f) {
		prList=new LinkedList<>();
		prList.add(f);		
	}


	public Branch(LinkedList<PointedRule> list) {
		 this.prList = list;
	}


	public LinkedList<PointedRule> getPrList() {
		return prList;
	}
	
	
	@Override
	public String toString(){		
		StringBuilder sb=new StringBuilder();
		boolean first=true;
		for(PointedRule pr2:prList)  {
		  if(!first) sb.append(" --- ");
		  sb.append(pr2);
		  first = false;
		}
		return sb.toString();
	}
	
	
	
}
