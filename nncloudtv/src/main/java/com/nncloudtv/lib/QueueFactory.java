package com.nncloudtv.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class QueueFactory {

	protected static final Logger log = Logger.getLogger(QueueFactory.class.getName());

	public final static String QUEUE_NNCLOUDTV = "QUEUE_NNCLOUDTV";
	
	public static byte[] toByteArray (Object obj)
	{
	   byte[] bytes = null;
	   ByteArrayOutputStream bos = new ByteArrayOutputStream();
	   try {
	      ObjectOutputStream oos = new ObjectOutputStream(bos); 
	      oos.writeObject(obj);
	      oos.flush(); 
	      oos.close(); 
	      bos.close();
	      bytes = bos.toByteArray ();
	   } catch (IOException ex) {	   
	   }
	  return bytes;
	}
	
	//for now, assuming if obj is not null, then it's a json request
	public static void add(HttpServletRequest req, String url, Object json) {
    	ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection;
		try {
			connection = factory.newConnection();
			Channel channel;
			channel = connection.createChannel();
	        channel.queueDeclare(QueueFactory.QUEUE_NNCLOUDTV, true, false, false, null);
	        
			Object[] obj = new Object[2];			
			String root = NnNetUtil.getUrlRoot(req);
			String msg = root.concat(url);
			obj[0] = msg;
			obj[1] = json;
	        channel.basicPublish( "", QueueFactory.QUEUE_NNCLOUDTV, 
	                    MessageProperties.PERSISTENT_TEXT_PLAIN,
	                    QueueFactory.toByteArray(obj));	        	        
	        channel.close();
	        connection.close();		
	        log.info(" [x] Sent '" + msg.toString() + "'");
		} catch (IOException e) {
			e.printStackTrace();
		}        
	}
	
}
