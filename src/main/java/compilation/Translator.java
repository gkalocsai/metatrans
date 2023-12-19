package compilation;

import java.util.List;

import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;

public class Translator {
	
	private Grammarhost grammarhost;

	private boolean debug = false;
	public Translator(String syntax) throws GrammarException {
	
		RuleReader rr = new RuleReader(syntax);
		List<Rule> ruleList=rr.getAllRules();
		this.grammarhost=new Grammarhost(ruleList);

	}
	
	public Translator(List<Rule> ruleList, String startSymbol) throws GrammarException {
		this.grammarhost=new Grammarhost(ruleList,startSymbol);
//		if(startSymbol != null) {
//			getGrammarhost().setRootGroup(startSymbol);
//		}else{
//			startSymbol=getGrammarhost().getRootGroup();
//		}
	}
	
	public Translator(Grammarhost gh) {
		this.grammarhost = gh;
	}

	public Translator(Grammarhost gh, boolean debug) {
		this.grammarhost = gh;
		this.debug = debug;
	}

	public TranslationResult translate(String source,String startsymbol){
		if(startsymbol != null) {
			getGrammarhost().setRootGroup(startsymbol);
		}	
		SyntaxTreeBuilder stb=new SyntaxTreeBuilder(getGrammarhost(), source);
		if(debug) stb.showSyntaxtree();
		boolean syntaxTreeBuilt = stb.build();
		if(!syntaxTreeBuilt) {
			return new TranslationResult("", "Couldn't build syntax tree");
		}

		if(debug) stb.showSyntaxtree();
		
    	CompilationTree ct=new CompilationTree(source,stb.getTree(), getGrammarhost().getRootGroup(), getGrammarhost().getGrammar());
    	
    	return new TranslationResult(ct.buildResult(ct.build()),null);
	}

	public Grammarhost getGrammarhost() {
		return grammarhost;
	}

	
		
}
