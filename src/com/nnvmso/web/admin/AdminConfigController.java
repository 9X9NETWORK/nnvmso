package com.nnvmso.web.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jsr107cache.Cache;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nnvmso.lib.CacheFactory;
import com.nnvmso.lib.CookieHelper;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoConfig;
import com.nnvmso.service.MsoConfigManager;
import com.nnvmso.service.MsoManager;
import com.nnvmso.service.TranscodingService;

@Controller
@RequestMapping("admin/config")
public class AdminConfigController {
			
	//check transcoding server setting
	@RequestMapping("transcodingServer")
	public ResponseEntity<String> transcodingServer(HttpServletRequest req) {
		TranscodingService tranService = new TranscodingService();
		tranService.getTranscodingEnv(req);
		String[] transcodingEnv = tranService.getTranscodingEnv(req);
		String transcodingServer = transcodingEnv[0];
		String callbackUrl = transcodingEnv[1];
		
		String output = "transcoding server: " + transcodingServer + "\n";
		output = output + "callback server: " + callbackUrl;
		
		return NnNetUtil.textReturn(output);
	}
	
	//check current mso, if it returns the expected data
	@RequestMapping("mso")
	public ResponseEntity<String> mso(HttpServletRequest req) {
		Mso mso = new MsoManager().findMsoViaHttpReq(req);
		String output = "";
		if (mso != null) { output = mso.getName(); }
		return NnNetUtil.textReturn(output);
	}
	
	//change mso, for devel testing on local machine
	@RequestMapping("changeMso")
	public ResponseEntity<String> changeMso(@RequestParam(value="mso",required=false) String mso, HttpServletRequest req, HttpServletResponse resp) {
		CookieHelper.setCookie(resp, CookieHelper.MSO, mso);
		return NnNetUtil.textReturn("OK");		
	}
	
	//change config 
	@RequestMapping("changeConfig")
	public ResponseEntity<String> changeConfig(@RequestParam(value="msoName",required=false) String msoName,
			                                   @RequestParam(value="key",required=false) String key,
			                                   @RequestParam(value="value", required=false) String value ) {
		MsoManager msoMngr = new MsoManager();
		MsoConfigManager configMngr = new MsoConfigManager();		
		
		Mso mso = msoMngr.findByName(msoName);
		if (mso == null) { return NnNetUtil.textReturn("mso not found");}
		MsoConfig config = configMngr.findByMsoIdAndItem(mso.getKey().getId(), key);
		if (config == null) { config = new MsoConfig();	}
		config.setItem(key);
		config.setValue(value);
		configMngr.save(config);
		return NnNetUtil.textReturn("OK");
	}
	
}
