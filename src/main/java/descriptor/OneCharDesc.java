package descriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class OneCharDesc {

    
	List<CharInterval> charIntervals;
	
	public OneCharDesc(List<CharInterval> charIntervals) {
		if(charIntervals == null) {
			charIntervals = new LinkedList<>();
		}
		this.charIntervals = charIntervals;
	}
	
	@Override
	public boolean equals(Object other){
		if(! (other instanceof OneCharDesc)) return false;
	    OneCharDesc o2=(OneCharDesc) other;
		List<CharInterval> a = this.charIntervals;
		List<CharInterval> b = o2.charIntervals;
		if (!(a.size() == b.size())) return false;
		
		Iterator<CharInterval> i1 = a.iterator();
		Iterator<CharInterval> i2 = b.iterator();
		
		while (i1.hasNext()) {
			CharInterval ci1 = i1.next();
			CharInterval ci2 = i2.next();
			if (ci1 == null) {
				return ci2 == null;
			}
			if (!ci1.equals(ci2)) return false;
		}
		return true;
	}

	public OneCharDesc(String s){
		charIntervals = new ArrayList<>();
		
		if(" ".equals(s)){
			addSingleCharInterval(' ');
		}

		String[] parts = s.split("\\s+");

		for(String p:parts){
			if(p.length() == 1 || p.indexOf('-')<0){
				char first=p.charAt(0);
				if (first!= '[') {
					if(first =='\\' && p.length()>1) first=p.charAt(1);
					addSingleCharInterval(first);
				} else {
					int numberInSqrBrackets=readNumberFromSquareBrackets(p);
					addSingleCharInterval(numberInSqrBrackets);
				}
			}else{
				String[] partsOfPart = p.split("-");
				String beforeMinus=partsOfPart[0];
				int first=beforeMinus.charAt(0);
				if (first== '[') {
					first=readNumberFromSquareBrackets(beforeMinus);
				}
				String afterMinus=partsOfPart[1];

				int firstCharAfterMinus=afterMinus.charAt(0);
				if (firstCharAfterMinus== '[') {
					firstCharAfterMinus=readNumberFromSquareBrackets(afterMinus);
				}
				charIntervals.add(new CharInterval(first, firstCharAfterMinus));
			}
		}
		merge();
	}


	private void merge() {
		if(charIntervals.size() == 0 || charIntervals.size() == 1) return;

		sortCharIntervalsByBeginIndex();

		CharInterval first = charIntervals.get(0);
		int start = first.a;
		int end = first.b;

		ArrayList<CharInterval> result = new ArrayList<CharInterval>();

		for (int i = 1; i < charIntervals.size(); i++) {
			CharInterval current = charIntervals.get(i);
			if (current.a <= end + 1) {
				end = Math.max(current.b, end);
			} else {
				result.add(new CharInterval(start, end));
				start = current.a;
				end = current.b;
			}
		}

		result.add(new CharInterval(start, end));
		this.charIntervals = result;
	}



	private void sortCharIntervalsByBeginIndex() {
		Collections.sort(charIntervals, new Comparator<CharInterval>(){
			@Override
			public int compare(CharInterval i1, CharInterval i2)
			{
				return i1.a - i2.a;
			}

		});
	}


	private int readNumberFromSquareBrackets(String p) {
		StringBuilder sb= new StringBuilder();
		for (int i=1; p.charAt(i)!=']';i++){
			sb.append(p.charAt(i));
		}
		int a=Integer.valueOf(sb.toString());
		return a;
	}





	private void addSingleCharInterval(int a) {
		CharInterval charInterval = new CharInterval(a, a);
		charIntervals.add(charInterval);

	}

	public boolean matches(char c) {
		for(CharInterval ci:charIntervals){	
			if(ci.contains(c)) return true;
		}
		return false;
	}


	
	public boolean hasCommonChar(OneCharDesc other){
		Iterator<CharInterval> oit = other.charIntervals.iterator();
		Iterator<CharInterval> it = charIntervals.iterator();
	
		CharInterval i1=null;
		CharInterval i2=null;
		
		while(it.hasNext() && oit.hasNext()) {
			if(i1==null) i1=it.next();
			if(i2==null) i2=oit.next();
			if(i1.hasCommonChar(i2)) return true;
			
			if(i1.b < i2.a) { 
				if(!it.hasNext()) break;
				i1=it.next();
			}else{
				if(!oit.hasNext()) break;
				i2=oit.next();
			}
		}
		return false;
		
	}

	public Character getExample() {
		if(charIntervals == null || charIntervals.isEmpty()) {
			return null;		
		}
		return charIntervals.get(0).a;
	}
	
	
	public Character getRandomExample() {
		if(charIntervals == null || charIntervals.isEmpty()) {
			return null;		
		}		
		int listIndex=(int) (Math.random() * charIntervals.size());
		
		CharInterval x = charIntervals.get(listIndex);
		
		
		return x.getRandomExample();
	}
	
	
	
	
	@Override
	public String toString(){
		if(charIntervals == null || charIntervals.isEmpty()) {
			return null;		
		}
		StringBuilder sb=new StringBuilder();
		for(CharInterval ci:charIntervals) {
			if(ci.a==ci.b) {
				sb.append(ci.a);
			}else{
				sb.append("|"+ci.a+"-"+ci.b+"|");
			}
			
		}
		return sb.toString();
	}
	
	
}
