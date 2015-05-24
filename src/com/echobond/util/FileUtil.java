package com.echobond.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 * @author Luck
 *
 */
public class FileUtil {
	public static boolean writeFile(String path, byte[] bytes){
		File file = new File(path);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static byte[] readFile(String path){
		StringBuffer sb = new StringBuffer();
		File file = new File(path);
		int len = -1;
		byte[] bytes = new byte[1024];
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			while((len=(bis.read(bytes)))!=-1){
				sb.append(bytes.toString(), 0, len);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return sb.toString().getBytes();
	}
}
