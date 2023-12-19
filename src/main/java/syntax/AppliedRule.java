package syntax;



public class AppliedRule {
	
	private Rule r;
	private int appliedLength;
	

	public AppliedRule(Rule r, int appliedLength) {
		this.r=r;
		this.appliedLength = appliedLength;
		
	}
	
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(r.toString() +"(" + appliedLength+")");
		
		return  sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if(! (other instanceof AppliedRule)) return false;
		AppliedRule o = (AppliedRule) other;
		return o.r == this.r && o.appliedLength == this.appliedLength ;
	};
	
	@Override
	public int hashCode(){
		return r.hashCode()+appliedLength;
		
	}


	public Rule getRule() {
		return r;
	}
	
	public int getAppliedLength(){
		return appliedLength;
	}


	public boolean step() {
		return false;
	}


	public int getB() {
		return appliedLength;
	}
	
}
