package syntax.tree.builder;

import java.util.List;
import java.util.Stack;

import hu.kg.list.RelatedChecker;
import hu.kg.list.StatefulList;
import syntax.Rule;
import syntax.grammar.Grammarhost;
import syntax.tree.NonNegInt1D;
import syntax.tree.RulePointingToSpacing;
import syntax.tree.Spacing;
import syntax.tree.SpacingArrayBuilder;
import syntax.tree.WaveElement;

public class SyntaxTreeBuilder {


	private CommonTreeData cd;
	private boolean debug = false;
	private Grammarhost grammarhost;

	private int freezeId=1;


	//StatefulList<WaveElement> waves=new StatefulList<>();
	private RelatedChecker<RulePointingToSpacing> rc;


	public SyntaxTreeBuilder(Grammarhost grammarhost, String source) {		

		this.grammarhost = grammarhost;
		SpacingArrayBuilder spacingArrayBuilder=new SpacingArrayBuilder(source, grammarhost);
		cd = new CommonTreeData(spacingArrayBuilder.getCreatedSpacings(), grammarhost.getRootGroup());
		rc=new RelatedChecker<RulePointingToSpacing>() {

			@Override
			public boolean isRelated(RulePointingToSpacing a, RulePointingToSpacing b) {
				return a.getRule() == b.getRule() && a.getSpacingIndex() == b.getSpacingIndex();				
			}
		};
	}

	public boolean build(){
		if(cd.spacings == null) return false;

		boolean result = buildInner();

		//Compilation tree will be generated top-down, syntax tree was generated bottom-up
		//so we have to change the direction of the index flow of the recursive rule applications.

		//IndexFlowDirectionchanger.changeDirection(cd.spacings);

		if(debug) cd.showSpacings();
		return result;
	}


	private boolean buildInner() {
		if(cd.spacings == null) return false;
		if(isSyntaxtreeReady()) return true;
		if(debug) cd.showSpacings();
		for(int i=0;i<cd.spacings.length;i++){						
			if(cd.spacings[i].getPrevs().isEmpty()) continue;
			cd.spacings[i].getPrevs().selectFirstElement();

			do
			{   
				RulePointingToSpacing rpp =cd.spacings[i].getPrevs().get();
				List<Rule> rl=grammarhost.getApplicableRuleList(rpp.getGroupname());					
				StatefulList<RulePointingToSpacing>.Entry<RulePointingToSpacing> saved = cd.spacings[i].getPrevs().getEntry();
				while((matchedAndSpacingsChanged(rl,new WaveElement(rpp.getSpacingIndex(),rpp.getRule(),i)))>=-1){ //-2 means not found
					if(debug) cd.showSpacings();
					if(debug) System.out.println(cd.spacingsIntegrityError());

					cd.spacings[i].getPrevs().setEntry(saved);
				}
				cd.spacings[i].getPrevs().setEntry(saved);
			}while(cd.spacings[i].getPrevs().stepNext());
		}

		return isSyntaxtreeReady();
	}





	private int matchedAndSpacingsChanged(List<Rule> rl, WaveElement w) {

		for(Rule r:rl) {
			int result=matched(r,w);
			if(result>=-1)  return result;
		}
		return -2;

	}

	private int matched(Rule r, WaveElement w) {
		if(simpleMatch(r, w)) return -1;

		if(r.isRightRecursive()) {
			cd.partialMatchFromRightDontCareFrozen(r, w.getPrevSpacingindex(), r.getRightSideLength()-2);
		}
		else cd.partialMatchFromRightDontCareFrozen(r, w.getNextSpacingindex(), r.getRightSideLength()-1);
		if(cd.prevSpacings.isEmpty()){
			return -2;
		}

		if(!r.isDirectRecursive()) {
			if(addNonRecResults(r, w)) {
				return -1;
			}else {
				return -2;
			}
		} else if(w.getPrevSpacingindex() <= cd.prevSpacings.getMin()) {
			return -2;
		}

		return solveCollision(r,w.getNextSpacingindex());		
	}



