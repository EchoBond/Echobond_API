package com.echobond.dao;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.util.FileUtil;

import net.sf.json.JSONObject;

/**
 * manipulate image data
 * @author Luck
 *
 */
public class ImageDAO {
	private Properties sqlProperties;
	private ServletContext ctx;
	private String localPath;
	private Logger log = LogManager.getLogger("Image");

	/**
	 * Base64 string->decoded string->image file
	 * @param req
	 * @return
	 */
	public JSONObject uploadImage(JSONObject req){
		log.debug("Uploading image.");
		JSONObject result = new JSONObject();
		/* Parsing path to store */
		String path = req.getString("path");
		/* Parsing image data */
		String dataStr = req.getString("data");
		/* Base64 decoding */
		byte[] data = Base64.decodeBase64(dataStr.getBytes());
		/* Write file */
		boolean r = FileUtil.writeFile(localPath+path, data);
		if(r)
			result.put("result", "1");
		else result.put("result", "0");
		log.debug("Image uploading processed.");
		return result;
	}
	
	/**
	 * get image from server
	 * @param req
	 * @return image
	 */
	public byte[] downloadImage(JSONObject req){
		log.debug("Downloading image.");
		/* Parsing path to read */
		String path = req.getString("path");
		/* get the bytes */
		byte[] bytes = FileUtil.readFile(localPath+path);
		if(null == bytes){
			log.error("Image not found!");
			bytes = ("Image " + path + " not found!").getBytes();
		}
		log.debug("Image downloading processed.");
		return bytes;
	}
	
	public Properties getSqlProperties() {
		return sqlProperties;
	}

	public void setSqlProperties(Properties sqlProperties) {
		this.sqlProperties = sqlProperties;
	}

	public ServletContext getCtx() {
		return ctx;
	}

	public void setCtx(ServletContext ctx) {
		this.ctx = ctx;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	
	
}
