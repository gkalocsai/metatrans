package syntax.display;

import java.awt.Color;

import hu.kg.color.ColorTable;

class Edge {
    
	
	int x;
    int y;
    String edgeString;
	int length;
	
	Color color;
	
    public Edge(int x, int y, String edgeString, int length) {
		this.x = x;
		this.y = y;
		this.edgeString = edgeString;
		
		if(edgeString.length() > length-4){
			this.edgeString = edgeString.substring(0, length-4);
		}
		
		
		this.length = length;
		if(edgeString == null || edgeString.isEmpty()) color=Color.BLACK;
		else color= ColorTable.getOne(edgeString.hashCode()*37);
				
		
	}    
}
