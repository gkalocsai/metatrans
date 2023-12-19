package syntax.display;

import java.awt.Color;

public class ColorPair {

	Color foreground;
	Color background;
	
	public ColorPair(Color foreground, Color background) {
		this.foreground = foreground;
		this.background = background;
	}

	public ColorPair(Color foreground) {		
		this.foreground = foreground;
		this.background = Color.BLACK;
	}

}
