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

import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoIpgManager;

@Controller
@RequestMapping("admin/msoIpg")
public class AdminMsoIpgController {
	protected static final Logger logger = Logger.getLogger(AdminMsoIpgController.class.getName());
	public final MsoIpgManager ipgMngr;
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}
	
	@Autowired
	public AdminMsoIpgController(MsoIpgManager ipgMngr) {
		this.ipgMngr = ipgMngr;		
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
		String output = "id\tname\tseq\ttype (1=general; 2=readonly)\n";
		for (MsoChannel c : channels) {
			String[] result = {
					String.valueOf(output + c.getKey().getId()),
					c.getName(),
					String.valueOf(c.getSeq()),
					String.valueOf(c.getType())										
			};
			output = NnStringUtil.getDelimitedStr(result) + "\n";
		}
		return NnNetUtil.textReturn(output);
	}

}
