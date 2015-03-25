package com.echobond.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.MyEmail;

/**
 * 
 * @author Luck
 *
 */
public class EmailUtil {
	private MimeMessage mimeMsg;
	private Session session;
	private Properties properties;
	private Multipart multipart;
	
	private String validation;
	private String server;
	private String port;
	private String username;
	private String pass;
	private String domain;
	private Logger log = LogManager.getLogger("Email");
	
	private static class EmailUtilHolder{
		public static final EmailUtil INSTANCE = new EmailUtil();
	}
	public static EmailUtil getInstance(){
		return EmailUtilHolder.INSTANCE;
	}
	public void setParameters(String username, String pass, String validation, String server, String port, String domain){
		this.username = username;
		this.pass = pass;
		this.validation = validation;
		this.server = server;
		this.port = port;
		this.domain = domain;
		if(null == properties)
			properties = new Properties();
		properties.put("mail.smtp.host", server);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.auth", validation);
		properties.put("mail.smtp.user", username+"@"+domain);
		
		properties.setProperty("mail.pop3.socketFactory.class","javax.net.ssl.SSLSocketFactory"); 
		properties.setProperty("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.smtp.socketFactory.fallback","false");
		properties.setProperty("mail.smtp.socketFactory.port",port);
		properties.put("mail.smtp.startssl.enable", "true");
	}
	@Deprecated
	public void sendTextMail(MyEmail email){
		Message msg = composeMailMetaData(email);
		try {
			msg.setText(email.getContent());
			Transport.send(msg);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	public void sendHtmlMail(MyEmail email){
		log.debug("Sending HTML mail");
		Message msg = composeMailMetaData(email);
		try{
			BodyPart html = new MimeBodyPart();
			html.setContent(email.getContent(),"text/html;charset=utf-8");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(html);
			msg.setContent(multipart);
			EmailThread emailThread = new EmailThread(msg);
			emailThread.start();
		} catch (MessagingException e) {
			log.error(e.getMessage() + " when sending HTML mail.");
		}
		log.debug("HTML mail sending processed.");
	}
	private Message composeMailMetaData(MyEmail email){
		log.debug("Composing mail meta.");
		Session session = Session.getDefaultInstance(properties, new MyEmailAuth(username+"@"+domain, pass));
		Message msg = new MimeMessage(session);
		try{
			//sender
			Address from = new InternetAddress(username+"@"+domain);
			msg.setFrom(from);
			//receiver
			Address to = new InternetAddress(email.getTo());
			msg.setRecipient(RecipientType.TO, to);
			//subject
			msg.setSubject(email.getSubject());
			//date
			msg.setSentDate(new Date());
		} catch (Exception e) {
			log.error(e.getMessage() + " when composing mail meta.");
		}
		log.debug("Mail meta composing processed.");
		return msg;
	}

	public MimeMessage getMimeMsg() {
		return mimeMsg;
	}
	public void setMimeMsg(MimeMessage mimeMsg) {
		this.mimeMsg = mimeMsg;
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	public String getValidation() {
		return validation;
	}
	public void setValidation(String validation) {
		this.validation = validation;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public Multipart getMultipart() {
		return multipart;
	}
	public void setMultipart(Multipart multipart) {
		this.multipart = multipart;
	}

}
class MyEmailAuth extends Authenticator{
	private String username;
	private String pass;
	public MyEmailAuth(String username, String pass) {
		this.username = username;
		this.pass = pass;
	}
	protected PasswordAuthentication getPasswordAuthentication(){  
        return new PasswordAuthentication(username, pass);  
    }  
}
