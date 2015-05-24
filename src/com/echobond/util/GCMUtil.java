package com.echobond.util;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Properties;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.echobond.entity.RawHttpRequest;
import com.echobond.entity.RawHttpResponse;

/**
 * 
 * @author Luck
 *
 */
public class GCMUtil {
	private static class GCMUtilHolder{
		public static GCMUtil INSTANCE = new GCMUtil(); 
	}
	public static GCMUtil getInstance(){
		return GCMUtilHolder.INSTANCE;
	}
	
	private Properties gcmProperties;
	
	public RawHttpResponse sendToDevice(String regId){
		//headers
		HashMap<String, String> headers = new HashMap<String, String>();
		String url = gcmProperties.getProperty("Auth-Server");
		String auth = gcmProperties.getProperty("Authorization");
		String contType = gcmProperties.getProperty("Content-Type");
		headers.put("Authorization", auth);
		headers.put("Content-Type", contType);
		//JSON request body
		JSONObject reqBody = new JSONObject();
		JSONObject data = new JSONObject();
		JSONArray regIds = new JSONArray();
		data.put("score", "5x1");
		data.put("time", "15:10");
		//regIds.add("APA91bFbVmWeNSV-Wp4nfWz6c2xqJskeM3wJNkREk2DOKyDCD2AzxuFNlE1oSm6dbUujIEpdc31gFeA6RBWTh7IsZHVYXTFC4FZVHU8xX65PPOqtbPHsDnIMc6hKXkSTKkmr82QkTRMuAwTsHgWZ7TbwolWwsXS5ww");
		regIds.add(regId);
		reqBody.put("registration_ids", regIds);
		reqBody.put("data", data);
		RawHttpRequest request = new RawHttpRequest(url, RawHttpRequest.HTTP_METHOD_POST, headers, reqBody);
		RawHttpResponse response = null;
		try {
			response = new HTTPUtil().send(request);
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		return response;
	}

	public Properties getGcmProperties() {
		return gcmProperties;
	}

	public void setGcmProperties(Properties gcmProperties) {
		this.gcmProperties = gcmProperties;
	}
	
}
