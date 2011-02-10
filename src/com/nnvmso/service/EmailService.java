package com.nnvmso.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.NnLogUtil;

/**
 *  This service is for potential future use, not well structured.
 *
 */
@Service
public class EmailService {
	private String fromEmail = "nncloudtv@gmail.com";
	private String fromName = "nncloudtv";
	
	public void sendEmail(String subject, String msgBody, String recipientEmail, String recipientName) {
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        try {
        	Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail, fromName));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail, recipientName));                             
            msg.setSubject(subject);
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (Exception e) {
        	NnLogUtil.logException(e);
		}					
	}	
	
	public void sendEmailToAdmin(String subject, String msgBody) {
		this.sendEmail(subject, msgBody, fromEmail, fromName);
	}
	

}
