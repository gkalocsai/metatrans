package syntax.tree;

import syntax.Rule;

public class WaveElement implements Comparable<WaveElement>{
	
	
	private int prevSpacingindex;
	private Rule rule;
	private int nextSpacingindex;
	
	public WaveElement(int prevSpacingindex, Rule rule, int nextSpacingindex) {
		super();
		this.prevSpacingindex = prevSpacingindex;
		this.rule = rule;
		this.nextSpacingindex = nextSpacingindex;
	}
	
	
	
	public Rule getRule() {
		return rule;
	}



	public int getPrevSpacingindex() {
		return prevSpacingindex;
	}
	
	
	public int getNextSpacingindex() {
		return nextSpacingindex;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof WaveElement)) return false;
		WaveElement w=(WaveElement)obj;
		return prevSpacingindex == w.prevSpacingindex 
				&& nextSpacingindex == w.nextSpacingindex 
				&& rule == w.rule;
		
	}
	


	@Override
	public int compareTo(WaveElement we) {
		return (nextSpacingindex - prevSpacingindex) - (we.nextSpacingindex - we.prevSpacingindex ); 
	}

	public String getGroupname() {
		return rule.getGroupname();
	}



	public void setPrevSpacingindex(int prevSpacingindex) {
		this.prevSpacingindex = prevSpacingindex;
	}



	public void setNextSpacingindex(int nextSpacingindex) {
		this.nextSpacingindex = nextSpacingindex;
	}



	public WaveElement copyWithNewRule(Rule r) {
		return new WaveElement(this.prevSpacingindex, r, this.nextSpacingindex);		
	}

	
	@Override
	public String toString() {
		return prevSpacingindex+" "+rule.toString()+" "+nextSpacingindex;
		
		
	}
}
