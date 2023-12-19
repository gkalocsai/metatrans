package syntax.grammar.badeater;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import descriptor.OneCharDesc;
import hu.kg.list.ObjectsInDoubleBuff;
import syntax.Rule;

public class Deduction {

	private ObjectsInDoubleBuff<Branch> dbuff=new ObjectsInDoubleBuff<>();
	private Map<String, ArrayList<Rule>> ruleMap;
	private List<OneCharDesc> ocdResult;

	public Deduction(List<Branch>  branchList,Map<String, ArrayList<Rule>> ruleMap ) {
		this.ruleMap = ruleMap;
		for(Branch br:branchList) {
			dbuff.write(br);
		}
		dbuff.changeBuffer();
	}



	public Deduction(PointedRule r, Map<String, ArrayList<Rule>> ruleMap) {
		this.ruleMap = ruleMap;
		dbuff.write(new Branch(r));
		dbuff.changeBuffer();

	}


	public void filterBranches(List<OneCharDesc> l ) {
		while(dbuff.moreToRead()) {
			Branch current= dbuff.read();
			OneCharDesc o=current.getPrList().get(current.getPrList().size()-1).getCurrentOCD();
			for(OneCharDesc r: l) {
				if(r.hasCommonChar(o) && !dbuff.written(current)){
					dbuff.write(current);
				}
			} 		
		}
		dbuff.changeBuffer();
	}
	public List<OneCharDesc> getCurrentOcds(){
		ocdResult = new LinkedList<>();
		while(dbuff.moreToRead()) {
			Branch current= dbuff.read();
			OneCharDesc o=current.getPrList().get(current.getPrList().size()-1).getCurrentOCD();
			if (o!=null) {
				ocdResult.add(o);
				dbuff.write(current);
			} else{
				ObjectsInDoubleBuff<Branch> branches=new ObjectsInDoubleBuff<>();
				branches.write(current);
				branches.changeBuffer();
				growBranchTillCsd(branches);

			}
		}
		dbuff.changeBuffer();
		return ocdResult;		
	}

	private void growBranchTillCsd(ObjectsInDoubleBuff<Branch> branches) {

		boolean wasProgress=true;
		while(wasProgress) {
			wasProgress = false;
			while(branches.moreToRead()) {
				Branch current = branches.read();	
				OneCharDesc o=current.getPrList().get(current.getPrList().size()-1).getCurrentOCD();
				if (o == null) {
					wasProgress = true;
					LinkedList<PointedRule> l=current.getPrList();
					PointedRule last=l.getLast();	
					ArrayList<Rule> rl = ruleMap.get(last.getCurrentGroup());

					for(Rule rr:rl) {
						PointedRule candidate=new PointedRule(rr, last.isForward()) ;
						if(!l.contains(candidate)) {
							LinkedList<PointedRule> l2=new LinkedList<>();
							l2.addAll(l);
							l2.add(candidate);
							branches.write(new Branch(l2));
						}      
					}					
				}else{
					ocdResult.add(o);
					dbuff.write(current);
				}
			}
			branches.changeBuffer();
		}
	}



	public boolean step() {
		while(dbuff.moreToRead()) {
			Branch br=dbuff.read();
			LinkedList<PointedRule> l = br.getPrList();
			if(stepBranch(l))  {
				dbuff.write(br);				
			}else{
				return false;
			}
			
		}
		dbuff.changeBuffer();
		return true;

	}

	public boolean isEmpty(){
		
		return !dbuff.moreToRead();
	}

	
	
	private boolean stepBranch(LinkedList<PointedRule> l) {
		if(l.isEmpty()) return false;
		PointedRule last = l.getLast();
		while(!(last.canMovePoint())) {
			l.removeLast();
			if(l.isEmpty()) return false;
			last=l.getLast(); 
		}
		last.stepPoint();
		return true;
	}
	
	@Override
	public String toString() {
		return dbuff.toString();
	}
}

