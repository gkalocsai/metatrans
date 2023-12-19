
package hu.kg.util;

import java.util.Collection;

public class Util {

	
	
	public static boolean[] convertBinaryNumber (int number, int digits) {
		boolean[] result = new boolean [digits];
		for (int i = digits-1; i >= 0; i--) {
			if (number%2==1) result[i] = true;
			number = number >> 1;
		}
		
		return result;	
	}


	public static int countTrue(boolean[] arr) {
		int result = 0;
		for(boolean b:arr){
			if(b) result++;
		}
		return result;
	}

	public static int[] convertToIntArray(Collection<Integer> result) {
		int[] r=new int[result.size()];
		int k=0;
		for(int i:result){
			r[k++] =i;
		}
		return r;
	}


	
}
