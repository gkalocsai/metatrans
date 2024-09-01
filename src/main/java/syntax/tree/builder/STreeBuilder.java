package syntax.tree.builder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import descriptor.CharSequenceDescriptor;
import syntax.Rule;
import syntax.grammar.Grammarhost;

public class STreeBuilder {

	Map<String,List<RuleInterval>> forward = new LinkedHashMap<String, List<RuleInterval>>();
	Map<RuleInterval, RuleInterval[]> deduction=new HashMap<RuleInterval, RuleInterval[]>();

	private Set<String> ruleIntervalEquality=new HashSet<String>();

	private Grammarhost gh;
	private String source;

	private boolean printOut;

	private String rootName;
	private List<Deduction> matched= new LinkedList<Deduction>();


	public STreeBuilder(Grammarhost gh, String source) {
		this.gh = gh;
		this.source = source;

		this.rootName=gh.getRootGroup();
	}



	public Map<RuleInterval, RuleInterval[]> build() {
		long startTime=System.currentTimeMillis();
		addInitialRules();
		processForward();
		return deduction;
	}



	private void processForward() {
		boolean wasChange = true;
		while(wasChange) {
			matched.clear();
			wasChange=false;
			for(String k:forward.keySet()) {
				List<RuleInterval> rl=forward.get(k);
				for(RuleInterval ri:rl) {
					for(Rule r: gh.getRefRules()) {
						List<Deduction> current = matchFromRuleBegin(ri, r);
						for(Deduction d: current) {
							deduction.put(d.getFrom(), d.getTo());
							if(!processLeftRecursiveMatched(d)) {
								matched.add(d);
							}
						}
					}
				}
			}
			wasChange=handleNewIntervals();

			if(isReady(gh.getRootGroup(), source.length())) {
				return;
			}
		}
	}

	private boolean processLeftRecursiveMatched(Deduction d) {
		Rule r= d.getFrom().getRule();
		if(!r.isLeftRecursive()) {
			return false;
		}
		List<Deduction> current = matchFromRuleBegin(d.getFrom(), r);
//		addToMaps(current);
		if(current.isEmpty()) {
			matched.add(d);
			return true;
		}

		while(!current.isEmpty()) {
			current = matchFromRuleBegin(d.getFrom(), r);
			//addToMaps(current);
			if(current.isEmpty()) {
				matched.add(d);
				break;
			}
			d=current.get(0);
			deduction.put(d.getFrom(), d.getTo());
		}
        return true;
	}

	private void addToMaps(List<Deduction> current) {
		for(Deduction d: current) {
            RuleInterval ri = d.getFrom();
            addToMaps(ri);
		}

	}



	private RuleInterval extendToRight(RuleInterval elem) {
		List<Deduction> li = matchFromRuleBegin(elem, elem.getRule());
		for(Deduction d:li) {
			matched.add(d);
		}

		RuleInterval x= null;
		while(!li.isEmpty()) {

			//TODO nem biztos, hogy az első szabály li.get(0) ????
			x = new RuleInterval(elem.getRule(), elem.getBegin() ,li.get(0).getFrom().getLast());
			li = matchFromRuleBegin(x, elem.getRule());
			for(Deduction d:li) {
				matched.add(d);
			}

		}
		return x;

	}




	private boolean isReady(String rootGroup, int length) {
		List<RuleInterval> list = forward.get("0");
		if(list == null) {
			return false;
		}
		for(RuleInterval ri:list ) {
			if(ri.getLast() >= length-1 && ri.getRule().getGroupname().equals(rootGroup)) {
				return true;
			}
		}
		return false;
	}


	private boolean handleNewIntervals() {
		boolean wasChange=false;
		// a ruleIntervalEquality -re azért van szükség, hogy ne adjuk hozzá ugyanazt a ruleIntervalt,
		// amit esetleg töröltünk az optimalizálás során
		for(Deduction d:matched) {
			RuleInterval ri = d.getFrom();
			String matchString=ri.matchingString();
			if(this.ruleIntervalEquality.contains(matchString)) {
				continue;
			}
			else {
				wasChange = true;
				ruleIntervalEquality.add(matchString);
				addToMaps(ri);
				if(printOut) {
					System.out.println(ri+"  ---"+source.substring(ri.getBegin(), ri.getLast()+1)+"---" );
				}
			}
		}
		return wasChange;
	}

