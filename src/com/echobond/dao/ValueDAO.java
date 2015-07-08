package com.echobond.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.Category;
import com.echobond.entity.Country;
import com.echobond.entity.Group;
import com.echobond.entity.Language;
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
	
	public JSONObject initFetch(JSONObject request){
		log.debug("Init fetching.");
		JSONObject result = new JSONObject();
		//load groups
		result.put("groups",loadSomeGroups(request));
		//load tags
		result.put("tags", loadSomeTags(request));
		//load categories
		result.put("categories", loadAllCategories());
		//load countries
		result.put("countries", loadAllCountries());
		//load language
		result.put("languages", loadAllLanguages());
		log.debug("Init fetching processed.");
		return result;
	}

	/**
	 * from a list of tags, if existed update, otherwise add new
	 * @param tags
	 * @return result
	 */
	public JSONObject updateTags(JSONObject request){
		log.debug("Updating tags.");
		ArrayList<Tag> tags = new ArrayList<Tag>();
		JSONArray tArray = request.getJSONArray("tags");
		for(int i = 0; i < tArray.size(); i++){
			String t = tArray.getString(i);
			Tag tag = new Tag();
			tag.setName(t);
			tags.add(tag);
		}
		JSONObject result = new JSONObject();
		ResultResource rr = null;
		ArrayList<Tag> newTags = new ArrayList<Tag>();
		for (Tag tag : tags) {
			if(null != tag.getName() && !tag.getName().isEmpty()){
				try {
					rr = new ResultResource();
					DBUtil.getInstance().update(sqlProperties.getProperty("addNewTag"), rr,
							new Object[]{tag.getName()});
					DBUtil.getInstance().query(sqlProperties.getProperty("loadInsertId"), rr, new Object[]{});
					if(rr.getRs().next()){
						int id = rr.getRs().getInt("id");
						tag.setId(id);
						newTags.add(tag);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					rr.close();
				}
			}
		}
		result.put("tags", newTags);
		log.debug("Tags update processed.");
		return result;
	}
	
	/**
	 * from a list of tags, if existed update, otherwise add new
	 * @param tags
	 * @return result
	 */
	public JSONObject updateGroups(JSONObject request){
		log.debug("Updating groups.");
		ArrayList<Group> groups = new ArrayList<Group>();
		JSONArray gArray = request.getJSONArray("groups");
		for(int i = 0; i < gArray.size(); i++){
			String g = gArray.getString(i);
			Group group = new Group();
			group.setName(g);
			groups.add(group);
		}
		JSONObject result = new JSONObject();		
		ResultResource rr = null;
		ArrayList<Group> newGroups = new ArrayList<Group>();
		for (Group g : groups) {
			if(null != g.getName() && !g.getName().isEmpty()){				
				try {
					rr = new ResultResource();
					DBUtil.getInstance().update(sqlProperties.getProperty("addNewGroup"), rr,
							new Object[]{g.getName()});
					DBUtil.getInstance().query(sqlProperties.getProperty("loadInsertId"), rr, new Object[]{});
					if(rr.getRs().next()){
						int id = rr.getRs().getInt("id");
						g.setId(id);
						newGroups.add(g);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					rr.close();
				}
			}
		}
		result.put("groups", newGroups);
		log.debug("Groups update processed.");
		return result;
	}
	
	/**
	 * add a new group
	 * @param group
	 * @return result
	 */
	public JSONObject updateGroup(Group group){
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
	 * load categories
	 * @return category list
	 */
	public JSONObject loadCategories(){
		log.debug("Loading categories");
		JSONObject result = new JSONObject();
		result.put("categories", loadAllCategories());
		log.debug("Categories loading processed.");
		return result;
	}
	
	/**
	 * load groups
	 * @return group list
	 */
	public JSONObject loadGroups(JSONObject request){
		log.debug("Loading groups");
		JSONObject result = new JSONObject(), userJSON;
		ArrayList<Group> groups = null;
		//load all groups
		if(null == request || request.isNullObject() || request.isEmpty()){
			groups = loadAllGroups();
		}
		//load user followed groups
		else if((userJSON = request.getJSONObject("user"))!= null && !userJSON.isNullObject() && !userJSON.isEmpty()){
			groups = loadGroupsByUser(request);
		}
		//load some groups
		else if(request.get("offset")!=null){
			groups = loadSomeGroups(request);
		}
		//load random groups
		else {
			groups = loadRandomGroups(request);
		}
		result.put("groups", groups);
		log.debug("Groups loading processed.");
		return result;		
	}
	
	/**
	 * load tags
	 * @return group list
	 */
	public JSONObject loadTags(JSONObject request){
		log.debug("Loading tags");
		JSONObject result = new JSONObject(), userJSON;
		ArrayList<Tag> tags = null;
		//load all tags
		if(null == request || request.isNullObject() || request.isEmpty()){
			tags = loadAllTags();
		}
		//load user followed tags
		else if((userJSON = request.getJSONObject("user"))!= null && !userJSON.isNullObject() && !userJSON.isEmpty()){
			tags = loadTagsByUser(request);
		}
		//load some tags
		else if(request.get("offset")!=null){
			tags = loadSomeTags(request);
		}
		//load random tags
		else {
			tags = loadRandomTags(request);
		}
		result.put("tags", tags);
		log.debug("Tags loading processed.");
		return result;
	}	
	
	public JSONObject loadSearchThoughtsValues(JSONObject request){
		JSONObject result = new JSONObject();
		result.put("categories", loadAllCategories());
		result.put("groups", loadRandomGroups(request));
		result.put("tags", loadRandomTags(request));
		return result;
	}
	
	public JSONObject loadSearchPeopleValues(JSONObject request){
		JSONObject result = new JSONObject();
		result.put("groups", loadRandomGroups(request));
		result.put("tags", loadRandomTags(request));
		return result;
	}
	
	private ArrayList<Category> loadAllCategories(){
		ArrayList<Category> categories = new ArrayList<Category>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadAllCategories"), new Object[]{});
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
		return categories;
	}
	
	private ArrayList<Group> loadAllGroups(){
		ArrayList<Group> groups = new ArrayList<Group>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadAllGroups"), new Object[]{});
		try {
			while(rr.getRs().next()){
				Group g = new Group();
				g.loadGroupProperties(rr.getRs());
				groups.add(g);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading groups.");
		} finally {
			rr.close();
		}
		return groups;
	}
	
	private ArrayList<Group> loadGroupsByUser(JSONObject request){
		ArrayList<Group> groups = new ArrayList<Group>();
		JSONObject userJSON = request.getJSONObject("user");
		User user = (User) JSONObject.toBean(userJSON, User.class);
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadGroupsByUserId"), 
				new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				Group g = new Group();
				g.loadGroupProperties(rr.getRs());
				groups.add(g);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		return groups;
	}
	
	private ArrayList<Group> loadSomeGroups(JSONObject request){
		ArrayList<Group> groups = new ArrayList<Group>();
		int offset = request.getInt("offset");
		int limit = request.getInt("limit");
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadSomeGroups"), 
				new Object[]{offset, limit});
		try {
			while(rr.getRs().next()){
				Group g = new Group();
				g.loadGroupProperties(rr.getRs());
				groups.add(g);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		return groups;
	}
	
	private ArrayList<Group> loadRandomGroups(JSONObject request){
		ArrayList<Group> groups = new ArrayList<Group>();
		int random = request.getInt("random");
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadRandomGroups"), 
				new Object[]{random});
		try {
			while(rr.getRs().next()){
				Group g = new Group();
				g.loadGroupProperties(rr.getRs());
				groups.add(g);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		return groups;
	}
	
	private ArrayList<Tag> loadAllTags(){
		ArrayList<Tag> tags = new ArrayList<Tag>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadAllTags"), new Object[]{});
		try {
			while(rr.getRs().next()){
				Tag t = new Tag();
				t.loadTagProperties(rr.getRs());
				tags.add(t);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading tags.");
		} finally {
			rr.close();
		}
		return tags;
	}
	
	private ArrayList<Tag> loadTagsByUser(JSONObject request){
		ArrayList<Tag> tags = new ArrayList<Tag>();
		JSONObject userJSON = request.getJSONObject("user");
		User user = (User) JSONObject.toBean(userJSON, User.class);
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadTagsByUserId"), 
				new Object[]{user.getId()});
		try {
			while(rr.getRs().next()){
				Tag t = new Tag();
				t.loadTagProperties(rr.getRs());
				tags.add(t);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading tags.");
		} finally {
			rr.close();
		}
		return tags;
	}
	
	private ArrayList<Tag> loadSomeTags(JSONObject request){
		ArrayList<Tag> tags = new ArrayList<Tag>();
		int offset = request.getInt("offset");
		int limit = request.getInt("limit");
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadSomeTags"), 
				new Object[]{offset, limit});
		try {
			while(rr.getRs().next()){
				Tag t = new Tag();
				t.loadTagProperties(rr.getRs());
				tags.add(t);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading tags.");
		} finally {
			rr.close();
		}
		return tags;
	}		
	
	private ArrayList<Tag> loadRandomTags(JSONObject request){
		ArrayList<Tag> tags = new ArrayList<Tag>();
		int random = request.getInt("random");
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadRandomTags"), 
				new Object[]{random});
		try {
			while(rr.getRs().next()){
				Tag t = new Tag();
				t.loadTagProperties(rr.getRs());
				tags.add(t);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading tags.");
		} finally {
			rr.close();
		}
		return tags;
	}	
	
	private ArrayList<Country> loadAllCountries(){
		ArrayList<Country> countries = new ArrayList<Country>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadAllCountries"),
				new Object[]{});
		try {
			while(rr.getRs().next()){
				Country c = new Country();
				c.loadCountryProperties(rr.getRs());
				countries.add(c);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading countries.");
		} finally {
			rr.close();
		}		
		return countries;
	}
	
	private ArrayList<Language> loadAllLanguages(){
		ArrayList<Language> languages = new ArrayList<Language>();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadAllLanguages"),
				new Object[]{});
		try {
			while(rr.getRs().next()){
				Language l = new Language();
				l.loadLanguageProperties(rr.getRs());
				languages.add(l);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when loading languages.");
		} finally {
			rr.close();
		}		
		return languages;
	}
	
	public Properties getSqlProperties() {
		return sqlProperties;
	}

	public void setSqlProperties(Properties sqlProperties) {
		this.sqlProperties = sqlProperties;
	}
	
}
