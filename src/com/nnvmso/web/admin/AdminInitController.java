package com.nnvmso.web.admin;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.PiwikLib;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnUser;
import com.nnvmso.service.ChannelSetChannelManager;
import com.nnvmso.service.ChannelSetManager;
import com.nnvmso.service.InitService;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.MsoProgramManager;
import com.nnvmso.service.NnUserManager;
import com.nnvmso.service.TranscodingService;
import com.nnvmso.web.json.transcodingservice.PostUrl;

/**
 * for testing only, works only for small set of data
 * 
 * most of the functions are private, turned it on if you need them.
 */


/* 2nd run */
//wipe out data: Category, CategoryChannelSet, (ChannelSet, ChannelSetChannel, ContentOwnership)
//admin/set/deleteMso?id=8001
//ChannelStatusMapper (mark all the channels to waiting_approval status)

//initSetsToTask?isEnglish=true&isDevel=false
//initChannelsToTask?isEnglish=true&isDevel=false  @@@
//initCategoriesToTask?isEnglish=true&isDevel=false  
//initSetAndChannelsToTask?isEnglish=true&isDevel=false @@@
//initCategoryAndSetsToTask?isEnglish=true&isDevel=false
//initRecommdned?isEnglish=true
//initSetImagesToTask?isEnglish=true

//initSetsToTask?isEnglish=false&isDevel=false
//initChannelsToTask?isEnglish=false&isDevel=false
//initCategoriesToTask?isEnglish=false&isDevel=false
//initSetAndChannelsToTask?isEnglish=false&isDevel=false
//initCategoryAndSetsToTask?isEnglish=false&isDevel=false
//initRecommdned?isEnglish=false
//initSetImagesToTask?isEnglish=false

//initCategoryCountToTask

@Controller
@RequestMapping("admin/init")
public class AdminInitController {
	protected static final Logger log = Logger.getLogger(AdminInitController.class.getName());		
	
	private final InitService initService;		
	
