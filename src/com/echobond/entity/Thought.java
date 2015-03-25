package com.echobond.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Luck
 *
 */
public class Thought {
	private int id;
	private String userId;
	private int langId;
	private int groupId;
	private int categoryId;
	private String content;
	private String image;
	private String time;
	private int boost;
	private ArrayList<Comment> comments;
	
	private User user;
	private Language lang;
	private Group group;
	private Category category;
	private String tagStr;
	private ArrayList<Integer> tagIds;
	private ArrayList<Tag> tags;
	public void loadThoughtProperties(ResultSet rs){
		try {
			id = rs.getInt("id");
			userId = rs.getString("user_id");
			user = new User();
			user.setId(userId);
			user.setUserName(rs.getString("username"));
			langId = rs.getInt("lang_id");
			category = new Category();
			categoryId = rs.getInt("category_id");
			category.setId(categoryId);
			category.setName(rs.getString("category_name"));
			group = new Group();
			groupId = rs.getInt("group_id");
			group.setId(groupId);
			group.setName(rs.getString("group_name"));
			content = rs.getString("content");
			image = rs.getString("image");
			time = rs.getString("time");
			boost = rs.getInt("boost");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static Map<String, Object> loadClassMap(){
		Map<String, Object> classMap = new HashMap<String, Object>();
		classMap.put("user", User.class);
		classMap.put("lang", Language.class);
		classMap.put("group", Group.class);
		classMap.put("category", Category.class);
		classMap.put("tagIds", Integer.class);
		classMap.put("tags", Tag.class);
		return classMap;
	}
	public void loadComments(ResultResource rrComment) {
		comments = new ArrayList<Comment>();
		try{
			while(rrComment.getRs().next()){
				Comment comment = new Comment();
				comment.loadCommentProperties(rrComment.getRs());
				comments.add(comment);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getLangId() {
		return langId;
	}
	public void setLangId(int langId) {
		this.langId = langId;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Language getLang() {
		return lang;
	}
	public void setLang(Language lang) {
		this.lang = lang;
	}
	public Group getGroup() {
		return group;
	}
	public void setGroup(Group group) {
		this.group = group;
	}
	public ArrayList<Integer> getTagIds() {
		return tagIds;
	}
	public void setTagIds(ArrayList<Integer> tagIds) {
		this.tagIds = tagIds;
	}
	public ArrayList<Tag> getTags() {
		return tags;
	}
	public void setTags(ArrayList<Tag> tags) {
		this.tags = tags;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public String getTagStr() {
		return tagStr;
	}
	public void setTagStr(String tagStr) {
		this.tagStr = tagStr;
	}
	public int getBoost() {
		return boost;
	}
	public void setBoost(int boost) {
		this.boost = boost;
	}
	public ArrayList<Comment> getComments() {
		return comments;
	}
	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}

}
