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

import com.echobond.dao.ImageDAO;
import com.echobond.util.StringUtil;

/**
 * @author Luck
 * Servlet implementation class ImageUploadServlet
 */
public class ImageUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Properties sqlProperties;
	private ImageDAO dao;
	private Logger log = LogManager.getLogger("ImageUpload");
	private String localPath;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject reqJSON = StringUtil.fromReaderToJSON(request.getReader());
		JSONObject result = dao.uploadImage(reqJSON);
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
		localPath = this.getServletConfig().getInitParameter("localPath");
		sqlProperties = (Properties) getServletContext().getAttribute("sqlProperties");
		dao = new ImageDAO();
		dao.setSqlProperties(sqlProperties);
		dao.setCtx(this.getServletContext());
		dao.setLocalPath(localPath);
		log.debug("Servlet initiated.");
	}
}
