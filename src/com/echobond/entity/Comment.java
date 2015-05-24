package com.echobond.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author Luck
 *
 */
public class Comment {
	private int id;
	private String userId;
	private int thoughtId;
	private int replyTo;
	private String content;
	private String time;
	public void loadCommentProperties(ResultSet rs){
		try {
			id = rs.getInt("id");
			userId = rs.getString("user_id");
			thoughtId = rs.getInt("thought_id");
			replyTo = rs.getInt("reply_to");
			content = rs.getString("content");
			time = rs.getString("time");
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
	public int getThoughtId() {
		return thoughtId;
	}
	public void setThoughtId(int thoughtId) {
		this.thoughtId = thoughtId;
	}
	public int getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(int replyTo) {
		this.replyTo = replyTo;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
}
