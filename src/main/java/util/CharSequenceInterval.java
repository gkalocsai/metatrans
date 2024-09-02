package util;

public class CharSequenceInterval implements CharSequence {

    private final CharSequence base;
    private final int a;
    private final int b;

    public CharSequenceInterval(CharSequence string) {
        this(string,0,string.length()-1);
    }

    public CharSequenceInterval(CharSequence string, int a, int b) {
    	if (string == null) {
            string = "";
        }
    	if (a < 0  || b > string.length() - 1) {
            throw new IndexOutOfBoundsException();
        }
        this.base = string;
        this.a = a;
        this.b = b;
    }

    @Override
    public char charAt(int index) {
        if (a + index > b) {
            throw new IndexOutOfBoundsException("CharSequenceInterval: " + toString() + " index: " + index);
        }
        return base.charAt(a + index);
    }

    @Override
    public int length() {
        if (isEmpty()) {
            return 0;
        } else {
            return b - a + 1;
        }
    }

    public CharSequenceInterval empty(){
    	return subSequence(0, -1);
    }

    public boolean contains(CharSequence other){
    	return indexOf(other)>=0;
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CharSequence)) {
            return false;
        }
        return hasSameContent((CharSequence) other);
    }

    public boolean beginsWith(CharSequence other){
    	if(other==null || other.length()>this.length()) {
			return false;
		}
    	return new CharSequenceInterval(base,a,a+(other.length()-1)).hasSameContent(other);
    }

    public boolean endsWith(CharSequence other){
    	if(other==null || other.length()>this.length()) {
			return false;
		}
    	return new CharSequenceInterval(base,b-(other.length()-1),b).hasSameContent(other);
    }

    public boolean hasSameContent(CharSequence other) {
        if (other.length() != length()) {
            return false;
        }
        for (int i = 0; i < other.length(); i++) {
            if (charAt(i) != other.charAt(i)) {
                return false;
            }
        }
        return true;
    }


	public boolean isEmpty() {
    	return ( b<a || base.length() ==0);
    }

    public boolean isNotEmpty() {
    	return !isEmpty();
    }

    public int indexOf(CharSequence other){
        return indexOf(other, 0);
    }

    public int indexOf(CharSequence other, int startPosition){
    	int otherLength=other.length();
    	int thisLength=this.length();
    	if(otherLength>thisLength || startPosition < 0) {
    		return -1;
    	}

    	if(otherLength==0 && startPosition <= thisLength){
    		return startPosition;
    	}

    	for(int i=startPosition;i <= thisLength-otherLength;i++){
    		for(int o=0; o <otherLength; o++){
    	    	if(charAt(i+o)!=other.charAt(o)){
    	    		break;
    	    	}
    	    	if(o==otherLength-1) {
    	    		return i;
    	    	}
    	     }
    	}
    	return -1;
    }



    @Override
    public CharSequenceInterval subSequence(int start, int end) {
        return new CharSequenceInterval(base, a + start, a + end);
    }

    public CharSequenceInterval subSequence(int start) {
        return new CharSequenceInterval(base, a + start,b);
    }

	public CharSequenceInterval copy() {
		return subSequence(0);
	}

	public CharSequenceInterval trim() {
		return trimLeft().trimRight();
	}

	public CharSequenceInterval trimLeft(){
		for(int i=0;i<length();i++){
			char c=charAt(i);
			if(!Character.isWhitespace(c)){
				return subSequence(i);
			}
		}
		return new CharSequenceInterval("");
	}

	public CharSequenceInterval trimRight() {
		for(int i=length()-1;i>=0;i--){
			char c=charAt(i);
			if(!Character.isWhitespace(c)){
				return subSequence(0,i);
			}
		}
		return new CharSequenceInterval("");
	}

	@Override
	public String toString() {
		if(isEmpty()) {
			return "";
		} else {
			return ""+base.subSequence(a, b + 1);
		}
	}

}
