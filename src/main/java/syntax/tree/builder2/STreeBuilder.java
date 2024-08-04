package syntax.tree.builder2;

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
	private Grammarhost gh;
	private String source; 
	
	
	public STreeBuilder(Grammarhost gh, String source) {
		this.gh = gh;
		this.source = source;	
	}
	
	
	public void build() {
		addInitialForwardRules();
		
		
	}
	private void addInitialForwardRules() {
		List<Rule> csdRuleList = this.gh.getCsdRules();
		for(int i = 0;i < source.length(); i++){
			for(Rule r:csdRuleList){
				CharSequenceDescriptor csd= r.getFirstV().getCsd();
				if(csd.matchesInFrom(source, i)){
					
					addToMaps(i, r,csd.getDescribedLength());
				}
			}
		}
	}


	private void addToMaps(int begin, Rule rule, int describedLength) {
		int last= begin+describedLength-1;
		RuleInterval ri= new RuleInterval(rule, begin, last);
		addToMap(forward, ""+begin, ri);
		addToMap(backward,""+last, ri);
		
		
		
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