	@Autowired
	public AdminInitController(InitService initService) {
		this.initService = initService;
	}		
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		if (e.getClass().equals(MissingServletRequestParameterException.class) ||
			e.getClass().equals(IllegalStateException.class)) {
		} else {
			NnLogUtil.logException(e);			
		}
		return "error/exception";				
	}
	
	//local machine
	@RequestMapping(value="groundStart", method=RequestMethod.GET)
	public String groundStartGet(HttpServletRequest req) {
		return "admin/groundStart";
	}
	
	@RequestMapping(value="groundStart", method=RequestMethod.POST)
	public String groundStartPost(HttpServletRequest req) {
		String host = NnNetUtil.getUrlRoot(req);
		if (host.equals("http://localhost:8888")) {
			initService.initAll(true, true);
		}
		return "admin/groundStart";
	}

	@RequestMapping("file")
	public ResponseEntity<String> file() {
		return NnNetUtil.textReturn("You will receive an email when it isdone.");
	}

	@RequestMapping("youtubeLink")
	public ResponseEntity<String> youtubeLink(
			@RequestParam(value="url",required=false) String url) {
			String result = YouTubeLib.formatCheck(url);
			return NnNetUtil.textReturn(result);
	}

	@RequestMapping("specialSorting")
	public ResponseEntity<String> specialSorting(
			@RequestParam(value="channel",required=false) long channelId,
			HttpServletRequest req) {
		initService.specialSorting(channelId, req);
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}		
	
	//gae environment	
	@RequestMapping("initChannelsToTask")
	public ResponseEntity<String> initChannelsToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initChannels")
			         .param("isEnglish", String.valueOf(isEnglish))
			         .param("isDevel", String.valueOf(isDevel))			      			      
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initChannels")
	public ResponseEntity<String> initChannels(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,			
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initChannels(isEnglish, isDevel);
		this.sendEmail("init all the channels done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initYoutubeOriNameToTask")
	public ResponseEntity<String> initYoutubeOriNameToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initYoutubeOriName")
			         .param("isEnglish", String.valueOf(isEnglish))
			         .param("isDevel", String.valueOf(isDevel))			      			      
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initYoutubeOriName")
	public ResponseEntity<String> initYoutubeOriName(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,			
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initYoutubeOriName(isEnglish, isDevel);
		this.sendEmail("initYoutubeOriName done", "done");
		return NnNetUtil.textReturn("OK");		
	}
	
	
	@RequestMapping("initSetsToTask")
	public ResponseEntity<String> initSetsToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSets")
			        .param("isEnglish", String.valueOf(isEnglish))   
			        .param("isDevel", String.valueOf(isDevel))			        
			      );			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initSets")
	public ResponseEntity<String> initSets(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSets(isEnglish, isDevel);
		this.sendEmail("init all the sets done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initCategoriesToTask")
	public ResponseEntity<String> initCategoriesToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel			
			) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initCategories")
			        .param("isEnglish", String.valueOf(isEnglish))   
			        .param("isDevel", String.valueOf(isDevel))			        			      
			      );			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initCategories")
	public ResponseEntity<String> initCategories(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish, 
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategories(isEnglish);
		this.sendEmail("init all the categories done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initSetAndChannelsToTask")
	public ResponseEntity<String> initSetAndChannelsToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {									
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSetAndChannels")
			        .param("isEnglish", String.valueOf(isEnglish))   
			        .param("isDevel", String.valueOf(isDevel))			        			      			      
			      );			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initSetAndChannels")
	public ResponseEntity<String> initSetAndChannels(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,				
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSetAndChannels(isEnglish);
		this.sendEmail("init all the SetsAndChannels done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initCategoryAndSetsToTask")
	public ResponseEntity<String> initCategoryAndSetsToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {										
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initCategoryAndSets")
			        .param("isEnglish", String.valueOf(isEnglish))   
			        .param("isDevel", String.valueOf(isDevel)));			        			      			      			      
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initCategoryAndSets")
	public ResponseEntity<String> initCategoryAndSets(
		@RequestParam(value="isEnglish",required=false) boolean isEnglish,
		@RequestParam(value="isDevel",required=false) boolean isDevel,				
		HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategoryAndSets(isEnglish);
		this.sendEmail("init all the CategoryAndSets done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initRecommdned")
	public ResponseEntity<String> initRecommended(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initRecommended(isEnglish);
		this.sendEmail("init all the Recommended done", "done");
		return NnNetUtil.textReturn("OK");		
	}
	
	@RequestMapping("initSetImagesToTask")
	public ResponseEntity<String> initSetImagesToTask(HttpServletRequest req) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSetImages"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}	

	@RequestMapping("initSetImages")
	public ResponseEntity<String> initSetImages(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSetImages();
		this.sendEmail("init all the initSetImagesToTask done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initCategoryCountToTask")
	public ResponseEntity<String> initCategoryCountToTask(HttpServletRequest req) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initCategoryCount"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}	
	
	//temp fix
	@RequestMapping("initCategoryCount")
	public ResponseEntity<String> initCategoryCount(HttpServletRequest req) {
		initService.setRequest(req);
		initService.initCategoryCount();
		this.sendEmail("init all the CategoryCount done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	//temp fix
	@RequestMapping("addMsoConfig")
	public ResponseEntity<String> initMso(HttpServletRequest req) {
		initService.setRequest(req);
		initService.addMsoConfig();
		return NnNetUtil.textReturn("OK");		
	}

	//temp fix
	@RequestMapping("badChannelReportToTask")
	public ResponseEntity<String> badChannelReportToTask() {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/badChannelReport"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("mapleTest")
	public ResponseEntity<String> mapleTest(HttpServletRequest req) {		
		MsoChannelManager channelMngr = new MsoChannelManager();
		long[] ids = {
				4989396 				
		};
		for (long id: ids) {
			MsoChannel c = channelMngr.findById(id);
			if (c == null) {				
				log.info("channel not found:" + id);
			} else {
				log.info("resubmit:" + id);
				TranscodingService tranService = new TranscodingService();
				tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);				
			}
		}
		return NnNetUtil.textReturn("OK");		
	}				

	@RequestMapping("statusChange")
	public ResponseEntity<String> statusChange(HttpServletRequest req) {		
		MsoChannelManager channelMngr = new MsoChannelManager();
		long[] ids = {
				4458625,
				4507132,
				4535459,
				4467357,
				4447970,
				4490257,
				4484403,
				4467858,
				4491096,
				4458914,
				4492132,
				4447485,
				4392431,
				4481448,
				4599433,                                    
				4638023,
				4585910,
				4591999,
				4077178,
				4534113,
				4605690,
				4583658,
				4598634,
				4586306,
				4484407,
				4449957,
				4477508,
				4449959,
				4487215,
				4458972,
				4491157,
				4492249,
				4576784,
				4485162,
				4472592,
				4490431,
				4468763,
				4472985,
				4571588,
				4612262,
				4434218,
				4570791,
				4579678,
				4565104,
				4599094,
				4415214,
				4562915,
				4832241,
				4426972,
				4560933,
				4541346,
				4427909,
				4420726,
				4565833,
				4430392,
				4484407,
				4449957,
				4477508,
				4449959,
				4487215,
				4458972,
				4491157,
				4492249,
				4566436,
				4576784,
				4561940,
				4586306,
				4576913,
				4401253,
				4598610,
		};
		for (long id: ids) {
			MsoChannel c = channelMngr.findById(id);
			if (c == null) {				
				log.info("channel not found:" + id);
			} else {
				c.setStatus(MsoChannel.STATUS_WAIT_FOR_APPROVAL);
				channelMngr.save(c);
			}
		}
		return NnNetUtil.textReturn("OK");		
	}				
	
//	@RequestMapping("mapleTest")
//	public ResponseEntity<String> mapleTest(HttpServletRequest req) {
//		
//	}
	
	//temp fix
	@RequestMapping("postTest")
	public ResponseEntity<String> postTest(HttpServletRequest req) {
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmail("mso@9x9.tv");		
		MsoChannelManager channelMngr = new MsoChannelManager();
			
		String url="http://www.maplestage.net/show/台灣演义/";
		PostUrl postUrl = new PostUrl();
		postUrl.setRss(url);	
		//String transcodingServer = "http://puppy.9x9.tv/admin/init/mapleReceive";
		String transcodingServer = "http://puppy.9x9.tv/playerAPI/mapleReceive";
		NnNetUtil.urlPostWithJson(transcodingServer, postUrl);
//		MsoChannel c = channelMngr.findBySourceUrlSearch(url);
//		if (c == null) {					
//			log.info("create maple:" + url);
//		}

		return NnNetUtil.textReturn("OK");		
	}		

	@RequestMapping("mapleNamesToTask")
	public ResponseEntity<String> mapleNamesToTask(HttpServletRequest req) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/mapleNames"));			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}	
	
	@RequestMapping("mapleNames")
	public ResponseEntity<String> mapleNames(
			HttpServletRequest req) {
		initService.setRequest(req);		
		//initService.excelTest(false);
		initService.mapleNames(false, false);
		this.sendEmail("mapleNames", "done");
		return NnNetUtil.textReturn("OK");
	}

	//temp fix
	@RequestMapping("missingUser")
	public ResponseEntity<String> missingUser() {
		NnUserManager userMngr = new NnUserManager();
		NnUser user = new NnUser("mso@9x9.tv", "9x9mso", "9x9 mso", NnUser.TYPE_NN);
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso();
		user.setMsoId(mso.getKey().getId());
		userMngr.create(user, null);
		return NnNetUtil.textReturn("OK");		
	}		
	
	//temp fix
	@RequestMapping("badChannelReport")
	public ResponseEntity<String> badChannelReport() {
		String report = initService.reportBadChannels();
		System.out.println(report);
		this.sendEmail("badChannelReport", report);
		return NnNetUtil.textReturn("OK");		
	}		
	
	public void sendEmail(String subject, String msgBody) {
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
        	Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("nncloudtv@gmail.com", "nncloudtv"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("nncloudtv@gmail.com", "nncloudtv"));                             
            msg.setSubject(subject);
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (Exception e) {
        	NnLogUtil.logException(e);
		}					
	}

	@RequestMapping("initPiwikSetToTask")
	public ResponseEntity<String> initPiwikSetToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initPiwikSet")
			         .param("isEnglish", String.valueOf(isEnglish))
			         .param("isDevel", String.valueOf(isDevel))			      			      
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initPiwikSet")
	public ResponseEntity<String> initPiwikSet(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,			
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initPiwikSet(isEnglish, isDevel);
		this.sendEmail("init piwik set", "done");
		return NnNetUtil.textReturn("OK");		
	}

	//admin/init/initChannelPiwikToTask?isEnglish=true&isDevel=false
	//admin/init/initChannelPiwikToTask?isEnglish=false&isDevel=false
	//admin/init/initPiwikSetToTask?isEnglish=false&isDevel=false
	//admin/init/initNnChannelsPiwikToTask
	@RequestMapping("initChannelPiwikToTask")
	public ResponseEntity<String> initChannelPiwikToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initChannelPiwik")
			         .param("isEnglish", String.valueOf(isEnglish))
			         .param("isDevel", String.valueOf(isDevel))			      			      
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initChannelPiwik")
	public ResponseEntity<String> initChannelPiwik(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,			
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initChannelPiwik(isEnglish, isDevel);
		this.sendEmail("init all the channels piwik done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("testPiwik")
	public ResponseEntity<String> testPiwik(
			@RequestParam(value="id",required=false) long id,			
			HttpServletRequest req) {
		MsoChannelManager mngr = new MsoChannelManager();
		MsoChannel c = mngr.findById(id);
		if (c != null) {
			String piwikId = PiwikLib.createPiwikSite(0, id, req);
			c.setPiwik(piwikId);
			mngr.save(c);
		}
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initNnChannelsPiwikToTask")
	public ResponseEntity<String> initNnChannelsPiwikToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initNnChannelsPiwik")
			         .param("isEnglish", String.valueOf(isEnglish))
			         .param("isDevel", String.valueOf(isDevel))			      			      
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}
	
	@RequestMapping("initNnChannelsPiwik")
	public ResponseEntity<String> initNnChannelsPiwik(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,			
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initNnChannelsPiwik(isEnglish, isDevel);
		this.sendEmail("init all the 9x9 channels piwik done", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initSetPublicToTask")
	public ResponseEntity<String> initSetPublicToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSetPublic")
			         .param("isEnglish", String.valueOf(isEnglish))
			         .param("isDevel", String.valueOf(isDevel))			      			      
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}

	@RequestMapping("initSetPublic")
	public ResponseEntity<String> initSetPublic(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,			
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSetPublic(isEnglish, isDevel);
		this.sendEmail("init all set to public", "done");
		return NnNetUtil.textReturn("OK");		
	}

	@RequestMapping("initSetChannelPublicToTask")
	public ResponseEntity<String> initSetChannelPublicToTask(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/admin/init/initSetChannelPublic")
			         .param("isEnglish", String.valueOf(isEnglish))
			         .param("isDevel", String.valueOf(isDevel))			      			      
		);			          
		return NnNetUtil.textReturn("You will receive an email when it is done.");
	}

	@RequestMapping("initSetChannelPublic")
	public ResponseEntity<String> initSetChannelPublic(
			@RequestParam(value="isEnglish",required=false) boolean isEnglish,
			@RequestParam(value="isDevel",required=false) boolean isDevel,			
			HttpServletRequest req) {
		initService.setRequest(req);
		initService.initSetChannelPublic(isEnglish, isDevel);
		this.sendEmail("init all set channels to public and success", "done");
		return NnNetUtil.textReturn("OK");		
	}	

	@RequestMapping("recommendedCleanup")
	public ResponseEntity<String> recommendedCleanup(@RequestParam(required=false) String lang) {			
		ChannelSetManager setMngr = new ChannelSetManager();		
		List<ChannelSet> sets = setMngr.findFeaturedSets(lang);
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		String output = "";
		for (ChannelSet s : sets) {
			List<ChannelSetChannel> cscs = cscMngr.findByChannelSet(s);
			int[] seqs = new int[cscs.size()+1];
			for (ChannelSetChannel csc : cscs) {
				try {
					if (seqs[csc.getSeq()] != 0)
						output += "duplication:" + s.getKey().getId() + "\n";
					else
						seqs[csc.getSeq()] = csc.getSeq();
				} catch (ArrayIndexOutOfBoundsException e) {
					output += "error:" + s.getKey().getId() + "\n"; 
				}
					
			}
		}
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping("seqChange")
	public ResponseEntity<String> seqChange(HttpServletRequest req) {		
		MsoProgramManager programMngr = new MsoProgramManager();
		long[] ids = {
				5087015,
				5073025,
				5087014,
				5074033,
				5036146,
				5024353,
				5027171,
				5034182,
				5035189,
				5026097,
				5037112,
				5031104,
				5035017,
				5027016,
				5041015,
				5024023,
				5001148,
				5014115,
				4999086,
				5010082,
				4984756,
				4970361,
				4981770,
				4981769,
				4981493,
				4986190,
				4986189,
				4983234,
				4990080,
				4979087,
				4979086,
				4981194,
				4944214,
				4942216,
				4928189,
				4948181,
				4945128,
				4922142,
				4955146,
				4912154,
				4912085,
				4957057,
				4958063,
				4939074,
				4898354,
				4901309,
				4883710,
				4893372,
				4899277,
				4899276,
		};
		int i=0;
		for (long id: ids) {
			MsoProgram p = programMngr.findById(id);
			if (p == null) {				
				log.info("program not found:" + id);
			} else {				
				String seq = String.format("%08d", i++);
				p.setSeq(seq);
				programMngr.save(p);
			}
		}
		return NnNetUtil.textReturn("OK");		
	}				
	
}
