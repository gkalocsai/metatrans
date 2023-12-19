package descriptor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class CharSequenceDescriptorTest {
	
	
	
	@Test
	public void slice(){
		List<String> x = CharSequenceDescriptor.sliceDescriptor("a(a-d \\s [15] [25]-[30])g");
		Assert.assertTrue ("a-d \\s [15] [25]-[30]".equals(x.get(1)));
		
	}
	
	@Test
	public void sliceWithSpecs (){
		List<String> x = CharSequenceDescriptor.sliceDescriptor("a\\s\\[15\\]m\\(g 7) ");
		Assert.assertTrue ("\\s".equals(x.get(1)));
	}
	
	@Test
	public void matchTest (){
		String descriptor = "alma";
		String source = "alma";
		CharSequenceDescriptor csd = new CharSequenceDescriptor(descriptor);
		boolean x = csd.matches(source);
		Assert.assertTrue (x);
	}

	@Test
	public void matchTest2 (){
		String descriptor = "a(a-g r) lm([54] b)a";
		String source = "alma";
		Assert.assertFalse (new CharSequenceDescriptor(descriptor).matches(source));
	}
	
	@Test
	public void matchTest3 (){
		String descriptor = "([65] A)a(a-g) lm([54] b)a";
		String source = "Aac lmba";
		Assert.assertTrue (new CharSequenceDescriptor(descriptor).matches(source));
	}
	
	@Test
	public void matchTest4 (){
		String descriptor = "   ";
		String source = "   ";
		CharSequenceDescriptor csd = new CharSequenceDescriptor(descriptor);
		boolean x = csd.matches(source);
		Assert.assertTrue (x);
	}
	
	@Test
	public void toStringTest (){
		CharSequenceDescriptor csd = new CharSequenceDescriptor("tok");
		Assert.assertEquals("tok", csd.toString());
	}
	
	@Test
	public void justOneBrace (){
		CharSequenceDescriptor csd = new CharSequenceDescriptor("\\(");
	}
	
	
}
