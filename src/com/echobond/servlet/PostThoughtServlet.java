package com.echobond.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONObject;

import com.echobond.dao.ThoughtDAO;
import com.echobond.entity.Thought;
import com.echobond.util.StringUtil;

/**
 * @author Luck
 * Servlet implementation class PostThoughtServlet
 */
public class PostThoughtServlet extends HttpServlet implements Servlet {
	private static final long serialVersionUID = 1L;
	private Properties sqlProperties;
	private ThoughtDAO dao;
	private Logger log = LogManager.getLogger("PostThought");
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PostThoughtServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject reqJSON = StringUtil.fromReaderToJSON(request.getReader());
		Thought t = (Thought) JSONObject.toBean(reqJSON, Thought.class, Thought.loadClassMap());
		JSONObject result = dao.postThought(t);
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
		dao = new ThoughtDAO();
		dao.setSqlProperties(sqlProperties);
		log.debug("Servlet initiated.");
	}

}
