package com.echobond.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.dao.UserDao;

/**
 * Servlet implementation class LoadUsersServlet
 */
public class LoadUsersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Properties sqlProperties;
	private UserDao dao;
	private Logger log = LogManager.getLogger("LoadUsers");
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoadUsersServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	@Override
	public void init() throws ServletException {
		log.debug("Servlet initiating.");
		sqlProperties = (Properties) this.getServletContext().getAttribute("sqlProperties");
		dao = new UserDao();
		dao.setSqlProperties(sqlProperties);
		log.debug("Servlet initiated.");
	}
}
