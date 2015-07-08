package com.echobond.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.dao.UserDao;
import com.echobond.dao.ValueDAO;
import com.echobond.entity.User;
import com.echobond.util.StringUtil;

/**
 * Servlet implementation class InitFetchServlet
 */
public class InitFetchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Properties sqlProperties;
	private ValueDAO valueDao;
	private UserDao userDao;
	private Logger log = LogManager.getLogger("InitFetch");
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InitFetchServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject reqJSON = StringUtil.fromReaderToJSON(request.getReader());
		JSONObject valueResult = valueDao.initFetch(reqJSON);
		User user = userDao.loadUserById(reqJSON);
		JSONObject userMeta = userDao.loadUserMeta(reqJSON);
		valueResult.put("user", user);
		valueResult.put("userMeta", userMeta);
		response.setContentType("text/json;charset=UTF-8");
		response.getWriter().write(valueResult.toString());
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
		sqlProperties = (Properties) getServletContext().getAttribute("sqlProperties");
		valueDao = new ValueDAO();
		valueDao.setSqlProperties(sqlProperties);
		userDao = new UserDao();
		userDao.setSqlProperties(sqlProperties);
		log.debug("Servlet initiated.");
	}

}
