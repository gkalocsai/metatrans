package syntax.display;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hu.kg.list.StatefulList;
import hu.kg.util.IntPoint2D;
import syntax.tree.RulePointingToSpacing;
import syntax.tree.Spacing;
import syntax.tree.WaveElement;

public class EdgeBuilder {
	
	private Map<Integer,IntPoint2D> heights = new HashMap<>();
	
	private Spacing[] Spacings;
	
	private Set<Integer>[] nextOccupiedHeights;
	private Set<Integer>[] prevOccupiedHeights;
	
	
	
	private int spaceBetween;
	
	private int picHeight;

	private List<Edge> edges;
	
	public EdgeBuilder(Spacing[] Spacings, int spaceBetween) {
		this.Spacings = Spacings;
		this.spaceBetween = spaceBetween;
		for(int i=0; i< Spacings.length;i++){
			heights.put(i, new IntPoint2D(0, 0));
		}		
		nextOccupiedHeights = new HashSet[Spacings.length];
		prevOccupiedHeights = new HashSet[Spacings.length];
		
		for(int i=0; i< Spacings.length;i++){
			nextOccupiedHeights[i]  = new HashSet<>();
			prevOccupiedHeights[i]  = new HashSet<>();
		}		
		
		this.edges=createEdges();
	}
	
	private  List<Edge> createEdges(){
		List<Edge> edgeList=new LinkedList<>();
		
		int waveElementCounter=0;
		for(Spacing p:Spacings) {
			waveElementCounter+=p.getNexts().size();
		}
		WaveElement[] all= new WaveElement[waveElementCounter];
		int weIndex=0;
		for(int i = 0; i<this.Spacings.length;i++){
			for(RulePointingToSpacing rp:Spacings[i].getNexts()){
				all[weIndex++] = new WaveElement(i, rp.getRule(), rp.getSpacingIndex()); 
			}
		}	
		Arrays.sort(all);
		
		for(WaveElement we:all){
			int prev=we.getPrevSpacingindex();
			int nHeight=getNextHeight(prev, we.getNextSpacingindex());
			int edgeX=(prev*(spaceBetween+1))+1;
			int edgeY=(nHeight*2)-1;
			if(edgeY > picHeight) picHeight = edgeY;
			int indexDist=we.getNextSpacingindex()-prev;				
			Edge e=new Edge(edgeX,edgeY,we.getRule().toStringWoCompilation(),(indexDist*(spaceBetween+1))-1);
			edgeList.add(e);
		}

		return edgeList;
	}

	private int getNextHeight(int j, int k) {				
		int nHeight=1;
		
		while(!freeHeight(nHeight, j ,k )){
			nHeight++;
		}
		
		occHeight(nHeight, j, k);
		
		
		
		return nHeight;
	}

	private void occHeight(int nHeight, int j, int k) {
		nextOccupiedHeights[j].add(nHeight);
		prevOccupiedHeights[k].add(nHeight);
		
		for(int i=j+1;i<k;i++){
			prevOccupiedHeights[i].add(nHeight);
			nextOccupiedHeights[i].add(nHeight);
		}
		
	}

	private boolean freeHeight(int nHeight, int j, int k) {
		
		if(nextOccupiedHeights[j].contains(nHeight)) return false;
		if(prevOccupiedHeights[k].contains(nHeight)) return false;
		
		for(int i=j+1;i<k;i++){
			if(prevOccupiedHeights[i].contains(nHeight)) return false;
			if(nextOccupiedHeights[i].contains(nHeight)) return false;
		}
		
		return true;
	}

	public int getPicHeight() {
		return picHeight;
	}

	public List<Edge> getEdges() {
		return edges;
	}
}
