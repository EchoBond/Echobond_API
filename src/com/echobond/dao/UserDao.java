package com.echobond.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.RawHttpResponse;
import com.echobond.entity.ResultResource;
import com.echobond.entity.User;
import com.echobond.entity.UserMsg;
import com.echobond.util.DBUtil;
import com.echobond.util.DateUtil;
import com.echobond.util.GCMUtil;

public class UserDao {
	private Properties sqlProperties;
	private Logger log = LogManager.getLogger("User");

	public JSONObject loadUserMsg(JSONObject request){
		log.debug("Loading user messages.");
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		int limit = request.getInt("limit");
		int offset = request.getInt("offset");
		ArrayList<UserMsg> msgList = new ArrayList<UserMsg>();
		JSONObject guestJSON = request.getJSONObject("guest");
		if(null == guestJSON || guestJSON.isNullObject()){
			ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserMsgByUserId"), new Object[]{
				user.getId(), user.getId(), offset, limit});
			try {
				while(rr.getRs().next()){
					UserMsg msg = new UserMsg();
					msg.loadUserMsgProperties(rr.getRs());
					msgList.add(msg);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
		} else {
			User guest = (User) JSONObject.toBean(guestJSON, User.class);
			ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserMsgByUserIdAndGuestId"), new Object[]{
				user.getId(),guest.getId(), guest.getId(), user.getId(), offset, limit});			
			try {
				while(rr.getRs().next()){
					UserMsg msg = new UserMsg();
					msg.loadUserMsgProperties(rr.getRs());
					msgList.add(msg);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
		}
		result.put("msgList", msgList);
		log.debug("Loading user messages processed.");
		return result;
	}
	
	public JSONObject addUserMsg(JSONObject request){
		log.debug("Adding user messages.");
		JSONObject result = new JSONObject();
		int id = 0;
		String time = null, regId = null, senderName = null, recverName = null;
		ArrayList<String> ids = new ArrayList<String>();
		JSONObject data = new JSONObject();
		UserMsg msg = (UserMsg) JSONObject.toBean(request.getJSONObject("msg"), UserMsg.class);
		/* insert in server */
		ResultResource rr = new ResultResource();
		time = DateUtil.dateToString(new Date(), null);
		DBUtil.getInstance().update(sqlProperties.getProperty("addUserMsg"), rr, new Object[]{
			msg.getSenderId(), msg.getRecverId(), time, msg.getContent()
		});
		DBUtil.getInstance().query(sqlProperties.getProperty("loadInsertId"), rr, new Object[]{});
		try{
			rr.getRs().next();
			id = rr.getRs().getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		/* query the sender's username */
		rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserById"), 
				new Object[]{msg.getSenderId()});
		try {
			rr.getRs().next();
			senderName = rr.getRs().getString("username");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		/* query the recipient's reg id */
		rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadGCMRegByUserId"), 
				new Object[]{msg.getRecverId()});
		try {
			rr.getRs().next();
			regId = rr.getRs().getString("reg_id");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		/* set the GCM recipient reg ID */
		ids.add(regId);
		/* send the message */
		msg.setId(id);
		msg.setTime(time);
		msg.setUserName(senderName);
		try {
			msg.setContent(URLEncoder.encode(msg.getContent(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		data.put("msg", JSONObject.fromObject(msg));
		RawHttpResponse response = GCMUtil.getInstance().sendToDevices(ids, data);
		/* query the recipient's username */
		rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserById"), 
				new Object[]{msg.getRecverId()});
		try {
			rr.getRs().next();
			recverName = rr.getRs().getString("username");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}				
		/* send result back to sender */
		msg.setUserName(recverName);
		if(null == response || 200 != response.getCode()){
			result.put("success", "0");
		} else {
			result.put("success", "1");
			result.put("msg", JSONObject.fromObject(msg));
		}
		log.debug("Adding user messages processed.");
		return result;
	}
	
	public JSONObject ackUserMsg(JSONObject request){
		log.debug("Acking user message.");
		JSONObject result = new JSONObject();
		
		log.debug("Acking user message processed.");
		return result;
	}
	
	public JSONObject loadUser(JSONObject request){
		log.debug("Loading user.");
		JSONObject result = new JSONObject();
		int action = request.getInt("action");
		User user;
		ResultResource rr;
		switch(action){
		//load by id
		case 1:
			user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
			rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserById"), new Object[]{user.getId()});
			try {
				rr.getRs().next();
				user.loadUserProperties(rr.getRs());
				result.put("user", user);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
			break;
		//load by email
		case 2:
			user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
			rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{user.getEmail()});
			try {
				rr.getRs().next();
				user.loadUserProperties(rr.getRs());
				result.put("user", user);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
			break;
		//TODO load by conditions
		case 3:break;
		//update
		case 4:
			user = (User) JSONObject.toBean((JSONObject) request.get("user"));	
			//TODO
			break;
		default:break;
		}
		log.debug("Loading user processed.");
		return result;
	}
	
	public JSONObject updateUserExt(JSONObject request){
		log.debug("Updating user.");
		JSONObject result = new JSONObject();
		//TODO
		DBUtil.getInstance().update(sqlProperties.getProperty("updateUserExt"), new Object[]{});
		log.debug("Updating user processed.");
		return result;
	}
	
	public Properties getSqlProperties() {
		return sqlProperties;
	}

	public void setSqlProperties(Properties sqlProperties) {
		this.sqlProperties = sqlProperties;
	}
	
}
