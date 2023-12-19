package syntax.grammar.badeater;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import descriptor.CharSequenceDescriptor;
import descriptor.OneCharDesc;
import syntax.Rule;
import syntax.V;
import syntax.grammar.Grammarhost;

public class BadTerminatorFinder {


	
	
	

	public static Boolean checkBackwardMovingPointed(Grammarhost gh){
		Rule[] rec = gh.getRecursiveRefRules();

		for(Rule r:rec) {
			List<OneCharDesc> ocds = getLastOcds(r, gh);
			List<Branch> brs = getBackwardMovingBranches(ocds, gh);
			if(brs.isEmpty()) continue;
			//System.out.println(brs);


			Deduction nonRec=new Deduction(brs, gh.getGrammar());
			Deduction rr=new Deduction(new PointedRule(r, false), gh.getGrammar());
			//System.out.println(rr);
			//System.out.println(nonRec);

			for(int i=0;i<1000;i++) {
				List<OneCharDesc> recChars=rr.getCurrentOcds();
				nonRec.getCurrentOcds();
				nonRec.filterBranches(recChars);


				if(nonRec.isEmpty()) break;
			//	System.out.println(nonRec);
				if(!nonRec.step()) {
					return false;
				}
				rr.step();
			}
		}
		return true;
	}


	public static List<Branch> getBackwardMovingBranches(List<OneCharDesc> ocds,Grammarhost gh){
		Rule[] refRules=gh.getNonRecursiveRefRules();
		List<Branch> result=new LinkedList<>();
		List<PointedRule> rulesWithNonLastMatchedOcds = getPointedCsdRulesWithCommonNonlast(ocds, gh);

		for(PointedRule pr:rulesWithNonLastMatchedOcds) {
			String gr=pr.getRule().getGroupname();
			for(Rule nonRec:refRules)  {
				String[] rsRefs=nonRec.extractRefGroups();
				for(int i=0;i<rsRefs.length;i++){
					if(rsRefs[i].equals(gr)){
						LinkedList<PointedRule> list = new LinkedList<>();
						list.add(new PointedRule(nonRec, i, false));
						list.add(pr);
						result.add(new Branch(list));

					}
				}
			}
		}

		List<PointedRule> rulesWithLastMatchedOcds = getPointedCsdRulesWithCommonLast(ocds, gh);

		for(PointedRule pr:rulesWithLastMatchedOcds) {
			String gr=pr.getRule().getGroupname();
			for(Rule nonRec:refRules)  {
				String[] rsRefs=nonRec.extractRefGroups();
				for(int i=0;i<rsRefs.length;i++){
					if(rsRefs[i].equals(gr)){
						LinkedList<PointedRule> list = new LinkedList<>();
						list.add(new PointedRule(nonRec, i, false));
						list.add(pr);
						result.add(new Branch(list));

					}
				}
			}
		}

		result = extendBackward(result,gh);

		return result;
	}

	private static List<Branch> extendBackward(List<Branch> branches, Grammarhost gh) {
		List<Branch> brs=new LinkedList<>();

		boolean check  = true;
		while(check) {
			check  = false;
			for(Branch br:branches) {
				if( br.getPrList().getFirst().getRule().getRightSideLength()  > 1 ) {
					brs.add(br);
				}else{

					List<Branch> extendedBranches = extendBranchOnLeft(br,gh);
					if(!extendedBranches.isEmpty()) {
						brs.addAll(extendedBranches);
						check = true;
					}
				}
			}
			branches=brs;
			brs=new LinkedList<>();
		}
		return branches;
	}

	private static List<Branch> extendBranchOnLeft(Branch br, Grammarhost gh) {

		List<Branch> result = new LinkedList<>();

		String group = br.getPrList().getFirst().getRule().getGroupname();//getFirstV().getReferencedGroup();
		Rule[] nonRecRules = gh.getNonRecursiveRefRules();
		for(Rule r:nonRecRules){
			int point=r.getIndexOfRefGroup(group);
			if(point<0) continue;
			else{
				LinkedList<PointedRule> prList=new LinkedList<>();
				prList.addAll(br.getPrList());
				prList.addFirst(new PointedRule(r,point, false));
				Branch nb=new Branch(prList);
				result.add(nb);
			}
			
			
		}
		return result;
	}

