package com.echobond.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.ResultResource;

/**
 * 
 * @author Luck
 *
 */
public class DBUtil {
    private BasicDataSource bds;  
    private String dbDriver;  
    private String url;
    private String userName;
    private String password;
    private int maxActive;  
    private int maxIdle;  
    private int maxWait;
    private int initialSize;
    private Logger log = LogManager.getLogger("DB");

    private static class DBUtilHolder{
    	public static final DBUtil INSTANCE = new DBUtil();
    }
    public static DBUtil getInstance(){
    	return DBUtilHolder.INSTANCE;
    }
    
    /** 
    * data source init
    * 
    */  
    public void createBasicDataSource() {
        try {  
            bds = new BasicDataSource();
            bds.setDriverClassName(dbDriver); // JDBC驱动  
            bds.setUrl(url); // URL  
            bds.setUsername(userName); // userName  
            bds.setPassword(password); // password  
            bds.setMaxActive(maxActive); // 最大连接数  
            bds.setMaxIdle(maxIdle); // 最大空闲数  
            bds.setMaxWait(maxWait); // 最大等待时间  
            bds.setInitialSize(initialSize);
        } catch (Exception e) {  
            log.error(e.getMessage() + " when creating data source.");
        }
    }
    
    /**
     * free resources
     */
    public void unregisterDataSource(){
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        try {
        	while (drivers.hasMoreElements()) {
        		Driver driver = drivers.nextElement();
                DriverManager.deregisterDriver(driver);
        	}
        } catch (SQLException e) {
        	log.error(e.getMessage() + " when unreg data source.");
        }
    }
    
    /** 
    * get connection, called automatically
    * 
    */  
    private Connection getConn() {  
        Connection conn = null;
		try {
			conn = bds.getConnection();
		} catch (SQLException e) {
			log.error(e.getMessage() + " when get connection.");
		}  
        return conn;  
    }  
  
    /**
     * insert, update and delete, manually close result set & statement
     * @param sql
     * @param params
     * @return
     */
    public int update(String sql, Object... params) {  
        int rowAffected = 0;
        PreparedStatement stat = null;
        Connection conn = getConn();
        try{
            stat = conn.prepareStatement(sql);
            bindObject(stat, params);
            rowAffected = stat.executeUpdate();  
        } catch(SQLException e){  
            log.error(e.getMessage() + " when " + sql + ".");
        } finally {
        	closeStatement(stat);
        	closeConnection(conn);
        }
        return rowAffected;  
    }  
      
    /**
     * insert, update and delete, manually close result set & statement
     * connection persisted, usually followed by a SELECT LAST_INSERT_ID() query
     * @param sql
     * @param params
     * @return
     */
    public int update(String sql, ResultResource rr, Object... params) {  
        int rowAffected = 0;
        PreparedStatement stat = null;
        Connection conn = getConn();
        try{
            stat = conn.prepareStatement(sql);
            bindObject(stat, params);
            rowAffected = stat.executeUpdate();  
        } catch(SQLException e){  
            log.error(e.getMessage() + " when " + sql + ".");
        } finally {
        	closeStatement(stat);
        	//closeConnection(conn);
        	rr.setConn(conn);
        }
        return rowAffected;  
    }  
    
    public int[] updateBatch(String sql, Object[]... params){
    	int[] rowAffected = new int[params.length];
    	PreparedStatement stat = null;
    	Connection conn = getConn();
    	try{
    		stat = conn.prepareStatement(sql);
    		for (Object[] objects : params) {
				bindObject(stat, objects);
				stat.addBatch();
			}
    		rowAffected = stat.executeBatch();
    	} catch (SQLException e){
    		log.error(e.getMessage() + " when " + sql + ".");
    	} finally {
    		closeStatement(stat);
    		closeConnection(conn);
    	}
    	return rowAffected;
    }
    
    /**
     * select, manually close result set & statement
     * @param sql
     * @param params
     * @return (result,statement) pair, for closing
     */
    public ResultResource query(String sql, Object... params){  
        ResultSet rs = null;
        PreparedStatement stat = null;
        Connection conn = getConn();
        try{
            stat = conn.prepareStatement(sql);
            bindObject(stat, params);
            rs = stat.executeQuery();
        } catch(SQLException e){  
        	log.error(e.getMessage() + " when " + sql + ".");
        }
        return new ResultResource(rs, stat, conn);
    }  
    
    /**
     * select, manually close result set & statement
     * usually follows a insert / update to SELECT LAST_INSERT_ID()
     * @param sql
     * @param params
     * @return (result,statement) pair, for closing
     */
    public void query(String sql, ResultResource rr, Object... params){  
        ResultSet rs = null;
        PreparedStatement stat = null;
        try{
        	Connection conn = rr.getConn();
            stat = conn.prepareStatement(sql);
            rr.setStat(stat);
            bindObject(stat, params);
            rs = stat.executeQuery();
            rr.setRs(rs);
        } catch(SQLException e){  
        	log.error(e.getMessage() + " when " + sql + ".");
        }
    }  
    
    /**
     * manually close result set
     * @param rs
     */
    public void closeResultSet(ResultSet rs){  
		try {
	        if(rs!=null)
	        	rs.close();
		} catch (SQLException e) {
			log.error(e.getMessage() + " when closing result set.");
		}  
    }
    /**
     * manually close statement
     * @param stat
     */
    public void closeStatement(Statement stat){
		try {
	        if(stat!=null)
	        	stat.close();
		} catch (SQLException e) {
			log.error(e.getMessage() + " when closing statement.");
		}  
    }
    
    public void closeConnection(Connection conn){
    	try{
    		if(null!=conn)
    			conn.close();
    	} catch (SQLException e) {
    		log.error(e.getMessage() + " when closing connection.");
		}
    }
    
    /**
     * bind object array to prepared statement
     * @param stat
     * @param params
     */
    private void bindObject(PreparedStatement stat, Object... params){
    	try{
    		for(int i=0;i<params.length;i++){
    			stat.setObject(i+1, params[i]);
    		}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when binding objects.");
			log.error("Objects: " + params);
		}        	
    }

	public BasicDataSource getBds() {
		return bds;
	}

	public void setBds(BasicDataSource bds) {
		this.bds = bds;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}
	
}  