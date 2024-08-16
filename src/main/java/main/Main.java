package main;

import java.io.IOException;

import compilation.Transpiler;
import syntax.grammar.GrammarException;
import util.StringLoadUtil;

public class Main {

	public static void main(String[] args) throws IOException, GrammarException {

		if(args.length < 2) {
    		System.out.println("Please provide a syntax descriptor file and a source");
    		System.exit(-1);
    	}

		String rootGroup = null;
		if(args.length>2) {
			rootGroup = args[2];
		}

		String syntaxFileContent = StringLoadUtil.load(args[0]);
		String sourceFileContent = StringLoadUtil.load(args[1]);


		Transpiler tr = new Transpiler(sourceFileContent,syntaxFileContent);

		String result=tr.transpile();

		if(result == null) {
			System.out.println("ERROR: "+"Could not build the syntax tree");
		}
		else{
			System.out.println(result);
		}




	}
}
