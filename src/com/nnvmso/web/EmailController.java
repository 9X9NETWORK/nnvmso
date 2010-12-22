package com.nnvmso.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLib;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

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

	protected static final Logger logger = Logger.getLogger(EmailController.class.getName());
	
	@RequestMapping("send")
	public @ResponseBody String send() {
		Properties props = new Properties();
		Session session = Session.getInstance(props);
		
		String msgBody = "Transcoding is finished.";

        try {
        	logger.info("Trying to send an email");
        	System.out.println("Trying to send an email");
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("nncloudtv@gmail.com", "9x9cloudtv"));
            msg.addRecipient(Message.RecipientType.TO,
                             new InternetAddress("nncloudtv@gmail.com", "9x9cloudtv"));
            msg.setSubject("Transcoding done");
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (Exception e) {
        	NnLib.logException(e);
		}	
        return "sent";
	}
	
}
