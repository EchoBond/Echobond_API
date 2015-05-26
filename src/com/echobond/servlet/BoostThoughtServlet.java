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

import com.echobond.dao.ThoughtDAO;
import com.echobond.entity.Thought;
import com.echobond.entity.User;
import com.echobond.util.StringUtil;

/**
 * Servlet implementation class BoostThoughtServlet
 */
public class BoostThoughtServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ThoughtDAO dao;
	private Properties sqlProperties;
	private Logger log = LogManager.getLogger("BoostThought");
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BoostThoughtServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Reader -> JSON String -> JSONObject
		JSONObject reqJSON = StringUtil.fromReaderToJSON(request.getReader());
		//JSONObject -> Bean
		JSONObject thoughtJSON = reqJSON.getJSONObject("thought");
		JSONObject userJSON = reqJSON.getJSONObject("user");
		JSONObject result = dao.boostThought((Thought)JSONObject.toBean(thoughtJSON, Thought.class), (User)JSONObject.toBean(userJSON, User.class));
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
		dao = new ThoughtDAO();
		dao.setSqlProperties(sqlProperties);
		log.debug("Servlet initiated.");
	}
}
