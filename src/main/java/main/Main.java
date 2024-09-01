package main;

import java.io.IOException;

import compilation.Transpiler;
import syntax.grammar.GrammarException;
import util.StringLoadUtil;

public class Main {

	public static void main(String[] args) throws IOException, GrammarException {

		if(args.length < 2) {
    		System.out.println("Missing syntax descriptor file and/ or source");
    		System.out.println("Options: ");
    		System.out.println("--d:descriptorFile     :   syntax descriptor file");
    		System.out.println("--s:sourcefile         :   source of the compilation");
    		System.out.println("--root:rootGroup       :   compile as [rootGroup] ");
    		System.out.println("--printOut             :   print the syntax matches");
    		System.exit(-1);
    	}

        boolean syntaxFileInArgs=false;

        String syntaxFileContent = null;
        String sourceFileContent = null;
        boolean printOut=false;
        String rootGroup = null;

        for(String p: args) {
            if(!p.startsWith("--")) {
				if(syntaxFileInArgs) {
	                sourceFileContent = StringLoadUtil.load(p);
				}else {
					syntaxFileContent = StringLoadUtil.load(p);
					syntaxFileInArgs =true;
				}
			}else {
				if("--printOut".contentEquals(p)) {
					printOut=true;
				}
				if(p.startsWith("--d:")) {
					p=p.substring(4);
					syntaxFileContent = StringLoadUtil.load(p);
					syntaxFileInArgs =true;
				}
				if(p.startsWith("--s:")) {
					p=p.substring(4);
					sourceFileContent = StringLoadUtil.load(p);
				}
				if(p.startsWith("--root:")) {
					p=p.substring(7);
					rootGroup=p;
				}
			}
        }

        if(syntaxFileContent == null || sourceFileContent ==null) {
 		      System.out.println("Missing syntax descriptor file and/ or source");
 	          System.exit(-1);
        }

		Transpiler tr = new Transpiler(sourceFileContent,syntaxFileContent, rootGroup, printOut);

		String result=tr.transpile();

		if(result == null) {
			System.out.println("ERROR: "+"Could not build the syntax tree");
		}
		else{
			System.out.println(result);
		}
	}
}
