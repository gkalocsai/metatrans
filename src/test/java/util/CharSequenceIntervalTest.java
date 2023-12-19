package util;



import org.junit.Assert;
import org.junit.Test;

import hu.kg.util.CharSequenceInterval;



public class CharSequenceIntervalTest {


	
	@Test
	public void trimTest(){
		CharSequenceInterval s=new CharSequenceInterval("  \t\n  deedw ").trim();
		Assert.assertEquals(s, new CharSequenceInterval("deedw"));
		
		CharSequenceInterval s2=new CharSequenceInterval("deedw").trim();
		Assert.assertEquals(s2, new CharSequenceInterval("deedw"));
		
		CharSequenceInterval s3=new CharSequenceInterval("\r \tdeedw").trim();
		Assert.assertEquals(s3, new CharSequenceInterval("deedw"));
		
		CharSequenceInterval s4=new CharSequenceInterval("deedw\n\r ").trim();
		Assert.assertEquals(s4, new CharSequenceInterval("deedw"));
		
		
	}
	
	
	
	
	

}
