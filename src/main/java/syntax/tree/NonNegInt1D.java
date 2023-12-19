package syntax.tree;

public class NonNegInt1D {

	public int[] data;
	public int elementCount=0;

	public NonNegInt1D(int maxElementCount) {		
		data=new int[maxElementCount];	
	}

	public int getMax(){
		if(elementCount == 0 ) return -1;
		return data[getIndexOfMax()];
	}

	public int getMin(){				
		if(elementCount == 0 ) return -1;
		return data[getIndexOfMin()];
	}



	public int getAndRemoveMax() {

		int i=getIndexOfMax();
		int result = data[i]; 
		removeElementAtIndex(i);
		return result;
	}

	private int getIndexOfMax(){
		if(elementCount == 0 ) return -1;
		int maxIndex=0;
		int maxValue=data[0];
		for(int i=1;i< this.elementCount;i++){
			if(data[i] > maxValue) {
				maxValue=data[i];
				maxIndex=i;
			}
		}
		return maxIndex;
	}

	public int getAndRemoveMin() {

		int i=getIndexOfMin();
		if(i<0) return -1;
		int result = data[i];
		removeElementAtIndex(i);
		return result;
	}

	private void removeElementAtIndex(int i) {
		data[i]=data[elementCount-1];
		elementCount--;
	}

	private int getIndexOfMin(){
		if(elementCount == 0 ) return -1;
		int minIndex=0;
		int minValue=data[0];
		for(int i=1;i< this.elementCount;i++){
			if(data[i] < minValue) {
				minValue=data[i];
				minIndex=i;
			}
		}
		return minIndex;
	}


	public boolean contains(int v){
		for(int i=0;i< this.elementCount;i++){
			if(data[i] == v) {
				return true;
			}			
		}
		return false;
	}


	public void push(int v){
		data[elementCount++] = v;		
	}

	public int pop(){
		if(elementCount == 0) return -1;
		elementCount--;
		return data[elementCount];		
	}


	public int top(){
		return data[elementCount-1];
	}

	public void clear(){
		elementCount=0;
	}

	public void addAllNew(NonNegInt1D other){
		for(int i=0;i<other.elementCount;i++){
			int v=other.data[i];
			if(!contains(v)) {
				push(v);
			}
		}
	}

	public void addAll(NonNegInt1D other){
		for(int i=0;i<other.elementCount;i++){
			int v=other.data[i];
			push(v);
		}

	}

	public boolean isEmpty() {
		return elementCount <= 0;
	}
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<elementCount-1;i++){
			sb.append(data[i]);
			sb.append(' ');
		}
		if(!isEmpty()){
			sb.append(data[elementCount-1]);
		}
		return sb.toString();
	}

}
