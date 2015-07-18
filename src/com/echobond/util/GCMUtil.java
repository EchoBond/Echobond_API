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
import com.echobond.entity.UserMsg;

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
		//regIdList.add("APA91bG2OAkEurJD5gefktYV1X7D_fxU2QjBrR1NW4g32SPxxenOSIeU5yKj1gvSCIb7Fg0R3udNfJ1OGSCQ17ptn6nkYEv30ecKrl5bEEYW-zxe-ipdZgAdcC-rmX5-NQ6zp1RmGEfa");
		//regIdList.add("APA91bGLJtPT7ox9CqOObUFeddoQa_KQEiPSZZih0nPsKDwps53K3cq1i6tbiPaN-k5l-wFZuG865oKD4BB8kz87tAcncvcn___dcjg3OI3EUtOKHTaZgiGvHWLqIDQh7kzCkOLdbcDB");
		regIdList.add("eMItJvVihCc:APA91bFUCuWrXyZmMmGLYVcQzQZjwsEBg_3OBuulC6OTLtbU9Wwg2a12q39_YEFDan8Dw7MrjMnmw3VQ4DOBfkBoBJgYpwqc8RXEr6Ia6p-OrTQ8urd7kYkgT59qzyJaH8CsdBgTyAdU");
		JSONObject data = new JSONObject();
		getInstance().gcmProperties = new Properties();
		//getInstance().gcmProperties.setProperty("Auth-Server", "https://android.googleapis.com/gcm/send");
		getInstance().gcmProperties.setProperty("Auth-Server", "https://gcm-http.googleapis.com/gcm/send");		
		getInstance().gcmProperties.setProperty("Authorization", "key=AIzaSyDr8FGg4tuPjGdeJhPosuO5KL6rti9N0pE");
		getInstance().gcmProperties.setProperty("Content-Type", "application/json");
		UserMsg msg = new UserMsg();
		msg.setId(666);
		msg.setContent("666");
		msg.setUserName("123");
		msg.setSenderId("1423913904795");
		msg.setRecverId("1423912193061");
		msg.setTime("2000-00-00");
		data.put("msg", msg);
		RawHttpResponse response = getInstance().sendToDevices(regIdList, data);
		System.out.println(response.getMsg());
	}
}
