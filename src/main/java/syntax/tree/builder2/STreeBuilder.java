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
	Map<String,List<RuleInterval>> backward = new HashMap<String, List<RuleInterval>>();
	Map<RuleInterval, List<RuleInterval>> deduction=new HashMap<RuleInterval, List<RuleInterval>>();

	private Grammarhost gh;
	private String source;
	private List<Rule> rules; 


	public STreeBuilder(Grammarhost gh, String source) {
		this.gh = gh;
		this.source = source;
		this.rules=gh.getRefRules();
	}



	public boolean build() {
		addInitialRules();






		return false;
	}

	private List<RuleInterval> matches(RuleInterval part,Rule pattern) {

		List<RuleInterval> result = new LinkedList<RuleInterval>();
		String groupname=part.rule.getGroupname();
		String[] patternRs=pattern.extractRefGroups(); 
		for(int i=0;i<patternRs.length;i++) {
			if(patternRs[i].equals(groupname)) {
				result.addAll(matches(part, pattern, i));
			}
		}
		return result;
	}

	private List<RuleInterval> matches(RuleInterval part, Rule pattern, int groupIndexInPattern) {
		List<RuleInterval> result = new LinkedList<RuleInterval>();
		String[] patternRs=pattern.extractRefGroups();
		String[] before=Arrays.copyOfRange(patternRs,0,groupIndexInPattern);
		String[] after=Arrays.copyOfRange(patternRs, groupIndexInPattern, patternRs.length);


		RuleInterval[]  originalMatches=setOriginalMatches(patternRs, groupIndexInPattern, part) ;


		List<RuleInterval[]> rights=new ArrayList<RuleInterval[]>();
       
		

		RuleInterval[] currentRight=new RuleInterval[patternRs.length - (groupIndexInPattern)];
		
		if(!preFillCurrentRight(currentRight,after,part.last+1));
		
		for(;;) {
			findNextValid(currentRight,after,part.last+1);
			//azért ez a sorrend, mert lehet üres is!
			if(onlyNonNulls(currentRight)) rights.add(currentRight);
			if(onlyNulls(currentRight)) break;
		}	
		


		return result;
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
					currentRight[z] = ri;
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

    
	
	

	private void findNextValid(RuleInterval[] currentRight, String[] after, int sourceIndex) {
		if(fillFirstNull(currentRight, after, sourceIndex)) {return;}
		//************************nem találtunk null-t, léptessük az utolsót************************************
		for(int z=currentRight.length-1; z>=0;z--) {
			RuleInterval searchAfterThis = currentRight[z];
			List<RuleInterval> l = forward.get(""+z);
			boolean found = false;
			for(RuleInterval ri:l) {
				if(found) {
					if(ri.rule.getGroupname().equals(searchAfterThis.rule.getGroupname())) {
						currentRight[z] = ri;
						return;
					}
				}
			    if(ri == searchAfterThis) found=true;
			}
			currentRight[z] = null;
		}
	}



	private boolean fillFirstNull(RuleInterval[] currentRight, String[] after, int sourceIndex) {
		if(after.length == 0) return;
		for(int firstNull=0;firstNull<currentRight.length;firstNull++) {
			if(currentRight[firstNull] == null) {
				if(firstNull != 0) {
					sourceIndex = currentRight[firstNull-1].last+1;
					List<RuleInterval> l = forward.get(""+sourceIndex);
					String search=after[firstNull];
					for(RuleInterval rl:l) {
						if(rl.rule.getGroupname().equals(search)) {
							currentRight[firstNull] = rl;
							return true;
						}
					}
				}
			}
		}
		return false;
	}



	private RuleInterval[] setOriginalMatches(String[] groupNames, int groupIndexInPattern, RuleInterval part) {

		RuleInterval[] currentMatches=new RuleInterval[groupNames.length];
		currentMatches[groupIndexInPattern]=part;






		return currentMatches;
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
		addToMap(backward,""+ruleInterval.last, ruleInterval);
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
