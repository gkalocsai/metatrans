package syntax;

import descriptor.CharSequenceDescriptor;

public class V {

	
	private String groupNameReference;
	private CharSequenceDescriptor csd;
	
	public V(String s) {
		this.groupNameReference = s;
		this.csd=null;
	}

	public V(CharSequenceDescriptor csd){
		this.csd=csd;
		this.groupNameReference=null;
	}
	
	public boolean isDescriptor() {
		return csd!=null;
	}

	public String getReferencedGroup() {
		if(isDescriptor()) {
			return null;
		}
		else {
			return groupNameReference;
		}
	}
	
	public int getDescribedLength(){
		if(isDescriptor()){
			return csd.getDescribedLength();
		}
		else {
			throw new RuntimeException("Don't ask it here, I cannot deteremine it now");
		}
	}
	
	@Override
	public boolean equals(Object other){
		if(! (other instanceof V)) return false;
		V o2 = (V) other;
		if (o2.isDescriptor() != this.isDescriptor()) return false;
		if(this.isDescriptor()){
			return (this.csd.equals(o2.csd));
		}else{
			return (this.groupNameReference.equals(o2.groupNameReference));
		}
				
	}
		
	@Override
	public String toString(){
		if(isDescriptor()) return "\""+csd+"\"";
		else return groupNameReference;
	}


	public CharSequenceDescriptor getCsd() {
		return csd;
	}

	public V copy() {
		if(isDescriptor()) return new V(this.csd);
		else return new V(this.groupNameReference);
	}
	
}
