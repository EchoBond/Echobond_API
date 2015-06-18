package com.echobond.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.MyEmail;
import com.echobond.entity.ResultResource;
import com.echobond.entity.User;
import com.echobond.util.DBUtil;
import com.echobond.util.DateUtil;
import com.echobond.util.EmailUtil;
import com.echobond.util.StringUtil;

import net.sf.json.JSONObject;

/**
 * 
 * manipulate account tables
 * @author Luck
 * 
 */
public class AccountDAO {
	private Properties sqlProperties;
	private int expiry;
	private Logger log = LogManager.getLogger("Account");
	
	public JSONObject regUserGCM(String userId, String email, String regId){
		JSONObject result = new JSONObject();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByUserIdAndEmail"), new Object[]{userId, email});
		try {
			if(rr.getRs().next()){
				DBUtil.getInstance().update(sqlProperties.getProperty("updateUserGCM"), new Object[]{regId, DateUtil.dateToString(new Date(), null), userId});
				result.put("result", "1");
			} else {
				result.put("result", "0");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rr.close();
		}
		return result;
	}
	
	/**
	 * activate an account
	 * @param to
	 * @param code
	 * @param timeStamp
	 * @return result
	 */
	public JSONObject activateAccount(String to, String code, String timeStamp){
		log.debug("Activating account");
		int activationId = 0;
		JSONObject result = new JSONObject();
		//1. check if param time expired
		if(System.currentTimeMillis() - Long.parseLong(timeStamp) >= expiry){
			result.put("paramExpire", 1);
		} else{
			Object[] params = new Object[]{to, code, timeStamp};
			ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadVerify"), params);
			try {
				//not existed
				if(!rr.getRs().next()){
					activationId = -1;
				}
				//already verified
				else if(rr.getRs().getInt("verified") == 1){
					activationId = 0;
				}
				//verify now
				else{
					activationId = rr.getRs().getInt("id");
				}
			} catch (SQLException e) {
				log.error(e.getMessage() + " when fetching results.");
			} finally{
				rr.close();
			}
			switch(activationId){
			case -1:{
				result.put("exists", 0);
				break;
			}
			case 0:{
				result.put("exists", 1);
				result.put("verified", 1);
				break;
			}
			default:{
				//2. verify now
				DBUtil.getInstance().update(sqlProperties.getProperty("updateVerifyById"), new Object[]{activationId});
				DBUtil.getInstance().update(sqlProperties.getProperty("updateUserVerifyByEmail"), new Object[]{to});
				result.put("exists", 1);
				result.put("verified", 0);
				result.put("extra", "verifyNow");
				break;
			}
			}
		}
		log.debug("Account activation processed");
		return result;
	}

	/**
	 * reset password and send the new password via email
	 * @param to
	 * @param code
	 * @param timeStamp
	 * @param passLength
	 * @param subject
	 * @param htmlTemplate
	 * @return result
	 */
	public JSONObject confirmResetPass(String to, String code, String timeStamp, int passLength, String subject, String htmlTemplate){
		log.debug("Confirming pass reset.");
		JSONObject result = new JSONObject();
		int resetId = 0;
		//1. check if param time expired
		if(System.currentTimeMillis() - Long.parseLong(timeStamp) > expiry){
			result.put("paramExpire", 1);
		} else{
			Object[] params = new Object[]{to, code, timeStamp};
			ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadReset"), params);
			try {
				//not existed
				if(!rr.getRs().next()){
					resetId = -1;
				}
				//already reset
				else if(rr.getRs().getInt("reset") == 1){
					resetId = 0;
				}
				//reset now
				else{
					resetId = rr.getRs().getInt("id");
				}
			} catch (SQLException e) {
				log.error(e.getMessage() + " when fetching results.");
			} finally{
				rr.close();
			}
			switch(resetId){
			case -1:{
				result.put("exists", 0);
				break;
			}
			case 0:{
				result.put("exists", 1);
				result.put("reset", 1);
				break;
			}
			default:{
				//2. reset now
				String newPass = StringUtil.genRandomCode(passLength);
				DBUtil.getInstance().update(sqlProperties.getProperty("updateResetById"), new Object[]{resetId});
				DBUtil.getInstance().update(sqlProperties.getProperty("updatePassByEmail"), new Object[]{newPass, to});
				genNewPassEmail(to, newPass, subject, htmlTemplate);
				result.put("exists", 1);
				result.put("reset", 0);
				result.put("extra", "resetNow&sendEmail");
				break;
			}
			}
		}
		log.debug("Pass reset confirmation processed.");
		return result;
	}
	
	/**
	 * send password reset confirmation email
	 * @param to
	 * @param codeLength
	 * @param subject
	 * @param htmlTemplate
	 * @return result
	 */
	public JSONObject resetPass(String to, int codeLength, String subject, String htmlTemplate){
		log.debug("Reseting pass.");
		JSONObject result = new JSONObject();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{to});
		try {
			//already existed
			if(rr.getRs().next()){
				result.put("accExists", 1);
				rr.close();
				rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadLastResetByEmail"), new Object[]{to});
				//reset email sent
				if(rr.getRs().next()){
					//already reset
					result.put("hadReset", 1);
					if(rr.getRs().getInt("reset") == 1){
						genResetEmail(to, codeLength, subject, htmlTemplate);
						result.put("reset", 1);
						result.put("extra", "resendEmail");
					}
					//not reset
					else{
						result.put("reset", 0);
						//not expired
						if(System.currentTimeMillis() - Long.parseLong(rr.getRs().getString("time_stamp")) < expiry){
							result.put("expire", 0);
						}
						//expired
						else{
							genResetEmail(to, codeLength, subject, htmlTemplate);
							result.put("expire", 1);
							result.put("extra", "resendEmail");
						}
					}
				}
				//reset email not sent, 1st password reset
				else{
					genResetEmail(to, codeLength, subject, htmlTemplate);
					result.put("hadReset", 0);
					result.put("extra", "sendEmail");
				}
			}
			//not exists
			else {
				result.put("accExists", 0);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " when fetching results;");
		} finally{
			rr.close();
		}
		return result;
	}
	
