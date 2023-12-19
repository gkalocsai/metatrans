package syntax.tree.builder;

import hu.kg.list.StatefulList;
import syntax.Rule;
import syntax.display.SyntaxTreePic;
import syntax.tree.NonNegInt1D;
import syntax.tree.RulePointingToSpacing;
import syntax.tree.Spacing;
import syntax.tree.WaveElement;

public class CommonTreeData {

    // result props from here 
	Spacing[] spacings;
	String rootgroup;
	private boolean finished= false;


	private int[] SpacingIndicesStack=new int[1000]; //FIXME : length:= max rightside length of reference rules
	private int sp=0;


	NonNegInt1D prevSpacings=new NonNegInt1D(1000);

	NonNegInt1D nextSpacings=new NonNegInt1D(1000);

	public CommonTreeData(Spacing[] spacings, String rootgroup) {
		super();
		
		this.rootgroup = rootgroup;	

		if(spacings==null)  return;
		this.spacings = spacings;

	}


	void partialMatchFromLeft( Rule r, int startingSpacingIndex, int rsBeg) {
		String[] rs = r.getGroupRefsAsArray();
		nextSpacings.clear();
		sp=0;
		SpacingIndicesStack[sp] = startingSpacingIndex;		
		spacings[startingSpacingIndex].getNexts().selectFirstElement();
		while(sp>=0){
			do{ 
				if(getTopSpacingFromStack().isFrozen()) {
					sp--;
					break;
				}

				if(getSelectedRPPFromNexts() == null) {	
					sp--;			
					if(!stepNext()) return;
				}		
				RulePointingToSpacing rp = getSelectedRPPFromNexts();
				if(rp.getGroupname().equals(rs[sp+rsBeg])  &&  !spacings[rp.getSpacingIndex()].isFrozen()){
					if(sp+rsBeg==rs.length-1) {
						int nextSpacingindex = getSelectedRPPFromNexts().getSpacingIndex();
						nextSpacings.push(nextSpacingindex);
						if(!stepNext()) return; 
						break;
					}
					SpacingIndicesStack[sp+1] = getSelectedRPPFromNexts().getSpacingIndex();
					sp++;
					getTopSpacingFromStack().getNexts().selectFirstElement();
					break;
				}else{
					if(!stepNext()) return;
				}
			}while(true);
		}
	}


	void partialMatchFromRight(Rule r, int startIndex, int rsLast) {  

		String[] rs = r.getGroupRefsAsArray();
		prevSpacings.clear();
		sp=0;
		SpacingIndicesStack[sp] = startIndex;		
		spacings[startIndex].getPrevs().selectFirstElement();
		while(sp>=0){
			do{ 
				if(getTopSpacingFromStack().isFrozen()) {
					sp--;
					break;
				}
				if(getSelectedRPPFromPrevs() == null) {	
					sp--;			
					if(!stepPrev()) return;
				}			
				RulePointingToSpacing rp = getSelectedRPPFromPrevs();
				if(rp.getGroupname().equals(rs[rsLast-sp])&&  !spacings[rp.getSpacingIndex()].isFrozen()){
					if(rsLast-sp == 0) {
						int prevSpacingindex = getSelectedRPPFromPrevs().getSpacingIndex();
						prevSpacings.push(prevSpacingindex);					
						if(!stepPrev()) return; 
						break;
					}
					SpacingIndicesStack[sp+1] = getSelectedRPPFromPrevs().getSpacingIndex();
					sp++;
					getTopSpacingFromStack().getPrevs().selectFirstElement();
					break;
				}else{
					if(!stepPrev()) return;

				}
			}while(true);
		}
	}
	
	void partialMatchFromLeftDontCareFrozen( Rule r, int startingSpacingIndex, int rsBeg) {
		String[] rs = r.getGroupRefsAsArray();
		nextSpacings.clear();
		sp=0;
		SpacingIndicesStack[sp] = startingSpacingIndex;		
		spacings[startingSpacingIndex].getNexts().selectFirstElement();
		while(sp>=0){
			do{ 
					if(getSelectedRPPFromNexts() == null) {	
					sp--;			
					if(!stepNext()) return;
				}		
				RulePointingToSpacing rp = getSelectedRPPFromNexts();
				if(rp.getGroupname().equals(rs[sp+rsBeg]) ){
					if(sp+rsBeg==rs.length-1) {
						int nextSpacingindex = getSelectedRPPFromNexts().getSpacingIndex();
						nextSpacings.push(nextSpacingindex);
						if(!stepNext()) return; 
						break;
					}
					SpacingIndicesStack[sp+1] = getSelectedRPPFromNexts().getSpacingIndex();
					sp++;
					getTopSpacingFromStack().getNexts().selectFirstElement();
					break;
				}else{
					if(!stepNext()) return;
				}
			}while(true);
		}
	}


	void partialMatchFromRightDontCareFrozen(Rule r, int startIndex, int rsLast) {  

		String[] rs = r.getGroupRefsAsArray();
		prevSpacings.clear();
		sp=0;
		SpacingIndicesStack[sp] = startIndex;		
		spacings[startIndex].getPrevs().selectFirstElement();
		while(sp>=0){
			do{ 
				if(getSelectedRPPFromPrevs() == null) {	
					sp--;			
					if(!stepPrev()) return;
				}			
				RulePointingToSpacing rp = getSelectedRPPFromPrevs();
				if(rp.getGroupname().equals(rs[rsLast-sp])){
					if(rsLast-sp == 0) {
						int prevSpacingindex = getSelectedRPPFromPrevs().getSpacingIndex();
						prevSpacings.push(prevSpacingindex);					
						if(!stepPrev()) return; 
						break;
					}
					SpacingIndicesStack[sp+1] = getSelectedRPPFromPrevs().getSpacingIndex();
					sp++;
					getTopSpacingFromStack().getPrevs().selectFirstElement();
					break;
				}else{
					if(!stepPrev()) return;

				}
			}while(true);
		}
	}



