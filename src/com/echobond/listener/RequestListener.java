package com.echobond.listener;

import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Application Lifecycle Listener implementation class RequestListener
 *
 */
public class RequestListener implements ServletRequestListener, ServletRequestAttributeListener {

	private Logger log = LogManager.getLogger("Request");
    /**
     * Default constructor. 
     */
    public RequestListener() {
    }

	/**
     * @see ServletRequestListener#requestInitialized(ServletRequestEvent)
     */
    public void requestInitialized(ServletRequestEvent arg0) {
    	HttpServletRequest request = (HttpServletRequest) arg0.getServletRequest();
    	String addr = request.getRemoteAddr();
    	String servlet = request.getServletPath();
    	log.debug("Request to [" + servlet.substring(1) + "] from " + addr + " processing.");
    }

    
    /**
     * @see ServletRequestListener#requestDestroyed(ServletRequestEvent)
     */
    public void requestDestroyed(ServletRequestEvent arg0) {
    	HttpServletRequest request = (HttpServletRequest) arg0.getServletRequest();
    	String addr = request.getRemoteAddr();
    	String servlet = request.getServletPath();
    	log.debug("Request to [" + servlet.substring(1) + "] from " + addr + " processed.");    	
    }

	/**
     * @see ServletRequestAttributeListener#attributeAdded(ServletRequestAttributeEvent)
     */
    public void attributeAdded(ServletRequestAttributeEvent arg0) {
    }

	/**
     * @see ServletRequestAttributeListener#attributeRemoved(ServletRequestAttributeEvent)
     */
    public void attributeRemoved(ServletRequestAttributeEvent arg0) {
    }

	/**
     * @see ServletRequestAttributeListener#attributeReplaced(ServletRequestAttributeEvent)
     */
    public void attributeReplaced(ServletRequestAttributeEvent arg0) {
    }

	
}