	/**
	 * sign the email user in
	 * @param email
	 * @param password
	 * @return result
	 */
	public JSONObject signIn(String email, String password){
		log.debug("Signing in.");
		JSONObject result = new JSONObject();
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{email});
		try {
			//not existed
			if(!rr.getRs().next()){
				result.put("exists", 0);
			}
			//password mismatch
			else if(!password.equals(rr.getRs().getString("password"))){
				result.put("exists", 1);
				result.put("passMatch", 0);
			} else {
				result.put("exists", 1);
				result.put("passMatch", 1);
				User user = new User();
				user.loadUserProperties(rr.getRs());
				result.put("user", user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			rr.close();
		}
		log.debug("Signing in processed.");
		return result;
	}
	
	/**
	 * sign the new email user up
	 * @param to
	 * @param password
	 * @param codeLength
	 * @param subject
	 * @param htmlTemplate
	 * @return result
	 */
	public JSONObject signUp(String to, String password, int codeLength, String subject, String htmlTemplate){
		log.debug("Signing up.");
		JSONObject result = new JSONObject();
		String id = "";
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{to});
		try {
			//already existed
			if(rr.getRs().next()){
				result.put("exists", 1);
				//verified
				if(rr.getRs().getInt("verified") == 1){
					result.put("verified", 1);
				}
				//not verified
				else{
					result.put("verified", 0);
					rr.close();
					rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadLastVerifyByEmail"), new Object[]{to});
					//email not sent
					if(!rr.getRs().next()){
						genVerifyEmail(to, codeLength, subject, htmlTemplate);
						result.put("email", 0);
						result.put("extra", "resendEmail");
					}
					//email sent
					else{
						result.put("email", 1);
						//not expired
						if(System.currentTimeMillis() - Long.parseLong(rr.getRs().getString("time_stamp")) < expiry){
							result.put("expired", 0);
						}
						//expired
						else{
							genVerifyEmail(to, codeLength, subject, htmlTemplate);
							result.put("expired", 1);
							result.put("extra", "resendEmail");
						}
					}
				}
			}
			//not existed, new user
			else {
				//insert into database
				DBUtil.getInstance().update(sqlProperties.getProperty("addNewEmailUser"), new Object[]{id = genUserId(), password, to, 0});
				//send verification email
				genVerifyEmail(to, codeLength, subject, htmlTemplate);
				User user = new User();
				user.setId(id);
				user.setPassword(password);
				user.setEmail(to);
				result.put("exists", 0);
				result.put("extra", "sendEmail");
				result.put("user", user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			rr.close();
		}
		log.debug("Signing up processed.");
		return result;
	}
	
	/**
	 * sign the FB user in
	 * @param user
	 * @return result
	 */
	public JSONObject signInFB(User user){
		log.debug("Signing in with Facebook.");
		JSONObject result = new JSONObject();
		String userId = "";
		ResultResource rr = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{user.getEmail()});
		try {
			//new user
			if(!rr.getRs().next()){
				user.setId(genUserId());
				DBUtil.getInstance().update(sqlProperties.getProperty("addNewFBUser"), new Object[]{userId, "", user.getEmail(), user.getFBId(), user.getFirstName(), user.getLastName(), user.getName(), user.getTimeZone(), user.getLocale(), user.getGender(), 1});
				result.put("user", user);
				result.put("new", 1);
			}
			//return user
			else{
				userId = rr.getRs().getString("id");
				user.setId(userId);
				result.put("id", user);
				result.put("new", 0);
			}
			result.put("user", user);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			rr.close();
		}
		log.debug("Signing in with Facebook processed.");
		return result;
	}
	
	/**
	 * generate the link to reset the password
	 * @param to
	 * @param code
	 * @param timeStamp
	 * @return link
	 */
	private String composeResetLink(String to, String code, String timeStamp){
		StringBuffer link = new StringBuffer();
		link.append("http://localhost/EchoBond_Demo/ConfirmPassResetServlet?email=");
		link.append(to);
		link.append("&code=");
		link.append(code);
		link.append("&timeStamp=");
		link.append(timeStamp);
		return link.toString();
	}
	
	/**
	 * generate the email containing password reset link
	 * @param to
	 * @param codeLength
	 * @param subject
	 * @param htmlTemplate
	 */
	private void genResetEmail(String to, int codeLength, String subject, String htmlTemplate){
		//1. generate code
		String code = StringUtil.genRandomCode(codeLength);
		//2. generate timestamp
		String timeStamp = System.currentTimeMillis()+"";
		//3. store email, code, timestamp in passreset table
		Object[] params = new Object[]{to, code, timeStamp, 0};
		DBUtil.getInstance().update(sqlProperties.getProperty("addNewReset"), params);
		//4. email reset link to user
		MyEmail email = new MyEmail(to, subject, htmlTemplate.replace("[!PUT LINK HERE]", composeResetLink(to, code, timeStamp)));
		EmailUtil.getInstance().sendHtmlMail(email);
	}
	
	/**
	 * generate email containing new password
	 * @param to
	 * @param pass
	 * @param subject
	 * @param htmlTemplate
	 */
	private void genNewPassEmail(String to, String pass, String subject, String htmlTemplate){
		MyEmail email = new MyEmail(to, subject, htmlTemplate.replace("[!PUT LINK HERE]", pass));
		EmailUtil.getInstance().sendHtmlMail(email);		
	}
	
	/**
	 * generate account verify email containg the activation link for new user
	 * @param to
	 * @param codeLength
	 * @param subject
	 * @param htmlTemplate
	 */
	private void genVerifyEmail(String to, int codeLength, String subject, String htmlTemplate){
		//1. generate code
		String code = StringUtil.genRandomCode(codeLength);
		//2. generate timestamp
		String timeStamp = System.currentTimeMillis()+"";
		//3. store email, code, timestamp in activation table
		Object[] params = new Object[]{to, code, timeStamp, 0};
		DBUtil.getInstance().update(sqlProperties.getProperty("addNewVerify"), params);
		//4. send email
		MyEmail myEmail = new MyEmail(to, subject, htmlTemplate.replace("[!PUT LINK HERE]", composeActivateLink(to, code, timeStamp)));
		EmailUtil.getInstance().sendHtmlMail(myEmail);
	}
	
	/**
	 * generate account activation link
	 * @param to
	 * @param code
	 * @param timeStamp
	 * @return link
	 */
	private String composeActivateLink(String to, String code, String timeStamp){
		StringBuffer link = new StringBuffer("http://localhost/EchoBond_Demo/AccountActiveServlet?email=");
		link.append(to);
		link.append("&code=");
		link.append(code);
		link.append("&timeStamp=");
		link.append(timeStamp);
		return link.toString();
	}
	
	/**
	 * generate a new user id
	 * @return new user id
	 */
	private String genUserId(){
		String userId = "";
		userId += System.currentTimeMillis();
		return userId;
	}
	
	public Properties getSqlProperties() {
		return sqlProperties;
	}
	
	public void setSqlProperties(Properties sqlProperties) {
		this.sqlProperties = sqlProperties;
	}

	public int getExpiry() {
		return expiry;
	}

	public void setExpiry(int expiry) {
		this.expiry = expiry;
	}
	
}
