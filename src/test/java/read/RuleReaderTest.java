package read;

import org.junit.Test;

public class RuleReaderTest {

	
	@Test
	public void checkForGroupStrings(){
		
		String init = " /*/cascdsc */ R{ffsfadda  adc \"  \"vsdv} T{A} ";
		RuleReader rr = new RuleReader(init);
		rr.getAllRules();
		
		//TODO assert
		
	}
	
}