	private int solveCollision(Rule r, int nextSpacingindex) {

		if(prevSpacingsDestroyedByStrongerOwners(r,nextSpacingindex) 
				|| hasBiggerOwnerSameRule(r, nextSpacingindex) ) return -2;

		if(!r.isLeftRecursive() || !r.isRightRecursive())  //??? 
			addOwnerDontCareFrozen(cd.prevSpacings.getMin(),r,nextSpacingindex);
		cd.freezeBetween(cd.prevSpacings.getMin(), nextSpacingindex, freezeId++);

		
		
		boolean sgAdded=false;
		if(!cd.prevSpacings.isEmpty()){
			for(int i=nextSpacingindex;i>=cd.prevSpacings.getMin();i--){

				WaveElement owner = cd.spacings[i].getOwner();
				if(owner!= null) {
					if(owner.getRule() == r){						
						merge(owner,nextSpacingindex, i);
						if(r.isLeftRecursive()) {
							for(RulePointingToSpacing rpp: cd.spacings[i].getPrevs()) {								
								if(rpp.getRule().getFirstV().isDescriptor()) continue;
								if(rpp.getRule().getLastRefGroup().equals(owner.getGroupname()))  {
								   extendToRight(rpp.getSpacingIndex(), new RulePointingToSpacing(rpp.getRule(), i), nextSpacingindex);										
								}
							}																					
						}						
					}				
					if(grammarhost.getStrength(owner.getRule()) < grammarhost.getStrength(r) && owner.getNextSpacingindex() < nextSpacingindex ){
						int prev=cd.prevSpacings.getMin();
						shrink(owner,i);
						boolean condition = prev>=0 && addRPP(cd.spacings[prev].getNexts(),r,nextSpacingindex);
						if(condition) {
							addRPP(cd.spacings[nextSpacingindex].getPrevs(),r,prev);
							sgAdded = true;
						}
					}
				}
			}
		}
		if(sgAdded) {
			return -1;
		}	
		else return -2;
	}

	private boolean prevSpacingsDestroyedByStrongerOwners(Rule r, int nextSpacing) {

		for(int i=cd.prevSpacings.getMax();i>=cd.prevSpacings.getMin();i--){
			if(i<0) return true;
			
			if(cd.prevSpacings.isEmpty()) {
				return true;
			}
			WaveElement owner = cd.spacings[i].getOwner();
			boolean condition = owner!= null && grammarhost.getStrength(owner.getRule()) > grammarhost.getStrength(r);
			if(condition) {
				while(cd.prevSpacings.getMin() >=0 && cd.spacings[cd.prevSpacings.getMin()].isFrozen() &&  owner.getNextSpacingindex() >= cd.prevSpacings.getAndRemoveMin()){
					break;
				}					
			} 
		}
		return false;
	}

	private boolean hasBiggerOwnerSameRule(Rule r, int nextSpacingindex) {
		for(int i=cd.prevSpacings.getMax();i<=nextSpacingindex;i++){

			WaveElement owner = cd.spacings[i].getOwner();
			boolean condition = owner!= null && owner.getRule() == r && owner.getPrevSpacingindex() <= cd.prevSpacings.getMin() && nextSpacingindex <=owner.getNextSpacingindex();
			if(condition) {
				return true;
			}
		}				
		return false;
	}

	private void addOwnerDontCareFrozen(int prevSpacingindex, Rule rule, int nextSpacingindex) {
		WaveElement we=new WaveElement(prevSpacingindex, rule, nextSpacingindex);


		if(rule.isMidRecursive()){
			addOwnerToMidRecDontCareFrozen(we);
			return;
		}

		if(rule.isLeftRecursive() && rule.isRightRecursive()){
			int l=getOwnerIndexLeftRecDCFrozen(we);
			if(l==-1) return;
			int r=getOwnerIndexRightRecDCFrozen(we);
			if(r==-1) return;
			cd.spacings[r].setOwner(we,l);
			cd.spacings[l].setOwner(we,r);



		}else{

			if(rule.isLeftRecursive()) {
				addOwnerToLeftRecDontCareFrozen(we);
			}

			if(rule.isRightRecursive()) {
				addOwnerToRightRecDontCareFrozen(we);//,oldOwner);
			}
		}

	}



	private boolean merge(WaveElement owner, int nextSpacingindex, int ownerIndex) {

		if(owner.getNextSpacingindex() >= cd.prevSpacings.getMin()) {
			if(owner.getNextSpacingindex() == nextSpacingindex && owner.getPrevSpacingindex()== cd.prevSpacings.getMin() ) {


				if(addRPP(cd.spacings[owner.getPrevSpacingindex()].getNexts(), owner.getRule(), owner.getNextSpacingindex())) {
					addRPP(cd.spacings[owner.getNextSpacingindex()].getPrevs(), owner.getRule(), owner.getPrevSpacingindex());
								
					return true;
				}else {
					return false;
				}
			}
			if(owner.getRule().isRightRecursive())  {
				cd.spacings[owner.getNextSpacingindex()].setOwner(owner, owner.getPrevSpacingindex());
			}


			return extend(cd.prevSpacings.getMin(), owner,nextSpacingindex,ownerIndex);

		}

		return false;
	}


