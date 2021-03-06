package com.echobond.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author Luck
 *
 */
public class FileUtil {
	private static final String DEFAULT_AVATAR = "C:/images/default_avatar.jpg";
	private static final String NO_IMAGE = "C:/images/no_image.jpg";
	/**
	 * write image, decoding
	 * @param path
	 * @param bytes
	 * @return result
	 */
	public static boolean writeFile(String path, byte[] data){		
		File file = new File(path);
		try {
			FileImageOutputStream fos = new FileImageOutputStream(file);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static byte[] readFile(String path) {
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		int data = -1;

			try {
				fis = new FileInputStream(new File(path));
			} catch (FileNotFoundException e) {
				try {
						if(path.contains("_")){
							fis = new FileInputStream(new File(NO_IMAGE));
						} else {
							fis = new FileInputStream(new File(DEFAULT_AVATAR));
						}
				} catch (FileNotFoundException e1) {
						e1.printStackTrace();
				}
			}
			try {
				bos = new ByteArrayOutputStream();
				while((data=(fis.read()))!=-1){
					bos.write(data);
				}
				bos.close();
				fis.close();				
			} catch (IOException e) {
				e.printStackTrace();
			}
		return bos.toByteArray();
	}
	
	public static byte[] decode(byte[] data){
		return Base64.decodeBase64(data);
	}
	public static byte[] encode(byte[] data){
		//make sure it is url safe
		return Base64.encodeBase64(data, false, true);
	}
	public static void main(String[] args) {
		byte[] org = readFile("E:/1.png");
		System.out.println(org.length);
		byte[] enc = Base64.encodeBase64(org, false, true);
		writeFile("E:/1.txt", enc);
		System.out.println(enc.length);
		byte[] dec = Base64.decodeBase64(enc);
		System.out.println(dec.length);
		writeFile("E:/eow.png", dec);
	}
}
