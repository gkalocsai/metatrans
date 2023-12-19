package compilation;

import java.util.Arrays;

import syntax.tree.Spacing;

public class IndexFlowDirectionchanger {


	public static void changeDirection(Spacing[] originalSpacings) {
	   int[] n=new int[originalSpacings.length];
	   Arrays.fill(n, -1);
	   for(int i=0;i<originalSpacings.length;i++){
		   int recEater=originalSpacings[i].getBite();
		   if(recEater != -1) {
			   n[recEater]=i; 
		   }
	   }
	   for(int i=0;i<n.length;i++){
		   originalSpacings[i].setBite(n[i]);
	   }
	}
}