	private boolean extendToRight(int prev, RulePointingToSpacing rppx, int next) {
		int originalNext=rppx.getSpacingIndex();
		Rule r=rppx.getRule();

		removeRPP(cd.spacings[originalNext].getPrevs(), r, prev);
		addRPP(cd.spacings[next].getPrevs(), r , prev);
		changeRPP(cd.spacings[prev].getNexts(), r,originalNext, next);
		
		WaveElement ow = getOwnerFromRight(prev, rppx.getRule(), next);
		if(ow!=null ) ow.setNextSpacingindex(next);
		
		String group = r.getGroupname();
		Stack<String> stack=new Stack<>();
		stack.push(group);


		while(!stack.isEmpty()){

			group=stack.pop();
			for(RulePointingToSpacing rpp: cd.spacings[originalNext].getPrevs()) {
				if(!rpp.getRule().isDirectRecursive()) continue;
				if(rpp.getRule().getLastRefGroup().equals(group))  {
					stack.push(rpp.getGroupname());
					WaveElement o = getOwnerFromRight(rpp.getSpacingIndex(), rpp.getRule(), originalNext);
					if(o == null) {
						stack.pop();
						break;
					}
					
					o.setNextSpacingindex(next);	

					removeRPP(cd.spacings[originalNext].getPrevs(), rpp.getRule(), rpp.getSpacingIndex());
					addRPP(cd.spacings[next].getPrevs(), rpp.getRule() , rpp.getSpacingIndex());
					changeRPP(cd.spacings[rpp.getSpacingIndex()].getNexts(), rpp.getRule(),originalNext, next);				
				}
			}
		}
		return true;

	}

	private boolean extend(int prev, WaveElement owner, int next, int ownerIndex) {
		if(owner  ==  null) return false;
		int op=owner.getPrevSpacingindex();
		int on=owner.getNextSpacingindex();




		if(prev<op) {
			owner.setPrevSpacingindex(prev);
			removeRPP(cd.spacings[op].getNexts(), owner.getRule(), on);		 
			addRPP(cd.spacings[prev].getNexts(), owner.getRule(), on);
			changeRPP(cd.spacings[on].getPrevs(), owner.getRule(),op, prev);

			String group = owner.getGroupname();


			for(RulePointingToSpacing rpp: cd.spacings[op].getNexts()) {
				if(!rpp.getRule().isDirectRecursive()) continue;
				if(rpp.getRule().extractRefGroups()[0].equals(group))  {
					WaveElement o=getOwnerFromLeft(op, rpp.getRule(), next);
					extend(prev,o,rpp.getSpacingIndex(),-1);

				}
			}

			return true;
		}

		if(next >on){
			owner.setNextSpacingindex(next);
			removeRPP(cd.spacings[on].getPrevs(), owner.getRule(), op);
			addRPP(cd.spacings[next].getPrevs(), owner.getRule(), op);
			changeRPP(cd.spacings[op].getNexts(), owner.getRule(),on, next);
			String group = owner.getGroupname();


			for(RulePointingToSpacing rpp: cd.spacings[on].getPrevs()) {
				if(!rpp.getRule().isDirectRecursive()) continue;
				if(rpp.getRule().getLastRefGroup().equals(group))  {
					WaveElement o=getOwnerFromLeft(prev, rpp.getRule(), on);
					extend(rpp.getSpacingIndex(),o,next,-1);
				}
			}
			return true;
		}		

		return false;
	}

