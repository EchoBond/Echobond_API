package com.echobond.util;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
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
	
	public RawHttpResponse sendToDevices(ArrayList<String> regIdList, JSONObject data){
		//headers
		HashMap<String, String> headers = new HashMap<String, String>();
		String url = gcmProperties.getProperty("Auth-Server");
		String auth = gcmProperties.getProperty("Authorization");
		String contType = gcmProperties.getProperty("Content-Type");
		headers.put("Authorization", auth);
		headers.put("Content-Type", contType);
		//JSON request body
		JSONObject reqBody = new JSONObject();
		JSONObject dataWrap = new JSONObject();
		JSONArray regIds = new JSONArray();
		//regId
		for(String regId: regIdList){
			regIds.add(regId);
		}
		reqBody.put("registration_ids", regIds);
		dataWrap.put("data", data);
		reqBody.put("data", dataWrap);
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
	public static void main(String[] args) {
		ArrayList<String> regIdList = new ArrayList<String>();
		regIdList.add("APA91bESvNoN0rNhPDx7MUkMVDmwWMD050pCzqr-ndg9wpU1I4ib7EUayVSgCAFOj0Xl0JjMy_xiDp6Fk1dqCb8qV37Yw7xBhyW-sxe_P94qZdIN6rehjpaBZNxRUvChw8FZA3tTk7Jf");
		JSONObject data = new JSONObject();
		getInstance().gcmProperties = new Properties();
		getInstance().gcmProperties.setProperty("Auth-Server", "https://android.googleapis.com/gcm/send");
		getInstance().gcmProperties.setProperty("Authorization", "key=AIzaSyAO4pcWMUq2xukdGphSdrht3H3fWKDP5u4");
		getInstance().gcmProperties.setProperty("Content-Type", "application/json");
		data.put("type", "newBoost");
		data.put("thought", "hike");
		data.put("category", "interest");
		RawHttpResponse response = getInstance().sendToDevices(regIdList, data);
		System.out.println(response.getMsg());
	}
}
