package com.echobond.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.Comment;
import com.echobond.entity.ResultResource;
import com.echobond.entity.Tag;
import com.echobond.entity.Thought;
import com.echobond.entity.User;
import com.echobond.util.DBUtil;
import com.echobond.util.DateUtil;

import net.sf.json.JSONObject;

/**
 * 
 * manipulate thought tables 
 * @author Luck
 *
 */
public class ThoughtDAO {
	private final static int HOT_THOUGHT = 0;
	private final static int HOME_THOUGHT=1;
	private Properties sqlProperties;
	private Logger log = LogManager.getLogger("Thought");
	
	/**
	 * comment a thought
	 * @param thought
	 * @param user
	 * @return id, time
	 */
	public JSONObject commentThought(Comment comment){
		log.debug("Commenting thought.");
		JSONObject result = new JSONObject();
		ResultResource rr = new ResultResource();
		String time = DateUtil.dateToString(new Date(), null);
		DBUtil.getInstance().update(sqlProperties.getProperty("commentThought"), rr, new Object[]{comment.getUserId(), comment.getThoughtId(), comment.getReplyTo(), comment.getContent(), time});
		DBUtil.getInstance().query(sqlProperties.getProperty("loadInsertId"), rr, new Object[]{});
		int id = 0;
		try{
			rr.getRs().next();
			id = rr.getRs().getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserById"), new Object[]{comment.getUserId()});
		try {
			if(rr.getRs().next()){
				String userName = rr.getRs().getString("username");
				comment.setUserName(userName);
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		comment.setId(id);
		comment.setTime(time);
		JSONObject cmt = JSONObject.fromObject(comment);
		result.put("comment", cmt);
		log.debug("Commenting thought processed.");
		return result;
	}
	
	/**
	 * boost or unboost a thought
	 * @param t
	 * @param u
	 * @return result
	 */
	public JSONObject boostThought(Thought thought, User user){
		log.debug("Boosting thought.");
		JSONObject result = new JSONObject();
		//new boost or reboost: 1, otherwise 0
		int boostValue = 1, boostIncrease, totalBoost = 0;
		boolean newBoost = false;
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadThoughtBoostByUserId"), new Object[]{thought.getId(), user.getId()});
		try {
			if(rr.getRs().next()){
				if(1 == rr.getRs().getInt("boost")){
					boostValue = 0;
				}
			} else {
				newBoost = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		if(newBoost){
			DBUtil.getInstance().update(sqlProperties.getProperty("userNewBoostThought"), new Object[]{thought.getId(), user.getId(), DateUtil.dateToString(new Date(), null)});			
		} else {
			DBUtil.getInstance().update(sqlProperties.getProperty("userBoostThought"), new Object[]{boostValue, DateUtil.dateToString(new Date(), null), thought.getId(), user.getId()});
		}
		if(boostValue == 0)
			boostIncrease = -1;
		else boostIncrease = 1;
		DBUtil.getInstance().update(sqlProperties.getProperty("boostThought"), new Object[]{boostIncrease, thought.getId()});
		rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadThoughtBoostByThoughtId"), new Object[]{thought.getId()});
		try {
			rr.getRs().next();
			totalBoost = rr.getRs().getInt("boost");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		result.put("action", boostValue);
		result.put("id", thought.getId());
		result.put("total", totalBoost);
		log.debug("Boosting thought processed.");
		return result;
	}
	
	/**
	 * save a thought
	 * @param t
	 * @return thought
	 */
	public JSONObject postThought(Thought t){
		log.debug("Saving thought post.");
		JSONObject result = new JSONObject();
		Object[] params = new Object[]{t.getUserId(), t.getLangId(), t.getCategoryId(), t.getGroupId(), t.getContent(), t.getImage(), DateUtil.dateToString(new Date(),null)};
		ResultResource rr = new ResultResource();
		DBUtil.getInstance().update(sqlProperties.getProperty("addNewThought"),rr, params);
		DBUtil.getInstance().query(sqlProperties.getProperty("loadInsertId"), rr, new Object[]{});
		int id = 0;
		try {
			rr.getRs().next();
			id = rr.getRs().getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
			t.setId(id);
		}
		tagThought(t);
		result.put("thought", t);
		log.debug("Thought posting processed.");
		return result;
	}
	
	/**
	 * load thought list
	 * @param request
	 * @return thought list
	 */
	@SuppressWarnings("unchecked")
	public JSONObject loadThought(JSONObject request){
		log.debug("Loading thoughts.");
		JSONObject result = new JSONObject();
		int type = request.getInt("type");
		int offset = request.getInt("offset");
		int limit = request.getInt("limit");
		ArrayList<Thought> thoughts = new ArrayList<Thought>();
		ResultResource rr = null;
		JSONObject user = request.getJSONObject("user");
		switch (type) {
		case HOT_THOUGHT:
			rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadHotThoughts"), new Object[]{offset,limit});
			try {
				while(rr.getRs().next()){
					Thought t = new Thought();
					t.loadThoughtProperties(rr.getRs());
					ResultResource rrComment = DBUtil.getInstance().query(sqlProperties.getProperty("loadCommentsByThoughtId"), new Object[]{t.getId(),0,limit});
					t.loadComments(rrComment);
					ResultResource rrUBoost = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserBoostByUserIdAndThoughtId"), new Object[]{user.getString("id"),t.getId()});
					t.loadUserBoost(rrUBoost);
					thoughts.add(t);
					rrComment.close();
					rrUBoost.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
			result.put("thoughts", thoughts);
			break;
		case HOME_THOUGHT:
			ArrayList<Thought> thoughts2 = new ArrayList<Thought>();
			ArrayList<Thought> thoughts3 = new ArrayList<Thought>();
			rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadThoughtsByTags"), new Object[]{user.getString("id"),offset,limit});
			try {
				while(rr.getRs().next()){
					Thought t = new Thought();
					t.loadThoughtProperties(rr.getRs());
					ResultResource rrComment = DBUtil.getInstance().query(sqlProperties.getProperty("loadCommentsByThoughtId"), new Object[]{t.getId(),0,limit});
					t.loadComments(rrComment);
					ResultResource rrUBoost = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserBoostByUserIdAndThoughtId"), new Object[]{user.getString("id"),t.getId()});
					t.loadUserBoost(rrUBoost);
					thoughts.add(t);
					rrComment.close();
					rrUBoost.close();
				}
			
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
			rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadThoughtsByGroups"), new Object[]{user.getString("id"),offset,limit});
			try {
				while(rr.getRs().next()){
					Thought t = new Thought();
					t.loadThoughtProperties(rr.getRs());
					thoughts2.add(t);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
			rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadThoughtsByUserId"), new Object[]{user.getString("id"),offset,limit});
			try {
				while(rr.getRs().next()){
					Thought t = new Thought();
					t.loadThoughtProperties(rr.getRs());
					ResultResource rrComment = DBUtil.getInstance().query(sqlProperties.getProperty("loadCommentsByThoughtId"), new Object[]{t.getId(),0,limit});
					t.loadComments(rrComment);
					ResultResource rrUBoost = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserBoostByUserIdAndThoughtId"), new Object[]{user.getString("id"),t.getId()});
					t.loadUserBoost(rrUBoost);					
					thoughts.add(t);
					rrComment.close();
					rrUBoost.close();
				}
			
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				rr.close();
			}
			ArrayList<Thought> list = sortThoughtsByTime(thoughts, thoughts2, thoughts3);
			if(list.size() > limit)
				result.put("thoughts",list.subList(0, limit));
			else result.put("thoughts", list);			
			break;
		default:
			break;
		}
		log.debug("Loading thoughts processed");
		return result;
	}
	
	/**
	 * tag a thought
	 * @param t
	 */
	private void tagThought(Thought t){
		log.debug("Tagging for thought.");
		if(null != t.getTags() && t.getTags().size() > 0){
			try{
				//get updated tags
				ArrayList<Tag> tags = updateTags(t.getTags());
				t.setTags(tags);
				if(0 != t.getId()){
					//update user_comment_thought
					for (Tag tag: tags){
						DBUtil.getInstance().update(sqlProperties.getProperty("AddNewTagToThought"), new Object[]{t.getUserId(), t.getId(), tag.getId()});
					}
				}
			} catch (SQLException e){
				e.printStackTrace();
			}
		}
		log.debug("Tagging thought processed.");
	}
	
	/**
	 * add tags if new
	 * @param tags
	 * @return
	 * @throws SQLException
	 */
	private ArrayList<Tag> updateTags(ArrayList<Tag> tags) throws SQLException{
		for (Tag tag : tags) {
			ResultResource rr = new ResultResource();
			DBUtil.getInstance().update(sqlProperties.getProperty("addNewTag"),rr, new Object[]{tag.getName()});
			DBUtil.getInstance().query(sqlProperties.getProperty("loadInsertId"),rr, new Object[]{});
			if(rr.getRs().next()){
				tag.setId(rr.getRs().getInt("id"));
			}
			rr.close();
		}
		return tags;
	}
	
	public Properties getSqlProperties() {
		return sqlProperties;
	}

	public void setSqlProperties(Properties sqlProperties) {
		this.sqlProperties = sqlProperties;
	}
	
	private ArrayList<Thought> sortThoughtsByTime(ArrayList<Thought> ...thoughtLists) {
		ArrayList<Thought> list = new ArrayList<Thought>();
		Set<Integer> idSet = new HashSet<Integer>();
		for (ArrayList<Thought> thoughtList : thoughtLists) {
			for (Thought thought : thoughtList) {
				//avoid duplication
				if(!idSet.contains(thought.getId())){
					idSet.add(thought.getId());
					list.add(thought);
				}
			}
		}
		Comparator<Thought> comparator = new Comparator<Thought>() {
			@Override
			public int compare(Thought o1, Thought o2) {
				return o1.getId() - o2.getId();
			}
		};
		Collections.sort(list, comparator);
		return list;
	}
	
}
