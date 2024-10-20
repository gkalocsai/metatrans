package syntax.grammar;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import descriptor.CharSequenceDescriptor;
import descriptor.GroupName;
import read.ComplexRuleCreator;

import syntax.Rule;
import syntax.SyntaxElement;

public class Grammarhost {

    private static final int MAX_LEVEL_OF_RULE_APPLICAION = 100;
	private Map<String, ArrayList<Rule>> grammar;
    private Set<String> rootGroups;

    private Map<String, List<Rule>> levelInSyntaxTreeToRuleList = new HashMap<String, List<Rule>>();
    private Map<String, Set<Rule>> killOnLevelToRuleList = new HashMap<String, Set<Rule>>();

    private Map<String, Set<String>> level2RemoveFromUnsafe= new HashMap<String, Set<String>>();
    
    private List<Rule> csdRuleList = null;
  //  private Map<String, String> groupName2Level;

    public Set<String> unsafeToDel = new HashSet<String>();
    private Set<String> subResults = new HashSet<>();

    public Grammarhost(List<Rule> rl, boolean strict) throws GrammarException {
        init(rl, null);

    }

    public Grammarhost(List<Rule> rl) throws GrammarException {
        init(rl, null);
    }

    public Grammarhost(List<Rule> rules, String rootGroup) throws GrammarException {
        init(rules, rootGroup);
    }



    public Grammarhost(List<Rule> ruleList,  Set<String> subResults) {
    	 init(ruleList, null);
        this.subResults = subResults;

    }

    private void init(List<Rule> rules, String rootGroup) throws GrammarException {
        Set<String> allIds = null;
        if (rules == null || rules.isEmpty()) {
            throw new GrammarException("ERROR: No grammar rules!");
        }
        Set<String> rgrs = new HashSet<>();
        if (rootGroup != null)
        rgrs.add(rootGroup);

        this.rootGroups = rgrs;

        this.grammar = createRuleMap(rules, rootGroup);
        pushDescriptors(rules);
        removeOneLongRecursiveRules();

        eliminateIndirectRecursion();

        validateReferencesToExists();

        validateReferencesInAllRules();

        checkForInvalidRecursions();

        allIds = getAllIdentifiers();

        IdCreator.INSTANCE.addExistingIds(allIds);

        moveRepeatersToOwnGroups();
        
        createUnsafeGroupNames();
        
        //FIXME: Error messages should be based on the deduction
        //WON'T FIX - too much effort, we just don't delete subResults from the forward map
      //  unsafeToDel.addAll(subResults);
   
        fillApplicationOrderToRuleList();
        fillKillLevel();
        fillUnsafeRemoval();
    }

    private void fillUnsafeRemoval() {
    	
    	List<String> referencedGroupNames=new LinkedList<String>();
    	referencedGroupNames.addAll(grammar.keySet());
    	for(String groupName: referencedGroupNames) {
    	    List<Rule> haveOnRightSide=new LinkedList<Rule>();
    	    List<Rule> refRules=getRefRules();
    	    for(Rule r: refRules) {
    	        if(r.getRightSideAsStrList().contains(groupName)) {
    	        	haveOnRightSide.add(r);
    	        }
    	    }
    	    int x=getUnsafeRemovalLevel(groupName,haveOnRightSide);
    	   
    	    
    	    Set<String> cset = level2RemoveFromUnsafe.get(""+x);
    	    if(cset ==null) cset = new HashSet<String>();
    	    cset.add(groupName);
    	    level2RemoveFromUnsafe.put(""+x, cset);
    	}
	}

	private int getUnsafeRemovalLevel(String groupName, List<Rule> haveOnRightSide) {
	
		if(subResults.contains(groupName))  return Integer.MAX_VALUE;
		for(Rule r:haveOnRightSide) {
			if(r.isLeftRecursive() && r.isRightRecursive()) return Integer.MAX_VALUE;
		}
		if(haveOnRightSide.size() == 1)  
			return  getMaxLevel(haveOnRightSide);
		Rule maxLevelRule = getRuleOnMaxLevel(haveOnRightSide);
		
		haveOnRightSide.remove(maxLevelRule);
		return getMaxLevel(haveOnRightSide);
	}

	private Rule getRuleOnMaxLevel(List<Rule> haveOnRightSide) {
	    int maxLevel=getMaxLevel(haveOnRightSide);
	    for(Rule r:haveOnRightSide) {
	    	int l = getLevel(r);
	    	if(l==maxLevel) {
	    		return r;
	    	}	
	    }
		return null;
	}