	private void shrink(WaveElement owner, int spacingIndex) {
		int maxNext=cd.prevSpacings.getMin();		
		int freezeIdToRemove=cd.spacings[spacingIndex].getFreezeId();
		int oldNext=owner.getNextSpacingindex();

		if(owner.getRule().isRightRecursive()) {
			removeRPP(cd.spacings[oldNext].getPrevs(), owner.getRule(), owner.getPrevSpacingindex());
			int righBiteIndex = getOwnerIndexRightRecSame(owner);





			if(righBiteIndex >=0) {
				int bite  =cd.spacings[righBiteIndex].getBite();
				if(bite < righBiteIndex) {
					removeOwnerAndMelt(maxNext,owner.getNextSpacingindex()-1, owner, freezeIdToRemove);
					owner.setNextSpacingindex(bite);
					cd.spacings[bite].setOwner(null,-1);
					cd.spacings[bite].freeze(-1);

					//removeRPP(cd.spacings[oldNext].getPrevs(), owner.getRule(), owner.getPrevSpacingindex());
					addRPP(cd.spacings[bite].getPrevs(), owner.getRule(), owner.getPrevSpacingindex());
					changeRPP(cd.spacings[owner.getPrevSpacingindex()].getNexts(), owner.getRule(), oldNext, bite);
					return;
				}

			}
		}
		if(owner.getRule().isMidRecursive()){
			//int bitesToRemove=0;
			removeOwnerAndMelt(maxNext,owner.getNextSpacingindex()-1, owner, freezeIdToRemove);
			return;
		}

		int nn=-1;
		for(int i=maxNext;i>owner.getPrevSpacingindex();i--){
			if(cd.spacings[i].getOwner() == owner &&
					cd.spacings[i].getBite() >= 0
					){
				if(!owner.getRule().isRightRecursive() || !owner.getRule().isLeftRecursive()){
					nn=i;
				}
				else{
					if(nn>0)  {
						nn=i;
						break;
					}else{
						nn=i;
						continue;
					}
				}
				break;
			}
		}	

		if(nn<0){
			removeOwnerAndMelt(owner.getPrevSpacingindex()+1,owner.getNextSpacingindex()-1,owner,freezeIdToRemove);
			removeRPP(cd.spacings[owner.getNextSpacingindex()].getPrevs(), owner.getRule(), owner.getPrevSpacingindex());
			removeRPP(cd.spacings[owner.getPrevSpacingindex()].getNexts(), owner.getRule(), owner.getNextSpacingindex());

		}else{
			removeOwnerAndMelt(nn, maxNext, owner, freezeIdToRemove);
			NonNegInt1D save=new NonNegInt1D(cd.prevSpacings.elementCount);
			save.addAll(cd.prevSpacings);

			cd.partialMatchFromRightDontCareFrozen(owner.getRule(), nn, owner.getRule().getRightSideLength()-1);

			if(cd.prevSpacings.contains(owner.getPrevSpacingindex())){

				removeRPP(cd.spacings[owner.getNextSpacingindex()].getPrevs(), owner.getRule(), owner.getPrevSpacingindex());
				addRPP(cd.spacings[nn].getPrevs(), owner.getRule(), owner.getPrevSpacingindex());
				changeRPP(cd.spacings[owner.getPrevSpacingindex()].getNexts(), owner.getRule(), owner.getNextSpacingindex(), nn);
				owner.setNextSpacingindex(nn);
			}  else{
				removeRPP(cd.spacings[owner.getNextSpacingindex()].getPrevs(), owner.getRule(), owner.getPrevSpacingindex());
				removeRPP(cd.spacings[owner.getPrevSpacingindex()].getNexts(), owner.getRule(), owner.getNextSpacingindex());

			}

			cd.prevSpacings.clear();
			cd.prevSpacings.addAll(save);

		} 
	}

	private int getOwnerIndexRightRecSame(WaveElement we) {
		for(int i=we.getNextSpacingindex()-1; i> we.getPrevSpacingindex(); i--){
			WaveElement owner=cd.spacings[i].getOwner();
			if(owner!=null && owner.equals(we)){
				return i;

			}	
		}
		return -1;
	}

	private void removeOwnerAndMelt(int j, int k, WaveElement owner, int freezeIdToRemove) {
		for(int i=j;i<=k;i++){
			if(owner.equals(cd.spacings[i].getOwner())) {
				freezeIdToRemove = cd.spacings[i].getFreezeId();
				cd.spacings[i].setOwner(null,-1);
			}
		}
		for(int i=j;i<=k;i++){
			if(cd.spacings[i].getFreezeId()==freezeIdToRemove) {
				cd.spacings[i].freeze(-1);
			}

		}
	}

