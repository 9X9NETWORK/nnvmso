package com.nnvmso.web.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.ContentOwnershipManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;

@Controller
@RequestMapping("admin/cms")
public class AdminCmsController {
	
	protected static final Logger logger = Logger.getLogger(AdminCmsController.class.getName());
	
	public final UserService userService;
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";
	}
	
	public AdminCmsController() {
		
		this.userService = UserServiceFactory.getUserService();
	}
	
	@RequestMapping("ownership/list")
	public ResponseEntity<String> ownershipList(@RequestParam(required = false) Long msoId) {
		
		MsoManager msoMngr = new MsoManager();
		String result = "";
		
		if (msoId != null) {
			Mso mso = msoMngr.findById(msoId);
			if (mso == null)
				return logErrorAndReturn("Ivalid msoId");
		}
		
		
		String[] title = { "id", "msoId", "contentType(1=CHANNELSET,2=CHANNEL)", "contentId" };
		
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		
		List<ContentOwnership> oss;
		if (msoId == null)
			oss = ownershipMngr.findAll();
		else
			oss = ownershipMngr.findAllByMsoId(msoId);
		
		for (ContentOwnership ownership : oss) {
			String[] ori = {
					String.valueOf(ownership.getKey().getId()),
					String.valueOf(ownership.getMsoId()),
					String.valueOf(ownership.getContentType()),
					String.valueOf(ownership.getContentId()),
			};
			result = result + NnStringUtil.getDelimitedStr(ori);
			result = result + "\n";
		}
		String output = NnStringUtil.getDelimitedStr(title) + "\n" + result;
		return NnNetUtil.textReturn(output);
	}
	@RequestMapping("ownership/addChannel")
	public @ResponseBody String ownershipAddChannel(@RequestParam Long msoId, @RequestParam String channelIds) {
		
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoManager msoMngr = new MsoManager();
		
		Mso mso = msoMngr.findById(msoId);
		if (mso == null)
			return "Invalid msoId";
		
		List<MsoChannel> channelList = channelMngr.findChannelsByIdStr(channelIds);
		List<MsoChannel> resultList = ownershipMngr.create(mso, channelList);
		
		String output = "";
		for (MsoChannel channel : resultList) {
			output = output + channel.getKey().getId() + "\t" + channel.getName() + "<br/>";
		}
		return output;
	}
	
	private ResponseEntity<String> logErrorAndReturn(String error) {
		logger.warning(error);
		return NnNetUtil.textReturn(error);
	}
	
	@RequestMapping("ownership/deleteChannel")
	public @ResponseBody String ownershipDeleteChannel(@RequestParam Long msoId, @RequestParam String channelIds) {
		
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoManager msoMngr = new MsoManager();
		
		List<MsoChannel> resultList = new ArrayList<MsoChannel>();
		
		Mso mso = msoMngr.findById(msoId);
		if (mso == null)
			return "Invalid msoId";
		
		List<MsoChannel> channelList = channelMngr.findChannelsByIdStr(channelIds);
		for (MsoChannel channel : channelList) {
			ContentOwnership ownership = ownershipMngr.findByMsoIdAndChannelId(msoId, channel.getKey().getId());
			if (ownership != null) {
				ownershipMngr.delete(ownership);
				resultList.add(channel);
			}
		}
		
		String output = "";
		for (MsoChannel channel : resultList) {
			output = output + channel.getKey().getId() + "\t" + channel.getName() + "<br/>";
		}
		return output;
	}
}
