package main;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import compilation.Transpiler;
import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;
import util.StringLoadUtil;

public class Main {

    public static void main(String[] args) throws IOException, GrammarException {

        if (args.length < 2) {
            System.out.println("Missing syntax descriptor file and/ or source");
            System.out.println("Options: ");
            System.out.println("--d:descriptorFile     :   syntax descriptor file");
            System.out.println("--s:sourcefile         :   source of the compilation");
            System.out.println("--root:root1,root2...  :   compile as the first matched root");
            System.out.println("--printOut             :   print the syntax matches");
            System.out.println("--showTree             :   prints the syntax tree");
            System.exit(-1);
        }

        boolean syntaxFileInArgs = false;

        String syntaxFileContent = null;
        String sourceFileContent = null;
        boolean printOut = false;

        Set<String> roots = new HashSet<>();

        boolean showTree = false;
        for (String p : args) {
            if (!p.startsWith("--")) {
                if (syntaxFileInArgs) {
                    sourceFileContent = StringLoadUtil.load(p);
                } else {
                    syntaxFileContent = StringLoadUtil.load(p);
                    syntaxFileInArgs = true;
                }
            } else {
                if ("--printOut".equalsIgnoreCase(p)) {
                    printOut = true;
                }
                if (p.startsWith("--d:")) {
                    p = p.substring(4);
                    syntaxFileContent = StringLoadUtil.load(p);
                    syntaxFileInArgs = true;
                }
                if (p.startsWith("--s:")) {
                    p = p.substring(4);
                    sourceFileContent = StringLoadUtil.load(p);
                }
                if (p.startsWith("--root:")) {
                    p = p.substring(7);

                    String[] grs = p.split(",");
                    for (String s : grs) {
                        roots.add(s);
                    }

                }
                if ("--showTree".equalsIgnoreCase(p)) {
                    showTree = true;
                }
            }
        }

        if (syntaxFileContent == null || sourceFileContent == null)

        {
            System.out.println("Missing syntax descriptor file and/ or source");
            System.exit(-1);
        }

        RuleReader rr = new RuleReader(syntaxFileContent);
        List<Rule> ruleList = rr.getAllRules();

        Grammarhost grammarhost = new Grammarhost(ruleList, roots);

        SyntaxTreeBuilder stb = new SyntaxTreeBuilder(grammarhost, sourceFileContent, printOut);

        Transpiler tr = new Transpiler(sourceFileContent, stb);

        stb.setPrintOut(printOut);
        stb.setShowTree(showTree);

        String result = tr.transpile();

        if (result == null) {
            System.out.println("ERROR: " + "Could not build the syntax tree");
            System.out.println("Syntax tree state: \n" + stb.getState());
        } else {
            System.out.println(result);
        }
    }
}
