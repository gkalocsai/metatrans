package e2e;

import org.junit.Test;

import compilation.Transpiler;
import syntax.grammar.GrammarException;

public class E2e {

//
//	@Test
//	public void exp3() throws IOException, GrammarException{
//
//
//		String syntaxFileContent;
//
//
//		syntaxFileContent="exp{n:ds>>n; \"\\(\" exp \"\\)\">> *exp;   e1:exp op:\"*\" e2:exp >> *e1 \" \" *e2"
//				+ " \" \" op; e1:exp op:\"+\" e2:exp >> *e1 \" \" *e2 \" \" op; e1:exp op:\"-\" e2:exp >> *e1 \" \" *e2 \" \" op;}"
//				+ "op{\"-\">>\"-\";\"*\">>\"*\";\"+\">>\"+\";}"
//				+ "ds{d>>\"z\";d ds:ds>>\"z\" ds;}"
//				+ "d{d:\"(0-9)\">> \"z\";}";
//
//
//		String sourceFileContent;
//
//		//sourceFileContent="9*(5)*2";
//		sourceFileContent="(53+40)*2+19";
//
//
//		RuleReader rr = new RuleReader(syntaxFileContent);
//		List<Rule> ruleList=rr.getAllRules();
//		Grammarhost gh=new Grammarhost(ruleList);
//		System.out.println(gh);
//
//		Translator tr = new Translator(gh,true);
//
//		TranslationResult trr = tr.translate( sourceFileContent,null);
//
//
//    	System.out.print(trr.getResult());
//        Assert.assertEquals("53 40 + 2 * 19 +",trr.getResult());
//	}
//
//	@Test
//	public void kivi() throws GrammarException {
//
//		String syntaxFileContent;
//
//
//		syntaxFileContent="cs{ c:c >>  *c;cs c>>*cs *c;}\n"+
//		"c{m: \"(a á e é i í o ó ö ő u ú ü ű)\" >> m \"v\" m;m: \"(A Á E É I Í O Ó Ö Ő U Ú Ü Ű)\" >> m \"V\" m;m: \"([0]-[65535])\" >>m;}";
//
//
//	    String sourceFileContent="NEM vagyok zebra.";
//	    //String sourceFileContent=util.StringLoadUtil.loadResource("konyv.txt");
//
//	    long startTime=System.currentTimeMillis();
//
//		RuleReader rr = new RuleReader(syntaxFileContent);
//		List<Rule> ruleList=rr.getAllRules();
//		Grammarhost gh=new Grammarhost(ruleList);
//		System.out.println(gh);
//
//		SyntaxTreeBuilder stb=new SyntaxTreeBuilder(gh, sourceFileContent);
//
//		boolean syntaxTreeBuilt = stb.build();
//
//
//		System.out.println("Time elapsed: " + (System.currentTimeMillis()-startTime)+" " +syntaxTreeBuilt);
//		Translator tr = new Translator(gh);
//
//		TranslationResult trr = tr.translate( sourceFileContent,null);
//
//
//    	System.out.print(trr.getResult());
//
//
//
//	}

	@Test
	public void kiviNew() throws GrammarException {

		String syntaxFileContent;


		syntaxFileContent="cs{ cs C>>*cs *C;C >>  *C;}\n"+
		"C{m: \"(a á e é i í o ó ö ő u ú ü ű)\" >> m \"v\" m;m: \"(A Á E É I Í O Ó Ö Ő U Ú Ü Ű)\" >> m \"V\" m;m: \"([0]-[65535])\" >>m;}";



		String sourceFileContent="NEMvu"
    		    + " vagyok zebra."
	    		+ "eseményeket vizionálja egybe, melyek sza- \n"
	    		+ "vakba, másokba átvivő szintézisbe szabadítják \n"
	    		+ "az életnek ezt a végzetes szuggeszcióját. Itt \n"
	    		+ "nincs szükség egy mesterkélt egység stilizálá- \n"
	    		+ "sára, főhősre s a harmadik oldalon már holt- \n"
	    		+ "bizonyosra vett befejezésre. Az élet részei az \n"
	    		+ "élet természetes elömlésével következnek egy- \n"
	    		+ "más után, a kezdet már mintegy folytatása és \n"
	    		+ "nincs külső, hókusz-pókusz befejezés, az egy- \n"
	    		+ "külső kapocs, mintahogy az életben sorsok "
	    		+ "másra következő részek közt néha alig van \n"
;
	//    sourceFileContent=StringLoadUtil.loadResource("45K.txt");

	//    sourceFileContent = sourceFileContent.substring(0, 100);
	    long startTime=System.currentTimeMillis();
//		RuleReader rr = new RuleReader(syntaxFileContent);
//		List<Rule> ruleList=rr.getAllRules();
//		Grammarhost grammarhost = new Grammarhost(ruleList);
//
//	    SyntaxTreeBuilder sb=new SyntaxTreeBuilder(grammarhost , sourceFileContent);
//	    //sb.showSyntaxtree();
//	    System.out.println(sb.build());
//
//
//
//	    Translator tr=new Translator(grammarhost);
//	    TranslationResult x = tr.translate(sourceFileContent, grammarhost.getRootGroup());
//
//	    System.out.println(x.getResult());
//	    System.out.println("Total Time elapsed: " + (System.currentTimeMillis()-startTime)+" ms");

	    Transpiler trp=new Transpiler(sourceFileContent, syntaxFileContent);
	    String x2 = trp.transpile();

	    System.out.println(x2);

		System.out.println("Total Time elapsed: " + (System.currentTimeMillis()-startTime)+" ms");





	}


}