	private int getMaxLevel(List<Rule> rules) {
	    int maxLevel=0;
	    for(Rule r:rules) {
	    	int l = getLevel(r);
	    	if(l>maxLevel) {
	    		maxLevel = l;
	    	}	
	    }
		return maxLevel;
	}
	private void moveRepeatersToOwnGroups() {
        Map<String, HashSet<String>> groupToRefs = new HashMap<String, HashSet<String>>();
        Map<String, Rule> repeaterGroups = new HashMap<>();
        for (String key : grammar.keySet()) {
            HashSet<String> currentGroupSet = new HashSet<String>();
            groupToRefs.put(key, currentGroupSet);
            ArrayList<Rule> currentGroup = grammar.get(key);
            ArrayList<Rule> modifiedGroup = new ArrayList<>();
            for (Rule r : currentGroup) {
                if (r.isRepeater()) {
                    String repGroupName = IdCreator.INSTANCE.generateYetUnusedId("rep_");
                    List<Rule> l = ComplexRuleCreator.createRules(repGroupName,
                            repGroupName + " >> *" + repGroupName + ";");
                    modifiedGroup.add(l.get(0));
                    repeaterGroups.put(repGroupName, r);
                } else {
                    modifiedGroup.add(r);
                }
            }
            currentGroup.clear();
            currentGroup.addAll(modifiedGroup);
        }
        for (String groupName : repeaterGroups.keySet()) {
            ArrayList<Rule> oneRepeater = new ArrayList<>();
            oneRepeater.add(repeaterGroups.get(groupName));
            grammar.put(groupName, oneRepeater);
        }
    }

    private void createUnsafeGroupNames() {
        //!!!FIXME
    //   unsafeToDel.addAll(grammar.keySet());
        
        Set<String> referencedGroups = new HashSet<String>();
        
    	for (String key : grammar.keySet()) {
            ArrayList<Rule> currentGroup = grammar.get(key);
            for (Rule r : currentGroup) {
            	//if(r.getRightside().length<2) continue;
                for(SyntaxElement se:r.getRightside()) {
            		if(se.getReferencedGroup()==null) continue;
                	if(referencedGroups.contains(se.getReferencedGroup())) {
                		unsafeToDel.add(r.getGroupname());
                	}
                	else referencedGroups.add(se.getReferencedGroup());
                }
            }
        }    
    }

      

	private void validateReferencesToExists() {
        Set<String> exists = new HashSet<>();
        exists.addAll(grammar.keySet());
        for (String key : grammar.keySet()) {
            ArrayList<Rule> currentGroup = grammar.get(key);
            for (Rule r : currentGroup) {
                String[] grs = r.getGroupRefsAsArray();
                for (String groupname : grs) {
                    if (groupname == null) continue;
                    if (!exists.contains(groupname))
                        throw new RuntimeException("Non existing group: " + groupname + " in rule: " + r);
                }
            }
        }

    }

    private void fillKillLevel() {
    	
        Map<String, String> groupName2Level = createGroupName2Level();

        for (String key : grammar.keySet()) {
            ArrayList<Rule> currentGroup = grammar.get(key);
            Set<Rule> rightSides = getRightSideContains(key);
            int max = getMaxLevelOf(rightSides, groupName2Level) + 1;

            if (this.killOnLevelToRuleList.get("" + max) == null) {
                this.killOnLevelToRuleList.put("" + max, new HashSet<Rule>());
            }
            Set<Rule> rl2 = this.killOnLevelToRuleList.get("" + max);
            rl2.addAll(currentGroup);
        }
    }

    private int getMaxLevelOf(Set<Rule> rightSides, Map<String, String> groupName2Level) {
        int max = 0;
        for (Rule r : rightSides) {
            String level = groupName2Level.get(r.getGroupname());
            if (level == null) continue;
            int current = Integer.valueOf(level);
            if (current > max) max = current;
        }
        return max;
    }

    private Set<Rule> getRightSideContains(String groupName) {

        Set<Rule> result = new HashSet<Rule>();

        for (String key : grammar.keySet()) {
            ArrayList<Rule> currentGroup = grammar.get(key);
            for (Rule r : currentGroup) {
                String ar[] = r.getGroupRefsAsArray();
                for (String rs : ar) {
                    if (groupName.equals(rs)) {
                        result.add(r);
                        break;
                    }
                }

            }
        }
        return result;
    }

