package com.echobond.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.echobond.util.DBUtil;

/**
 * 
 * @author Luck
 *
 */
public class ResultResource {
	private ResultSet rs;
	private PreparedStatement stat;
	private Connection conn;
	
	public ResultResource(){}
	
	public ResultResource(ResultSet rs, PreparedStatement stat, Connection conn) {
		super();
		this.rs = rs;
		this.stat = stat;
		this.conn = conn;
	}
	public ResultSet getRs() {
		return rs;
	}
	public void setRs(ResultSet rs) {
		this.rs = rs;
	}
	public PreparedStatement getStat() {
		return stat;
	}
	public void setStat(PreparedStatement stat) {
		this.stat = stat;
	}
	public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	public void close(){
		DBUtil.getInstance().closeResultSet(rs);
		DBUtil.getInstance().closeStatement(stat);
		DBUtil.getInstance().closeConnection(conn);
		rs = null;
		stat = null;
		conn = null;
	}
	
}
