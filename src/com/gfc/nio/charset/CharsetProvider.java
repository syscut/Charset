package com.gfc.nio.charset;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


/** 字元編碼器連結器，用來向 JVM 提交自訂的編碼器
 */
public class CharsetProvider extends java.nio.charset.spi.CharsetProvider {

    static Map<String, Charset> name2charset;
    static Collection<Charset> charsets;
	

    public Charset charsetForName(String charsetName) {
        if (charsets == null){ init(); }

        return name2charset.get(charsetName.toLowerCase());
    }

    public Iterator<Charset> charsets() {
        if (charsets == null){ init(); }

        return charsets.iterator();            
    }
    
    void init() {
    	name2charset = new HashMap<String, Charset>();
    	
    	charsets = new HashSet<Charset>();
    	charsets.add(new Big5_GFC());
    	
    	
    	for (Charset charset : charsets) {
    		name2charset.put(charset.name().toLowerCase(), charset);
    		for (String aliase: charset.aliases()) {
    		    name2charset.put(aliase.toLowerCase(), charset);
            }
		}
    }
    
}