	private boolean simpleMatch(Rule r, WaveElement w) {
		if(!r.isRightRecursive()) {
			cd.partialMatchFromRight(r, w.getNextSpacingindex(), r.getRightSideLength()-1);
		}else{
			cd.partialMatchFromRight(r, w.getPrevSpacingindex(), r.getRightSideLength()-2);
		}
		if(!r.isDirectRecursive()) return addNonRecResults(r, w);
		int prev=cd.prevSpacings.getMin();
		if(prev <0 )  return false; 
		if(r.isRightRecursive() && killWeakerRulesFromRight(w.getNextSpacingindex(),r)); 
		if(r.isLeftRecursive() && killWeakerRulesFromLeft(prev,r)); 


		StatefulList<RulePointingToSpacing> l = cd.spacings[prev].getNexts();

		if(addRPP(l,r,w.getNextSpacingindex())){
			addRPP(cd.spacings[w.getNextSpacingindex()].getPrevs(),r,prev);	
			if(!merged(prev,r,w.getNextSpacingindex())) {
				addOwnerToNonExtendedMatch(prev,r,w.getNextSpacingindex());
			}
			cd.freezeBetween(prev, w.getNextSpacingindex(),freezeId++);			
			return true;
		}

		return false;
	}

	private boolean killWeakerRulesFromLeft(int i, Rule r) {
		boolean killed=false;
		for(RulePointingToSpacing rpp: cd.spacings[i].getNexts()){
			if(!rpp.getRule().isDirectRecursive()) continue;

			if(grammarhost.getStrength(r) > grammarhost.getStrength(rpp.getRule())){
				killed=true;
				removeRPP(cd.spacings[i].getNexts(), rpp.getRule(), rpp.getSpacingIndex());
				removeRPP(cd.spacings[rpp.getSpacingIndex()].getPrevs(), rpp.getRule(), i);
				removeOwnerAndMelt(i,rpp.getSpacingIndex(),  new WaveElement(i,  rpp.getRule(),rpp.getSpacingIndex()), -1);
			}

		}
		return killed;
	}

	private boolean killWeakerRulesFromRight(int i, Rule r) {
		boolean killed=false;
		for(RulePointingToSpacing rpp: cd.spacings[i].getPrevs()){
			if(!rpp.getRule().isDirectRecursive()) continue;
			if(grammarhost.getStrength(r) > grammarhost.getStrength(rpp.getRule())){
				killed=true;
				removeRPP(cd.spacings[i].getPrevs(), rpp.getRule(), rpp.getSpacingIndex());
				removeRPP(cd.spacings[rpp.getSpacingIndex()].getNexts(), rpp.getRule(), i);
				removeOwnerAndMelt(rpp.getSpacingIndex(), i, new WaveElement(rpp.getSpacingIndex(), rpp.getRule(), i), -1);
			}

		}
		return killed;
	}


	private boolean addNonRecResults(Rule r, WaveElement w) {
		boolean elementAdded=false;
		while(!cd.prevSpacings.isEmpty()){
			int prev=cd.prevSpacings.getAndRemoveMin();
			StatefulList<RulePointingToSpacing> l = cd.spacings[prev].getNexts();
			for(RulePointingToSpacing  rpp:l) {
				boolean condition = rpp.getRule() == r && rpp.getSpacingIndex() < w.getNextSpacingindex() && extendToRight(prev, rpp, w.getNextSpacingindex());
				if(condition)
					return true;
			}
			if(addRPP(l,r,w.getNextSpacingindex())){
				addRPP(cd.spacings[w.getNextSpacingindex()].getPrevs(),r,prev);
				elementAdded = true;
			}
		}
		return elementAdded;
	}

