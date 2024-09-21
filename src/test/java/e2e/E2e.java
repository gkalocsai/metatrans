package e2e;

import org.junit.Test;

import compilation.Transpiler;
import syntax.grammar.GrammarException;
import util.StringLoadUtil;

public class E2e {



    @Test
    public void kiviNew() throws GrammarException {

        String syntaxFileContent;

        syntaxFileContent = "cs{...c>>*c  ;}\n"
                + "c{m: \"(a á e é i í o ó ö ő u ú ü ű)\" >> m \"v\" m;m: \"(A Á E É I Í O Ó Ö Ő U Ú Ü Ű)\" >> m \"V\" m;m: \"([0]-[65535])\" >>m;}";
        // syntaxFileContent = "CS{ CS c>>*CS *c;c >> *c;}\n"
//                + "c{m: \"(a á e é i í o ó ö ő u ú ü ű)\" >> m \"v\" m;m: \"(A Á E É I Í O Ó Ö Ő U Ú Ü Ű)\" >> m \"V\" m;m: \"([0]-[65535])\" >>m;}";

        String sourceFileContent = "NEM" + " vagyok zebra." + "eseményeket vizionálja egybe, melyek sza- \n"
                + "vakba, másokba átvivő szintézisbe szabadítják \n"
                + "az életnek ezt a végzetes szuggeszcióját. Itt \n"
                + "nincs szükség egy mesterkélt egység stilizálá- \n"
                + "sára, főhősre s a harmadik oldalon már holt- \n"
                + "bizonyosra vett befejezésre. Az élet részei az \n"
                + "élet természetes elömlésével következnek egy- \n" + "más után, a kezdet már mintegy folytatása és \n"
                + "nincs külső, hókusz-pókusz befejezés, az egy- \n" + "külső kapocs, mintahogy az életben sorsok "
                + "másra következő részek közt néha alig van \n";
        sourceFileContent = StringLoadUtil.loadResource("45K.txt");

//        sourceFileContent = sourceFileContent.substring(0, 17);
        long startTime = System.currentTimeMillis();

        Transpiler trp = new Transpiler(sourceFileContent, syntaxFileContent);
        String x2 = trp.transpile();

        System.out.println(x2);

        System.out.println("Total Time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");

    }

    @Test
    public void inner() throws GrammarException {

        String syntaxFileContent= StringLoadUtil.loadResource("inner.s2t");;

        String sourceFileContent = StringLoadUtil.loadResource("inner.txt");

//        sourceFileContent = sourceFileContent.substring(0, 17);
        long startTime = System.currentTimeMillis();

        Transpiler trp = new Transpiler(sourceFileContent, syntaxFileContent);
        String x2 = trp.transpile();

        System.out.println(x2);

        System.out.println("Total Time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");

    }

}
