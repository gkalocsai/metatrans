package util;

public class IntInterval {

	private int a;
	private int b;
	
	public IntInterval(int a, int b) {
		if(a <= b){
			this.a = a;
			this.b = b;
		}else{
			this.a = b;
			this.b = a;
		}
		
	}

	
	public boolean contains(int c) {		
		return c>=this.a && c<=this.b;
	}
	
	@Override
	public boolean equals(Object other){
		if(! (other instanceof IntInterval)) return false;
		IntInterval o2=(IntInterval) other;
		return o2.a == this.a && o2.b == this.b;
	}
	
	public IntInterval copy() {
		 return new IntInterval(this.a, this.b);
	}
	
	
	@Override
	public int hashCode(){
		return toString().hashCode();
	}
	
	@Override
	public String toString(){
		return "["+a+","+b+"]";
	}
	
	public int getA() {
		return a;
	}

	public int getB() {
		return b;
	}
		
}
