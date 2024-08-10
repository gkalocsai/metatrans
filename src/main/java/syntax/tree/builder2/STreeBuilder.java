package syntax.tree.builder2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import descriptor.CharSequenceDescriptor;
import syntax.Rule;
import syntax.grammar.Grammarhost;

public class STreeBuilder {

	Map<String,List<RuleInterval>> forward = new HashMap<String, List<RuleInterval>>();
	//Map<String,List<RuleInterval>> backward = new HashMap<String, List<RuleInterval>>();
	Map<RuleInterval, RuleInterval[]> deduction=new HashMap<RuleInterval, RuleInterval[]>();

	private Set<String> ruleIntervalEquality=new HashSet<String>();

	private Grammarhost gh;
	private String source;
	private List<Rule> rules;
	private String rootName;



	public STreeBuilder(Grammarhost gh, String source) {
		this.gh = gh;
		this.source = source;
		this.rules=gh.getRefRules();
		this.rootName=gh.getRootGroup();
	}



	public Map<RuleInterval, RuleInterval[]> build() {
		long startTime=System.currentTimeMillis();
		addInitialRules();
		List<RuleInterval> nRIs= new LinkedList<RuleInterval>();
		//List<RuleInterval> nRIs2= new LinkedList<RuleInterval>();
		boolean wasChange = true;
		while(wasChange) {
		wasChange = false;
here:			for(String k:forward.keySet()) {

				List<RuleInterval> rl=forward.get(k);
				for(RuleInterval ri:rl) {
					for(Rule r: rules) {
						List<RuleInterval> current = matches(ri, r);

						if(r.isLeftRecursive() && !current.isEmpty()) {
							for(RuleInterval elem:current) {
								extendToRight(elem);
							}
							nRIs.addAll(current);
							break here;
						}
						nRIs.addAll(current);
					}
				}
			}
			wasChange=handleNewIntervals(nRIs);
			nRIs.clear();
		}
		System.out.println("Time elapsed: " + (System.currentTimeMillis()-startTime)+" ms");
		return deduction;
	}

	private void extendToRight(RuleInterval elem) {
		List<RuleInterval> li = matches(elem, elem.getRule());

	    RuleInterval x= null;
		while(!li.isEmpty()) {

			//TODO nem biztos, hogy az első szabály li.get(0) ????
			x= new RuleInterval(elem.getRule(), elem.getBegin() ,li.get(0).getLast());
			li = matches(x, elem.getRule());
		}
       if(x!=null) {
		addToMaps(x);
	}


	}



	private boolean handleNewIntervals(List<RuleInterval> nRIs) {
		boolean wasChange=false;
		// a ruleIntervalEquality -re azért van szükség, hogy ne adjuk hozzá ugyanazt a ruleIntervalt,
		// amit esetleg töröltünk az optimalizálás során
		for(RuleInterval ri:nRIs) {
			String matchString=ri.matchingString();
			if(this.ruleIntervalEquality.contains(matchString)) {
				continue;
			}
			else {
				wasChange = true;
				ruleIntervalEquality.add(matchString);
				addToMaps(ri);
			}
		}

		// Kidobunk olyan szabályokat a forward és backward mapekből,
		// amelyek már valószínűleg nem hasznosak a szintaxisfa felépítésében
		//
		for(RuleInterval ri:nRIs) {
			if(ri.getRule().isLeftRecursive()) {
				for(int i= ri.getBegin()+1;i<ri.getLast();i++) {
					forward.remove(""+i);
				}
			}
			RuleInterval[] children=deduction.get(ri);
			if(children.length <2) {
				continue;
			}
			for(RuleInterval rr:children) {
				if(children.length <2) {
					continue;
				}
				if(rr.getBegin() == 0 && rr.getLast() == source.length()-1) {
					continue;
				}
				if(!rr.getRule().isFreezer()) {
					removeFromMaps(rr);
				}
			}
		}

		return wasChange;
	}

	private void removeFromMaps(RuleInterval rr) {
		List<RuleInterval> x = forward.get(""+rr.getBegin());
		if(x!=null) {
			x.remove(rr);
			if(x.isEmpty()) {
				forward.remove(""+rr.getBegin());
			}
		}


		//x=backward.get(""+rr.last)
		//x.remove(rr);
	}


	private List<RuleInterval> matches(RuleInterval part,Rule pattern) {

		List<RuleInterval> result = new LinkedList<RuleInterval>();
		String groupname=part.getRule().getGroupname();
		String[] patternRs=pattern.extractRefGroups();

		//Az első részre mindenképp kell illeszkednie RuleInterval-nak,
		//ez alapján döntünk az illesztésről

		if(patternRs[0].equals(groupname)) {
			result.addAll(matches(part, pattern, 0));
		}
		return result;
	}

