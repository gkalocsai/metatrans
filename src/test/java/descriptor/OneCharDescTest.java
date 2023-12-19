package descriptor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class OneCharDescTest {
	
	@Test
	public void singleCharDescriptor(){
		
		String s="2";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		CharInterval firstInterval=intervalList.get(0);
		char beginningOfTheInterval = firstInterval.a;
		
		Assert.assertTrue('2'==beginningOfTheInterval);
		
	}
	
	
	@Test
	public void oneCharWithMultipleOptions(){
		
		String s="T z m x";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		CharInterval firstInterval=intervalList.get(0);
		CharInterval secondInterval=intervalList.get(1);
		char beginningOfTheInterval = firstInterval.a;
		Assert.assertTrue('T'==beginningOfTheInterval);
		char endOfTheInterval = secondInterval.b;
		Assert.assertTrue('m'==endOfTheInterval);
	}
		
	@Test
	public void singleCharWithSqrBrackets(){
		
		String s="[65]";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		CharInterval firstInterval=intervalList.get(0);
		char beginningOfTheInterval = firstInterval.a;
		Assert.assertTrue('A'==beginningOfTheInterval);
		int meret=intervalList.size();
		Assert.assertTrue(meret==1);
		
	}
	
	@Test
	public void twoCharsWithSqrBrackets(){
		
		String s="T [65] n";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		
		Assert.assertTrue(intervalList.get(0).a == 'A');

		int meret=intervalList.size();
		Assert.assertTrue(meret==3);
		
		Assert.assertTrue(intervalList.get(2).a == 'n');
		
		
	}
	
	@Test
	public void twoCharsWithMinus(){
		
		String s="a-d";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		
		Assert.assertTrue(intervalList.get(0).a == 'a' && intervalList.get(0).b == 'd');

	}
	
	@Test
	public void mergeTest(){
		
		String s="a-f z-g";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		CharInterval firstInterval=intervalList.get(0);
		char beginningOfTheInterval = firstInterval.a;
		char endOfTheInterval = firstInterval.b;
		Assert.assertTrue('a'==beginningOfTheInterval);
		Assert.assertTrue('z'==endOfTheInterval);
		
	}
	
	@Test
	public void mergeTest2(){
		
		String s="a-f z-h";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		CharInterval firstInterval=intervalList.get(0);
		char beginningOfTheInterval = firstInterval.a;
		char endOfTheInterval = intervalList.get(1).b;
		Assert.assertTrue('a'==beginningOfTheInterval);
		Assert.assertTrue('z'==endOfTheInterval);
		
	}
	
	@Test
	public void mergeTest3(){
		
		String s="z-g j-l";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		CharInterval firstInterval=intervalList.get(0);
		char beginningOfTheInterval = firstInterval.a;
		char endOfTheInterval = firstInterval.b;
		Assert.assertTrue('g'==beginningOfTheInterval);
		Assert.assertTrue('z'==endOfTheInterval);
		Assert.assertTrue(intervalList.size()==1);
		
	}
	
	@Test
	public void mergeTest4(){
		
		String s="a-c b-d";
		OneCharDesc oneCharDesc = new OneCharDesc(s);
	
		List<CharInterval> intervalList=oneCharDesc.charIntervals;
		CharInterval firstInterval=intervalList.get(0);
		char beginningOfTheInterval = firstInterval.a;
		char endOfTheInterval = firstInterval.b;
		Assert.assertTrue('a'==beginningOfTheInterval);
		Assert.assertTrue('d'==endOfTheInterval);
		Assert.assertTrue(intervalList.size()==1);
		
	}
	
}