    private void fillApplicationOrderToRuleList() {

        Set<String> foundGroups=new HashSet<String>();
        List<Rule> currentRules=new LinkedList<Rule>();
        for (Rule r : getCsdRules()) {
            foundGroups.add(r.getGroupname());
            currentRules.add(r);
        }
        levelInSyntaxTreeToRuleList.put("0",currentRules);

		List<Rule> refs=getRefRules();
        for(int i=1;i<=MAX_LEVEL_OF_RULE_APPLICAION;i++) {
        	List<Rule> nextRules= new LinkedList<Rule>();
        	for(Rule r:refs) {
        		  if(canBeNext(r, currentRules, foundGroups)){
        			  nextRules.add(r);
        		  }
        	}
        	if(nextRules.isEmpty()) break;
        	levelInSyntaxTreeToRuleList.put(""+i,nextRules);
        	for(Rule n:nextRules) {
        		foundGroups.add(n.getGroupname());
        	}
        	currentRules = nextRules;
        }
    }

    private boolean canBeNext(Rule r, List<Rule> currentRules, Set<String> foundGroups) {
    	if(r.isDirectRecursive() && currentRules.contains(r)) return false;
    	Set<String> currentGroups=new HashSet<String>();
		for(Rule cr:currentRules) {
			currentGroups.add(cr.getGroupname());
		}
		if(foundGroups.containsAll(r.getRightSideAsStrList())) {
			for(String s:r.getRightSideAsStrList()) {
				if(currentGroups.contains(s))  return true;
			}
		}
		return false;
	}

	private Map<String, String> createGroupName2Level() {
        Map<String, String> groupName2Order = new HashMap<String, String>();
        for (Rule r : getCsdRules()) {
            groupName2Order.put(r.getGroupname(), "0");
        }
        boolean wasChange = true;
        while (wasChange) {
            wasChange = false;
            for (String key : grammar.keySet()) {
                if (groupName2Order.keySet().contains(key)) continue;
                ArrayList<Rule> currentGroup = grammar.get(key);
                Set<String> rightSides = getRightSides(currentGroup);
                rightSides.remove(key);
                if (groupName2Order.keySet().containsAll(rightSides)) {
                    int max = getMaxLevel(rightSides, groupName2Order);
                    groupName2Order.put(key, "" + (max + 1));
                    wasChange = true;
                }
            }
        }
        return groupName2Order;
    }

    private int getMaxLevel(Set<String> rightSides, Map<String, String> groupName2Order) {
        int max = 0;
        for (String s : rightSides) {
            int t = Integer.valueOf(groupName2Order.get(s));
            if (t > max) max = t;
        }
        return max;
    }

    private Set<String> getRightSides(ArrayList<Rule> currentGroup) {
        Set<String> result = new HashSet<String>();
        for (Rule r : currentGroup) {
            String[] arr = r.getGroupRefsAsArray();
            Collections.addAll(result, arr);
        }
        return result;
    }

    private void validateReferencesInAllRules() throws GrammarException {

        Set<String> finisherGroupnames = findFinisherGroupnames();
        if (finisherGroupnames.size() != grammar.keySet().size()) {
            throw new GrammarException("Infinite recursion");
        }
    }

    private Set<String> findFinisherGroupnames() {
        Set<String> finisherGroupnames = new HashSet<>();
        for (Rule csd : getCsdRules()) {
            finisherGroupnames.add(csd.getGroupname());
        }
        int oldSize = 0;

        while (oldSize != finisherGroupnames.size()) {
            oldSize = finisherGroupnames.size();
            for (Rule r : getRefRules()) {
                if (finisherGroupnames.containsAll(r.getRightSideAsStrList())) {
                    finisherGroupnames.add(r.getGroupname());
                }
            }
        }
        return finisherGroupnames;
    }

    private void removeOneLongRecursiveRules() throws GrammarException {
        for (Rule r : getRefRules()) {
            if (r.getRightSideLength() == 1 && r.isDirectRecursive()) {
                List<Rule> group = grammar.get(r.getGroupname());
                if (group.size() == 1) {
                    grammar.remove(r.getGroupname());
                } else {
                    group.remove(r);
                    // throw new GrammarException("Infinite recursion in rule: "+r);
                }
            }
        }
    }

    private void checkForInvalidRecursions() throws GrammarException {
        for (Rule r : getRefRules()) {
            if (r.isMidRecursive() && (r.isLeftRecursive() || r.isRightRecursive())) {
                throw new GrammarException("Middle recursion must be single recursion. Rule: " + r.toString());
            } else if (r.hasMultipleMidRecursions()) {
                throw new GrammarException("Multiple middle recursion found. Rule: " + r.toString());
            }
        }
    }