	private boolean merged(int prev, Rule r, int nextSpacingindex) {
		boolean wasmerge =false;
		if(r.isMidRecursive()) {

			//TODO
		}

		for(RulePointingToSpacing rpp: cd.spacings[prev].getNexts()){
			if(rpp.getRule() == r && rpp.getSpacingIndex() <nextSpacingindex) {
				wasmerge = true;
				WaveElement owner=null;
				if(r.isRightRecursive()) {
					owner=getOwnerFromRight(prev,r,nextSpacingindex);

				}else{
					owner=getOwnerFromLeft(prev,r,nextSpacingindex);					
				}
				if(owner == null) continue;
				int on=owner.getNextSpacingindex();
				owner.setNextSpacingindex(nextSpacingindex);
				if(r.isLeftRecursive() && !r.isRightRecursive()){
					cd.spacings[on].setOwner(owner,on);
				}

				if(r.isRightRecursive()){
					//recapture the rightmost border 

					StatefulList<RulePointingToSpacing> rl = cd.spacings[nextSpacingindex].getPrevs();
					for(RulePointingToSpacing rpp2:rl){
						if(rpp2.getGroupname().equals(r.getGroupname())){
							cd.partialMatchFromRightDontCareFrozen(r, rpp2.getSpacingIndex(), r.getRightSideLength()-2);
							if(cd.prevSpacings.contains(prev)){
								cd.spacings[rpp2.getSpacingIndex()].setOwner(owner,on);
								if(rpp.getRule().isLeftRecursive() && rpp.getRule().isRightRecursive()){
									cd.spacings[on].setOwner(owner,rpp2.getSpacingIndex());
								}

							}
						}						
					}																
				}

				removeRPP(cd.spacings[prev].getNexts(), r, on);
				removeRPP(cd.spacings[on].getPrevs(), r, prev);

			}			
		}

		if(!wasmerge){
			for(RulePointingToSpacing rpp: cd.spacings[nextSpacingindex].getPrevs()){
				if(rpp.getRule() == r && rpp.getSpacingIndex() > prev) {
					wasmerge = true;
					WaveElement owner=getOwnerFromRight(prev,r,nextSpacingindex);


					for(int i=nextSpacingindex-1;i>prev;i--){
						owner=cd.spacings[i].getOwner();
						if(owner!=null && owner.getRule() == r ) {
							int op = owner.getPrevSpacingindex();
							cd.spacings[op].setOwner(owner, op);

							owner.setPrevSpacingindex(prev);

							removeRPP(cd.spacings[op].getNexts(), r, nextSpacingindex);
							removeRPP(cd.spacings[nextSpacingindex].getPrevs(), r, op);	
							break;
						}
					}
				}			
			}
		}

		return wasmerge;
	}

	private WaveElement getOwnerFromLeft(int prev, Rule r, int nextSpacingindex) {
		for(int i=prev+1;i<nextSpacingindex;i++){
			WaveElement owner=cd.spacings[i].getOwner();
			boolean condition = owner!=null && owner.getRule() == r && owner.getPrevSpacingindex() ==prev;
			if(condition) {
				return owner;
			}
		}
		return null;
	}

	private WaveElement getOwnerFromRight(int prev, Rule r, int nextSpacingindex) {
		for(int i=nextSpacingindex-1;i>prev;i--){
			WaveElement owner=cd.spacings[i].getOwner();
			boolean condition = owner!=null && owner.getRule() == r && owner.getPrevSpacingindex() ==prev;
			if(condition) {
				return owner;
			}
		}
		return null;
	}

	private void addOwnerToNonExtendedMatch(int prevSpacingindex, Rule rule, int nextSpacingindex) {
		WaveElement we=new WaveElement(prevSpacingindex, rule, nextSpacingindex);
		if(rule.isMidRecursive()){
			//addOwnerToMidRecFirstApplication(we);
			addOwnerToMidRecDontCareFrozen(we);
			return;
		}
		if(rule.isLeftRecursive()  && rule.isRightRecursive())  {
			addFirstOwnerToLeftAndRightRec(we);
			return;
		}

		if(rule.isLeftRecursive()) {
			//addOwnerToLeftRecFirstApplication(we);
			addOwnerToLeftRecDontCareFrozen(we);
		}
		if(rule.isRightRecursive()) {
			//addOwnerToRightRecFirstApplication(we);
			addOwnerToRightRecDontCareFrozen(we);
		}
	}


	private void addFirstOwnerToLeftAndRightRec(WaveElement we) {
		int left=getOwnerIndexLeftRecDCFrozen(we);

		cd.spacings[left].setOwner(we,left);
		int right=getOwnerIndexRightRecDCFrozen(we);
		cd.spacings[right].setOwner(we,right);
	}

	private int getOwnerIndexRightRecDCFrozen(WaveElement we) {



		Rule rule = we.getRule();
		int prevSpacingindex = we.getPrevSpacingindex();
		int nextSpacingindex = we.getNextSpacingindex();		
		StatefulList<RulePointingToSpacing> l = cd.spacings[nextSpacingindex].getPrevs();
		for(RulePointingToSpacing rpp:l){
			if(rpp.getGroupname().equals(rule.getGroupname())){
				cd.partialMatchFromRightDontCareFrozen(rule, rpp.getSpacingIndex(), rule.getRightSideLength()-2);
				if(cd.prevSpacings.contains(prevSpacingindex)){
					return rpp.getSpacingIndex();
				}
			}
		}
		return -1;
	}

