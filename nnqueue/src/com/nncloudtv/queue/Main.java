package com.nncloudtv.queue;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nncloudtv.lib.QueueMessage;
import com.nncloudtv.model.Category;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.service.CategoryManager;
import com.nncloudtv.service.MsoManager;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

@Component
public class Main
{
	
	@SuppressWarnings("unchecked")
	public static void main (String[] args) throws Exception {
		String exchange_name = args[0];
		if (!exchange_name.equals(QueueMessage.HELLO) &&
			!exchange_name.equals(QueueMessage.BRAND_COUNTER) && 
			!exchange_name.equals(QueueMessage.CATEGORY_CREATE) && 
			!exchange_name.equals(QueueMessage.CHANNEL_CREATE_RELATED) && 
			!exchange_name.equals(QueueMessage.TRANSCODING_SUBMIT)) {		
			System.out.println("[*] Please check your input [brand_counter, category_create, channel_create_related, transcoding_submit]");
			System.exit(0);
		}
			
		ConnectionFactory factory = new ConnectionFactory();
	    //factory.setHost("localhost");
	    //Connection connection = factory.newConnection();
		Connection connection = factory.newConnection("localhost");
	    Channel channel = connection.createChannel();                  
	    channel.exchangeDeclare(exchange_name, "fanout");
	    String queueName = channel.queueDeclare().getQueue();
	    channel.queueBind(queueName, exchange_name, "");
	    System.out.println(" [*] Waiting for messages from " + exchange_name + ". To exit press CTRL+C");
	
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(queueName, true, consumer);
	     
	    MsoManager msoMngr = new MsoManager();
	
	    while (true) {
	       QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	       String message = new String(delivery.getBody());
	       System.out.println("message:" + message);
	       if (exchange_name.equals(QueueMessage.BRAND_COUNTER)) {
		       System.out.println(" [x] Brand Counter Received '" + message + "'");
		       msoMngr.addMsoInfoVisitCounter(message);
	       } else if (exchange_name.equals(QueueMessage.CATEGORY_CREATE)) {
		       System.out.println(" [x] Category Create Received '" + message + "'");
		       Object[] obj = (Object[])new QueueMessage().toObject(delivery.getBody());
		       new CategoryManager().addCategory((Long)obj[0], (ArrayList<Category>)obj[1]);
	       } else if (exchange_name.equals(QueueMessage.CHANNEL_CREATE_RELATED)) {
	           Object[] obj = (Object[])new QueueMessage().toObject(delivery.getBody());
	       	   NnChannel c = (NnChannel)obj[0];
	           List<Category> categories = (List<Category>)obj[1];
	           new CategoryManager().createChannelRelated(c, categories);
	       } else if (exchange_name.equals(QueueMessage.TRANSCODING_SUBMIT)) {
	        	/*
				TranscodingService tranService = new TranscodingService();
				tranService.submitToTranscodingService(channel.getId(), sourceUrl, req);
				*/
	       }
	    }
    }
}
