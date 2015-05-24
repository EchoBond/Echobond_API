package com.echobond.dao;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.MyImage;
import com.echobond.util.FileUtil;
import com.echobond.util.StringUtil;

import net.sf.json.JSONObject;

public class ImageDAO {
	private Properties sqlProperties;
	private ServletContext ctx;
	private Logger log = LogManager.getLogger("Image");

	public JSONObject uploadImage(JSONObject req){
		log.debug("Uploading image.");
		JSONObject result = new JSONObject();
		/* Parsing path to store */
		String type = req.getString("type");
		String path = type;
		/* store the string */
		String imgStr = req.getString("image");
//		MyImage image = new MyImage();
		log.debug("Image uploading processed.");
		return result;
	}
	
	public JSONObject downloadImage(JSONObject req){
		log.debug("Downloading image.");
		JSONObject result = new JSONObject();
		/* Parsing path to read */
		String type = req.getString("type");
		String path = type;
		/* get the string */
		byte[] bytes = FileUtil.readFile(path);
		log.debug("Image downloading processed.");
		return result;
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
	
	
}