	private int getOwnerIndexLeftRecDCFrozen(WaveElement we) {
		Rule rule = we.getRule();
		int prevSpacingindex = we.getPrevSpacingindex();
		int nextSpacingindex = we.getNextSpacingindex();		
		StatefulList<RulePointingToSpacing> l = cd.spacings[prevSpacingindex].getNexts();
		for(RulePointingToSpacing rpp:l){
			if(rpp.getGroupname().equals(rule.getGroupname())){
				cd.partialMatchFromLeftDontCareFrozen(rule, rpp.getSpacingIndex(), 1);
				if(cd.nextSpacings.contains(nextSpacingindex)) {
					cd.spacings[rpp.getSpacingIndex()].setOwner(we,nextSpacingindex);
					return rpp.getSpacingIndex();
				}
			}			
		}	
		return -1;
	}

	private void addOwnerToRightRecDontCareFrozen(WaveElement we) {
		int right=getOwnerIndexRightRecDCFrozen(we);
		if(right<0) return;



		cd.spacings[right].setOwner(we,right); //we.getNextSpacingindex());  
	}


	private void addOwnerToLeftRecDontCareFrozen(WaveElement we) {
		int l=getOwnerIndexLeftRecDCFrozen(we);
		if(l==-1) return;

		cd.spacings[l].setOwner(we,we.getNextSpacingindex());	
	}


	private void addOwnerToMidRecDontCareFrozen(WaveElement we) {
		Rule rule = we.getRule();
		int prevSpacingindex = we.getPrevSpacingindex();
		int nextSpacingindex = we.getNextSpacingindex();

		int indexInRs=rule.getIndexOfRefGroup(rule.getGroupname());	
		for(int i=prevSpacingindex+1; i< nextSpacingindex ;i++){
			StatefulList<RulePointingToSpacing> l = cd.spacings[i].getNexts();
			for(RulePointingToSpacing rpp:l){

				cd.partialMatchFromRight(rule, i, indexInRs-1);
				if(cd.prevSpacings.contains(prevSpacingindex)) {
					cd.partialMatchFromLeftDontCareFrozen(rule, rpp.getSpacingIndex(), indexInRs+1);
					if(cd.nextSpacings.contains(nextSpacingindex)) {
						cd.spacings[i].setOwner(we,i);
						cd.spacings[rpp.getSpacingIndex()].setOwner(we,rpp.getSpacingIndex());//i+1);
						return;
					}
				}												

			}
		}
	}


	private boolean addRPP(StatefulList<RulePointingToSpacing> list, Rule r, int n) {

		RulePointingToSpacing rp=new RulePointingToSpacing(r, n);
		if(list.containsValue(rp)) return false;
		else {
			list.pushAfter(rp);
			return true;
		}


	}

	private void removeRPP(StatefulList<RulePointingToSpacing> list, Rule r, int n) {
		list.remove(new RulePointingToSpacing(r, n),rc);
	}

	private void changeRPP(StatefulList<RulePointingToSpacing> l, Rule r, int original, int newPointsto) {
		for(RulePointingToSpacing rpp:l){
			if(rpp.getRule() == r  && rpp.getSpacingIndex() == original) {
				rpp.setSpacingIndex(newPointsto);
			}			
		}		
	}

	public Spacing[] getTree() {
		return cd.spacings;
	}	

	private boolean isSyntaxtreeReady() { 
		return cd.finished();
	}

	public void showSyntaxtree(){
		this.debug =  true;
	}

	public void createFirstWave() {
		for(Rule r:grammarhost.getNonRecursiveRefRules()) {
			for(int i=0; i<cd.spacings.length;i++){
				cd.partialMatchFromLeft(r,i,0);
				for(int k=0;k<cd.nextSpacings.elementCount;k++){
					addRPPsToSpacings(i,cd.nextSpacings.data[k],r);
				}
			}
		}
	}
	private void addRPPsToSpacings(int prevSpacingindex, int nextSpacingindex, Rule r) {
		cd.spacings[prevSpacingindex].getNexts().addAfter(new RulePointingToSpacing(r, nextSpacingindex));
		cd.spacings[nextSpacingindex].getPrevs().addAfter(new RulePointingToSpacing(r, prevSpacingindex));	
	}
}
