package syntax.tree.builder2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import descriptor.CharSequenceDescriptor;
import syntax.Rule;
import syntax.grammar.Grammarhost;

public class STreeBuilder {

	Map<String,List<RuleInterval>> forward = new HashMap<String, List<RuleInterval>>(); 
	//Map<String,List<RuleInterval>> backward = new HashMap<String, List<RuleInterval>>();
	Map<RuleInterval, RuleInterval[]> deduction=new HashMap<RuleInterval, RuleInterval[]>();

	private Grammarhost gh;
	private String source;
	private List<Rule> rules; 


	public STreeBuilder(Grammarhost gh, String source) {
		this.gh = gh;
		this.source = source;
		this.rules=gh.getRefRules();
	}



	public Map<RuleInterval, RuleInterval[]> build() {
		
		addInitialRules();
        
         




		return deduction;
	}

	private List<RuleInterval> matches(RuleInterval part,Rule pattern) {

		List<RuleInterval> result = new LinkedList<RuleInterval>();
		String groupname=part.rule.getGroupname();
		String[] patternRs=pattern.extractRefGroups(); 
		
		
		if(patternRs[0].equals(groupname)) {
			result.addAll(matches(part, pattern, 0));
		}
		
		return result;
	}

	private List<RuleInterval> matches(RuleInterval part, Rule pattern, int groupIndexInPattern) {
		List<RuleInterval> result = new LinkedList<RuleInterval>();
		String[] patternRs=pattern.extractRefGroups();
		//String[] before=Arrays.copyOfRange(patternRs,0,groupIndexInPattern);
		String[] afterGroupNames=Arrays.copyOfRange(patternRs, groupIndexInPattern+1, patternRs.length);

        int firstSourceIndex=part.last+1;
        RuleInterval[] currentRight=new RuleInterval[patternRs.length - (groupIndexInPattern)];
		
        List<RuleInterval[]> rights=getRigths(firstSourceIndex, groupIndexInPattern, currentRight, afterGroupNames);
	    
		
		for(RuleInterval[] ria:rights) {
			RuleInterval[] n=new RuleInterval[ria.length+1];
			n[0] = part;
			int i=1;
			for(RuleInterval ri: ria) {
				n[i++] = ri;
			}
			
			RuleInterval nri = new RuleInterval(pattern, part.begin, n[n.length-1].last);
			this.deduction.put(nri, n);
			
			result.add(nri);
		}
		
		return result;
	}



	private List<RuleInterval[]> getRigths(int  firstSourceIndex, int groupIndexInPattern, RuleInterval[] currentRight, String[] afterGroupNames) {
		List<RuleInterval[]> rights=new LinkedList<RuleInterval[]>();
		
	
		if(!stepRight(currentRight, afterGroupNames, 0, firstSourceIndex)) return null;
		rights.add(currentRight);

		int i=afterGroupNames.length-1;
		
		int sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, i);
		
		for(;;) {
		   while(stepRight(currentRight, afterGroupNames, i, sourceIndex)) rights.add(currentRight);
		   i--;
		   sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, i);
		   while(!stepRight(currentRight, afterGroupNames, i, sourceIndex)) {
			   i--;
			   if(i<0) return rights;
		   }
		   rights.add(currentRight);
		   
   		   i=afterGroupNames.length-1;
   		   sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, i);
		}
		
	}



	private int setSourceIndexRightside(int firstSourceIndex, RuleInterval[] currentRight, int i) {
		int sourceIndex= firstSourceIndex;
		if(i>0) {
			sourceIndex= currentRight[i-1].last+1; 
		}
		return sourceIndex;
	}



	


	private boolean stepRight(RuleInterval[] currentRight, String[] groupNames, int fromGroupNameIndex, int sourceIndex) {
		if(fromGroupNameIndex >= groupNames.length) return true;
		RuleInterval searchAfterThis = currentRight[fromGroupNameIndex];
		String groupName = groupNames[fromGroupNameIndex];
		List<RuleInterval> l = forward.get(""+sourceIndex);
		if(l == null) return false;
		boolean found = false;
		if(searchAfterThis == null) found=true;
		for(RuleInterval ri:l) {
			if(found) {
				if(ri.rule.getGroupname().equals(groupName)) {
					currentRight[fromGroupNameIndex] = ri;					
					for(int i=fromGroupNameIndex+1;i<currentRight.length;i++) {
						currentRight[i] = null;
					}
					return stepRight(currentRight, groupNames, fromGroupNameIndex+1, ri.last+1);
				}
			}
		    if(ri == searchAfterThis) found=true;
		}
		for(int i=fromGroupNameIndex;i<currentRight.length;i++) {
			currentRight[i] = null;
		}
		return false;
	}



	private boolean onlyNulls(RuleInterval[] currentRight) {
		for(RuleInterval ri:currentRight) {
			if(ri != null) return false;
		}
		return true;
	}

	private boolean onlyNonNulls(RuleInterval[] currentRight) {
		for(RuleInterval ri:currentRight) {
			if(ri == null) return false;
		}
		return true;
	}

    

	private void addInitialRules() {
		List<Rule> csdRuleList = this.gh.getCsdRules();
		for(int i = 0;i < source.length(); i++){
			for(Rule r:csdRuleList){
				CharSequenceDescriptor csd= r.getFirstV().getCsd();
				if(csd.matchesInFrom(source, i)){			
					addToMaps(new RuleInterval(r, i,i+csd.getDescribedLength()-1));
				}
			}
		}
	}


	private void addToMaps(RuleInterval ruleInterval) {
		addToMap(forward, ""+ruleInterval.begin, ruleInterval);
		//addToMap(backward,""+ruleInterval.last, ruleInterval);
	}


	private void addToMap(Map<String, List<RuleInterval>> map, String key, RuleInterval ri) {
		if(map.get(key) == null) {
			map.put(key,new LinkedList<RuleInterval>());
		}
		List<RuleInterval> l = map.get(key);
		for(RuleInterval rio:l) {
			if(ri.equals(rio)) {
				return;
			}
		}
		l.add(ri);
	}


}
