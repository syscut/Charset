import org.junit.Assert;
import org.junit.Test;

public class TestBig5_GFC {

	public static void main(String[] args) throws Throwable {	
    	TestBig5_GFC test = new TestBig5_GFC();
    	test.charTest2();
    }

    
	@Test
    public void decodeTest() throws Exception {
    	byte[] source = { 
			(byte)0xa4, (byte)0xa4, 
			(byte)0x91, (byte)0x78, 
			(byte)0x91, (byte)0x78, 
			(byte)0xa4, (byte)0xa4, 
		};
    	String result = new String(source, "X-Big5-GFC");

    	Assert.assertEquals("中譱譱中", result);
    }      
	
	
    @Test
    public void convertTest() throws Throwable {
    	String source = "中譱譱中中";
    	
    	byte[] bytes = source.getBytes("X-Big5-GFC");
	    for (byte b : bytes) {
	    	System.out.printf("%x ", b);
	    }
	    System.out.printf("\n");
	    
	    String result = new String(bytes, "X-Big5-GFC");
	    
	    Assert.assertEquals(source, result);
        
    }	
    
    
    @Test
    public void charTest() throws Throwable {
        
        String source = "峯";
        
        byte[] bytes = source.getBytes("X-Big5-GFC");
        for (byte b : bytes) {
            System.out.printf("%x ", b);
        }
        System.out.printf("\n");
    }
    
    @Test
    public void charTest2() throws Throwable {
        
//        String source = "譱";
//        String source = "�";
        String source = "�";
        
        byte[] bytes = source.getBytes("X-Big5-GFC");
        for (byte b : bytes) {
            System.out.printf("%x ", b);
        }
        System.out.printf("\n");
    }

}
