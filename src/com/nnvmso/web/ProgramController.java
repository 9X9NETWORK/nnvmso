package com.nnvmso.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.google.appengine.api.datastore.KeyFactory;
import com.nnvmso.json.AwsS3Post;
import com.nnvmso.lib.AwsLib;
import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.ProgramScript;
import com.nnvmso.service.AuthService;
import com.nnvmso.service.ChannelManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.ProgramManager;

@Controller
@RequestMapping("show")
@SessionAttributes("program")
public class ProgramController {
	private final ProgramManager programMngr;
	private final AuthService authService;
	private final ChannelManager channelService;
	private final String viewRoot = "program/";

	@Autowired
	public ProgramController(AuthService authService,
			ProgramManager programMngr, ChannelManager channelService) {
		this.authService = authService;
		this.programMngr = programMngr;
		this.channelService = channelService;
	}

	/*
	 * upload thumbnail
	 */
	@RequestMapping(value = "thumbupload/{channelId}/{programId}", method = RequestMethod.GET)
	public @ResponseBody
	AwsS3Post thumbUpload(@PathVariable long channelId,
			@PathVariable long programId, Model model, HttpServletRequest req) {
		MsoProgram program = programMngr.findById(programId);
		// others
		String bucketName = "com-nnaws-showthumb";
		Properties pro = AwsLib.getAwsCredentials();
		String accessKey = pro.getProperty("accessKey");
		String acl = "public-read"; // private

		// prepare aws document
		AwsS3Post s3 = new AwsS3Post();
		String uploadUrl = "http://" + bucketName + ".s3.amazonaws.com/";
		// !!!! it should be updated after amazon returns a success, cheating
		// here.
		program.setImageUrl(uploadUrl + NnLib.getKeyStr(program.getKey()));
		programMngr.save(program);
		s3.setUpload_url(uploadUrl);
		s3.setAWSAccessKeyId(accessKey);
		s3.setKey(NnLib.getKeyStr(program.getKey()));
		s3.setAcl(acl);
		String redirectUrl = "http://" + req.getServerName() + ":"
				+ req.getServerPort();
		s3.setSuccess_action_redirect(redirectUrl);
		s3.setBucket_name(bucketName);
		s3.setContent_type("image/");
		// !!!! to be removed
		s3.setFilename("filename");
		s3.setX_amz_meta_filename("filename");
		s3.setX_amz_meta_token("token");
		s3.setX_amz_meta_creatDate("date");
		s3.setPid(program.getId());
		// !!! need better handling
		String policy = AwsLib.getPolicy(s3);
		String signature = AwsLib.getSignature(policy);
		s3.setSignature(signature);
		s3.setPolicy(policy);

		return s3;
	}

	/*
	 * create program via file upload params
	 */
	@RequestMapping(value = "fileupload/{channelId}", method = RequestMethod.GET)
	public @ResponseBody
	AwsS3Post fileUpload(@PathVariable long channelId, Model model,
			HttpServletRequest req) {
		System.out.println(DebugLib.OUT + "fileupload");
		// filename, type, create content
		String fileFullName = req.getParameter("filename");
		int dot = fileFullName.lastIndexOf(".");
		String fileName = fileFullName.substring(0, dot);
		String ext = fileFullName.substring(dot + 1, fileFullName.length());
		String contentType = MsoProgram.TYPE_VIDEO;
		if (ext.equals("ppt") || ext.equals("pps")) {
			contentType = MsoProgram.TYPE_SLIDESHOW;
		}
		MsoProgram program = new MsoProgram(fileName);
		program.setChannelId(channelId);
		program.setType(contentType);
		program.setImageUrl("/WEB-INF/../images/thumb_noImage.jpg");
		programMngr.create(program);
		System.out.println("filename=" + fileName + ";" + program.getName());
		// others
		String bucketName = "com-nnaws";
		Properties pro = AwsLib.getAwsCredentials();
		String accessKey = pro.getProperty("accessKey");
		String redirectUrl = "http://" + req.getServerName() + ":"
				+ req.getServerPort() + "/aws/contentDrop";
		String contentKey = KeyFactory.keyToString(program.getKey());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String createDate = df.format(program.getCreateDate());
		String acl = "public-read"; // private
		String token = "token";

		// prepare aws document
		AwsS3Post s3 = new AwsS3Post();
		String uploadUrl = "http://" + bucketName + ".s3.amazonaws.com/";
		s3.setUpload_url(uploadUrl);
		s3.setFilename(fileName);
		s3.setAWSAccessKeyId(accessKey);
		s3.setKey(contentKey);
		s3.setAcl(acl);
		s3.setSuccess_action_redirect(redirectUrl);
		s3.setBucket_name(bucketName);
		s3.setContent_type(contentType);
		s3.setX_amz_meta_filename(fileFullName);
		s3.setX_amz_meta_token(token);
		s3.setX_amz_meta_creatDate(createDate);
		s3.setPid(program.getId());

		// !!! move awslib to s3 object
		String policy = AwsLib.getPolicy(s3);
		String signature = AwsLib.getSignature(policy);
		s3.setSignature(signature);
		s3.setPolicy(policy);

		return s3;
	}

