package com.echobond.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author Luck
 *
 */
public class Group{
	private int id;
	private String name;
	public void loadGroupProperties(ResultSet rs){
		try {
			id = rs.getInt("id");
			name = rs.getString("name");
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
