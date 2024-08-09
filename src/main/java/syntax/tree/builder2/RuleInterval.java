package syntax.tree.builder2;

import java.util.Objects;

import syntax.Rule;

public class RuleInterval {

	   Rule rule;
	   int begin;
	   int last;
	
	   public RuleInterval(Rule rule, int begin, int last) {
		this.rule = rule;
		this.begin = begin;
		this.last = last;
	}

	@Override
	public int hashCode() {
		return Objects.hash(begin, last, rule);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RuleInterval other = (RuleInterval) obj;
		return begin == other.begin && last == other.last && rule.getGroupname().equals(other.rule.getGroupname());
	}
	
	public String matchingString() {
		return ""+begin+rule.getGroupname()+last;
	}
	@Override
	public String toString() {
		return rule.getGroupname() +": "+"["+begin+","+last+"]";
	}
	
	   
}