	void matchMid(Rule r, int matchInRule, int prevIndex, int nextIndex) {

		prevSpacings.clear();
		prevSpacings.push(prevIndex);

		nextSpacings.clear();
		nextSpacings.push(nextIndex);



		if(r.getRightSideLength() == 1) {
			return;
		}

		if(matchInRule != 0)  {
			partialMatchFromRight(r, prevIndex, matchInRule-1);
		}

		if(matchInRule != r.getRightSideLength() -1) {
			partialMatchFromLeft(r, nextIndex, matchInRule+1);
		}


	}


	private RulePointingToSpacing getSelectedRPPFromPrevs() {
		return getTopSpacingFromStack().getPrevs().get();

	}
	private RulePointingToSpacing getSelectedRPPFromNexts() {
		return getTopSpacingFromStack().getNexts().get();
	}


	private Spacing getTopSpacingFromStack() {
		return spacings[SpacingIndicesStack[sp]];
	}


	private boolean stepNext(){

		if(sp<0) return false;

		while(!getTopSpacingFromStack().getNexts().stepNext()) {
			sp--;
			if(sp < 0) return false;	
		}
		return true;
	}


	private boolean stepPrev(){
		if(sp<0) return false;
		while(!getTopSpacingFromStack().getPrevs().stepNext()) {
			sp--;
			if(sp < 0) return false;	
		}
		return true;
	}


	void showSpacings() {
		SyntaxTreePic pac=new SyntaxTreePic(spacings);
		System.out.println(pac.getPic());
		System.out.println(pac.getBitesAndOwnersStr());
		
		
	}


	void simplePrintSpacings() {
		System.out.println();
		for(int i = 0;i <  spacings.length;i++){
			System.out.println(i+": "+spacings[i]);			
		}
		System.out.println("-------------------------------------------");
	}


	boolean finished() {
		if(finished) return true;
		for(RulePointingToSpacing rpp: spacings[0].getNexts()) {
			if(rpp.getSpacingIndex() == spacings.length-1 && rpp.getGroupname().equals(rootgroup)) return true;
		}

		return false;
	}





	public boolean addToSpacings(WaveElement we) {

		Rule rule = we.getRule();
		Spacing right=spacings[we.getNextSpacingindex()];
		boolean changed = false;
		if(!right.prevContains(rule,we.getPrevSpacingindex())){
			right.getPrevs().addAfter(new RulePointingToSpacing(rule, we.getPrevSpacingindex()));
			changed = true;
		}

		Spacing left=spacings[we.getPrevSpacingindex()];
		if(!left.nextContains(rule,we.getNextSpacingindex())){

			left.getNexts().addAfter(new RulePointingToSpacing(rule, we.getNextSpacingindex()));
			changed = true;
		}
		return changed;
	}


	public void freezeBetween(int before, int after, int freezeId) {
		for(int i = before+1; i<after ;i++){
			
			spacings[i].freeze(freezeId);			
		}

	}



	@Override
	public String toString(){
		StringBuilder sb= new StringBuilder();
		for(int i=0;i<spacings.length;i++){
			String is=Integer.toString(i)+":";
			sb.append(is);
			sb.append('\n');
			sb.append("    prevs:          "+spacings[i].getPrevs()+"\n");
			sb.append("    nexts:          "+spacings[i].getNexts()+"\n");
		}
		return sb.toString();
	}



	public WaveElement createMidRecWaveElement(Rule r, int recIndexInRule, int prevOfFirst, int nextOfFirst) {

		int rMin=prevOfFirst;
		int rMax=nextOfFirst;
		
		matchMid(r, recIndexInRule, rMin, rMax);
		while(!prevSpacings.isEmpty() &&  !nextSpacings.isEmpty()) {
			spacings[rMin].setBite(prevSpacings.getMin());
			spacings[rMax].setBite(nextSpacings.getMax());
			rMin=prevSpacings.getMin();
			rMax=nextSpacings.getMax();
			matchMid(r, recIndexInRule, rMin, rMax);
		}
		
		return new WaveElement(rMin, r, rMax);

	}

	public String spacingsIntegrityError(){
		for(int i=0; i<spacings.length;i++ ){
			for(RulePointingToSpacing rpp: spacings[i].getNexts()){
				if(!contains(spacings[rpp.getSpacingIndex()].getPrevs(),rpp.getRule(),i)){
					return rpp.toString();
				}
			}
		}
		for(int i=spacings.length-1; i>=0;i-- ){
			for(RulePointingToSpacing rpp: spacings[i].getPrevs()){
				if(!contains(spacings[rpp.getSpacingIndex()].getNexts(),rpp.getRule(),i)){
					return rpp.toString();
				}
			}
		}
		
		
		return null;
	}


	private boolean contains(StatefulList<RulePointingToSpacing> rl, Rule rule, int spacingIndex){
		for(RulePointingToSpacing rpp:rl){
			if(rpp.getRule() == rule && rpp.getSpacingIndex() == spacingIndex) return true;
		}
		return false;
	}
	
}
