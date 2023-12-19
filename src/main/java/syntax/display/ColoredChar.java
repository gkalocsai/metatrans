package syntax.display;

public class ColoredChar {

	private char ch;
	private int colorPairId;
	
	
	public ColoredChar(char ch, int colorPairId) {
		super();
		this.ch = ch;
		this.colorPairId = colorPairId;
	}


	public char getChar() {
		return ch;
	}


	public int getColorPairId() {
		return colorPairId;
	}

}
