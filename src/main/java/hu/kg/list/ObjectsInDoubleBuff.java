package hu.kg.list;

public class ObjectsInDoubleBuff<T> {


	private StatefulList<T> aList;	
	private StatefulList<T> bList; 
	
	private StatefulList<T> readList;
	private StatefulList<T> writeList;
	

	private int writtenSize;
	private int readCount;
	private int oiginialReadCount;
	
	public ObjectsInDoubleBuff() {
		this(new StatefulList<T>());
	}
	
	public ObjectsInDoubleBuff(StatefulList<T> firstWave) {
		bList=new StatefulList<>();
		init(firstWave);
	}

	public void init(StatefulList<T> init) {
		aList=init;
		readCount = init.size();
		oiginialReadCount = readCount;
		writtenSize = 0;
		readList=aList;
		writeList=bList;
		readList.selectFirstElement();
		writeList.selectFirstElement();	
	}
		
	public void changeBuffer(){  
		aList.selectFirstElement();
		bList.selectFirstElement();
		readCount = writtenSize;
		oiginialReadCount=writtenSize;
		writtenSize = 0;
		if(writeList == bList) {
			writeList = aList;
			readList = bList;
		}else{
			writeList = bList;
			readList = aList;
		}
		
	}
	
	public void write(T value){		
		if(writtenSize < writeList.size()){
			writeList.setValue(value);
			writeList.stepNext();
		}else{
			writeList.addAfter(value);
		}
		writtenSize++;
	}
	
	
	public T read(){
		if(readCount <= 0) {
			return null;
		}else{
			readCount--;
			T result=readList.get();
			readList.stepNext();
			return result;
		}
		
	}

	public boolean moreToRead() {
		return readCount > 0;
	}

	public void rewind() {
		readList.selectFirstElement();
		readCount = oiginialReadCount;
		
	}
	
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(readList.toString());
		sb.append("\n");
		sb.append(writeList.toString());
		return sb.toString();
	}

	public boolean written(Object current) {
		int i=0;
		for(Object o:writeList) {
			if(i==writtenSize) break;
			if(o == current) return true;
			i++;
		}
		return false;
	}
	
}
