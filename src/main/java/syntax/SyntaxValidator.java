package syntax;

import java.util.HashSet;
import java.util.Set;

public class SyntaxValidator {

	
	public static void areLabelCollisionsSolved(Rule into, Rule what) {
		Set<String> names=new HashSet<String>();
		for(String s:into.getLabels()){
			if(names.contains(s)) {
				throw new RuntimeException(into+" "+what);
			}
			if(s!=null && !s.isEmpty()) {
			names.add(s);
			}
		}
		
		for(String s:what.getLabels()){
			if(names.contains(s)) {
				throw new RuntimeException(into+" "+what);
			}
			if(s!=null && !s.isEmpty()) {
			names.add(s);
			}
		}	
	}
}