	/*
	 * prepare program create form
	 */
	@RequestMapping(value = "create/{channelId}", method = RequestMethod.GET)
	public String createForm(@PathVariable long channelId, Model model) {
		MsoChannel channel = channelService.findById(channelId);
		MsoProgram p = new MsoProgram();
		p.setChannelId(channelId);
		model.addAttribute("channel", channel);
		model.addAttribute("program", p);
		String fileBucketName = "com-nnaws";
		String thumbBucketName = "com-nnaws-showthumb";
		String fileUploadUrl = "http://" + fileBucketName + ".s3.amazonaws.com/";				
		String thumbUploadUrl = "http://" + thumbBucketName + ".s3.amazonaws.com/";				
		model.addAttribute("file_upload_url", fileUploadUrl);
		model.addAttribute("thumb_upload_url", thumbUploadUrl);
		model.addAttribute("action", "create");
		return viewRoot + "programForm";
	}

	/*
	 * create program based on file url
	 */
	@RequestMapping(value = "create/{channelId}", method = RequestMethod.POST)
	public @ResponseBody AwsS3Post createSubmit(@PathVariable long channelId, HttpServletRequest req) {
		String fileUrl = req.getParameter("fileUrl");
		String filename = "";
		URL url;
		MsoProgram p = new MsoProgram();
		try {
			url = new URL(fileUrl);
			filename = url.getFile().substring(1);
			p.setName(filename);
			p.setChannelId(channelId);
			p.setImageUrl("/WEB-INF/../images/thumb_noImage.jpg");
			p.setType(MsoProgram.TYPE_VIDEO);
			p.setWebMFileUrl(fileUrl);
			p.setMpeg4FileUrl(fileUrl); // !!!!!
			programMngr.create(p);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		AwsS3Post s3 = new AwsS3Post();
		s3.setFilename(filename);
		s3.setPid(p.getId());
		return s3;
	}

	@RequestMapping(value = "edit/{programId}", method = RequestMethod.GET)
	public String editForm(@PathVariable long programId, Model model) {
		MsoProgram program = programMngr.findById(programId);
		MsoChannel channel = channelService.findById(program.getChannelKey()
				.getId());
		String bucketName = "com-nnaws-showthumb";
		String uploadUrl = "http://" + bucketName + ".s3.amazonaws.com/";
		model.addAttribute("thumb_upload_url", uploadUrl);
		model.addAttribute("program", program);
		model.addAttribute("channel", channel);
		model.addAttribute("action", "edit");
		return viewRoot + "programForm";
	}

	@RequestMapping(value = "edit/{programId}", method = RequestMethod.POST)
	public @ResponseBody
	String editSubmit(@PathVariable long programId, HttpServletRequest req, SessionStatus status) {			
		System.out.println("programid=" + programId);
		MsoProgram program = programMngr.findById(programId);
		String fileUrl = req.getParameter("fileUrl");
		System.out.println(DebugLib.OUT + fileUrl);
		if (fileUrl != null) {
			System.out.println("enter here");
			program.setWebMFileUrl(fileUrl);
			program.setMpeg4FileUrl(fileUrl);
		}
		program.setImageUrl(req.getParameter("imageUrl"));
		program.setName(req.getParameter("name"));
		program.setIntro(req.getParameter("intro"));
		programMngr.save(program);
		status.setComplete();
		return "";
	}

	@RequestMapping(value = "clips/{channelId}")
	public String clips(@PathVariable long channelId, Model model, HttpSession session) {			
		List<MsoProgram> onair = programMngr.findByChannelIdAndIsPublic(
				channelId, true);
		List<MsoProgram> offair = programMngr.findByChannelIdAndIsPublic(
				channelId, false);
		model.addAttribute("onair", onair);
		model.addAttribute("offair", offair);
		return (viewRoot + "clips");

	}

	@RequestMapping(value = "delete/{programIds}")
	public ResponseEntity<String> delete(@PathVariable String programIds, HttpSession session) {			
		String[] split = programIds.split(",");
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		for (int i = 0; i < split.length; i++) {
			programs.add(programMngr.findById(Long.parseLong(split[i])));
		}
		programMngr.deleteAll(programs);
		return new ResponseEntity<String>("", HttpStatus.OK);
	}

	@RequestMapping(value = "onoff/{programIds}", method = RequestMethod.POST)
	public ResponseEntity<String> onoff(@PathVariable String programIds,
			HttpServletRequest req) {
		// !!! find by id AND msokey
		String[] split = programIds.split(",");
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		boolean isPublic = false;
		if (req.getParameter("isPublic").equals("true")) {
			isPublic = true;
		}
		for (int i = 0; i < split.length; i++) {
			MsoProgram p = programMngr.findById(Long.parseLong(split[i]));
			programs.add(p);
		}
		programMngr.setOnOff(programs, isPublic);
		return new ResponseEntity<String>("", HttpStatus.OK);
	}

	@RequestMapping(value = "ordering/{programIds}")
	public @ResponseBody String ordering(@PathVariable String programIds) {	
		String[] split = programIds.split(",");
		List<MsoProgram> programs = new ArrayList<MsoProgram>();
		for (int i = 0; i < split.length; i++) {
			MsoProgram p = programMngr.findById(Long.parseLong(split[i]));
			programs.add(p);
		}
		programMngr.setOrders(programs);
		return "";
	}
	
	@RequestMapping(value = "scriptEdit/{programId}", method = RequestMethod.GET)
	public String scriptEditForm(@PathVariable long programId, Model model) {		
		MsoProgram p = programMngr.findGroupById(programId);
		ProgramScript s = p.getNnScript();
		if (s == null) {
			s = new ProgramScript();
		}
		model.addAttribute("script", s);
		return viewRoot + "scriptForm";
	}
	
	@RequestMapping(value = "scriptEdit/{programId}", method = RequestMethod.POST)
	public String scriptEditSubmit(@PathVariable long programId, @ModelAttribute ProgramScript script, Model model) {
		MsoProgram p = programMngr.findGroupById(programId);
		ProgramScript s = p.getNnScript();
		if (s== null) {
			s = new ProgramScript();
		}
		s.setScript(script.getScript());
		s.setProgram(p);
		p.setNnScript(s);

		programMngr.save(p);
		model.addAttribute("script", script);
		return viewRoot + "scriptForm";
	}	
	
	// !!!!!! detached child
	@RequestMapping(value = "list/{cIdStr}")
	public String list(@PathVariable String cIdStr, Model model, HttpSession session) {			
		// find mso's channels
		Mso mso = (Mso) authService.getAuthSession(session, "mso");
		List<MsoChannel> channels = channelService.findByMso(mso.getKey());
		long channelId;
		if (cIdStr.equals("*")) {
			channelId = channels.get(0).getKey().getId();
		} else {
			boolean exists = false;
			channelId = Long.parseLong(cIdStr);
			// !!!! everyone can see everyone's channels, for easier integration
			MsoManager mService = new MsoManager();
			mso = mService.findByKey(NnLib.getKeyStr(channelService.findById(channelId).getMsoKey()));					
			channels = channelService.findByMso(mso.getKey());
			// !!!! for integration testing end
			for (MsoChannel c : channels) {
				System.out.println("channel id=" + c.getKey().getId());
				if (c.getKey().getId() == channelId) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				channelId = channels.get(0).getId();
			}
		}
		List<MsoProgram> onair = programMngr.findByChannelIdAndIsPublic(channelId, true);		
		List<MsoProgram> offair = programMngr.findByChannelIdAndIsPublic(channelId, false);
		 
		model.addAttribute("currentChannel", channelId);
		model.addAttribute("channels", channels);
		model.addAttribute("onair", onair);
		model.addAttribute("offair", offair);

		return ("program/programList");
	}

}
