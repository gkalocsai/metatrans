package main;

import java.io.IOException;
import java.util.List;

import compilation.Transpiler;
import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.STreeBuilder;
import util.StringLoadUtil;

public class Main {

    public static void main(String[] args) throws IOException, GrammarException {

        if (args.length < 2) {
            System.out.println("Missing syntax descriptor file and/ or source");
            System.out.println("Options: ");
            System.out.println("--d:descriptorFile     :   syntax descriptor file");
            System.out.println("--s:sourcefile         :   source of the compilation");
            System.out.println("--root:rootGroup       :   compile as [rootGroup] ");
            System.out.println("--printOut             :   print the syntax matches");
            System.out.println("--strict:boolean       :   sets the grammar strict or compliant");
            System.out.println("--showTree             :   prints the syntax tree");

            System.exit(-1);
        }

        boolean syntaxFileInArgs = false;

        String syntaxFileContent = null;
        String sourceFileContent = null;
        boolean printOut = false;
        String rootGroup = null;
        boolean strict = true;
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
                    rootGroup = p;
                }
                if (p.startsWith("--strict:")) {
                    p = p.substring(9);
                    if ("false".equalsIgnoreCase(p)) {
                        strict = false;
                    }
                }
                if ("--showTree".equalsIgnoreCase(p)) {
                    showTree = true;
                }
            }
        }

        if (syntaxFileContent == null || sourceFileContent == null) {
            System.out.println("Missing syntax descriptor file and/ or source");
            System.exit(-1);
        }

        RuleReader rr = new RuleReader(syntaxFileContent);
        List<Rule> ruleList = rr.getAllRules();

        Grammarhost grammarhost = new Grammarhost(ruleList, rootGroup);
        grammarhost.setStrict(strict);
        STreeBuilder stb = new STreeBuilder(grammarhost, sourceFileContent, printOut);

        Transpiler tr = new Transpiler(sourceFileContent, stb);

        stb.setPrintOut(printOut);
        stb.setShowTree(showTree);

        String result = tr.transpile();

        if (result == null) {
            System.out.println("ERROR: " + "Could not build the syntax tree");
            System.out.println("Last deduction: " + stb.getLastDeduction());
        } else {
            System.out.println(result);
        }
    }
}
