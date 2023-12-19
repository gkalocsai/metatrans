package syntax;

import java.util.List;

import descriptor.CharSequenceDescriptor;
import hu.kg.list.StatefulList;

public class CsdRulesApplier {

	
	private String source;
	private List<Rule> csdRules;

	
	
	private ListPair<AppliedRule>[] ward;
	
	public CsdRulesApplier(String source, List<Rule> csdRules ) {
		this.source = source;
		this.csdRules = csdRules;
		
	}
	
	
	

	private  boolean addToWard(int pos, Rule r, int appliedLength , boolean toRight) {

		if("0".equals(r.getGroupname()) && (pos!=0 || appliedLength != (ward.length))) return false;

		ListPair<AppliedRule> cl=ward[pos];
		if(cl==null) {
			cl = new ListPair<AppliedRule>();
			ward[pos] = cl;

		}
		if(toRight && cl.toRight.isEmpty()) {
			cl.toRight.addAfter(new AppliedRule(r, appliedLength));
			return true;
		}
		if(!toRight && cl.toLeft.isEmpty()) {
			cl.toLeft.addAfter(new AppliedRule(r, appliedLength));
			return true;
		}

		StatefulList<AppliedRule> theList = toRight? ward[pos].toRight : ward[pos].toLeft;
		theList.selectFirstElement();
		
		
	
		theList.push(new AppliedRule(r, appliedLength));
		
		return true;
	}
	
	
	public ListPair<AppliedRule>[] createWard(){
		ward = new ListPair[source.length()];
		addInitialForwardRules();
		boolean zeroFilled = false;
		for(int i=ward.length-1;i>=0;i--){
			if(ward[i]==null){
				continue;
			}
			
			StatefulList<AppliedRule> originalElements=ward[i].toRight;
			ward[i]= null;
			for(AppliedRule current:originalElements){
				AppliedRule currentAr = (AppliedRule) current;
				int elementToCheck=i+currentAr.getAppliedLength();
				if(elementToCheck == ward.length ||
						( ward[elementToCheck] != null && !ward[elementToCheck].toRight.isEmpty())){
					if(i == 0) zeroFilled = true;
					addToWard(i+currentAr.getAppliedLength()-1, currentAr.getRule(),currentAr.getAppliedLength(), false);
					addToWard(i,currentAr.getRule(),currentAr.getAppliedLength(),true);
				}
			}
		}

		if (!zeroFilled) return null;
		return ward;
	}

	
	private void addInitialForwardRules() {
		List<Rule> csdRuleList = this.csdRules;
		for(int i = 0;i < source.length(); i++){
			for(Rule r:csdRuleList){
				CharSequenceDescriptor csd= r.getFirstV().getCsd();
				if(csd.matchesInFrom(source, i)){
					addToWard(i, r,csd.getDescribedLength(), true);
				}
			}
		}
	}
}

