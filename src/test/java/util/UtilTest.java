package util;

import org.junit.Assert;
import org.junit.Test;

import util.Util;

public class UtilTest {

		
	
	@Test
	public void convertBinaryNumberTest (){	
		int number = 59;
		int digits = 8;
		boolean[] test = Util.convertBinaryNumber(number, digits);
		Assert.assertTrue(test[7]);
		Assert.assertTrue(test[6]);
		Assert.assertFalse(test[5]);
		Assert.assertTrue(test[4]);
		Assert.assertTrue(test[3]);
		Assert.assertTrue(test[2]);
		Assert.assertFalse(test[1]);
		Assert.assertFalse(test[0]);

	}
	
	
}
