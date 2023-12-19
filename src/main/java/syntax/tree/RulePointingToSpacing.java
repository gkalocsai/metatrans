package syntax.tree;

import syntax.Rule;

public class RulePointingToSpacing implements Comparable<RulePointingToSpacing>{

	private int spacingIndex;
	private Rule rule;
	
	
	public RulePointingToSpacing(Rule rule, int Spacing) {
		super();
		this.spacingIndex = Spacing;
		this.rule = rule;
	}
	
	public int getSpacingIndex() {
		return spacingIndex;
	}
	public Rule getRule() {
		return rule;
	}

	@Override 
	public boolean equals(Object other) {
		if(other == null ) return false;
		if(!(other instanceof RulePointingToSpacing)) {
			return false;
		}
		RulePointingToSpacing that = (RulePointingToSpacing)other;
		
		if(this.spacingIndex == that.spacingIndex && this.rule == that.rule) {
			return true;
		}
		return false;
	};
	

	@Override
	public int compareTo(RulePointingToSpacing rpp) {
		return spacingIndex-rpp.spacingIndex; 
		
	}

	
	@Override
	public String toString(){
		return "+"+spacingIndex+" "+rule;
		
	}

	public String getGroupname() {
		
		return rule.getGroupname();
	}

	public void setRule(Rule rule) {
		this.rule = rule;
		
	}

	public void setSpacingIndex(int SpacingIndex) {
		this.spacingIndex = SpacingIndex;
		
	}

	
	
}
