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

import com.echobond.dao.ValueDAO;
import com.echobond.util.StringUtil;

/**
 * @author Luck
 * Servlet implementation class UpdateTagServlet
 */
public class UpdateTagsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Properties sqlProperties;
	private ValueDAO dao;
	private Logger log = LogManager.getLogger("UpdateTag");
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateTagsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject result = dao.updateTags(StringUtil.fromReaderToJSON(request.getReader()));
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
		dao = new ValueDAO();
		dao.setSqlProperties(sqlProperties);
		log.debug("Servlet initiated.");
	}
}
