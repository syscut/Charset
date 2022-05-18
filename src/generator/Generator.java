package generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;


/** 編碼程式產生器，會去解析對應表 moz18-b2u.txt, moz18-u2b.txt 來產生轉換陣列
 */
public class Generator {
    
    /** 取得陣列中第一個有值的位置 */
    private static <T> int findStartPosition(T[] array) {
        int start = 0;
        for (int i = 0; i < array.length; i++) {
            if(array[i] != null){ start = i; break; }
        }   
        return start;
    }
    
    
    /** 取得陣列中最後一個有值的位置 */
    private static <T> int findEndPosition(T[] array) {
        int end = 0;
        for (int i = array.length - 1; i >=0; i--) {
            if(array[i] != null){ end = i; break; }
        }   
        return end;
    }
    
    
    
    /** 讀取檔案至對應表 */
    private static void readFileToMap(String filePath, String[][] map, boolean isB2c) throws Throwable {
        /* JVM 原始的 Big5 編碼器，用來檢查已經定義的編碼對應 */
        Charset big5Charset = Charset.forName("Big5");
        CharsetEncoder big5Encoder = big5Charset.newEncoder();
        CharsetDecoder big5Decoder = big5Charset.newDecoder();
        Method encodeChar = big5Encoder.getClass().getDeclaredMethod("encodeChar", char.class);
        Method decodeDouble = big5Decoder.getClass().getDeclaredMethod("decodeDouble", int.class, int.class);
        
        int lineCount = 0;
        int extendCount = 0;
        String line;
        
        /* 解析檔案 */
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if(!line.startsWith("0x")){ continue; } /* 忽略開頭不是 0x 的行 */
            lineCount++;
            
            String[] split = line.split(" ");
            String from, to;
            if(isB2c){
                from = split[0]; to = split[1];
            }else{
                from = split[1]; to = split[0];
            }
            
            /*取得高低位元*/
            int index = Integer.parseInt(from.replace("0x", ""), 16);  
            int high = index >> 8; 
            int low = index & 0xFF; 

            /*檢查是否已經存在預設編碼 */
            int has;
            if(isB2c){
                has = (char)decodeDouble.invoke(big5Decoder, high, low);
            }else{
                has = (int)encodeChar.invoke(big5Encoder, (char)index);
            }
            if(has != 65533){ continue; }            

            
            if(map[high] == null){ map[high] = new String[256]; }
            map[high][low] = to.replace("0x", "\\u").toLowerCase();
            extendCount++;
        }
        reader.close();
 
        System.out.printf("line: %d, extend: %d, filePath: %s\n", lineCount, extendCount, filePath);
    }
    
    
    /** 組合 mappingList */
    private static String[] buildMappingList(String[][] map) {
        
        String[] mappingList = new String[map.length];
        for (int i = 0; i < map.length; i++) {
            if(map[i] == null){ continue; }
            
            int startLow = findStartPosition(map[i]); /* 尋找第二層最先有值的位置 */
            int endLow = findEndPosition(map[i]); /* 尋找第二層最後有值的位置 */
            if(startLow == 11){ startLow -= 1; } /* 迴避 \u000a 的字元錯誤 */
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("\\u%04x", (short)(startLow - 1)));
            
            for (int j = startLow; j <= endLow; j++) {
                if(map[i][j] != null){
                    sb.append(map[i][j]); 
                }else{
                    sb.append("\\0");
                }               
            }
            mappingList[i] = sb.toString();
        }
        
        return mappingList;        
    }
    
    
    /** 從檔案中讀取 Big5 到 Unicode 對應表 */
    private static String[] getB2cMapping() throws Throwable {
        String[][] b2c = new String[256][];     
        
        /* 解析檔案 moz18-b2u.txt */
        readFileToMap("src/generator/moz18-b2u.txt", b2c, true);        
        
        /* 組合 mappingList */
        return buildMappingList(b2c);
    }


    /** 從檔案中讀取 Unicode 到 Big5 對應表 */
    private static String[] getC2bMapping() throws Throwable {
        String[][] c2b = new String[256][];
        
        /* 解析檔案 moz18-u2b.txt */
        readFileToMap("src/generator/moz18-u2b.txt", c2b, false);        
        
        /* 解析檔案 moz18-u2b.txt */
        readFileToMap("src/generator/moz18-b2u.txt", c2b, false);        
        
        /* 組合 mappingList */
        return buildMappingList(c2b);
    }
    
    
    
    
    public static void main(String[] args) throws Throwable {
        String[] b2c = getB2cMapping();
        String[] c2b = getC2bMapping();

        BufferedReader reader = new BufferedReader(new FileReader("src/generator/Big5_GFC.java"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/com/gfc/nio/charset/Big5_GFC.java"));
        String line;
        while ((line = reader.readLine()) != null) {
            if(line.contains("/*b2cMappingTable*/")){
                String padding = line.substring(0, line.indexOf("/*b2cMappingTable*/"));
                writer.write(padding);
                writer.write("b2c = new char[" + b2c.length + "][];\n");
                for (int i = 0; i < b2c.length; i++) {
                    if(b2c[i] == null){ continue; }
                    writer.write(padding);
                    writer.write("b2c[" + i + "] = \"");
                    writer.write(b2c[i]);
                    writer.write("\".toCharArray();\n");
                }
            }
            else if(line.contains("/*c2bMappingTable*/")){
                String padding = line.substring(0, line.indexOf("/*c2bMappingTable*/"));
                writer.write(padding);
                writer.write("c2b = new char[" + c2b.length + "][];\n");
                for (int i = 0; i < c2b.length; i++) {
                    if(c2b[i] == null){ continue; }
                    writer.write(padding);
                    writer.write("c2b[" + i + "] = \"");
                    writer.write(c2b[i]);
                    writer.write("\".toCharArray();\n");
                }
            }
            else if(line.contains("package generator;")){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                writer.write("package com.gfc.nio.charset;"); 
                writer.write("  /* Generate Date: "); 
                writer.write(formatter.format(new Date())); 
                writer.write(" */"); 
            }
            else{
                writer.write(line); 
            }
            writer.write("\n"); 
        }
        reader.close();     
        writer.close();     
        
        System.out.printf("Generate Complete.");
    }
    
}
