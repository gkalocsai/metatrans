package compilation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import hu.kg.util.CharSeqUtil;
import syntax.Rule;
import syntax.tree.NonNegInt1D;
import syntax.tree.RulePointingToSpacing;
import syntax.tree.Spacing;
import syntax.tree.WaveElement;

public class CompilationTree {



	private RulePointingToSpacing[][] rppa;

	private String rootgroup;

	private Map<String, ArrayList<Rule>> grammar;


	NonNegInt1D borders=new NonNegInt1D(1000);
	NonNegInt1D indices=new NonNegInt1D(1000);



	Stack<CSE> compilationStack = new Stack<>();

	private RulePointingToSpacing currentRpp;

	private Spacing[] originalSpacings;

	private String src;


	public CompilationTree(String src,Spacing[] tree, String rootgroup, Map<String, ArrayList<Rule>> map) {

		this.src = src;

		this.originalSpacings=tree;
		this.rootgroup = rootgroup;
		this.grammar = map;
		rppa=new RulePointingToSpacing[tree.length][];



		for(int i=0;i<rppa.length;i++){
			rppa[i] = new RulePointingToSpacing[tree[i].getNexts().size()];
		}

		for(int i=0;i<rppa.length;i++){
			tree[i].getNexts().selectFirstElement();
			for(int k=0;k<rppa[i].length;k++){
				rppa[i][k] = tree[i].getNexts().get();
				tree[i].getNexts().stepNext();
			}
			Arrays.sort(rppa[i]);
		}	
	}


	public Node build(){
		Rule  root = getAppliedRule(0,rootgroup,rppa.length-1);
		Node rootNode = new Node();
		compilationStack.push(new CSE(0, root.getGroupname(), this.rppa.length-1, rootNode));
		while(!compilationStack.isEmpty()){
			CSE cse = compilationStack.pop();
			Rule r= getAppliedRule(cse.prev,cse.group,cse.next);		
			if(!r.isDirectRecursive()){
				processNonrecursiveRule(r,cse);
			}
			else{
				processRecursiveRule(r,cse);
			}
		}
		return rootNode;
	}

	
	//FIX: resolve left and right recursion
	
	private void processRecursiveRule(Rule r, CSE cse) {

		int leftBorder = cse.prev;
		int rightBorder = cse.next;
 

		String[] rrefs=r.extractRefGroups();

		if(r.isLeftRecursive()) {  
			borders.push(leftBorder);
			int rightBite = getRightBite(r,cse);
			//originalSpacings[rightBorder].getRecursiveEater().getSpacingIndex();
			indices.push(getAppliedRuleIndex(leftBorder, r.getGroupname(), rightBite));
			pushBorders(rightBite, rrefs, rightBorder,1, r.getRightSideLength()-1);

			int reSpacingmentNextIndex=rightBite;
			processCSEwithBorders(r, cse.node.childNodes, reSpacingmentNextIndex);

			borders.clear();
			indices.clear();


		}
		else if(r.isRightRecursive()){
	

			int leftBite=getLeftBite(r,cse);


			pushBorders(leftBorder, rrefs, leftBite,0, r.getRightSideLength()-2);
			borders.push(leftBite);
			indices.push(getAppliedRuleIndex(leftBite, r.getGroupname(), rightBorder));

			int reSpacingmentNextIndex=cse.next;
			processCSEwithBorders(r, cse.node.childNodes, reSpacingmentNextIndex);

			borders.clear();
			indices.clear();

		}else{       

			int rightBite = getRightBite(r,cse);
			int leftBite=getLeftBite(r,cse);


			int indexOfMid=r.getIndexOfRefGroup(r.getGroupname());

			pushBorders(leftBorder, rrefs,leftBite, 0,indexOfMid-1);

			borders.push(leftBite); 
			indices.push(getAppliedRuleIndex(leftBite, r.getGroupname(), rightBite));

			pushBorders(rightBite, rrefs, rightBorder, indexOfMid+1, r.getRightSideLength()-1);

			int reSpacingmentNextIndex=rightBite;
			processCSEwithBorders(r, cse.node.childNodes, reSpacingmentNextIndex);

			borders.clear();
			indices.clear();	
		}
	}



	private int getLeftBite(Rule r,CSE cse) {

		for(int k=cse.prev+1;k<cse.next;k++) {
			WaveElement owner=originalSpacings[k].getOwner();
			if(owner !=null && owner.getRule() == r && cse.prev >=owner.getPrevSpacingindex()  && cse.next<=owner.getNextSpacingindex()){
				return originalSpacings[k].getBite();
			}
		}
		return cse.prev;
	}




