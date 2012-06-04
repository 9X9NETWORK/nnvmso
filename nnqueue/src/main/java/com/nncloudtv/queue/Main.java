package com.nncloudtv.queue;

import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.nncloudtv.lib.QueueFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

@Component
public class Main
{
	protected final static Logger log = Logger.getLogger(Main.class.getName());
	
	public static void main (String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare(QueueFactory.QUEUE_NNCLOUDTV, true, false, false, null);	    
	    log.info("[*] Waiting for messages. To exit press CTRL+C");
	    channel.basicQos(1);
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(QueueFactory.QUEUE_NNCLOUDTV, false, consumer);
	    while (true) {
	      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	      Task.doWork(delivery.getBody());
	      channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	    }	    
    }
	
}
