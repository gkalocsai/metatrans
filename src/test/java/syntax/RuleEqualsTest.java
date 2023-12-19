package syntax;

import org.junit.Assert;
import org.junit.Test;


public class RuleEqualsTest {

	
	@Test
	public void equalsTest(){
		
		Rule test1 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		Rule test2 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		
		Assert.assertEquals(test1, test2);
		
	}
	
	@Test
	public void equalsTest2(){
		
		Rule test1 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		Rule test2 = createRule("S->a:B q:C d:E>>*a \"m\" d exp(c+\"5\")");
		
		Assert.assertNotEquals(test1, test2);
		
	}
	
	@Test
	public void equalsTest3(){
		
		Rule test1 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		Rule test2 = createRule("S->a:B c:K d:E>>*a \"m\" d exp(c+\"5\")");
		
		Assert.assertNotEquals(test1, test2);
		
	}
	
	@Test
	public void equalsTest4(){
		Rule test1 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		Rule test2 = createRule("S->a:B c:C d:E>>a \"m\" d exp(c+\"5\")");
		Assert.assertNotEquals(test1, test2);
		
	}
	
	@Test
	public void equalsTest5(){
		Rule test1 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		Rule test2 = createRule("S->a:B c:C d:E>>*a \"mm\" d exp(c+\"5\")");
		Assert.assertNotEquals(test1, test2);
		
	}
	
	@Test
	public void equalsTest6(){
		Rule test1 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		Rule test2 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"6\")");
		Assert.assertNotEquals(test1, test2);
		
	}
	
	@Test
	public void equalsTest7(){
		Rule test1 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		Rule test2 = createRule("S->c:C a:B d:E>>*a \"m\" d exp(c+\"5\")");
		Assert.assertNotEquals(test1, test2);
		
	}
	
	@Test
	public void equalsTest8(){
		Rule test1 = createRule("S->a:B c:C d:E>>*a \"m\" d exp(c+\"5\")");
		Rule test2 = createRule("S->c:C a:B d:E>>*a \"m\" d exp(\"5\"+c)");
		Assert.assertNotEquals(test1, test2);
		
	}

	private Rule createRule(String string) {
		return RuleCreator.createRule(string);
	}
}
