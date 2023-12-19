package main;

import java.io.IOException;

import compilation.TranslationResult;
import compilation.Translator;
import hu.kg.util.StringLoadUtil;
import syntax.grammar.GrammarException;

public class Main {

	public static void main(String[] args) throws IOException, GrammarException {
		
		if(args.length < 2) {
    		System.out.println("Please provide a syntax descriptor file and a source");
    		System.exit(-1);
    	}
	
		String rootGroup = null;
		if(args.length>2) rootGroup = args[2];
		
		String syntaxFileContent = StringLoadUtil.load(args[0]);
		String sourceFileContent = StringLoadUtil.load(args[1]);
		
		
		Translator tr = new Translator(syntaxFileContent);
		
		TranslationResult trr = tr.translate( sourceFileContent, rootGroup);
		if(trr.getError()!= null && !trr.getError().isEmpty()) {		
			System.out.println("ERROR: "+trr.getError());
		}
		else{
			System.out.println(trr.getResult());
		}
    	
    	
    	
		
	}
}
