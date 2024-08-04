package syntax.grammar.badeater;

import descriptor.CharSequenceDescriptor;
import descriptor.OneCharDesc;
import syntax.Rule;
import syntax.SyntaxElement;

public class PointedRule {


	private Rule rule;
	private int index;
	private CharSequenceDescriptor csd;

	private boolean forward;

	public PointedRule(Rule r, boolean forward) {
		super();
		this.rule = r;
		this.forward=forward;
		SyntaxElement first=r.getFirstV();
		if(first.isDescriptor())  {
			this.csd = first.getCsd();
			this.index = first.getDescribedLength()-1;	

		}else{
			this.index = r.getRightSideLength()-1;
		}

	}


	public PointedRule(Rule rule, int index, boolean forward) {
		this.rule = rule;
		this.index = index;
		this.forward = forward;
		if(rule.getFirstV().isDescriptor())  {
			this.csd = rule.getFirstV().getCsd();
		}
	}


	public void stepPoint(){
		if(this.index>0 && !forward ) {
			this.index--;
		} else if(forward && index < rule.getRightSideLength()-1) {
			index++;
		}
	}

	public boolean canMovePoint(){
		return (this.index>0 && !forward ) || (forward && index < rule.getRightSideLength()-1);		
	}

	public OneCharDesc getCurrentOCD(){
		if(this.csd==null) return null;
		return this.csd.getOcdArray()[this.index];

	}

	public String getCurrentGroup() {
		if(csd!=null) return null;
		return rule.getRightSideRef(index);

	}

	public Rule getRule() {
		return rule;
	}

	public boolean isCsd(){
		return csd != null;
	}

	public boolean isForward() {
		return forward;
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof PointedRule)) return false;
		PointedRule o=(PointedRule) other;
		return this.rule == o.rule && this.index == o.index;
	}


	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(rule.getGroupname()+" -> ");
        
		if(csd != null) {
			OneCharDesc[] ocda = csd.getOcdArray();
			for(int i=0;i<ocda.length;i++){
				if(index == i) {
					sb.append(" ");
					if(!forward) {
						sb.append("<");
					}
					sb.append(ocda[i].getExample());
					if(forward) {
						sb.append(">");
					}
					sb.append(" ");
				}else{
					sb.append(ocda[i].getExample());
				}
			}
		}else{
			SyntaxElement[] tnt=rule.getRightside();
			for(int i=0;i<tnt.length;i++) {
				if(index == i) {
					sb.append(" ");
					if(!forward) {
						sb.append("<");
					}
					sb.append(tnt[i].toString());
					if(forward) {
						sb.append(">");
					}
					sb.append(" ");
				}else{
					sb.append(tnt[i].toString());
				}
			}
		}
		return sb.toString();

	}

}