	static List<PointedRule> getPointedCsdRulesWithCommonNonlast(List<OneCharDesc> ocds,Grammarhost gh){

		List<PointedRule> result=new LinkedList<>();

		for(Rule c:gh.getCsdRules()){
			CharSequenceDescriptor csd = c.getFirstV().getCsd();
			OneCharDesc[] ocda = csd.getOcdArray();
			for(int i=0;i<ocda.length-1;i++) {
				for(OneCharDesc other:ocds) {
					if(ocda[i].hasCommonChar(other)) {
						result.add(new PointedRule(c,i, false));
					}
				}

			}
		}
		return result;

	}

	static List<PointedRule> getPointedCsdRulesWithCommonLast(List<OneCharDesc> ocds,Grammarhost gh){

		List<PointedRule> result=new LinkedList<>();

		for(Rule c:gh.getCsdRules()){
			CharSequenceDescriptor csd = c.getFirstV().getCsd();
			OneCharDesc[] ocda = csd.getOcdArray();

			for(OneCharDesc other:ocds) {
				if(ocda[ocda.length-1].hasCommonChar(other)) {
					PointedRule pointedRule = new PointedRule(c,ocda.length-1, false);
					if(!result.contains(pointedRule)) result.add(pointedRule);
				}
			}
		}
		return result;
	}



	public static List<OneCharDesc> getLastOcds(Rule r, Grammarhost gh){


		List<OneCharDesc> result=new LinkedList<>();

		Stack<Rule> st=new Stack<>();
		st.push(r);

		Set<Rule> checked=new HashSet<>(); 

		while(!st.isEmpty()) {
			r=st.pop();
			checked.add(r);


			V[] rs2=r.getRightside();
			V last=rs2[rs2.length-1];

			if(last.isDescriptor()) {
				OneCharDesc[] x = last.getCsd().getOcdArray();
				result.add(x[x.length-1]);
			}else{
				ArrayList<Rule> rl = gh.getGrammar().get(last.getReferencedGroup());
				for(Rule rule:rl){
					if(!checked.contains(rule)) {
						st.push(rule);
					}			
				}
			}
		}
		return result;
	}


	public static Boolean isOK(Grammarhost gh) {
		Map<String, ArrayList<Rule>> map = gh.getGrammar();
		for(ArrayList<Rule> rl: map.values()){
			for(Rule r:rl) {
				if(r.isDirectRecursive())  {
					Boolean pr=isBad(r,gh);
					if(pr == null || pr == false)  {
						return pr;
					}

				}
			}
		}
		return true;
	}

	private static Boolean isBad(Rule r, Grammarhost gh) {
		V[] rsBeg = null;
		if(r.isRightRecursive() ){
			rsBeg=new V[r.getRightSideLength()-1];
			for(int i=0;i<r.getRightSideLength()-1;i++) {
				rsBeg[i] = r.getRightside()[i];
			}
		}

		V[] rsEnd=null;

		if(r.isLeftRecursive()) {
			rsEnd=new V[r.getRightSideLength()-1];
			for(int i=1;i<r.getRightSideLength();i++) {
				rsEnd[i-1] = r.getRightside()[i];
			}
		}

		if(r.isMidRecursive())  {
			int midIndex=r.getIndexOfRefGroup(r.getGroupname());
			rsBeg=new V[midIndex];
			for(int i=0;i<midIndex;i++) {
				rsBeg[i] = r.getRightside()[i];
			}
			rsEnd = new V[r.getRightSideLength() - midIndex -1];
			for(int i=0;i<rsEnd.length;i++) {
				rsEnd[i] = r.getRightside()[i+midIndex+1];
			}			
		}	


		Map<String, ArrayList<Rule>> map = gh.getGrammar();
		for(ArrayList<Rule> rl: map.values()){
			for(Rule a:rl) {
				if(a == r ) continue;
				if(r.isRightRecursive() ){
					Boolean result=canEndWith(a.getRightside(), rsBeg );
					if(result ==  null || result)  {
						return result;
					}
				}
				if(r.isLeftRecursive() ){
					Boolean result=canBeginWith(a.getRightside(), rsEnd );
					if(result ==  null || result)  {
						return result;
					}
				}
				if(r.isMidRecursive())  {
					Boolean result=canEndWith(a.getRightside(), rsBeg );
					if(result ==  null || result)  {
						return result;
					}

					result=canBeginWith(a.getRightside(), rsEnd );
					if(result ==  null || result)  {
						return result;
					}
				}
			}
		}	
		return false;
	}

	private static Boolean canEndWith(V[] full, V[] end) {
		// TODO Auto-generated method stub
		return null;
	}

	private static Boolean canBeginWith(V[] full, V[] start) {
		//TODO
		return null;
	}








}
