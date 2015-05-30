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
import com.echobond.util.StringUtil;

/**
 * Servlet implementation class GCMRegServlet
 */
public class GCMRegServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AccountDAO dao;
	private Properties sqlProperties;
	private Logger log = LogManager.getLogger("GCMReg");
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GCMRegServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject reqJSON = StringUtil.fromReaderToJSON(request.getReader());
		String userId = reqJSON.getString("userId");
		String email = reqJSON.getString("email");
		String regId = reqJSON.getString("regId");
		JSONObject result = dao.regUserGCM(userId, email, regId);
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
		sqlProperties = (Properties) getServletContext().getAttribute("sqlProperties");
		dao = new AccountDAO();
		dao.setSqlProperties(sqlProperties);
		log.debug("Servlet initiated.");
	}

}