	private int getRightBite(Rule r,CSE cse) {
		for(int k=cse.next-1;k>cse.prev;k--) {
			WaveElement owner=originalSpacings[k].getOwner();
			if(owner !=null && owner.getRule() == r && cse.prev >=owner.getPrevSpacingindex()  && cse.next<=owner.getNextSpacingindex()){
				return originalSpacings[k].getBite();
			}
		}
		return cse.next;
	}


	private void processNonrecursiveRule(Rule r, CSE cse) {
		if(r.getCompilation().length == 1 && r.getCompilation()[0].getType() =='\"') {
			cse.node.s=CharSeqUtil.resolveFormattedSeq(r.getCompilation()[0].getBase());
			return;
		}
		pushBorders(cse.prev,r.extractRefGroups(),cse.next,0,r.getRightSideLength()-1);
		processCSEwithBorders(r, cse.node.childNodes,-1);
		borders.clear();
		indices.clear();
	}


	private void processCSEwithBorders(Rule r, List<Node> children, int parentsNext) {


		for(CompilationElement ce: r.getCompilation()){
			char type=ce.getType();
			String base=ce.getBase();
			if(type==' '){
				children.add(new Node(createSourceSubStr(r.getIndexOfLabel(base), parentsNext)));
			}else if(type=='*'){
				Node n=new Node();
				children.add(n);
				int refIndex=r.getIndexOfLabel(base);
				compilationStack.push(createCSE(refIndex,r,n,parentsNext));
			}else if(type=='\"'){
				Node n=new Node(CharSeqUtil.resolveFormattedSeq(base));
				children.add(n);
			}else if(type =='('){
				String source=buildInnerSource(ce.getParams(),r ,parentsNext);
				CompilationTree inner=new CompilationTree(source,originalSpacings,rootgroup,grammar);
				Node inn=inner.build();
				String result = inner.buildResult(inn);
				children.add(new Node(result));
			}
			else throw new RuntimeException("Internal error: Invalid compilation type.");
		}
	}


	private String buildInnerSource(CompilationElement[] params, Rule r, int reSpacingment) {
		StringBuilder sb=new StringBuilder();	
		if(params == null || params.length ==0) {
			throw new RuntimeException("Internal error: No source parameter in rule: "+r);
		}
		for(CompilationElement p:params){
			if(p.getType() == '\"') {
				sb.append(p.getBase());
			}else if(p.getType() == ' '){
				int rsIndex=r.getIndexOfLabel(p.getBase());
				if(rsIndex<0) continue;
				sb.append(createSourceSubStr(rsIndex, reSpacingment));
			}else{
				throw new RuntimeException("Bad source parameter in rule: "+r);
			}
		}
		return sb.toString();
	}


	private CSE createCSE(int refIndex, Rule r, Node n, int parentNext) {
		String rref = r.getRightSideRef(refIndex);

		int SpacingIndex = borders.data[refIndex];
		int rppIndex=indices.data[refIndex];

		if(rppIndex < 0 && r.isMidRecursive()) {

			return new CSE(SpacingIndex, rref, borders.data[refIndex+1], n);
		}

		if(rppIndex < 0 && r.isLeftRecursive()) {

			return new CSE(borders.data[0], rref, borders.data[1], n);
		}

		if(rppIndex < 0 && r.isRightRecursive()) {

			return new CSE(SpacingIndex, rref, parentNext, n);
		}
		RulePointingToSpacing choosen=rppa[SpacingIndex][rppIndex];
		return new CSE(SpacingIndex, rref, choosen.getSpacingIndex(), n);
	}


	private String createSourceSubStr(int indexOfLabel, int reSpacingmentIndex) {
		int SpacingIndex = borders.data[indexOfLabel];
		int startIndex=originalSpacings[SpacingIndex].getSourceIndex();
		int rppIndex=indices.data[indexOfLabel];
		int afterEndIndex;
		if(rppIndex < 0){
			afterEndIndex=originalSpacings[reSpacingmentIndex].getSourceIndex();
		}else{
			RulePointingToSpacing choosen=rppa[SpacingIndex][rppIndex];
			afterEndIndex=originalSpacings[choosen.getSpacingIndex()].getSourceIndex();
		}
		if(afterEndIndex<0) return this.src.substring(startIndex);
		else return this.src.substring(startIndex,afterEndIndex); 
	}


