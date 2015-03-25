package com.echobond.dao;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.echobond.entity.MyEmail;
import com.echobond.entity.ResultResource;
import com.echobond.entity.User;
import com.echobond.util.DBUtil;
import com.echobond.util.EmailUtil;
import com.echobond.util.StringUtil;

import net.sf.json.JSONObject;

public class AccountDAO {
	private Properties sqlProperties;
	private int expiry;
	private Logger log = LogManager.getLogger("Account");
	
	public JSONObject activateAccount(String to, String code, String timeStamp){
		log.debug("Activating account");
		int activationId = 0;
		JSONObject result = new JSONObject();
		//1. check if param time expired
		if(System.currentTimeMillis() - Long.parseLong(timeStamp) >= expiry){
			result.put("paramExpire", 1);
		} else{
			Object[] params = new Object[]{to, code, timeStamp};
			ResultResource rp = DBUtil.getInstance().query(sqlProperties.getProperty("loadVerify"), params);
			try {
				//not existed
				if(!rp.getRs().next()){
					activationId = -1;
				}
				//already verified
				else if(rp.getRs().getInt("verified") == 1){
					activationId = 0;
				}
				//verify now
				else{
					activationId = rp.getRs().getInt("id");
				}
			} catch (SQLException e) {
				log.error(e.getMessage() + " when fetching results.");
			} finally{
				rp.close();
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

	public JSONObject confirmResetPass(String to, String code, String timeStamp, int passLength, String subject, String htmlTemplate){
		log.debug("Confirming pass reset.");
		JSONObject result = new JSONObject();
		int resetId = 0;
		//1. check if param time expired
		if(System.currentTimeMillis() - Long.parseLong(timeStamp) > expiry){
			result.put("paramExpire", 1);
		} else{
			Object[] params = new Object[]{to, code, timeStamp};
			ResultResource rp = DBUtil.getInstance().query(sqlProperties.getProperty("loadReset"), params);
			try {
				//not existed
				if(!rp.getRs().next()){
					resetId = -1;
				}
				//already reset
				else if(rp.getRs().getInt("reset") == 1){
					resetId = 0;
				}
				//reset now
				else{
					resetId = rp.getRs().getInt("id");
				}
			} catch (SQLException e) {
				log.error(e.getMessage() + " when fetching results.");
			} finally{
				rp.close();
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
	
	public JSONObject resetPass(String to, int codeLength, String subject, String htmlTemplate){
		log.debug("Reseting pass.");
		JSONObject result = new JSONObject();
		ResultResource rp = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{to});
		try {
			//already existed
			if(rp.getRs().next()){
				result.put("accExists", 1);
				rp.close();
				rp = DBUtil.getInstance().query(sqlProperties.getProperty("loadLastResetByEmail"), new Object[]{to});
				//reset email sent
				if(rp.getRs().next()){
					//already reset
					result.put("hadReset", 1);
					if(rp.getRs().getInt("reset") == 1){
						genResetEmail(to, codeLength, subject, htmlTemplate);
						result.put("reset", 1);
						result.put("extra", "resendEmail");
					}
					//not reset
					else{
						result.put("reset", 0);
						//not expired
						if(System.currentTimeMillis() - Long.parseLong(rp.getRs().getString("time_stamp")) < expiry){
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
			rp.close();
		}
		return result;
	}
	
	public JSONObject signIn(String email, String password){
		log.debug("Signing in.");
		JSONObject result = new JSONObject();
		ResultResource rp = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{email});
		try {
			//not existed
			if(!rp.getRs().next()){
				result.put("exists", 0);
			}
			//password mismatch
			else if(!password.equals(rp.getRs().getString("password"))){
				result.put("exists", 1);
				result.put("passMatch", 0);
			} else {
				result.put("exists", 1);
				result.put("passMatch", 1);
				User user = new User();
				user.loadUserProperties(rp.getRs());
				result.put("user", user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			rp.close();
		}
		log.debug("Signing in processed.");
		return result;
	}
	
	public JSONObject signUp(String to, String password, int codeLength, String subject, String htmlTemplate){
		log.debug("Signing up.");
		JSONObject result = new JSONObject();
		String id = "";
		ResultResource rp = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{to});
		try {
			//already existed
			if(rp.getRs().next()){
				result.put("exists", 1);
				//verified
				if(rp.getRs().getInt("verified") == 1){
					result.put("verified", 1);
				}
				//not verified
				else{
					result.put("verified", 0);
					rp.close();
					rp = DBUtil.getInstance().query(sqlProperties.getProperty("loadLastVerifyByEmail"), new Object[]{to});
					//email not sent
					if(!rp.getRs().next()){
						genVerifyEmail(to, codeLength, subject, htmlTemplate);
						result.put("email", 0);
						result.put("extra", "resendEmail");
					}
					//email sent
					else{
						result.put("email", 1);
						//not expired
						if(System.currentTimeMillis() - Long.parseLong(rp.getRs().getString("time_stamp")) < expiry){
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
			rp.close();
		}
		log.debug("Signing up processed.");
		return result;
	}
	
	public JSONObject signInFB(User user){
		log.debug("Signing in with Facebook.");
		JSONObject result = new JSONObject();
		String userId = "";
		ResultResource rp = DBUtil.getInstance().query(sqlProperties.getProperty("loadUserByEmail"), new Object[]{user.getEmail()});
		try {
			//new user
			if(!rp.getRs().next()){
				user.setId(genUserId());
				DBUtil.getInstance().update(sqlProperties.getProperty("addNewFBUser"), new Object[]{userId, "", user.getEmail(), user.getFBId(), user.getFirstName(), user.getLastName(), user.getName(), user.getTimeZone(), user.getLocale(), user.getGender(), 1});
				result.put("user", user);
				result.put("new", 1);
			}
			//return user
			else{
				userId = rp.getRs().getString("id");
				user.setId(userId);
				result.put("id", user);
				result.put("new", 0);
			}
			result.put("user", user);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			rp.close();
		}
		log.debug("Signing in with Facebook processed.");
		return result;
	}
	
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
	
	private void genNewPassEmail(String to, String pass, String subject, String htmlTemplate){
		MyEmail email = new MyEmail(to, subject, htmlTemplate.replace("[!PUT LINK HERE]", pass));
		EmailUtil.getInstance().sendHtmlMail(email);		
	}
	
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
	
	private String composeActivateLink(String to, String code, String timeStamp){
		StringBuffer link = new StringBuffer("http://localhost/EchoBond_Demo/AccountActiveServlet?email=");
		link.append(to);
		link.append("&code=");
		link.append(code);
		link.append("&timeStamp=");
		link.append(timeStamp);
		return link.toString();
	}
	
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
