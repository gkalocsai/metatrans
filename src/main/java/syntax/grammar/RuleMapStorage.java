package syntax.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import syntax.Rule;

public class RuleMapStorage {

	private LinkedHashMap<String, ArrayList<Rule>> theMap;

	private Map<String, ArrayList<Rule>> storage=new HashMap<>();

	private Stack<String> groupNameStack= new Stack<>();

	private String rootGroup;

	public RuleMapStorage(){}

	public RuleMapStorage(Collection<Rule> rl) {

		for(Rule r:rl){
			if(rootGroup == null) rootGroup =  r.getGroupname();
			addRuleToStorage(r);
		}
		
	}


	public RuleMapStorage(List<Rule> rl, String rootGroup) {
		this(rl);
		this.rootGroup = rootGroup;

	}


	public void addRuleToStorage(Rule r) {
		String groupname = r.getGroupname();
		
		if(!groupNameStack.contains(groupname)) {
			groupNameStack.push(groupname);
		}

		ArrayList<Rule> group = storage.get(groupname);
		if(group == null){
			storage.put(groupname, new ArrayList<Rule>());

		} 
		group = storage.get(groupname);
		group.add(r);
	}


	public LinkedHashMap<String, ArrayList<Rule>> getGrammar() {
		if(theMap == null) {
			theMap = new LinkedHashMap<>();
			while(! groupNameStack.isEmpty()) {
				String nextGroup=groupNameStack.pop();
				theMap.put(nextGroup, storage.get(nextGroup));
			}
		}
		
		return theMap;
	}


   



}
