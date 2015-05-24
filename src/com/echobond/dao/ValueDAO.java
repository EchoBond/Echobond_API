package com.echobond.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.Category;
import com.echobond.entity.Group;
import com.echobond.entity.ResultResource;
import com.echobond.entity.Tag;
import com.echobond.entity.User;
import com.echobond.util.DBUtil;

/**
 * manipulate values including group, category, tags, user tags
 * @author Luck
 *
 */
public class ValueDAO {
	private Properties sqlProperties;
	private Logger log = LogManager.getLogger("Value");
	
	/**
	 * from a list of tags, if existed update, otherwise add new
	 * @param tags
	 * @return result
	 */
	public JSONObject updateTags(ArrayList<Tag> tags){
		log.debug("Updating tags.");
		JSONObject result = new JSONObject();
		int added = 0;
		for (Tag tag : tags) {
			if(null != tag.getName()){
				added += DBUtil.getInstance().update(sqlProperties.getProperty("addNewTag"), new Object[]{tag.getName()});
			}
		}
		result.put("added", added);
		result.put("exists", tags.size() - added);
		log.debug("Tags update processed.");
		return result;
	}
	
	/**
	 * add a new group
	 * @param group
	 * @return result
	 */
	public JSONObject addGroup(Group group){
		log.debug("Adding group.");
		JSONObject result = new JSONObject();
		int added = 0;
		if(null != group.getName()){
			added += DBUtil.getInstance().update(sqlProperties.getProperty("addNewGroup"), new Object[]{group.getName()});
		}
		result.put("added", added);
		log.debug("Group adding processed.");
		return result;
	}

	/**
	 * add a new category
	 * @param category
	 * @return result
	 */
	public JSONObject addCategory(Category category){
		log.debug("Adding group.");
		JSONObject result = new JSONObject();
		int added = 0;
		if(null != category.getName()){
			added += DBUtil.getInstance().update(sqlProperties.getProperty("addNewCategory"), new Object[]{category.getName()});
		}
		result.put("added", added);
		log.debug("Category adding processed.");
		return result;
	}

	/**
	 * load a list of groups followed by a user
	 * @param user
	 * @return group list
	 */
	public JSONObject loadGroupsByUserId(User user){
		log.debug("Loading groups by userId");
		JSONObject result = new JSONObject();
		ArrayList<Group> groups = new ArrayList<Group>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadGroupsByUserId"), new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				Group g = new Group();
				g.setId(rr.getRs().getInt("g.id"));
				g.setName(rr.getRs().getString("g.name"));
				groups.add(g);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading groups by userId.");
		} finally {
			rr.close();
		}
		result.put("groups", groups);
		log.debug("Groups loading by userId processed.");
		return result;
	}

	/**
	 * load all categories
	 * @return category list
	 */
	public JSONObject loadCategories(){
		log.debug("Loading categories");
		JSONObject result = new JSONObject();
		ArrayList<Category> categories = new ArrayList<Category>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadCategories"), new Object[]{});
		try {
			while(rr.getRs().next()){
				Category c = new Category();
				c.setId(rr.getRs().getInt("id"));
				c.setName(rr.getRs().getString("name"));
				categories.add(c);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading categories.");
		} finally {
			rr.close();
		}
		result.put("categories", categories);
		log.debug("Categories loading processed.");
		return result;
	}
	
	/**
	 * load a list of tags followed by a user
	 * @param user
	 * @return tag list
	 */
	public JSONObject loadTagsByUserId(User user){
		log.debug("Loading tags by userId");
		JSONObject result = new JSONObject();
		ArrayList<Tag> tags = new ArrayList<Tag>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadTagsByUserId"), new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				Tag t = new Tag();
				t.setId(rr.getRs().getInt("t.id"));
				t.setName(rr.getRs().getString("t.name"));
				tags.add(t);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading tags by userId.");
		} finally {
			rr.close();
		}
		result.put("tags", tags);
		log.debug("Tags loading by userId processed.");
		return result;
	}
	
	/**
	 * load all tags
	 * @return tag list
	 */
	public JSONObject loadTags(){
		log.debug("Loading tags");
		JSONObject result = new JSONObject();
		ArrayList<Tag> tags = new ArrayList<Tag>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadTags"), new Object[]{});
		try {
			while(rr.getRs().next()){
				Tag t = new Tag();
				t.setId(rr.getRs().getInt("id"));
				t.setName(rr.getRs().getString("name"));
				tags.add(t);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading tags.");
		} finally {
			rr.close();
		}
		result.put("tags", tags);
		log.debug("Tags loading processed.");
		return result;
	}
	
	/**
	 * load all groups
	 * @return group list
	 */
	public JSONObject loadGroups(){
		log.debug("Loading groups");
		JSONObject result = new JSONObject();
		ArrayList<Group> groups = new ArrayList<Group>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadGroups"), new Object[]{});
		try {
			while(rr.getRs().next()){
				Group t = new Group();
				t.setId(rr.getRs().getInt("id"));
				t.setName(rr.getRs().getString("name"));
				groups.add(t);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading groups.");
		} finally {
			rr.close();
		}
		result.put("groups", groups);
		log.debug("Groups loading processed.");
		return result;		
	}
	
	public Properties getSqlProperties() {
		return sqlProperties;
	}

	public void setSqlProperties(Properties sqlProperties) {
		this.sqlProperties = sqlProperties;
	}
	
}
