package com.nnvmso.web;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.*;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.nnvmso.json.AwsS3Post;
import com.nnvmso.json.PodcastFeed;
import com.nnvmso.lib.AwsLib;
import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.AuthService;
import com.nnvmso.service.ChannelManager;

@Controller
@RequestMapping("channel")
@SessionAttributes("channel")
public class ChannelController {
	private final AuthService auth;
	private final ChannelManager channelMngr;
	private final String viewRoot = "channel/";
	
	@Autowired
	public ChannelController(AuthService auth, ChannelManager channelMngr) {
		this.auth = auth;
		this.channelMngr = channelMngr;
	}	
	
	/*
	 * upload thumbnail 
	 */
	@RequestMapping(value="thumbupload/{channelId}", method=RequestMethod.GET)
	public @ResponseBody AwsS3Post thumbUpload(@PathVariable long channelId, Model model, HttpServletRequest req) {
		MsoChannel c = channelMngr.findById(channelId);

		String bucketName = "com-nnaws-channelthumb";
		Properties pro = AwsLib.getAwsCredentials();
		String accessKey = pro.getProperty("accessKey");	    
		String acl = "public-read"; //private
		
		//prepare aws document		
		AwsS3Post s3 = new AwsS3Post();
		String uploadUrl = "http://" + bucketName + ".s3.amazonaws.com/";
		//!!!! it should be updated after amazon returns a success, cheating here.
		c.setImageUrl(uploadUrl + NnLib.getKeyStr(c.getKey()));
		channelMngr.save(c);
		s3.setUpload_url(uploadUrl);		
		s3.setAWSAccessKeyId(accessKey);
		s3.setKey(NnLib.getKeyStr(c.getKey()));
		s3.setAcl(acl);
		String redirectUrl = "http://" + req.getServerName() + ":" + req.getServerPort();
		s3.setSuccess_action_redirect(redirectUrl);
		s3.setBucket_name(bucketName);
		s3.setContent_type("image/");
		//!!!! to be removed
		s3.setFilename("filename");
		s3.setX_amz_meta_filename("filename");
		s3.setX_amz_meta_token("token");
		s3.setX_amz_meta_creatDate("date");
		//!!! need better handling
		String policy = AwsLib.getPolicy(s3);
		String signature = AwsLib.getSignature(policy);
		s3.setSignature(signature);
		s3.setPolicy(policy);
				
		return s3; 		
	}

	@RequestMapping(value="podcast", method=RequestMethod.GET)
    public String podcastForm(Model model) {
		MsoChannel channel = new MsoChannel();
		model.addAttribute("channel", channel);
		return (viewRoot + "podcastForm");
    }
	
	@RequestMapping(value="podcast", method=RequestMethod.POST)
	public String podcastSubmit(@ModelAttribute("channel") MsoChannel channel, HttpSession session, SessionStatus status) {		
		Mso mso = (Mso)auth.getAuthSession(session, "mso");			
		channel.setImageUrl("/WEB-INF/../images/thumb_noImage.jpg");
		channel.setPublic(false);
		MsoChannel saved = channelMngr.create(channel, mso);
		status.setComplete();
		
		PodcastFeed feed = new PodcastFeed();
		feed.setKey(NnLib.getKeyStr(saved.getKey()));
		feed.setRss(channel.getPodcast()); 
		String urlStr = "http://awsapi.9x9cloud.tv/api/podpares.php";
		NnLib.urlFetch(urlStr, feed);
		return ("redirect:/channel/edit/" + channel.getKey().getId());
	}
	
	@RequestMapping(value="create", method=RequestMethod.GET)
    public String createForm(Model model) {
		MsoChannel channel = new MsoChannel();
		model.addAttribute("channel", channel);
		model.addAttribute("action", "create");		
		return (viewRoot + "channelForm");
    }
	
	@RequestMapping(value="create", method=RequestMethod.POST)
	public String createSubmit(@ModelAttribute("channel") MsoChannel channel, HttpSession session, SessionStatus status) {		
		Mso mso = (Mso)auth.getAuthSession(session, "mso");
		System.out.println(DebugLib.OUT + "controller: " + mso.getEmail());
		if (channel.getImageUrl().equals("") || channel.getImageUrl() == null) {			
			channel.setImageUrl("/WEB-INF/../images/thumb_noImage.jpg");
		}
		channel.setPublic(false);
		channelMngr.create(channel, mso);
		status.setComplete();
		return ("redirect:/channel/edit/" + channel.getKey().getId());
	}
		
	@RequestMapping("list")
	public String list(Model model, HttpSession session) {
		Mso m = (Mso)auth.getAuthSession(session, "mso");
		List<MsoChannel> onair = channelMngr.findByIsPublic(m.getKey(), true);
		List<MsoChannel> offair = channelMngr.findByIsPublic(m.getKey(), false);
		System.out.println("onair size:" + onair.size());
		System.out.println("offair size:" + offair.size());
		int others = channelMngr.MAX_MSOCHANNEL_SIZE - onair.size() - offair.size();
		model.addAttribute("onair", onair);
		model.addAttribute("offair", offair);
		model.addAttribute("others", others);
		model.addAttribute("system", channelMngr.SYSTEM_CHANNEL_SIZE);
		return (viewRoot + "channelList");
	}
	
	@RequestMapping(value="edit/{channelId}", method=RequestMethod.GET)
	public String editForm(@PathVariable long channelId, Model model) {
		MsoChannel channel = channelMngr.findById(channelId);
		model.addAttribute("channel", channel);
		model.addAttribute("action", "edit");
		String thumbBucketName = "com-nnaws-channelthumb";
		String thumbUploadUrl = "http://" + thumbBucketName + ".s3.amazonaws.com/";		
		model.addAttribute("thumb_upload_url", thumbUploadUrl);
		return (viewRoot + "channelForm");
	}
	
	@RequestMapping(value="edit/{channelId}", method=RequestMethod.POST)
	public String editSubmit(@PathVariable long channelId, @ModelAttribute("channel") MsoChannel channel, BindingResult result, SessionStatus status) {
		channelMngr.save(channel);
		status.setComplete();
		return (viewRoot + "channelForm");
	}		
	
	@RequestMapping(value="detail/{channelId}")
	public String detail(@PathVariable long channelId, Model model) {
		MsoChannel channel = channelMngr.findById(channelId);
		System.out.println(DebugLib.OUT + channelId);
		model.addAttribute("channel", channel);
		return "channel/info_bubble";
	}

	@RequestMapping(value="onoff/{channelId}", method=RequestMethod.POST)
	public ResponseEntity<String> onoff(@PathVariable long channelId, HttpServletRequest req) {
		//!!! find by id AND msokey
		MsoChannel channel = channelMngr.findById(channelId);
		if (req.getParameter("isPublic").equals("true")) {
			channel.setPublic(true);
		} else {
			channel.setPublic(false);
		}
		channelMngr.save(channel);
		return new ResponseEntity<String>("", HttpStatus.OK);
	}
	
	@RequestMapping(value="delete/{channelId}")
	public ResponseEntity<String> delete(@PathVariable long channelId, Model model) {
		//!!! find by id AND msokey
		MsoChannel channel = channelMngr.findById(channelId);
		channelMngr.delete(channel);
		return new ResponseEntity<String>("", HttpStatus.OK);
	}
	
}
