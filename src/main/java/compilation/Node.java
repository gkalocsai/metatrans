package compilation;

import java.util.ArrayList;

class Node {
	
	String s;
	ArrayList<Node> childNodes;
	
	Node(String s){
		
		this.s=s;
		
	}
	
	Node(){
		this.childNodes= new ArrayList<>();
	}
	
	public String getS() {
		return s;
	}

	public ArrayList<Node> getNodes() {
		return childNodes;
	}
	
	
	@Override
	public String toString(){
		if(s!=null) return s;
		else{
			StringBuilder sb=new StringBuilder();
			sb.append("[");
			for(int i=0;i<childNodes.size();i++){
				if(i<childNodes.size()-1) sb.append(", ");
				sb.append(childNodes.get(i).s);
			}
			sb.append("]");
			return sb.toString();
		}
	}
}