	private List<RuleInterval> matches(RuleInterval part, Rule pattern, int groupIndexInPattern) {
		List<RuleInterval> result = new LinkedList<RuleInterval>();
		String[] patternRs=pattern.extractRefGroups();

		//lehet, hogy nem lesz before
		//String[] before=Arrays.copyOfRange(patternRs,0,groupIndexInPattern);
		String[] afterGroupNames=Arrays.copyOfRange(patternRs, groupIndexInPattern+1, patternRs.length);

		int firstSourceIndex=part.getLast()+1;
		RuleInterval[] currentRight=new RuleInterval[afterGroupNames.length];

		List<RuleInterval[]> rights=getRigths(firstSourceIndex, groupIndexInPattern, currentRight, afterGroupNames);

		if(rights == null) {
			return result;
		}
		for(RuleInterval[] ria:rights) {
			RuleInterval[] n=new RuleInterval[ria.length+1];
			n[0] = part;
			int i=1;
			for(RuleInterval ri: ria) {
				n[i++] = ri;
			}

			RuleInterval nri = new RuleInterval(pattern, part.getBegin(), n[n.length-1].getLast());

			//megtartjuk a levezetésben mert amúgy kidobjuk az újak alatt szereplö rekurzív szabályokat
			this.deduction.put(nri, n);

			result.add(nri);
		}

		return result;
	}



	private List<RuleInterval[]> getRigths(int  firstSourceIndex, int groupIndexInPattern, RuleInterval[] currentRight, String[] afterGroupNames) {
		List<RuleInterval[]> rights=new LinkedList<RuleInterval[]>();


		if(!stepRight(currentRight, afterGroupNames, 0, firstSourceIndex)) {
			return null;
		}
		rights.add(Arrays.copyOf(currentRight, currentRight.length));

		int i=afterGroupNames.length-1;

		if(i == -1) {
			return rights;
		}
		int sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, i);

		for(;;) {
			while(stepRight(currentRight, afterGroupNames, i, sourceIndex)) {
				rights.add(Arrays.copyOf(currentRight, currentRight.length));
			}

			i--;
			if(i<0) {
				return rights;
			}
			sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, i);
			while(!stepRight(currentRight, afterGroupNames, i, sourceIndex)) {
				i--;
				if(i<0) {
					return rights;
				}
			}
			rights.add(Arrays.copyOf(currentRight, currentRight.length));
			i=afterGroupNames.length-1;
			sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, i);
		}

	}



	private int setSourceIndexRightside(int firstSourceIndex, RuleInterval[] currentRight, int i) {
		int sourceIndex= firstSourceIndex;
		if(i>0) {
			sourceIndex= currentRight[i-1].getLast()+1;
		}
		return sourceIndex;
	}






	private boolean stepRight(RuleInterval[] currentRight, String[] groupNames, int fromGroupNameIndex, int sourceIndex) {

		if(fromGroupNameIndex >= groupNames.length) {
			return true;
		}
		RuleInterval searchAfterThis = currentRight[fromGroupNameIndex];
		String groupName = groupNames[fromGroupNameIndex];
		List<RuleInterval> l = forward.get(""+sourceIndex);
		if(l == null) {
			return false;
		}
		boolean found = false;
		if(searchAfterThis == null) {
			found=true;
		}
		for(RuleInterval ri:l) {

			if(found) {
				if(ri.getRule().getGroupname().equals(groupName)) {
					currentRight[fromGroupNameIndex] = ri;
					for(int i=fromGroupNameIndex+1;i<currentRight.length;i++) {
						currentRight[i] = null;
					}
					return stepRight(currentRight, groupNames, fromGroupNameIndex+1, ri.getLast()+1);
				}
			}
			if(ri == searchAfterThis) {
				found=true;
			}
		}
		for(int i=fromGroupNameIndex;i<currentRight.length;i++) {
			currentRight[i] = null;
		}
		return false;
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


	private boolean addToMaps(RuleInterval ruleInterval) {
		return addToMap(forward, ""+ruleInterval.getBegin(), ruleInterval);
		//addToMap(backward,""+ruleInterval.last, ruleInterval);
	}


	private boolean addToMap(Map<String, List<RuleInterval>> map, String key, RuleInterval ri) {
		if(map.get(key) == null) {
			map.put(key,new LinkedList<RuleInterval>());
		}
		List<RuleInterval> l = map.get(key);
		for(RuleInterval rio:l) {
			if(ri.equals(rio)) {
				return false;
			}
		}
		l.add(ri);
		return true;
	}



	public RuleInterval getRoot() {
		List<RuleInterval> candidates = forward.get("0");
		if(candidates == null) {
			return null;
		}
		for(RuleInterval ri:candidates) {
			if(ri.getLast() == source.length()-1  && ri.getRule().getGroupname().equals(this.rootName) ) {
				return ri;
			}
		}
		return null;

	}


}
