package com.nnvmso.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.appengine.repackaged.org.apache.commons.logging.Log;
import com.google.appengine.repackaged.org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Controller
@RequestMapping("email")
public class EmailController {

	protected final Log logger = LogFactory.getLog(getClass());
	
	@RequestMapping("send")
	public @ResponseBody String send() {
		Properties props = new Properties();
        //Session session = Session.getDefaultInstance(props, null);
		Session session = Session.getInstance(props);
		
		String msgBody = "Transcoding is finished.";

        try {
        	logger.info("Trying to send an email");
        	System.out.println("Trying to send an email");
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("yiwen@teltel.com", "teltel"));
            msg.addRecipient(Message.RecipientType.TO,
                             new InternetAddress("yiwen.teltel@gmail.com", "yiwen"));
            msg.setSubject("Transcoding done");
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (AddressException e) {
        	System.out.println("exception1");
        	logger.info("AddressException = " + e.toString());
        } catch (MessagingException e) {
        	System.out.println("exception2");
        	logger.info("MessagingException = " + e.toString());
        } catch (UnsupportedEncodingException e) {
        	System.out.println("exception3");
        	logger.info("UnsupportedEncodingException = " + e.toString());
		}	
        return "sent";
	}
	
}
