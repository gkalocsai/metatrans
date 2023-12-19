package syntax.compilation.i2p;

public class ExpGenerator {
	private String exp=randomNum ();

	static String randomNum(){
		int a =  (int) (Math.random()*7);  //3
		int m = 1;
		double b = Math.random();
		int i;
		for (i=0; i<a; i++){
			m=m*10;
		}
		b=b*10*m;
		int result=(int) b;
		return Integer.toString((result));
	}
	String generate(){
		if (exp.length()>12) return exp;
		if (Math.random()<0.75){
			int extend=(int) (Math.random()*5);
			ExpGenerator expGen = new ExpGenerator();
			switch (extend){
			  case 0:{
				  exp = "("+expGen.generate()+")";
				  break;  
			  }
			  case 1:{
				  exp = expGen.generate()+"+"+exp;
				  break;
			  }
			  case 2:{
				  exp = expGen.generate()+"*"+exp;
				  break;
			  }
			  case 3:{
				  exp = exp + "+" + expGen.generate();
				  break;
			  }
			  case 4:{
				  exp = exp + "*" + expGen.generate();
				  break;
			  }
			}
		}
		return exp;
	}
}
