package util;

public class RandomUtil {

	

	public static int randomFromToInclusive(int x, int y) {
		if(y<x) throw new RuntimeException("Bad interval");
		return x+(int) (Math.random()*(y-x));
	}
	public static int randomInclusive(int n){
		return (int) (Math.random()*(n+1));
	}
	
	
}
