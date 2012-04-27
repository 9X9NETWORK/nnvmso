package com.nncloudtv.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.nncloudtv.dao.NnChannelDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.PMF;
import com.nncloudtv.lib.YouTubeLib;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnEmail;
import com.nncloudtv.model.NnSet;
import com.nncloudtv.model.Pdr;
import com.nncloudtv.service.EmailService;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnSetManager;
import com.nncloudtv.service.PdrManager;
import com.nncloudtv.service.PlayerApiService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
 
@Controller
@RequestMapping("hello")
public class HelloController {

	protected static final Logger log = Logger.getLogger(HelloController.class.getName());

	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) { 
		model.addAttribute("message", "Spring Security Hello World");
		return "hello";
 
	}
	@RequestMapping("logger")
    public ModelAndView logger(HttpServletRequest req) throws Exception {
		String message = log.getName() + "\n";
		message += log.getLevel(); 
        return new ModelAndView("hello", "message", message);
    }    
	
	//basic test
    @RequestMapping("world")
    public ModelAndView world(HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession();
		session.setMaxInactiveInterval(1);
        String message = "Hello NnCloudTv";
        return new ModelAndView("hello", "message", message);
    }    

    @RequestMapping("log")
    public ModelAndView log()  {
    	log.info("----- hello log -----");
    	log.warning("----- hello warning -----");
    	log.severe("----- hello severe -----");
        return new ModelAndView("hello", "message", "log");
    }    
    
    @RequestMapping("timeout")
    public ModelAndView timeout(HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession();
		session.setMaxInactiveInterval(1);
		for (long i=0; i<500000000; i++) {
			System.out.println(i);
			for (long j=0; j<500000000; j++) {			
			}			
		}
        String message = "Hello NnCloudTv";
        return new ModelAndView("hello", "message", message);
    }    
    
    /*
    @RequestMapping("root")
    public ModelAndView root() { 
        String message = "Hello NnCloudTv";
        return new ModelAndView("hello", "message", message);
    } 
    */   
    
    @RequestMapping("locale")
    public ModelAndView locale(HttpServletRequest req) {
    	String message = "locale name: " + req.getLocalName() + "<br/>" + 
    	                 "locale address: " + req.getLocalAddr() + "<br/>" +  
    	                 req.getLocale().getLanguage();    	
        return new ModelAndView("hello", "message", message);
    }            
    
    @RequestMapping("search")
    public @ResponseBody String search(
    		@RequestParam String text,
    		HttpServletRequest req) {
    	List<NnChannel> channels = NnChannelDao.searchChannelEntries(text);
    	String result = "size:" + channels.size();
    	for (NnChannel c : channels) {
    		result += c.getId() + ";" + c.getName() + "<br/>";
    	}
		return result;
    }            
    
    //test email service
    @RequestMapping("email")
    public @ResponseBody String email(
    		@RequestParam String toEmail, 
    		@RequestParam String toName, 
    		HttpServletRequest req) {
		EmailService service = new EmailService();
		NnEmail mail = new NnEmail(toEmail, toName, NnEmail.SEND_EMAIL_SHARE, "share 9x9", NnEmail.SEND_EMAIL_SHARE, "hello", "world");
		service.sendEmail(mail);
		return "email sent";
    }            

    /*
    @RequestMapping("slave")
    public @ResponseBody String slave() throws Exception{
		PersistenceManager pm = PMF.getAnalyticsSlave().getPersistenceManager();
		try {
			long id = 1;
			Pdr pdr = (Pdr)pm.getObjectById(Pdr.class, id);
			if (pdr != null)
				log.info("<<< detached >>> " + pdr.getId());
			else 
				log.info("<<< detached >>> pdr is empty");
		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}		
        return "OK";
    }    
     */ 
        
    //db test
    @RequestMapping("pdr")
    public @ResponseBody String pdr() throws Exception{
    	try {
    		PersistenceManager pm = PMF.getAnalytics().getPersistenceManager();
			try {
				Pdr raw = new Pdr(1, "session1", "test");
				pm.makePersistent(raw);
    	    }finally {
				pm.close();
			}
    	} catch (JDOFatalDataStoreException e){
    		log.severe("Fatal Exception");
    	} catch (Throwable t) {    		
    		if (t.getCause() instanceof JDOFatalDataStoreException) {
    			log.severe("");
    		}	
    	}
        return "OK";
    }    

    //db test through manager
    @RequestMapping("pdr_mngt")
    public @ResponseBody String pdr_mngt() { 
		PdrManager pdrMngr = new PdrManager();
		Pdr pdr = new Pdr(1, "session1", "test");
		pdrMngr.create(pdr);
        return "OK";
    }                
    
	@RequestMapping("cache_set")
	public ResponseEntity<String> cache_set() {
		String output = "No Cache";
		String cacheValue = (String)CacheFactory.get("hello");
		output = "original: " + cacheValue + "\n";
		if (CacheFactory.isRunning) {
			output += "it's running"  + "\n";
			if (cacheValue == null) {
			   CacheFactory.set("hello", "9x9");
			   cacheValue = (String)CacheFactory.get("hello");
			   output += "after set cache: " + cacheValue;
			}
		} else {
			output += "cache is not running";
		}
		
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping("jdo_cached")
	public ResponseEntity<String> jdoCached() {
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig c = configMngr.findByItem("test");
		return NnNetUtil.textReturn(c.getValue());
	}

	@RequestMapping("set")
	public ResponseEntity<String> set(
			@RequestParam String value) {
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso();
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig c = configMngr.findByItem("test");
		c.setValue(value);
		configMngr.save(mso, c);
		return NnNetUtil.textReturn("set value");
	}

	@RequestMapping("get")
	public ResponseEntity<String> get() {
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig c = configMngr.findByItem("test");
		return NnNetUtil.textReturn(c.getValue());
	}
	
	@RequestMapping("queue_visitor") 
	public ResponseEntity<String> queue_db() {
		boolean status = MsoConfigManager.isQueueEnabled(true);
		PlayerApiService service = new PlayerApiService();
		int cnt = service.addMsoInfoVisitCounter(false);
		return NnNetUtil.textReturn(String.valueOf(status) + ";" + cnt);
	}

	@RequestMapping("queue_addChToSet") 
	public ResponseEntity<String> queue_addChToSet(
			@RequestParam String sid,
			@RequestParam String cid) {
		boolean status = MsoConfigManager.isQueueEnabled(true);
		NnSetManager setMngr = new NnSetManager();
		NnSet set = setMngr.findById(Long.parseLong(sid));
		List<NnChannel> channels = new ArrayList<NnChannel>();
		channels.add(new NnChannelManager().findById(Long.parseLong(cid)));
		setMngr.addChannels(set, channels);
		return NnNetUtil.textReturn(String.valueOf(status));
	}
	        
    @RequestMapping("fanout")
    public @ResponseBody String fanout(@RequestParam String exchange_name) throws IOException {
 	   ConnectionFactory factory = new ConnectionFactory();
	   Connection connection = factory.newConnection("localhost");
       Channel channel = connection.createChannel();

       channel.exchangeDeclare(exchange_name, "fanout");
       String message = "hello superman";
       channel.basicPublish(exchange_name, "", null, message.getBytes());
       System.out.println(" [x] Sent '" + message + "'");
       channel.close();
       connection.close();
       return "OK";
    }
 
    @RequestMapping("youtube")
    public ModelAndView youtube() { 
    	Map<String, String> maps = YouTubeLib.getYouTubeEntry("nike", true);
        String msg = "thumbnail:" + maps.get("thumbnail") + "<br/>";
        msg += "title:" + maps.get("title") + "<br/>";
        msg += "description:" + maps.get("description") + "<br/>";
        msg += "status:" + maps.get("status") + "<br/><br/><br/><br/>";

    	maps = YouTubeLib.getYouTubeEntry("android", true);
        msg += "thumbnail:" + maps.get("thumbnail") + "<br/>";
        msg += "title:" + maps.get("title") + "<br/>";
        msg += "description:" + maps.get("description") + "<br/>";
        msg += "status:" + maps.get("status") + "<br/><br/><br/><br/>";
        
    	maps = YouTubeLib.getYouTubeEntry("98145fdd67deb31d", false);
        msg += "thumbnail:" + maps.get("thumbnail") + "<br/>";
        msg += "title:" + maps.get("title") + "<br/>";
        msg += "description:" + maps.get("description") + "<br/>";
        msg += "status:" + maps.get("status") + "<br/><br/><br/><br/>";

    	maps = YouTubeLib.getYouTubeEntry("98145fdd67d", false);
        msg += "thumbnail:" + maps.get("thumbnail") + "<br/>";
        msg += "title:" + maps.get("title") + "<br/>";
        msg += "description:" + maps.get("description") + "<br/>";
        msg += "status:" + maps.get("status") + "<br/>";
                
        return new ModelAndView("hello", "message", msg);
    }    
 
	@RequestMapping("fiveHundred")
	public ModelAndView fiveHundred(HttpServletRequest req, HttpServletResponse resp) {
		resp.setStatus(500);
		return new ModelAndView("hello", "message", "msg");
	}
    
	@RequestMapping("error")
	public ModelAndView error(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		throw new Exception();
	}
	
    //rabbitmqctl list_queues
    @RequestMapping("receive")
    public @ResponseBody String receive() throws IOException, InterruptedException {
    	String queue_name = "hello";
    	ConnectionFactory factory = new ConnectionFactory();
    	Connection connection = factory.newConnection("localhost");
        Channel channel = connection.createChannel();

        //channel.queueDeclare(queue_name, false, false, false, null);
        channel.queueDeclare(queue_name, false, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queue_name, true, consumer);
        
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        String message = new String(delivery.getBody());
        System.out.println(" [x] Received '" + message + "'");

    	return "OK";
    }

    //test ip behind proxy, aka load balancer
    @RequestMapping("getIp")
    public ResponseEntity<String> getIp(HttpServletRequest req) {
    	String ip = NnNetUtil.getIp(req);
    	return NnNetUtil.textReturn(ip);
    }
    
}
    
