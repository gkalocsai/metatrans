package list;

import org.junit.Assert;
import org.junit.Test;

import hu.kg.list.ObjectsInDoubleBuff;

public class DataInDoubleBuffTest {

	
	@Test
	public void empty(){
		ObjectsInDoubleBuff<String> db= new ObjectsInDoubleBuff<>();
		Assert.assertNull(db.read());	
	}
	
	@Test
	public void noReadBecauseNoBufferChange(){
		ObjectsInDoubleBuff<String> db= new ObjectsInDoubleBuff<>();
		db.write("Hello");
		Assert.assertNull(db.read());	
	}

	@Test
	public void oneWriteOneRead(){
		ObjectsInDoubleBuff<String> db= new ObjectsInDoubleBuff<>();
		db.write("Hello");
		db.changeBuffer();
		Assert.assertEquals("Hello", db.read());	
	}
	

	@Test
	public void multiWriteOneRead(){
		ObjectsInDoubleBuff<String> db= new ObjectsInDoubleBuff<>();
		db.write("Yes");
		db.write(" of course");
		db.changeBuffer();
		Assert.assertEquals("Yes", db.read());	
	}
	
	
	@Test
	public void multiChangeBuffer(){
		ObjectsInDoubleBuff<String> db= new ObjectsInDoubleBuff<>();
		db.write("Yes");
		db.write(" of course");
		db.changeBuffer();
		Assert.assertEquals("Yes", db.read());
		db.write("X");
		db.changeBuffer();
		Assert.assertEquals("X", db.read());
		db.write("Y");
		db.changeBuffer();		
		Assert.assertEquals("Y", db.read());
	}
	
}
