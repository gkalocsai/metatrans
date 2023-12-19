package syntax.grammar;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import compilation.CompilationElement;
import syntax.Rule;
import syntax.V;

public class RuleIntoRulePuller {
	
	

	public static Rule pullInto(Rule into, Rule r, IdCreator idCreator) {
		Stack<CompilationElementPair> cepStack;
		int indexOfReplacement= getIndexOfReplacement(into,r.getGroupname());

		V[] rightside = generateVs(into, r, indexOfReplacement);
		setAllLabelsInR(into, r, idCreator);
		String[] labels=generateLabels(into,r, rightside.length,indexOfReplacement);
		String compilationElementToReplace = getCompilationElementToReplaceFromLabels(into, indexOfReplacement);
		int compLength = determineCompilationLength(into.getCompilation(), r, compilationElementToReplace);		
		CompilationElement[] compilation = new CompilationElement[compLength];		
		cepStack = new Stack<>();
		fillCompilation(compilation, into.getCompilation(), compilationElementToReplace, r, cepStack);
		while(!cepStack.isEmpty()) {
			CompilationElementPair current = cepStack.pop();
			compLength = determineCompilationLength(current.source, r, compilationElementToReplace);		
			compilation = new CompilationElement[compLength];		
			fillCompilation(compilation, current.source, compilationElementToReplace, r,cepStack);
			current.dest.setParams(compilation);
		}

		into.reset(rightside, labels, compilation);		
		return into;
	}

	private static int determineCompilationLength(CompilationElement[] into, Rule r, String compilationElementToReplace) {
		int compilationElementNumber = 0;
		for(CompilationElement ce:into) {
			if(ce.getType() == '(' || ce.getType() == '\"' || !ce.getBase().equals(compilationElementToReplace)) {
				compilationElementNumber++;
			}else if(ce.getType() == ' ') {
				compilationElementNumber+=r.getRightSideLength();
			}else if(ce.getType() == '*') {
				compilationElementNumber+=r.getCompilation().length;
			}else{
				throw new RuntimeException("Unexpected compilation element type");
			}
		}
		return compilationElementNumber;
	}

	private static void fillCompilation(CompilationElement[] result, CompilationElement[] into,
			String baseToReSpacing, Rule r, Stack<CompilationElementPair> cepStack) {
		int resultIndex=0;
		for(CompilationElement ce:into) {
			if(ce.getType()=='\"' || !ce.getBase().equals(baseToReSpacing)){
				result[resultIndex++] = ce;
			}else if(ce.getType() == ' ') {
				for(String l:r.getLabels()){
					result[resultIndex++] = new CompilationElement(l);
				}
			}else if(ce.getType() == '*') {
				for(CompilationElement cex:r.getCompilation()){
					result[resultIndex++] = cex;
				}
			}else if(ce.getType() == '(') {
				CompilationElement cex= new CompilationElement(ce.getBase(),ce.getType());
				result[resultIndex++] = cex;
				cepStack.push(new CompilationElementPair(cex, ce.getParams()));


			}else{
				throw new RuntimeException("Unexpected compilation element type occured in compilation");
			}
		}
	}

	private static String getCompilationElementToReplaceFromLabels(Rule into, int indexOfReSpacingment) {
		String[] labelsOfInto = into.getLabels();
		String compilationElementToReSpacing=labelsOfInto[indexOfReSpacingment];
		return compilationElementToReSpacing;
	}

	private static int getIndexOfReplacement(Rule into, String groupname) {
		for(int intoIndex=0;intoIndex < into.getRightSideLength();intoIndex++){
			if(groupname.equals(into.getRightSideRef(intoIndex))) {
				return intoIndex;
			}
		}
		throw new RuntimeException("No Spacing to include the other rule");
	}

	private static String[] generateLabels(Rule into, Rule r, int length, int indexOfReSpacingment) {
		String[] labels = new String[length];
		int labelsIndex=0;
		String[] labelsOfInto = into.getLabels();
		for(int intoIndex=0;intoIndex < into.getRightSideLength();intoIndex++){
			if(intoIndex == indexOfReSpacingment) {
				for(String s:r.getLabels()){
					labels[labelsIndex++] = s;
				}
			}
			else labels[labelsIndex++] = labelsOfInto[intoIndex];
		}		
		return labels;
	}

	private static V[] generateVs(Rule into, Rule r, int indexOfReSpacingment) {


		V[] rightside= new V[into.getRightSideLength()+r.getRightSideLength()-1];
		int rightsideIndex=0;

		V[] intosRS = into.getRightside();
		for(int i=0; i < intosRS.length ;i++){
			if(i==indexOfReSpacingment) {
				for(V v2:r.getRightside()) {
					rightside[rightsideIndex++] = new V(v2.getReferencedGroup());
				}
			}else{
				rightside[rightsideIndex++] = intosRS[i];
			}
		}
		return rightside;
	}

	private static void setAllLabelsInR(Rule into, Rule r, IdCreator idc) {
		Set<String> currentLabels= getLabels(into,r);
		for(String s:r.getLabels()){
			if(into.hasLabel(s) || s.isEmpty()){
				String label2 = idc.generateYetUnusedId(currentLabels, "_");
				currentLabels.add(label2);
				r.renameLabel(s, label2);
			}
		}

	}


	private static Set<String> getLabels(Rule into, Rule r) {
		Set<String> origLabels=new HashSet<>();

		for(String l:into.getLabels()) {
			if(!l.isEmpty()) origLabels.add(l);
		}
		for(String l:r.getLabels()) {
			if(!l.isEmpty()) origLabels.add(l);
		}
		return origLabels;
	}

}
