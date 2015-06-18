package com.echobond.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author Luck
 *
 */
public class UserMsg {
	private int id;
	private String senderId;
	private String recverId;
	private String time;
	private String content;
	private String userName;
	
	private User sender;
	private User recver;
	public void loadUserMsgProperties(ResultSet rs){
		if(null != rs){
			try {
				id = rs.getInt("id");
				senderId = rs.getString("sender_id");
				recverId = rs.getString("recver_id");
				time = rs.getString("time");
				content = rs.getString("content");
				try{
					userName = rs.getString("username");
				} catch(SQLException e1){
					userName = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getRecverId() {
		return recverId;
	}
	public void setRecverId(String recverId) {
		this.recverId = recverId;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public User getSender() {
		return sender;
	}
	public void setSender(User sender) {
		this.sender = sender;
	}
	public User getRecver() {
		return recver;
	}
	public void setRecver(User recver) {
		this.recver = recver;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
}
