package com.echobond.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author Luck
 *
 */
public class Country {
	private int id;
	private String name;
	private String region;
	public void loadCountryProperties(ResultSet rs){
		try{
			id = rs.getInt("id");
			name = rs.getString("name");
			region = rs.getString("region");
		} catch (SQLException e){
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
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	
}
