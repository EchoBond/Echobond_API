package com.echobond.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Luck
 *
 */
public class DateUtil {
	private static final String defaultPattern = "yyyy-MM-dd HH:mm:ss";
	
	public static Date stringToDate(String src, String pattern){
		SimpleDateFormat sdf;
		if(pattern == null || pattern.length() < 1)
			pattern = defaultPattern;
		sdf = new SimpleDateFormat(pattern);
		Date date = null;
		try {
			date = sdf.parse(src);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	public static String dateToString(Date date, String pattern){
		if(null == pattern || pattern.length() < 1)
			pattern = defaultPattern;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
}
