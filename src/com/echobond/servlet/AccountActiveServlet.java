package com.echobond.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONObject;

import com.echobond.dao.AccountDAO;

/**
 * @author Luck
 * Servlet implementation class AccountActiveServlet
 */
public class AccountActiveServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Properties sqlProperties;
	private int expiry;
	private AccountDAO dao;
	private Logger log = LogManager.getLogger("AccountActive");
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AccountActiveServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String to = request.getParameter("email");
		String code = request.getParameter("code");
		String timeStamp = request.getParameter("timeStamp");
		JSONObject result = dao.activateAccount(to, code, timeStamp);
		response.setContentType("text/json;charset=UTF-8");
		response.getWriter().write(result.toString());
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
		expiry = Integer.parseInt(this.getServletConfig().getInitParameter("expireDuration"));
		sqlProperties = (Properties) this.getServletContext().getAttribute("sqlProperties");
		dao = new AccountDAO();
		dao.setSqlProperties(sqlProperties);
		dao.setExpiry(expiry);
		log.debug("Servlet initiated.");
	}

}
