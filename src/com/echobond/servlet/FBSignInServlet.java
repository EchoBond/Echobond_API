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
import com.echobond.entity.User;
import com.echobond.util.StringUtil;

/**
 * @author Luck
 * Servlet implementation class FBSignInServlet
 */
public class FBSignInServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private Properties sqlProperties;
	private AccountDAO dao;
	private Logger log = LogManager.getLogger("FBSignIn");
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FBSignInServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Reader -> JSON String -> JSON Object
		JSONObject reqJSON = StringUtil.fromReaderToJSON(request.getReader());
		//JSON Object -> Bean
		User user = (User) JSONObject.toBean(reqJSON,User.class);
		JSONObject result = dao.signInFB(user);
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
		sqlProperties = (Properties) this.getServletContext().getAttribute("sqlProperties");
		dao = new AccountDAO();
		dao.setSqlProperties(sqlProperties);
		log.debug("Servlet initiated.");
	}
}
