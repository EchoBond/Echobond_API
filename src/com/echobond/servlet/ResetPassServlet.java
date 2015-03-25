package com.echobond.servlet;

import java.io.IOException;
import java.io.InputStream;
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
 * @author Luck
 * Servlet implementation class ResetPassServlet
 */
public class ResetPassServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private int expiry, codeLength;
	private String subject, htmlTemplate;
	private Properties sqlProperties;
	private AccountDAO dao;
	private Logger log = LogManager.getLogger("ResetPass");
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResetPassServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject to = StringUtil.fromReaderToJSON(request.getReader());
		JSONObject result = dao.resetPass(to.getString("email"), codeLength, subject, htmlTemplate);
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
		codeLength = Integer.parseInt(this.getServletConfig().getInitParameter("codeLength"));
		subject = this.getServletConfig().getInitParameter("emailSubject");
		String htmlPath = this.getServletConfig().getInitParameter("htmlTemplate");
		InputStream htmlStream = this.getServletContext().getResourceAsStream("/WEB-INF/"+htmlPath);
        htmlTemplate = StringUtil.fromInputStreamToString(htmlStream);
        sqlProperties = (Properties) this.getServletContext().getAttribute("sqlProperties");
        dao = new AccountDAO();
        dao.setSqlProperties(sqlProperties);
        dao.setExpiry(expiry);
		log.debug("Servlet initiated.");
	}	

}
