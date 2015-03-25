package com.echobond.dao;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.MyImage;
import com.echobond.util.StringUtil;

import net.sf.json.JSONObject;

public class ImageDAO {
	private Properties sqlProperties;
	private Logger log = LogManager.getLogger("Image");

	public JSONObject uploadImage(JSONObject req){
		log.debug("Uploading image.");
		JSONObject result = new JSONObject();
		MyImage image = new MyImage();
		log.debug("Image uploading processed.");
		return result;
	}
	
	public Properties getSqlProperties() {
		return sqlProperties;
	}

	public void setSqlProperties(Properties sqlProperties) {
		this.sqlProperties = sqlProperties;
	}
	
}