	private void pushBorders(int prev, String[] refs, int next, int minRefIndex, int maxRefIndex) {

		borders.push(prev);
		indices.push(-1);

		while(!borders.isEmpty()){
			for(int i=indices.top()+1; i<rppa[borders.top()].length;i=indices.top()+1) {
				indices.pop();
				indices.push(i);
				currentRpp = rppa[borders.top()][i];
				if(currentRpp.getRule().getGroupname().equals(refs[borders.elementCount-1]))  {
					int currentSpacingIndex = currentRpp.getSpacingIndex();
					if(borders.elementCount  == maxRefIndex + 1 && currentSpacingIndex == next) return;
					if(currentSpacingIndex > next) continue;
					if(borders.elementCount == refs.length) {
						continue;
					}
					borders.push(currentSpacingIndex); 
					indices.push(-1);

					continue;
				}
			}
			borders.pop();
			indices.pop();
			if(borders.isEmpty()) break;
		}
		throw new RuntimeException("Bug: No applied rule found");

	}


	private Rule getAppliedRule(int p, String group, int n) {

		int i = getAppliedRuleIndex(p, group,n);
		if(i>=0) {
			return rppa[p][i].getRule();	
		}

		Rule max=null;
		int maxInterval=0;

		for(int k=p+1;k<n;k++) {
			WaveElement owner=originalSpacings[k].getOwner();
			if(owner !=null ){
				int interval=owner.getNextSpacingindex()-owner.getPrevSpacingindex();
				if(interval > maxInterval) {
					maxInterval = interval;
					max=owner.getRule();
					k=owner.getNextSpacingindex()-1;
				}
			}
		}
		return max;
	}



	private int getAppliedRuleIndex(int p, String group, int n) {

		for(Rule r:grammar.get(group)){
			for(int i=0;i<rppa[p].length;i++) {
				RulePointingToSpacing rpp= rppa[p][i];
				if(rpp.getSpacingIndex() == n 
						&& rpp.getRule().equals(r)) return i;
			}
		}
		return -1;
	}


	public String buildResult(Node root) {
		CompTreeTraverser ctt=new CompTreeTraverser(root);
		return ctt.buildResult();
		//		StringBuilder sb=new StringBuilder();
		//		if(root.s != null) {
		//			sb.append(root.s);
		//			return sb.toString();
		//		}else{
		//			for(Node n:root.childNodes){
		//				sb.append(buildResult(n)); 	
		//			}
		//		}
		//		return sb.toString();
	}

	RulePointingToSpacing getRoot(){

		for(Rule r:grammar.get(rootgroup)){
			for(RulePointingToSpacing rpp: rppa[0]) {
				if(rpp.getSpacingIndex() == rppa.length-1 
						&& rpp.getRule().equals(r)) return rpp;
			}
		}
		throw new RuntimeException("no proper root rule in syntax tree");
	}

	private class CSE {

		private int prev;
		private String group;
		private int next;
		private Node node;


		//CompilationStackElement
		CSE(int prev, String group, int next, Node node){
			this.prev = prev;
			this.group = group;
			this.next = next;
			this.node = node;	
		}
		@Override
		public String toString(){
			StringBuilder sb=new StringBuilder();
			sb.append("("+prev+" Group: "+group+" "+next+") Node: " + node.toString());


			return sb.toString();
		}

	}


	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();

		for (RulePointingToSpacing[] aRppa : rppa) {
			for(int j=0;j<aRppa.length;j++){
				sb.append(aRppa[j]);
			}
			sb.append("\n");
		}
		sb.append("Spacings: "+borders+"\n");
		sb.append("Indices: "+indices+"\n");

		sb.append("CE Stack:");
		for(int i=0;i<compilationStack.size();i++){
			sb.append(compilationStack.get(i).toString());
			if(i<compilationStack.size()-1)
				sb.append("  ");
		}
		sb.append("\ncurrent: ");
		if(currentRpp != null) sb.append(currentRpp.toString());
		sb.append("\n");
		for(int i=0;i<originalSpacings.length;i++){
			if(originalSpacings[i].getOwner()!=null)
				sb.append(i+": "+originalSpacings[i].getOwner().toString());
			else sb.append(i+": null");
			sb.append("--->  "+originalSpacings[i].getBite() +"\n");
		}

		return sb.toString();
	}

}

