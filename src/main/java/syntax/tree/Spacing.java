package syntax.tree;

import hu.kg.list.StatefulList;
import syntax.Rule;

public class Spacing {
	
	
	
	private StatefulList<RulePointingToSpacing> nexts = new StatefulList<>();
	private StatefulList<RulePointingToSpacing> prevs = new StatefulList<>();
	
	private int freezeId = -1;
	
	

	private WaveElement owner;
	private int bite =-1;
	
	
	int sourceIndex = -1;

	public StatefulList<RulePointingToSpacing> getNexts() {
		return nexts;
	}

	public StatefulList<RulePointingToSpacing> getPrevs() {
		return prevs;
	}

	
	@SuppressWarnings("unchecked")
	public void sort(){
		//TODO
		//Collections.sort(nexts);
		//Collections.sort(prevs);
		
	}
	
	

	public boolean prevContains(Rule rule, int i) {
		for(RulePointingToSpacing current:prevs){
			if(current.getRule() == rule && current.getSpacingIndex() == i) return true;
		}
		return false;
	}

	public boolean nextContains(Rule rule, int i) {
		for(RulePointingToSpacing current:nexts){
			if(current.getRule() == rule && current.getSpacingIndex() == i) return true;
		}
		return false;
	
	}

	public boolean isFrozen() {
		return freezeId>0;
	}

	public void freeze(int freezeId) {
		this.freezeId=freezeId;
	}

	public int getSourceIndex() {
		return sourceIndex;
	}

	public WaveElement getOwner() {
		return owner;
	}

	public void setOwner(WaveElement owner, int bite) {
		this.owner = owner;
		if( owner == null || (owner.getRule().isRightRecursive() && this.bite <0 ) || !owner.getRule().isRightRecursive() || 
				(bite> owner.getPrevSpacingindex() && bite < owner.getNextSpacingindex())) 
		
		this.bite = bite;
	}

	public int getBite() {
		return bite;
	}

	public void setBite(int bite) {
		this.bite = bite;
	}

	
	public int getFreezeId() {
		return freezeId;
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb=new StringBuilder();
		sb.append("\nOwner: "+this.owner);
		sb.append(" sourceIndex: "+ sourceIndex);
		sb.append(" bite: "+ bite);
		sb.append("\nprevs: "+ prevs);
		sb.append("\nnexts: "+ nexts);
		
		
		return sb.toString();
		

		
		
		
	}
	
}
