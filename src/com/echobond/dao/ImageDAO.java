package com.echobond.dao;

import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.ResultResource;
import com.echobond.util.DBUtil;
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
	private static final String IMAGE_FILE_SUFFIX = ".jpg";

	/**
	 * Base64 string->decoded string->image file
	 * @param req
	 * @return
	 */
	public JSONObject uploadImage(JSONObject req){
		log.debug("Uploading image.");
		JSONObject result = new JSONObject();
		/* Parsing image data */
		String dataStr = req.getString("data");
		/* Base64 decoding */
		byte[] data = Base64.decodeBase64(dataStr.getBytes());
		/* verify the uploader */
		String userId = req.getString("userId");
		String email = req.getString("email");
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByUserIdAndEmail"), new Object[]{userId, email});
		try {
			if(!rr.getRs().next()){
				result.put("result", 0);
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result.put("result", 0);
			return result;
		} finally {
			rr.close();
		}
		/* Write file */
		String path = userId + "_" + System.currentTimeMillis();
		boolean r = FileUtil.writeFile(localPath + path + IMAGE_FILE_SUFFIX, data);
		if(r){
			result.put("result", "1");
			result.put("path", path);
		}
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
		String path = req.getString("path") + ".jpg";
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
