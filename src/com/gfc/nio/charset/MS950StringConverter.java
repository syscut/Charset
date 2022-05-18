package com.gfc.nio.charset;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public class MS950StringConverter {
	
	public String decode(String s) {
		Charset charset = Charset.forName("MS950");
		StringBuilder sb = new StringBuilder();
		char[][] b2c = new char[0][];
		b2c = new char[252][];
		//私人造字區第一區為0xFA40-0XFEFE 對應\ue000-\ue310 造字跳一格補17個\0
		//為什麼是250 => 因為FA
		//b2c[250] 由(堃FA40)編碼到(㛢FAFE)
		b2c[250] = "\u003f\u5803\u83d3\u854b\u4f8a\u7d89\u78d8\u701e\u4e98\u7215\u548f\u73cf\u6f34\u4f03\u5d10\u73f7\u5a96\u7065\u569e\u5ef8\u5cef\u7175\u71ba\u732e\u5586\u73c9\u6630\u5f63\u9235\u5742\u6d72\u3d7e\u5ecd\ue020\u7fa3\u6e70\u6e29\u3ab1\u70f1\u664b\u7add\u52f2\u53cc\u69e1\u36ac\u4fe5\u90a8\u7866\u6822\u51c9\u512f\u4552\u7081\u8c51\u9686\u8218\u71d1\u811a\u8385\u668e\u6f44\u7460\u96a3\u8054\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\u7d8b\u70df\u9427\u706f\u68ca\u714a\u6e76\u6716\u69fa\u5b96\u935f\u51c3\u6fda\u7ff1\u9f8e\u43aa\u745d\u5a81\u5b9d\u306e\u98d2\u781c\u6ed9\u74c8\u59f8\u5ce0\u44f5\u7551\u743c\u57c4\u936e\u7907\u92ed\u5050\u003f\u7ccd\u003f\u98c8\u99c5\u8534\u9ec4\u4f40\u797a\u945b\u747a\u548a\u34a5\u52b9\u6ff5\u6667\u53a6\u4d7a\u003f\u583a\u5bd7\u944d\u798e\u7950\u926e\u7089\u9341\u7468\u941b\u5ac3\u9c15\u97d9\u5bed\u6c97\u7347\u708f\u533b\u4f8e\u9241\u4fe4\u003f\u5cfe\u52a4\u60a6\u936b\u003f\u4fe3\u7310\u3a17\u8471\u753b\u53f6\u53c2\u5fb3\u5754\u555f\u68b9\u4057\u78dc\u36e2\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0".toCharArray();
		//b2c[251] 由(嬑FB40)編碼到(悧FB52)2022.04.29
		b2c[251] = "\u003f\u5b11\u7950\u5857\u9834\u7448\u58e0\u833d\u754a\u8846\u9289\u771e\u66ad\u926b\u9c7b\u5a63\u512b\u5553\u6d5c\u60a7".toCharArray();
		
		//佔4byte的字(𡘙[132],𧃙[134],𣲲[150],𤂻[172],𤀠[177])
		final Map<Integer,String> fourBytes = new LinkedHashMap<>();
		fourBytes.put(132,"\ud845\ude19");
		fourBytes.put(134,"\ud85c\udcd9");
		fourBytes.put(150,"\ud84f\udcb2");
		fourBytes.put(172,"\ud850\udcbb");
		fourBytes.put(177,"\ud850\udc20");
		for (String characer : s.split("")) {
			if(!characer.isEmpty()&&characer.codePointAt(0)>=57344&&characer.codePointAt(0)<=58128) {
				ByteBuffer buffer = ByteBuffer.wrap(characer.getBytes(charset));
				int high = buffer.get(0) & 0xFF;
				int low = buffer.get(1) & 0xFF;
				low -= (short)b2c[high][0];
		        	if(fourBytes.containsKey(low)) {
		        		sb.append(fourBytes.get(low));
		        	}else {
		        		sb.append(b2c[high][low]);
		        	}
			}else {
				sb.append(characer);
			}
		}
		return sb.toString();
	}
	
}