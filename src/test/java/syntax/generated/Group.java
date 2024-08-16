package syntax.generated;

import java.util.UUID;

import syntax.Rule;
import syntax.RuleCreator;
import util.RandomUtil;

public class Group {

	private Rule[] rsa;
	private char name;
	private boolean canBeRec;
	private Group[] allGroups;
	

	public Group(int groupIndex, Group[] all){
		this.allGroups = all;
		char c=(char) (groupIndex+'A');
		this.name = c;
		this.canBeRec=Math.random()<0.5;
		this.rsa= new Rule[RandomUtil.randomFromToInclusive(2, 4)];
		
	}
	
	public void init(){
		for(int i=0;i<rsa.length;i++){
		   int rightsideLength=RandomUtil.randomFromToInclusive(2,6);
		   String rside;
		   if(alreadyHasRecRule(i) || !canBeRec){
			   rside = createNonRecRs(rightsideLength);
		   }else{
			   rside = createRs(rightsideLength);
			   
		   }
		   
		
		   rsa[i]=RuleCreator.createRule(name+"->"+rside);
		}
	}

	private boolean alreadyHasRecRule(int j) {
		for(int i=0;i<j;i++)  {
			Rule r = rsa[i]; 
			if(r.isDirectRecursive()) return true;
		}
		
		
		return false;
	}

	private String createRs(int rightsideLength) {
	    StringBuilder sb=new StringBuilder();
	    int type; //0:NonREc,1:L,2:R,3:LR,4:MID
	    if(rightsideLength>=3) {
	    	type=RandomUtil.randomInclusive(4);
	    	 
	    }else{
	    	type=RandomUtil.randomInclusive(2);
	    }
	    if(type==0) return createNonRecRs(rightsideLength);
	    if(type==1 || type == 3) {
	    	sb.append(name+" ");
	    	rightsideLength--;
	    }
	    if(type == 2 || type == 3) rightsideLength --;

	    int midRecIndex=-1;
	    if(type == 4) {
	    	midRecIndex = RandomUtil.randomFromToInclusive(1, rightsideLength-2);
	    }
	    
	    for(int i=0;i<rightsideLength;i++) {
			if(i == midRecIndex) {
				sb.append(name+" ");
				continue;
			}
	    	sb.append(createRsPart());
			sb.append(" ");
		}
	    
	    if(type == 2 || type == 3) {
	    	sb.append(name);
	    }
	    
		return sb.toString().trim();
	}

	private String createNonRecRs(int rightsideLength) {
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<rightsideLength;i++) {
			sb.append(createNonRecRsPart());
			sb.append(" ");
		}	
		return sb.toString().trim();
	}

	private String createNonRecRsPart() {
		int index = RandomUtil.randomFromToInclusive(name  - 'A'+1, allGroups.length+2);
		if(index >= allGroups.length) {
			return createNonRecPrefixedCsd();
		}
		return ""+(char)(index+'A');
	}

	private String createNonRecPrefixedCsd() {
		int len=RandomUtil.randomFromToInclusive(1, 1);   //,4
		String result="'";
		for(int i=0;i<len;i++) {
			char h=createRandomLowerCaseChar();
			while (!isAllowedForNonRecCsd(h)){
				h=createRandomLowerCaseChar();
			}
			result=result+h;
		}
		//return result;
		return "'"+UUID.randomUUID();//+(char)((name-'A')+'a');
		
		
	}

	private boolean isAllowedForNonRecCsd(char c) {
		int index= c-'a';
		if(index >= allGroups.length) return true;
		if(allGroups[index].canBeRec) return false;
		return true;
	}

	private String createRsPart() {
		int startIndex=(name  - 'A')+1;
		int maxIndex = allGroups.length+2;
		if(maxIndex < startIndex) {
			System.out.println();
		}
		int index = RandomUtil.randomFromToInclusive(startIndex, maxIndex);
		if(index >= allGroups.length) {
			return "'"+UUID.randomUUID();  //((char)((name  - 'A' ) + 'a')); //createCsdStringWithPrefix((char)((name  - 'A' ) + 'a'));
		}
		return ""+(char)(index+'A');
	}
	
	
	
	

	private String createCsdStringWithPrefix(char c) {
		int len=RandomUtil.randomFromToInclusive(1, 4);
		if(len == 1) return "'"+c;
		len-=2;
		String mid="";
		for(int i=0;i<len;i++) {
			char h=createRandomLowerCaseChar();
			while (h==c){
				h=createRandomLowerCaseChar();
			}
			mid=mid+h;
		}
		
		return "'"+c+mid+c;
	}

	private char createRandomLowerCaseChar() {
		int i=RandomUtil.randomInclusive(25);
		return (char) ('a'+i);
	}

	private boolean onlyRecRulesPresent() {
		for(int i=0;i<getRsa().length-1;i++){
			if(rsa[i] != null &&  !rsa[i].isDirectRecursive()) {
				return false;
			}
		}
		return true;
	}

	public Rule[] getRsa() {
		return rsa;
	}

	public void setRsa(Rule[] rsa) {
		this.rsa = rsa;
	}
		
}
