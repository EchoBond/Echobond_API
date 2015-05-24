package com.echobond.listener;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.util.DBUtil;
import com.echobond.util.EmailUtil;
import com.echobond.util.GCMUtil;

/**
 * @author Luck
 * Application Lifecycle Listener implementation class ContextListener
 *
 */
public class ContextListener implements ServletContextListener, ServletContextAttributeListener {

	private Properties dbProperties, sqlProperties, gcmProperties;
	private final static Logger log = LogManager.getLogger("Context");
    /**
     * Default constructor. 
     */
    public ContextListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
    	ServletContext ctx = arg0.getServletContext();
    	initDB(ctx);
    	initEmail(ctx);
    	loadSql(ctx);
    	initGCM(ctx);
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    	ServletContext ctx = arg0.getServletContext();
    	stopDB(ctx);
    }

	/**
     * @see ServletContextAttributeListener#attributeAdded(ServletContextAttributeEvent)
     */
    public void attributeAdded(ServletContextAttributeEvent arg0) {
    }

	/**
     * @see ServletContextAttributeListener#attributeReplaced(ServletContextAttributeEvent)
     */
    public void attributeReplaced(ServletContextAttributeEvent arg0) {
    }

	/**
     * @see ServletContextAttributeListener#attributeRemoved(ServletContextAttributeEvent)
     */
    public void attributeRemoved(ServletContextAttributeEvent arg0) {
    }
    
    /**
     * initiate database resources
     * @param ctx
     */
    private void initDB(ServletContext ctx){
    	log.debug("Initiating database.");
    	dbProperties = new Properties();
		try {
			dbProperties.load(new FileInputStream(new File(ctx.getRealPath("/WEB-INF/conf/db.properties"))));
		} catch (Exception e) {
			log.error("Error loading database config!");
			e.printStackTrace();
		}
    	DBUtil dbUtil = DBUtil.getInstance();
		dbUtil.setDbDriver(dbProperties.getProperty("dbDriver"));
		dbUtil.setUrl(dbProperties.getProperty("url"));
		dbUtil.setUserName(dbProperties.getProperty("userName"));
		dbUtil.setPassword(dbProperties.getProperty("password"));
		dbUtil.setMaxActive(Integer.parseInt(dbProperties.getProperty("maxActive")));
		dbUtil.setMaxIdle(Integer.parseInt(dbProperties.getProperty("maxIdle")));
		dbUtil.setMaxWait(Integer.parseInt(dbProperties.getProperty("maxWait")));
		dbUtil.setInitialSize(Integer.parseInt(dbProperties.getProperty("initialSize")));
		dbUtil.createBasicDataSource();
		log.debug("Database initiation processed.");
    }
    
    /**
     * initiate email settings
     * @param ctx
     */
    private void initEmail(ServletContext ctx){
    	log.debug("Loading email service properties.");
		String username = ctx.getInitParameter("SMTPUsername");
		String pass = ctx.getInitParameter("SMTPPass");
		String server = ctx.getInitParameter("SMTPServer");
		String validation = ctx.getInitParameter("SMTPValidation");
		String port = ctx.getInitParameter("SMTPPort");
		String domain = ctx.getInitParameter("domain");
		EmailUtil emailUtil = EmailUtil.getInstance();
		emailUtil.setParameters(username, pass, validation, server, port, domain);
		log.debug("Loading Email service properties processed.");
    }
    
    /**
     * load pre-defined SQLs
     * @param ctx
     */
	private void loadSql(ServletContext ctx) {
		log.debug("Loading SQLs.");
		sqlProperties = new Properties();
		try {
			sqlProperties.load(new FileInputStream(new File(ctx.getRealPath("/WEB-INF/conf/sql.properties"))));
		} catch (Exception e) {
			log.error("Error loading SQL config!");
			e.printStackTrace();
		}
		ctx.setAttribute("sqlProperties", sqlProperties);
		log.debug("Loading SQLs processed.");
	}
	
	/**
	 * initiate GCM settings
	 * @param ctx
	 */
	private void initGCM(ServletContext ctx) {
		log.debug("Loading GCM properties.");
		gcmProperties = new Properties();
		try {
			gcmProperties.load(new FileInputStream(new File(ctx.getRealPath("/WEB-INF/conf/gcm.properties"))));
		} catch (Exception e) {
			log.error("Error loading GCM properties!");
			e.printStackTrace();
		}
		ctx.setAttribute("gcmProperties", gcmProperties);
		GCMUtil.getInstance().setGcmProperties(gcmProperties);
		log.debug("Loading GCM properties processed.");
	}

	/**
	 * release database resources
	 * @param ctx
	 */
	private void stopDB(ServletContext ctx) {
		log.debug("Releasing Database resources.");
		DBUtil.getInstance().unregisterDataSource();
		log.debug("Database resources releasing processed.");
	}
	
}
