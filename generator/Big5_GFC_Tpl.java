import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.LinkedHashMap;
import java.util.Map;


/** 編碼器主程式 */
public class Big5_GFC_Tpl extends Charset {

	private static volatile boolean b2cInitialized = false;
	private static volatile boolean c2bInitialized = false;
	
	private static char[][] b2c = new char[0][];
	private static char[][] c2b = new char[0][];
	
    private static final String BASE_CHARSET = "MS950";
    private static final String NAME = "X-MS950-GFC";
    private static final String[] ALIASES = { "X-MS950_GFC" };
    
    private Charset baseCharset;

/** 原BASE_CHARSET為Big5 所以class為Big5_GFC 不影響使用故不更改 */
    public Big5_GFC_Tpl() {
    	this(NAME, ALIASES);
    }
    
    public Big5_GFC_Tpl(String canonical, String[] aliases) {
        super(canonical, aliases);
        baseCharset = Charset.forName(BASE_CHARSET);
    }
    
    public boolean contains(Charset cs) {
        return this.getClass().isInstance(cs);
    } 
    
        
    public CharsetDecoder newDecoder() {
    	initb2c();
        return new Decoder(this, baseCharset.newDecoder(), b2c);
    }

    public CharsetEncoder newEncoder() {
    	initc2b();
    	return new Encoder(this, baseCharset.newEncoder(), c2b);
    }


	/** 初始化 Big5 到 Unicode 對照表 
	 */
	public static void initb2c() {
		if (b2cInitialized) { return; }
		synchronized (b2c) {
			if (b2cInitialized) { return; }
			/*b2cMappingTable*/
			b2cInitialized = true;
		}
	}
    
	
	/** 初始化 Unicode 到 Big5 對照表 
	 */
    public static void initc2b() {
    	if (c2bInitialized) { return; }
    	synchronized (c2b) {
    		if (c2bInitialized) { return; }
    		/*c2bMappingTable*/
	        c2bInitialized = true;
    	}
    }    
    

    
    
    /*=====================================================*/
    
    /** Decoder 從 Big5 byte[] 轉成 unicode char[] 
     * */
    private class Decoder extends CharsetDecoder {
    	
        /* Big5 到 Unicode 對照表 */
    	private final char[][] b2c;
    	
    	/* JVM 原始的 Big5 解碼器 */
    	private final CharsetDecoder baseDecoder; 

    	
        Decoder(Charset cs, CharsetDecoder baseDecoder, char[][] b2c) {
            super(cs, baseDecoder.averageCharsPerByte(), baseDecoder.maxCharsPerByte());
            this.baseDecoder = baseDecoder;
            this.b2c = b2c;
        }

        
        /** 解碼迭代，先用原始的 Big5 進行解碼，如果無法轉換才進一步使用自訂的解碼表。
         * @see java.nio.charset.CharsetDecoder#decodeLoop(java.nio.ByteBuffer, java.nio.CharBuffer)
         */
        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
        	
            baseDecoder.reset();
            CoderResult result = baseDecoder.decode(in, out, true);
            if(!result.isUnmappable() || in.remaining() < 2){ return result; }
            
/* BASE_CHARSET為Big5會報錯 用MS950則正常 但永遠不會unmappable 故下方Decoder不會執行 */
            
            /* 無法轉換，進一步使用自訂的解碼表 */
            int pos = in.position();
            int high = in.get(pos) & 0xFF; 
            int low = in.get(pos + 1) & 0xFF; 
            if(high >= b2c.length || b2c[high] == null){ return result; }
            
            
            /* 減去偏移量， mapping table 第一個值為 offset*/
            low -= (short)b2c[high][0]; 
            if(low < 1 || low >= b2c[high].length){ return result; }

            /* 檢查解碼表是否有對應的轉換，沒有就直接回傳沒對應的字元 */
            int j = b2c[high][low];            
            if(j == 0){ return result; }            

            out.put((char)j);        
            
            in.position(pos + 2);
            return decodeLoop(in, out);            
        }

    }


    
    /*=====================================================*/
    
    /** Encoder 從 unicode char[] 轉成 Big5 byte[] 
     * */
    private class Encoder extends CharsetEncoder {
    	
        /* Unicode 到 Big5 對照表 */
    	private final char[][] c2b;
    	
        /* JVM 原始的 Big5 編碼器 */
    	private final CharsetEncoder baseEncoder;

        Encoder(Charset cs, CharsetEncoder baseEncoder, char[][] c2b) {
            super(cs, baseEncoder.averageBytesPerChar(), baseEncoder.maxBytesPerChar());
            this.baseEncoder = baseEncoder;
            this.c2b = c2b;
        }

        
        /** 編碼迭代，先用 JVM 原始的 Big5 進行編碼，如果無法轉換才進一步使用自訂的編碼表。
         * @see java.nio.charset.CharsetDecoder#decodeLoop(java.nio.ByteBuffer, java.nio.CharBuffer)
         */
        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            baseEncoder.reset();
            CoderResult result = baseEncoder.encode(in, out, true);
            if(!result.isUnmappable() || out.remaining() < 2){ return result; }
            
            
            /* 無法轉換，進一步使用自訂的編碼表 */
            int pos = in.position();
            int index = in.get(pos);
            
/*針對4個Byte字元特別處理 𡘙(\ud845\ude19),𧃙(\ud85c\udcd9),𣲲(\ud84f\udcb2),𤂻(\ud850\udcbb),𤀠(\ud850\udc20) */
            final Map<String,String> fourBytes = new LinkedHashMap<>();
                          /*HEX 轉 DEC     實體位置*/
            fourBytes.put("55365,56857", "\ufac3");
            fourBytes.put("55388,56537", "\ufac5");
            fourBytes.put("55375,56498", "\ufad5");
            fourBytes.put("55376,56507", "\ufaeb");
            fourBytes.put("55376,56352", "\ufaf0");
            if(index >= 55296 && index <= 56319) {
            	int next = in.get(pos+1);
            	String key = index+","+next;
            	if(fourBytes.containsKey(key)) {
            		out.put((byte)( fourBytes.get(key).charAt(0) >> 8));
            		out.put((byte)fourBytes.get(key).charAt(0));
                    in.position(pos + 2);
                    return encodeLoop(in, out);
            	}
            }
            
            int high = index >> 8; 
            int low = index & 0xFF; 
            if(high >= c2b.length || c2b[high] == null){ return result; }

            
            /* 減去偏移量， mapping table 第一個值為 offset*/
            low -= (short)c2b[high][0]; 
            if(low < 1 || low >= c2b[high].length){ return result; }

            /* 檢查編碼表是否有對應的轉換，沒有就直接回傳沒對應的字元 */
            int j = c2b[high][low];
            if(j <= 255){ return result; }
            
            out.put((byte)(j >> 8));
            out.put((byte)j);        
            
            in.position(pos + 1);
            return encodeLoop(in, out);
        }
    }

}