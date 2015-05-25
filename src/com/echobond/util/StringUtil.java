package com.echobond.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import sun.misc.BASE64Decoder;

import net.sf.json.JSONObject;

/**
 * 
 * @author Luck
 *
 */
public class StringUtil {
	public static ArrayList<?> fromStringToList(String src, String seperator){
		if(null == src)
			return null;
		else if(src.length() < 1)
			return null;
		else if(seperator == null)
			return null;
		else if(seperator.length() < 1)
			return null;
		else if(seperator.length() > src.length())
			return null;
		else{
			String[] elems = src.split(seperator);
			ArrayList<Object> list = new ArrayList<Object>();
			for (String string : elems) {
				if(string.length() > 0)
					list.add(string);
			}
			return null;
		}
	}
	public static String fromListToString(ArrayList<?> list, String seperator, boolean isFrontSep){
		if(list == null)
			return null;
		else if(list.isEmpty())
			return null;
		else if(seperator == null)
			return null;
		else if(seperator.length() < 1)
			return null;
		else{
			String str = "";
			for (Object object : list) {
				if(isFrontSep)
					str+=seperator;
				str += object;
				if(!isFrontSep){
					if(list.indexOf(object) < list.size()-1)
						str+=seperator;
				}
			}
			return str;
		}
	}
	public static String removeFromString(String src, String rem, String seperator, boolean isFrontSep){
		if(null == src)
			return src;
		else if(src.length() < 1)
			return src;
		else if(null == rem)
			return src;
		else if(rem.length() < 1)
			return src;
		else if(rem.length() > src.length())
			return src;
		else if(null == seperator)
			return src;
		else if(seperator.length() < 1)
			return src;
		else if(seperator.length() > src.length())
			return src;
		String[] elems = src.split(seperator);
		String dest = "";
		for (int i=0;i<elems.length;i++) {
			String string = elems[i];
			if(string.length() > 0){
				if(isFrontSep){
					if(!string.equals(rem))
						dest+=seperator+string;
				}
				else{
					if(!string.equals(rem)){
						dest+=string;
						if(elems.length - 1 != i)
							dest+=seperator;
					}
				}
			}
		}
		if(dest.charAt(dest.length()-1) == seperator.charAt(0))
			dest = dest.substring(0, dest.length()-1);
		return dest;
	}
	public static boolean removeFromList(ArrayList<?> src, String rem){
		return src.remove(rem);
	}
	
	
	public static String fromInputStreamToString(InputStream is){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i=-1; 
        try{
	        while((i=is.read())!=-1){ 
	        	baos.write(i); 
	        }
        } catch (IOException e) {
        	e.printStackTrace();
		}
        return baos.toString();
	}
	
	public static String genRandomCode(int codeLength){
		String code = "";
		if(codeLength > 0){
			Random random = new Random();
			for(int i=0;i<codeLength;i++){
				char c = (char) (48 + random.nextInt(79));
				code += c;
			}
		}
		return code;
	}
	
	public static String encodeURL(String src, int key){
		String dest = "";
		for(int i=0;i<src.length();i++){
			dest += (char)(src.charAt(i)^key);
		}
		return dest;
	}
	
	public static String decodeURL(String dest, int key){
		return encodeURL(dest, key);
	}
	
	public static String fromReaderToString(Reader reader){
		StringBuffer jsonBuf=new StringBuffer();
		char[] buf=new char[2048];
		int len=-1;
		try{
			while((len=reader.read(buf))!=-1){
				jsonBuf.append(new String(buf,0,len));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return jsonBuf.toString();
	}
	
	public static JSONObject fromReaderToJSON(Reader reader){
		String jStr = fromReaderToString(reader);
		JSONObject jObj = JSONObject.fromObject(jStr);		
		return jObj;
	}
	
	/**
	 * Decode string to byte array by Base64
	 * @param imgSrc, path
	 * @return
	 */
	public static byte[] stringToImg(String imgSrc){
	    if (imgSrc == null)
	        return null;
	    try {
	        // Base64 decoding
	        byte[] b = new BASE64Decoder().decodeBuffer(imgSrc);
	        for (int i = 0; i < b.length; ++i) {
	            if (b[i] < 0) {
	                // adjust error
	                b[i] += 256;
	            }
	        }
	        return b;
	    } catch (Exception e) {
	        return null;
	    }  
	}
	
	/**
	 * Encode image to string by Base64
	 * @param path
	 * @return
	 */
	public static String imageToStr(String path) {
		ByteArrayOutputStream bos = null;
		GZIPOutputStream gos = null;
		try{
			byte[] data = FileUtil.readFile(path);
			bos = new ByteArrayOutputStream(data.length);
			gos = new GZIPOutputStream(bos);
			gos.write(data);
			gos.close();
			bos.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		byte[] newdata = bos.toByteArray();
		return new String(newdata);
	}
}
