package com.echobond.util;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Luck
 * @author Luck
 *
 */
public class EmailThread extends Thread {
	private Message msg;
	private Logger log = LogManager.getLogger("Email");
	public EmailThread(Message msg) {
		this.msg = msg;
	}
	public Message getMsg() {
		return msg;
	}
	public void setMsg(Message msg) {
		this.msg = msg;
	}

	@Override
	public void run() {
		boolean success = true;
		try {
			log.debug("Sending email.");
			Transport.send(msg);
		} catch (MessagingException e) {
			log.debug(e.getMessage() + " when sending email.");
			success = false;
		}
		if(success){
			log.debug("Email sent.");
		} else {
			log.debug("Failed sending email.");
		}
	}
}
