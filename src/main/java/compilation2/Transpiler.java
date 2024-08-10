package compilation2;

import java.util.List;
import java.util.Map;

import compilation.CompilationElement;
import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder2.RuleInterval;
import syntax.tree.builder2.STreeBuilder;

public class Transpiler {

	private String source;
	private Grammarhost grammarhost;
    private StringBuilder sb=new StringBuilder();
	private Map<RuleInterval, RuleInterval[]> deduction;
	
	
	
	public Transpiler(String source, String syntaxFileContent) throws GrammarException  {
		
		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList=rr.getAllRules();
		this.grammarhost = new Grammarhost(ruleList);
		this.source=source;
		
	}
	
	public String transpile() {
		STreeBuilder stb=new STreeBuilder(grammarhost, source);
		deduction = stb.build();
		RuleInterval root=stb.getRoot();
		if(root == null) return null;
		
		
		doTranspile(root);
		
		return sb.toString();
		
	}

	
	private void doTranspile(RuleInterval e) {
		Rule r=e.getRule();
		RuleInterval[] ra = deduction.get(e);
		CompilationElement[] compArray = r.getCompilation();
		for(CompilationElement ce:compArray) {
			char type=ce.getType();
			if(type =='\"') {
				sb.append(ce.getBase());
			} else if(type == ' ') {
				int x = r.getIndexOfLabel(ce.getBase());
				sb.append(source.substring(ra[x].getBegin(), ra[x].getLast()+1));
			} else if(type == '*') {
				int x = r.getIndexOfLabel(ce.getBase());
				doTranspile(ra[x]);
			}
		}
	}
	
}
