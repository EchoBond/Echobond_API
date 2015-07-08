package com.echobond.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.Group;
import com.echobond.entity.RawHttpResponse;
import com.echobond.entity.ResultResource;
import com.echobond.entity.Tag;
import com.echobond.entity.User;
import com.echobond.entity.UserMsg;
import com.echobond.util.DBUtil;
import com.echobond.util.DateUtil;
import com.echobond.util.GCMUtil;

public class UserDao {
	private Properties sqlProperties;
	private Logger log = LogManager.getLogger("User");
	
	private final static int USER_LOAD_BY_ID = 1;
	private final static int USER_LOAD_BY_EMAIL = 2;
	private final static int USER_LOAD_BY_USERNAME = 3;
	private final static int USER_LOAD_BY_CONDITIONS = 4;
	private final static int USER_UPDATE = 5;
	
	
	public JSONObject loadUserMsg(JSONObject request){
		log.debug("Loading user messages.");
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		int limit = request.getInt("limit");
		int offset = request.getInt("offset");
		ArrayList<UserMsg> msgList = new ArrayList<UserMsg>();
		String userName = "";
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
			ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserById"), new Object[]{guest.getId()});
			try{
				if(rr.getRs().next()){
					userName = rr.getRs().getString("username");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
			rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserMsgByUserIdAndGuestId"), new Object[]{
				user.getId(),guest.getId(), guest.getId(), user.getId(), offset, limit});			
			try {
				while(rr.getRs().next()){
					UserMsg msg = new UserMsg();
					msg.loadUserMsgProperties(rr.getRs());
					msg.setUserName(userName);
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
		String regId = null, senderName = null, recverName = null;
		ArrayList<String> ids = new ArrayList<String>();
		JSONObject data = new JSONObject();
		UserMsg msg = (UserMsg) JSONObject.toBean(request.getJSONObject("msg"), UserMsg.class);
		/* insert in server */
		ResultResource rr = new ResultResource();
		msg = storeMsg(msg);
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
		JSONObject msgJSON = request.getJSONObject("msg"), userJSON = request.getJSONObject("user");
		if(null != msgJSON && !msgJSON.isNullObject() && !msgJSON.isEmpty()){
			UserMsg msg = (UserMsg) JSONObject.toBean(request.getJSONObject("msg"), UserMsg.class);
			DBUtil.getInstance().update(sqlProperties.getProperty("ackUserMsgById"), new Object[]{msg.getId()});
			result.put("sucess", "1");
		}
		if(null != userJSON && !userJSON.isNullObject() && !userJSON.isEmpty()){
			User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
			DBUtil.getInstance().update(sqlProperties.getProperty("ackUserMsgByUserId"), new Object[]{user.getId()});
			result.put("sucess", "1");			
		}
		log.debug("Acking user message processed.");
		return result;
	}
	
	public JSONObject fetchUnreadMsg(JSONObject request){
		JSONObject result = new JSONObject();
		JSONObject countJSON = countUnreadUserMsg(request);
		if(0 != countJSON.getInt("userCount")){
			result.put("count", countJSON);
			ArrayList<UserMsg> msgs = loadUnreadUserMsg(request);
			result.put("msgs", msgs);
		}
		return result;
	}
	
	public JSONObject countUnreadUserMsg(JSONObject requset){
		int userCount = 0, msgCount = 0;
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(requset.getJSONObject("user"), User.class);
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUnreadUserMsgCount"), 
				new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				userCount++;
				msgCount += rr.getRs().getInt("msgCount");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		result.put("userCount", userCount);
		result.put("msgCount", msgCount);
		return result;
	}
	
	public ArrayList<UserMsg> loadUnreadUserMsg(JSONObject request){
		ArrayList<UserMsg> msgs = new ArrayList<UserMsg>();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUnreadUserMsg"), 
				new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				UserMsg msg = new UserMsg();
				msg.loadUserMsgProperties(rr.getRs());
				msgs.add(msg);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		return msgs;
	}
	
	public UserMsg storeMsg(UserMsg msg){
		int id = 0;
		ResultResource rr = new ResultResource();
		String time = DateUtil.dateToString(new Date(), null);
		msg.setTime(time);
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
		msg.setId(id);
		return msg;
	}
	
	public JSONObject loadUser(JSONObject request){
		log.debug("Loading user.");
		JSONObject result = new JSONObject();
		int action = request.getInt("action");
		switch(action){
		case USER_LOAD_BY_ID:
			result.put("user", loadUserById(request));
			result.put("userMeta", loadUserMeta(request));
			break;
		case USER_LOAD_BY_EMAIL:
			result.put("user", loadUserByEmail(request));
			result.put("userMeta", loadUserMeta(request));
			break;
		case USER_LOAD_BY_USERNAME:
			result.put("user", loadUserByUserName(request));
			result.put("userMeta", loadUserMeta(request));
			break;
		case USER_LOAD_BY_CONDITIONS:
			result = searchPeople(request);
			break;
		case USER_UPDATE:
			result = updateUser(request);
			break;
		default:break;
		}
		log.debug("Loading user processed.");
		return result;
	}
	
	public User loadUserById(JSONObject request){
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserById"), 
				new Object[]{user.getId()});
		try {
			if(rr.getRs().next()){
				user.loadUserProperties(rr.getRs());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		return user; 
	}
	
	public User loadUserByEmail(JSONObject request){
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), 
				new Object[]{user.getEmail()});
		try {
			if(rr.getRs().next()){
				user.loadUserProperties(rr.getRs());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		return user;
	}
	
	public User loadUserByUserName(JSONObject request){
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByUserName"), 
				new Object[]{user.getUserName()});
		try {
			if(rr.getRs().next()){
				user.loadUserProperties(rr.getRs());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		return user;
	}
	
	public JSONObject searchPeople(JSONObject request){
		JSONObject result = new JSONObject();
		JSONObject condition = request.getJSONObject("condition");
		String key = condition.getString("key");
		Integer offset = request.getInt("offset");
		Integer limit = request.getInt("limit");
		ResultResource rr = null;
		ArrayList<User> users = new ArrayList<User>();
		JSONArray metaArray = new JSONArray();
		if(null != key){
			if(key.equals("g")){
				Integer gId = condition.getInt("id");
				rr = DBUtil.getInstance().query(sqlProperties.getProperty("searchPplByGroupId"), 
					new Object[]{gId, offset,limit});
			} else if(key.equals("t")){
				JSONArray idList = condition.getJSONArray("idList");
				Object[] params = new Object[12];
				for(int i=0;i<10;i++){
					if(idList.size() <= i){
						params[i] = 0;
					} else {
						params[i] = idList.get(i);
					}
				}
				params[10] = offset;
				params[11] = limit;
				rr = DBUtil.getInstance().query(sqlProperties.getProperty("searchPplByTagId"), 
					params);
			} else if(key.equals("k")){
				String keyword = condition.getString("keyword");
				String sql = sqlProperties.getProperty("searchPplByKeyword") + "'%" + keyword +
					"%' ORDER BY id DESC LIMIT ?, ?";
				rr = DBUtil.getInstance().query(sql,new Object[]{offset,limit});
			}
			try {
				while(rr.getRs().next()){
					User user = new User();
					user.loadUserProperties(rr.getRs());
					users.add(user);
					JSONObject metaRequest = new JSONObject();
					metaRequest.put("user", user);
					metaArray.add(loadUserMeta(metaRequest));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
		}
		result.put("users", users);
		result.put("userMeta", metaArray);
		return result;
	}
	
	public JSONObject loadUserMeta(JSONObject request){
		JSONObject result = new JSONObject();
		ArrayList<Tag> likedTags = new ArrayList<Tag>();
		ArrayList<Tag> selfTags = new ArrayList<Tag>();
		ArrayList<Group> followedGroups = new ArrayList<Group>();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserLikedTags"), 
				new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				Tag t = new Tag();
				t.loadTagProperties(rr.getRs());
				likedTags.add(t);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserFollowedGroups"), 
				new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				Group g = new Group();
				g.loadGroupProperties(rr.getRs());
				followedGroups.add(g);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}		
		rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserSelfTags"), 
				new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				Tag t = new Tag();
				t.loadTagProperties(rr.getRs());
				selfTags.add(t);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		result.put("likedTags", likedTags);
		result.put("followedGroups", followedGroups);
		result.put("selfTags", selfTags);
		return result;
	}
	
	public JSONObject updateUser(JSONObject request){
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean((JSONObject) request.get("user"), User.class);
		DBUtil.getInstance().update(sqlProperties.getProperty("updateUser"), new Object[]{
			user.getUserName(), user.getTimeZone(), user.getGender(),
			user.getAge(), user.getBirthday(), user.getCountryId(), user.getHomeId(), user.getBio(),
			user.getSthInteresting(), user.getAmzExp(), user.getToDo(), user.getPhilosophy(), 
			user.getFriendsDesc(), user.getInterest(), user.getLittleSecret(), user.getLangId(),
			user.getLocale(), user.getId()});
		result.put("user", user);
		return result;
	}
	
	public JSONObject tagSelf(JSONObject request){
		log.debug("Tagging self.");
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		Integer[] tags = (Integer[]) JSONArray.toArray(request.getJSONArray("tagIds"), Integer.class);
		for (Integer id : tags) {
			DBUtil.getInstance().update(sqlProperties.getProperty("tagSelf"), new Object[]{user.getId(),
				id, 1});
		}
		result.put("success", "1");
		log.debug("Tagging self processed.");
		return result;
	}
	
	public JSONObject untagSelfAll(JSONObject request){
		log.debug("Untagging self.");
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		DBUtil.getInstance().update(sqlProperties.getProperty("untagSelfAll"), new Object[]{user.getId()});
		result.put("success", "1");
		log.debug("Untagging self processed.");
		return result;
	}
	
	public JSONObject followGroups(JSONObject request){
		log.debug("Following groups.");
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		Integer[] groups = (Integer[]) JSONArray.toArray(request.getJSONArray("groupIds"), Integer.class);
		for (Integer id : groups) {
			DBUtil.getInstance().update(sqlProperties.getProperty("followGroups"), new Object[]{user.getId(),
				id, 1});
		}
		result.put("success", "1");
		log.debug("Following groups processed.");
		return result;
	}
	
	public JSONObject unfollowAllGroups(JSONObject request){
		log.debug("Unfollowing groups.");
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		DBUtil.getInstance().update(sqlProperties.getProperty("unfollowAllGroups"), new Object[]{user.getId()});
		result.put("success", "1");
		log.debug("Unfollowing groups processed.");
		return result;
	}
	
	public JSONObject followTags(JSONObject request){
		log.debug("Following tags.");
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		Integer[] tags = (Integer[]) JSONArray.toArray(request.getJSONArray("tagIds"), Integer.class);
		for (Integer id : tags) {
			DBUtil.getInstance().update(sqlProperties.getProperty("followTags"), new Object[]{user.getId(),
				id, 1});
		}
		result.put("success", "1");
		log.debug("Following tags processed.");
		return result;
	}	
	
	public JSONObject unfollowAllTags(JSONObject request){
		log.debug("Unfollowing tags.");
		JSONObject result = new JSONObject();
		User user = (User) JSONObject.toBean(request.getJSONObject("user"), User.class);
		DBUtil.getInstance().update(sqlProperties.getProperty("unfollowAllTags"), new Object[]{user.getId()});
		result.put("success", "1");
		log.debug("Unfollowing tags processed.");
		return result;
	}
	
	public Properties getSqlProperties() {
		return sqlProperties;
	}

	public void setSqlProperties(Properties sqlProperties) {
		this.sqlProperties = sqlProperties;
	}
	
}
