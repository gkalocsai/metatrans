package syntax.tree;

import java.util.HashMap;
import java.util.Map;

import syntax.AppliedRule;
import syntax.CsdRulesApplier;
import syntax.ListPair;
import syntax.grammar.Grammarhost;

public class SpacingArrayBuilder {

	
	//Vertices are positions in the source
	//Edges will be connections between Rules and Spacings
	
	
	private Map<Integer, Integer> wardIndex2SpacingIndex = new HashMap<>();
	private Spacing[] spacings = null;
	
	private ListPair<AppliedRule>[] ward;

	public SpacingArrayBuilder(String src, Grammarhost gh) {		
		CsdRulesApplier csdRulesApplier = new CsdRulesApplier(src, gh.getCsdRules());		
		this.ward=csdRulesApplier.createWard();
		
	}
	
	
	
	public Spacing[] getCreatedSpacings(){		
		if(spacings != null) return spacings;
		if(ward == null) return null; 
		
		int length=countValidWardPositions();
		spacings = createEmptySpacings(length+1);
		fillWard2Spacing(length);
	    fillSpacings();
	    sortNextsAndPrevs();
	    
		return spacings;
	}


	

	private void sortNextsAndPrevs() {
		for (Spacing spacing : spacings) {
			spacing.sort();
		}
		
	}



	private void fillWard2Spacing(int length) {
		wardIndex2SpacingIndex.put(ward.length, spacings.length-1);
		int SpacingIndex = 0;		
		for(int i = 0; i< ward.length;i++){
			if(existsAndNotEmpty(ward[i])){
				wardIndex2SpacingIndex.put(i, SpacingIndex);
				SpacingIndex++;
			}
			
		}		
	}

	private void fillSpacings() {
		for(int i = 0;i < ward.length;i++) {
		   	if(existsAndNotEmpty(ward[i])) {
		   		int SpacingIndex=wardIndex2SpacingIndex.get(i);
		   		spacings[SpacingIndex].sourceIndex = i;
		   		for(AppliedRule r: ward[i].toRight){
		   			int nextSpacing=wardIndex2SpacingIndex.get(i+r.getAppliedLength());		   			
		   			spacings[SpacingIndex].getNexts().addAfter(new RulePointingToSpacing(r.getRule(), nextSpacing));
		   			spacings[nextSpacing].getPrevs().addAfter(new RulePointingToSpacing(r.getRule(), SpacingIndex));
		   		}		   		
		   	}
		}
	}


	private Spacing[] createEmptySpacings(int length) {
		Spacing[] emptySpacings=new Spacing[length];
		for(int i=0;i<emptySpacings.length;i++){
			emptySpacings[i] = new Spacing();			
		}
		return emptySpacings;
	}



	private int countValidWardPositions() {
		int count=0;
		for(ListPair<AppliedRule> lp:ward) {
			if(existsAndNotEmpty(lp)) {
				count++;
			}			
		}
		return count;
	}

	private boolean existsAndNotEmpty(ListPair<AppliedRule> lp) {
		if (lp == null) return false;		
		else if(lp.toLeft.isEmpty() && lp.toRight.isEmpty()) return false;
		else return true;
	}
	
	
	
}