    void eliminateIndirectRecursion() throws GrammarException {
        new IndirectRecursionEliminator().eliminate(grammar, null, true);

    }

    private static Map<String, ArrayList<Rule>> createRuleMap(List<Rule> rl, String rootGroup) {

        return new RuleMapStorage(rl, rootGroup).getGrammar();

    }

    private void pushDescriptors(List<Rule> rl) {
        for (Rule r : rl) {
            extractAndAddDescriptorRules(r);
        }
    }

    private void extractAndAddDescriptorRules(Rule r) {
       SyntaxElement[] vs = r.getRightside();

        for (int i = 0; i < vs.length; i++) {
            SyntaxElement v = vs[i];
            if (v.isDescriptor()) {
                CharSequenceDescriptor csd = v.getCsd();
                String groupName = IdCreator.INSTANCE.generateYetUnusedId("_");
                
                ArrayList<Rule> rlist = new ArrayList<>();
                rlist.add(createCsdRule(groupName, csd));
                grammar.put(groupName, rlist);

                vs[i] = new GroupName(groupName);
            }
        }
    }



    private Rule createCsdRule(String groupName, CharSequenceDescriptor csd) {
        return new Rule(groupName, createVArray(csd), null, null, false);
    }

    private SyntaxElement[] createVArray(CharSequenceDescriptor csd) {
        SyntaxElement[] va = new SyntaxElement[1];
        va[0] = csd;
        return va;
    }

    public List<Rule> getRefRules() {
        List<Rule> rList = createListFromAllRules();
        List<Rule> refRuleList = new ArrayList<>();
        for (Rule r : rList) {
            if (!r.getFirstV().isDescriptor() && !"0".equals(r.getGroupname())) {

                refRuleList.add(r);
            }
        }
        return refRuleList;
    }

    public List<Rule> getCsdRules() {
        if (csdRuleList != null) return csdRuleList;
        List<Rule> rList = createListFromAllRules();

        csdRuleList = new LinkedList<>();

        for (Rule r : rList) {
            if (r.getFirstV().isDescriptor()) {
                csdRuleList.add(r);
            }
        }
        return csdRuleList;
    }

    public Set<String> getAllIdentifiers() {
        Set<String> allIdentifiers = new HashSet<>();
        for (ArrayList<Rule> group : grammar.values()) {
            for (Rule r : group) {
                allIdentifiers.addAll(r.getUsedIdentifiers());
            }
        }
        return allIdentifiers;
    }

    public String getGrammarString() {
        return grammar.toString();
    }

    public Map<String, ArrayList<Rule>> getGrammar() {
        return grammar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ArrayList<Rule> x : grammar.values()) {
            for (Rule r : x) {
                sb.append(r.toStringWoCompilation());
                sb.append("\n");
            }
        }
        sb.append("Levels: " + this.levelInSyntaxTreeToRuleList);
        sb.append("\nKill levels: " + killOnLevelToRuleList);

        return sb.toString();
    }

    public List<Rule> createListFromAllRules() {
        List<Rule> all = new ArrayList<>();
        for (ArrayList<Rule> rl : grammar.values()) {
            all.addAll(rl);
        }
        return all;
    }


    public Map<String, List<Rule>> getApplicationOrderToRuleList() {
        return levelInSyntaxTreeToRuleList;
    }

    public Map<String, Set<Rule>> getKillOnLevelToRuleList() {
        return killOnLevelToRuleList;
    }


    public Set<String> getRootGroups() {
        return rootGroups;
    }

    public void setRootGroups(Set<String> rootGroups) {
        this.rootGroups = rootGroups;
    }

    public Set<String> getSubResults() {
        return subResults;
    }

    public void setRoot(String string) {
        rootGroups = new HashSet<>();
        rootGroups.add(string);
    }

    public boolean isInSubResults(Rule r) {
        return isInSubResults(r.getGroupname());
    }
    private boolean isInSubResults(String groupname) {
        return subResults.contains(groupname);
    }

	public Set<String> getUnsafeToDel() {
		
		return this.unsafeToDel;
	}

	public int getLevel(Rule current) {
		int level = -1;
		for(int i=0;i<=MAX_LEVEL_OF_RULE_APPLICAION;i++) {
			List<Rule> cl=this.levelInSyntaxTreeToRuleList.get(""+i);
			if(cl == null || cl.isEmpty()) break;
			if(cl.contains(current)) {
				level = i;
			}
			
		}
		return level;
	}

	public Map<String, Set<String>> getLevel2RemoveFromUnsafe() {
		return level2RemoveFromUnsafe;
	}
	
}
