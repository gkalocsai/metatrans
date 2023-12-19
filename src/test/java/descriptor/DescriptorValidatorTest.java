package descriptor;


import org.junit.Assert;
import org.junit.Test;

public class DescriptorValidatorTest {


	@Test
	public void descriptorStringCannotBeNull(){
		BooleanResult r = DescriptorValidator.check(null);		
		Assert.assertFalse(r.getBooleanResult());	
	}
	
	@Test
	public void mustBeSgInBrackets(){
		BooleanResult r = DescriptorValidator.check("sss()ss");		
		Assert.assertFalse(r.getBooleanResult());		
	}


	@Test
	public void descriptorStringCanBeEmpty(){
		CharSequenceDescriptor dsc = new CharSequenceDescriptor("");
		Assert.assertTrue(dsc.matches(""));

	}
	
	@Test
	public void invalidInterval(){	
		checkMultipleDescriptors("Bad syntax around the minus character","(f-g-m)","(fg-g)","ddd(s[45]-g)","ddd(ss-g[45])","(f-hh)");
	}
	
	@Test
	public void invalidCharseqInBrackets(){	
		checkMultipleDescriptors("Missing space inside the parentheses","(fg)");
	}
	
	

	@Test
	public void lastCharASingleBackslash(){
		checkMultipleDescriptors("LastCharIsASingleBackslash","khih\\");
	}


	@Test
	public void convertValidBackslashesToATest(){
		String s = DescriptorValidator.convertValidBackslashesToA("a\\s\\[\\]\\]ffff\\(\\n");
		Assert.assertTrue("aAAAAffffAA".equals(s));
	}


	@Test
	public void lastCharIsInvalid(){
		checkMultipleDescriptors("Last char is invalid","khih(", "khih[");
	}



	//String specs="[]s\\()-rnt"; 

	@Test
	public void backslashFollowedByNonSpecChar(){
		checkMultipleDescriptors("Invalid escape sequence","ghg\\hjh" );


	}
	
	@Test
	public void oneSpaceAfterBracket(){
		checkMultipleDescriptors("oneSpaceAfterBracket","( ", "( )");

	}

	@Test
	public void oneMinusAfterBracket(){
		checkMultipleDescriptors("oneMinusAfterBracket","dwwd(-)", "(-)");

	}

	@Test
	public void sqrBracketOutsideOfBrackets (){
		checkMultipleDescriptors("Square bracket without parenthesis","[45]", "ff[11]ff", "[45]");

	}

	@Test
	public void notOnlyDigitsInSqrBrackets (){
		checkMultipleDescriptors("Only digits are allowed between square brackets","([45p]h)", "ff([1u1]f)f", "(g[s45])f");
	}

	@Test
	public void mustBeDigitInSqrBrackets (){
		checkMultipleDescriptors("There is nothing between square brackets","([])");
	}

	
	

	@Test
	public void missingSqrBracket (){
		checkMultipleDescriptors("A square bracket is missing","([45)", "ff(11]f)f", "(g[45)f");
	}
	
	@Test
	public void missingBracketTest(){
		checkMultipleDescriptors("A bracket is missing","([45]h", "ff[1u1]f)f", "g[s45])f");
	}


	private void checkMultipleDescriptors(String errorMessage, String... sArray) {

		for(String x:sArray){
			BooleanResult r=DescriptorValidator.check(x);
			if(!r.getMessage().contains(errorMessage)){
				System.out.println(r);
			}
			Assert.assertTrue(r.getMessage().contains(errorMessage));
			Assert.assertFalse(r.getBooleanResult());		
		}


	}

	@Test
	public void descriptorIsOkTest(){

		String descriptor = "(t-g)";
		BooleanResult r=DescriptorValidator.check(descriptor);

		Assert.assertTrue(r.getBooleanResult());	
	}




}
