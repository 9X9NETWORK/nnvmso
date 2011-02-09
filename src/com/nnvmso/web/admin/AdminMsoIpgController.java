package com.nnvmso.web.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.Mso;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoIpgManager;
import com.nnvmso.service.MsoManager;

@Controller
@RequestMapping("admin/msoIpg")
public class AdminMsoIpgController {
	protected static final Logger logger = Logger.getLogger(AdminMsoIpgController.class.getName());
	public final MsoIpgManager ipgMngr;
	public final MsoChannelManager channelMngr;
	public final MsoManager msoMngr;
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}
	
	@Autowired
	public AdminMsoIpgController(MsoIpgManager ipgMngr,
	                             MsoChannelManager channelMngr,
	                             MsoManager msoMngr) {
		this.ipgMngr = ipgMngr;		
		this.channelMngr = channelMngr;
		this.msoMngr = msoMngr;
	}	
	
	@RequestMapping("list")
	public ResponseEntity<String> list(@RequestParam String mso) {		
		List<MsoIpg> ipg = (List<MsoIpg>)ipgMngr.findAllByMsoId(Long.parseLong(mso));
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		if (ipg != null) {
			for (MsoIpg i : ipg) {
				MsoChannel c = channelMngr.findById(i.getChannelId()); 
				c.setSeq(i.getSeq());
				c.setType(i.getType());
				if (c!= null) {channels.add(c);};
			}
		}
		String output = "channel id\tseq\ttype\tname (type, 1=general; 2=readonly)\n";
		for (MsoChannel c : channels) {
			String[] result = {
					String.valueOf(output + c.getKey().getId()),
					String.valueOf(c.getSeq()),
					String.valueOf(c.getType()),										
					c.getName()
			};
			output = NnStringUtil.getDelimitedStr(result) + "\n";
		}
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping(value="delete")
	public @ResponseBody String delete(@RequestParam(value="mso") String msoId,
	                                   @RequestParam(value="channel") String channelId) {
		
		ipgMngr.deleteMsoIpg(Long.parseLong(msoId), Long.parseLong(channelId));
		return "OK";
	}
	
	@RequestMapping(value="add")
	public @ResponseBody String add(@RequestParam(required=true, value="mso") String msoId,
	                                @RequestParam(required=true, value="channel") String channelId,
	                                @RequestParam(required=true) String seq,
	                                @RequestParam(required=true) String type) {
		
		if (Short.parseShort(type) != 1 && Short.parseShort(type) != 2)
			return "Invalid Type";
		
		MsoChannel channel = channelMngr.findById(Long.parseLong(channelId));
		if (channel == null)
			return "Channel Does Not Exist";
		Mso mso = msoMngr.findById(Long.parseLong(msoId));
		if (mso == null)
			return "Mso Does Not Exist";
		
		if (ipgMngr.findByMsoIdAndChannelId(Long.parseLong(msoId), Long.parseLong(channelId)) != null)
			return "Channel Is Already Subscribed";
		if (ipgMngr.findByMsoIdAndSeq(Long.parseLong(msoId), Integer.parseInt(seq)) != null)
			return "Seq Is Already In Used";
		
		ipgMngr.create(new MsoIpg(Long.parseLong(msoId), Long.parseLong(channelId), Integer.parseInt(seq), Short.parseShort(type)));
		return "OK";
	}
	
	@RequestMapping(value="modify")
	public @ResponseBody String modify(@RequestParam(required=true, value="mso") String msoId,
	                                   @RequestParam(required=true, value="channel") String channelId,
	                                   @RequestParam(required=false) String seq,
	                                   @RequestParam(required=false) String type) {
		
		MsoChannel channel = channelMngr.findById(Long.parseLong(channelId));
		if (channel == null)
			return "Channel Does Not Exist";
		Mso mso = msoMngr.findById(Long.parseLong(msoId));
		if (mso == null)
			return "Mso Does Not Exist";
		
		MsoIpg msoIpg = ipgMngr.findByMsoIdAndChannelId(Long.parseLong(msoId), Long.parseLong(channelId));
		if (msoIpg == null)
			return "Subscription Does Not Exist";
		if (seq != null) {
			if (ipgMngr.findByMsoIdAndSeq(Long.parseLong(msoId), Integer.parseInt(seq)) != null)
				return "Seq Is Already In Used";
			msoIpg.setSeq(Integer.parseInt(seq));
		}
		if (type != null) {
			if (Short.parseShort(type) != 1 && Short.parseShort(type) != 2)
				return "Invalid Type";
			msoIpg.setType(Short.parseShort(type));
		}
		ipgMngr.save(msoIpg);
		return "OK";
	}
}