	private List<Deduction> matchFromRuleBegin(RuleInterval part,Rule pattern) {

		List<Deduction> result = new LinkedList<Deduction>();
		String groupname=part.getRule().getGroupname();
		String[] patternRs=pattern.extractRefGroups();

		//Az első részre mindenképp kell illeszkednie RuleInterval-nak,
		//ez alapján döntünk az illesztésről

		if(patternRs[0].equals(groupname)) {
			result.addAll(matchAfterGroupIndex(part, pattern, 0));
		}
		return result;
	}

	private List<Deduction> matchAfterGroupIndex(RuleInterval part, Rule pattern, int groupIndexInPattern) {
		List<Deduction> result = new LinkedList<Deduction>();
		String[] patternRs=pattern.extractRefGroups();

		String[] afterGroupNames=rangeCopyTillEndExclusive(patternRs, groupIndexInPattern);

		int firstSourceIndex=part.getLast()+1;
		RuleInterval[] currentRight=new RuleInterval[afterGroupNames.length];

		List<RuleInterval[]> rights=getRigths(firstSourceIndex, groupIndexInPattern, currentRight, afterGroupNames);

		if(rights == null) {
			return result;
		}
		fillResultWithDeductions(part, pattern, result, rights);

		return result;
	}



	private void fillResultWithDeductions(RuleInterval part, Rule pattern, List<Deduction> result, List<RuleInterval[]> rights) {
		for(RuleInterval[] ria:rights) {
			RuleInterval[] n = createDeductionValue(part, ria);
			RuleInterval nri = new RuleInterval(pattern, part.getBegin(), n[n.length-1].getLast());
			result.add(new Deduction(nri, n));

		}
	}



	private RuleInterval[] createDeductionValue(RuleInterval part, RuleInterval[] ria) {
		RuleInterval[] n=new RuleInterval[ria.length+1];
		n[0] = part;
		int i=1;
		for(RuleInterval ri: ria) {
			n[i++] = ri;
		}
		return n;
	}



	private List<RuleInterval[]> getRigths(int  firstSourceIndex, int groupIndexInPattern, RuleInterval[] currentRight, String[] afterGroupNames) {
		List<RuleInterval[]> rights=new LinkedList<RuleInterval[]>();

		if(!stepRight(currentRight, afterGroupNames, 0, firstSourceIndex)) {
			return null;
		}
		rights.add(copy(currentRight));

		int groupNameIndex=afterGroupNames.length-1;

		if(groupNameIndex == -1) {
			return rights;
		}

		int sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, groupNameIndex);

		for(;;) {
			while(stepRight(currentRight, afterGroupNames, groupNameIndex, sourceIndex)) {
				rights.add(copy(currentRight));
			}

			groupNameIndex--;
			if(groupNameIndex<0) {
				return rights;
			}
			sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, groupNameIndex);
			while(!stepRight(currentRight, afterGroupNames, groupNameIndex, sourceIndex)) {
				groupNameIndex--;
				if(groupNameIndex<0) {
					return rights;
				}
			}
			rights.add(copy(currentRight));
			groupNameIndex=afterGroupNames.length-1;
			sourceIndex = setSourceIndexRightside(firstSourceIndex, currentRight, groupNameIndex);
		}
	}

	private RuleInterval[] copy(RuleInterval[] current) {
		return Arrays.copyOf(current, current.length);
	}



	private int setSourceIndexRightside(int firstSourceIndex, RuleInterval[] currentRight, int groupNameIndex) {
		int sourceIndex= firstSourceIndex;
		if(groupNameIndex>0) {
			sourceIndex= currentRight[groupNameIndex-1].getLast()+1;
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
					boolean stepped = stepRight(currentRight, groupNames, fromGroupNameIndex+1, ri.getLast()+1);
					if(stepped) {
						return true;
					}
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


	private void addToMaps(RuleInterval ruleInterval) {
		addToMap(forward, ""+ruleInterval.getBegin(), ruleInterval);
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

	private String[] rangeCopyTillEndExclusive(String[] patternRs, int index) {
		return Arrays.copyOfRange(patternRs, index+1, patternRs.length);
	}

	@Override
	public String toString() {
		List<char[]> x = ToStr.toCharArrayList(forward,source);
		StringBuilder sb=new StringBuilder();
		for(char[] ca: x) {
			for(char c:ca) {
				if(c==0) {
					sb.append(" ");
				}else {
					sb.append(c);
				}
			}

			sb.append("\n");
		}
		return sb.toString();
	}



	public boolean isPrintOut() {
		return printOut;
	}



	public void setPrintOut(boolean printOut) {
		this.printOut = printOut;
	}
}
