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

/**
 * @author Luck
 * Servlet implementation class ImageDownloadServlet
 */
public class ImageDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Properties sqlProperties;
	private ImageDAO dao;
	private Logger log = LogManager.getLogger("ImageDownload");
	private String localPath;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageDownloadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		JSONObject reqJSON = StringUtil.fromReaderToJSON(request.getReader());
		JSONObject reqJSON = new JSONObject();
		String path = request.getParameter("path");
		reqJSON.put("path", path);
		byte[] bytes = dao.downloadImage(reqJSON);
		if(path.endsWith(".jpg") || path.endsWith("jpeg") || path.endsWith(".JPG") || path.endsWith(".JPEG"))
			response.setContentType("image/jpg");
		else if(path.endsWith(".png") || path.endsWith(".PNG"))
			response.setContentType("image/png");
		else response.setContentType("text/json;charset=UTF-8");
		response.getOutputStream().write(bytes);
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
		localPath = this.getServletConfig().getInitParameter("localPath");
		dao = new ImageDAO();
		dao.setSqlProperties(sqlProperties);
		if(null == dao.getLocalPath())
			dao.setLocalPath(localPath);
		log.debug("Servlet initiated.");
	}
}
