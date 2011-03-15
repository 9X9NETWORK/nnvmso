package com.nnvmso.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.Model;

import com.nnvmso.lib.CookieHelper;
import com.nnvmso.model.Mso;

public class PlayerService {
	
	public Model prepareBrand(Model model, String msoName, HttpServletResponse resp) {
		if (msoName != null) {
			msoName = msoName.toLowerCase();
		} else {
			msoName = Mso.NAME_9X9;
		}
		
		FBService fbService = new FBService();
		model = fbService.setBrandMetadata(model, msoName);
		if (msoName.equals(Mso.NAME_5F)) {
			model.addAttribute("brandInfo", "5f");
			model.addAttribute("gaqAccount", "UA-21595932-2");		
			model.addAttribute("gaqDomainName", "5f.tv");
			CookieHelper.setCookie(resp, CookieHelper.MSO, Mso.NAME_5F);
		} else {
			model.addAttribute("brandInfo", "9x9");
			model.addAttribute("gaqAccount", "UA-21595932-1");		
			model.addAttribute("gaqDomainName", "9x9.tv");
			CookieHelper.deleteCookie(resp, CookieHelper.MSO); //delete brand cookie
		}
		return model;		
	}

}
