package syntax.display;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.kg.color.Colorizer;
import hu.kg.util.CharSeqUtil;
import syntax.tree.Spacing;

public class SyntaxTreePic {
	
	private static final int COLUMN_COLOR_ID = 1;

	private static final int FROZEN_COLUMN_COLOR_ID = 2;

	public static final int SPACE_BETWEEN_COLS = 40; 
	
	private final static Color NON_FROZEN = new Color(170,170,170);
	
	private final static Color FROZEN_COLOR = new Color(30,30,30);
	
	
	ColoredChar[][] pic;
	
	Map<Integer,ColorPair> colorMap = new HashMap<>();

	int currentColorId=3;

	private Spacing[] spacings;
	
	public SyntaxTreePic(Spacing[] Spacings) {
		this.spacings = Spacings;
		ColorPair columnColor=new ColorPair(NON_FROZEN,NON_FROZEN);
		colorMap.put(COLUMN_COLOR_ID, columnColor);
		ColorPair frozenColumnColor=new ColorPair(FROZEN_COLOR,FROZEN_COLOR);
		colorMap.put(FROZEN_COLUMN_COLOR_ID, frozenColumnColor);
		
		EdgeBuilder eBuilder=new EdgeBuilder(Spacings, SPACE_BETWEEN_COLS);
		int picWidth=( (Spacings.length-1 ) * (SPACE_BETWEEN_COLS+1))+1;
		pic=new ColoredChar[picWidth][eBuilder.getPicHeight()];
		addSpacingColumns();		
		List<Edge> edges= eBuilder.getEdges();
		for(Edge e:edges) {
			addEdgeToThePic(e);
		}
	}

	private void addEdgeToThePic(Edge e) {
		int ownColor = currentColorId;
		int ownBg = currentColorId+1;
		int ownColorGreyBg = currentColorId+2;
		int ownColorDarkGreyBg = currentColorId+3;
		
		currentColorId+=4;
		colorMap.put(ownColor, new ColorPair(e.color, Color.BLACK));
		colorMap.put(ownBg, new ColorPair(Color.BLACK,e.color));
		colorMap.put(ownColorGreyBg, new ColorPair(e.color,NON_FROZEN));
		colorMap.put(ownColorDarkGreyBg, new ColorPair(e.color,FROZEN_COLOR));
		
		ColoredChar[] edgePic=createEdgePic(e,ownColor,ownBg, ownColorGreyBg, ownColorDarkGreyBg);
		int edgePicIndex=0;
		for(int x= e.x; x<e.x+edgePic.length; x++){
			pic[x][pic[0].length-e.y] = edgePic[edgePicIndex++];
		}		
	}

	private ColoredChar[] createEdgePic(Edge e, int ownColor, int ownBg, int ownColorGreyBg, int ownColorDarkGreyBg) {
		
		
		final ColoredChar horizontalLine= new ColoredChar('─', ownColor);
		final ColoredChar cube = new ColoredChar('⬛',ownColor);
		
		
		ColoredChar[] result=new ColoredChar[e.length];
		int pi=(e.x/SPACE_BETWEEN_COLS)+1;
		for(int i=SPACE_BETWEEN_COLS; i< result.length; i+=(SPACE_BETWEEN_COLS+1)){
			if(pi<spacings.length && spacings[pi++].isFrozen() ) {
				result[i] = new ColoredChar('─' , ownColorDarkGreyBg);
			}
			else result[i] = new ColoredChar('─' , ownColorGreyBg);
		}
		int displayedStringLength=e.edgeString.length()+2;
		int fullLength=e.length;
		int stringIndex=(fullLength-displayedStringLength)/2;
		result[stringIndex++] = cube;
		
		char fullBlock=' ';
		//result[stringIndex++] = new ColoredChar(fullBlock,ownColor);
		
		for(int i = 0; i< e.edgeString.length();i++) {
			result[stringIndex++] = new ColoredChar(e.edgeString.charAt(i),ownBg);			
		}
		//result[stringIndex++] = new ColoredChar(fullBlock,ownColor);
		result[stringIndex] = cube;
		
		for(int i=0; i<result.length; i++) {
			if(result[i] == null) {
				result[i] = horizontalLine;
			}
		}
		
		result[0] = cube;
		result[result.length-1] = cube;
		
		return result;
	}

	private void addSpacingColumns() {
		char fullBlock=9608;
		ColoredChar frozenCol=new ColoredChar('▓',FROZEN_COLUMN_COLOR_ID);
		ColoredChar darkCol=new ColoredChar(fullBlock,COLUMN_COLOR_ID);
		
		int SpacingIndex=0;
		for(int x = 0; x< pic.length ; x+=(SPACE_BETWEEN_COLS+1)){
			for(int y=0;y<pic[0].length;y++){
				if(spacings[SpacingIndex].isFrozen()) pic[x][y]  = frozenCol;
				
				else pic[x][y]= darkCol;
			}
			SpacingIndex++;
		}
	}

	
	public String getPic(){
		StringBuilder sb= new StringBuilder();
		sb.append("\n\n");
		for(int y=0;y<pic[0].length;y++){
		for (ColoredChar[] aPic : pic) {			
				ColoredChar cc=aPic[y];
				if( cc == null) {sb.append(" "); continue;}
				if(cc.getChar() =='⬛') {
					sb.append('─');
				}
				else sb.append(cc.getChar());
			}
		sb.append("\n");
		}
		
		
		return sb.toString();
	}

	
	
	public String getColorizedPic(){
		StringBuilder sb= new StringBuilder();
		sb.append("\n\n");
		for(int y=0;y<pic[0].length;y++){
		for (ColoredChar[] aPic : pic) {			
				ColoredChar cc=aPic[y];
				if( cc == null) {sb.append(" "); continue;}
				ColorPair cPair = colorMap.get(cc.getColorPairId());
				if(cc.getChar() =='⬛') {
					sb.append(Colorizer.colorize("─", cPair.foreground));
				}
				else sb.append(Colorizer.colorize(""+cc.getChar(), cPair.foreground, cPair.background));
			}
		sb.append("\n");
		}
		
		
		return sb.toString();
	}

	public String getBitesAndOwnersStr() {
		StringBuilder sb=new StringBuilder();
		for (Spacing spacing : spacings) {
			sb.append(CharSeqUtil.trailingChars(Integer.toString(spacing.getBite()),' ', SPACE_BETWEEN_COLS+1));
		}
		sb.append('\n');
		for (Spacing spacing : spacings) {
			sb.append(CharSeqUtil.trailingChars(""+spacing.getOwner(),' ', SPACE_BETWEEN_COLS+1));
		}
		sb.append('\n');
		
		
		return sb.toString();
	}
	
	
	

}