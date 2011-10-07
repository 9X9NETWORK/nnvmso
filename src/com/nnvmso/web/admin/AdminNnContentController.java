package com.nnvmso.web.admin;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.LangTable;
import com.nnvmso.model.NnContent;
import com.nnvmso.service.NnContentManager;

@Controller
@RequestMapping("admin/content")
public class AdminNnContentController {

	protected static final Logger logger = Logger.getLogger(AdminNnContentController.class.getName());		
	
	private final NnContentManager contentMngr;
	private final UserService userService;
	
	@Autowired
	public AdminNnContentController(NnContentManager contentMngr) {
		this.contentMngr = contentMngr;
		this.userService  = UserServiceFactory.getUserService();
	}

	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}	
	
	@RequestMapping(value="create")
	public @ResponseBody String create(@RequestParam String key, 
			                           @RequestParam String text,
			                           @RequestParam String lang) {
		logger.info("admin = " + userService.getCurrentUser().getEmail());
		if (lang == null)
			lang = LangTable.LANG_EN;
		if (!lang.equals(LangTable.LANG_EN) && !lang.equals(LangTable.LANG_ZH))
			return "lang error";
		
		Text txt = new Text(text); 
		NnContent content = new NnContent(key, txt, lang);
		contentMngr.create(content);
		return "OK";
	}

	@RequestMapping(value="form", method=RequestMethod.GET)
	public String editGet(HttpServletRequest req) {
		return "admin/static.jsp";
	}

	@RequestMapping(value="form", method=RequestMethod.POST)
	public String editPost(
			@RequestParam String key, 
            @RequestParam String text,
            @RequestParam String lang,
			HttpServletRequest req) {
		return "admin/static.jsp";
	}
	
	@RequestMapping(value="edit")
	public @ResponseBody String edit(@RequestParam String key, 
			                         @RequestParam String text,
			                         @RequestParam String lang) {
		NnContent content = contentMngr.findByItemAndLang(key, lang);
		if (content == null)
			return "does not exist";
		if (lang == null)
			lang = LangTable.LANG_EN;
		if (!lang.equals(LangTable.LANG_EN) && !lang.equals(LangTable.LANG_ZH))
			return "lang error";
		
		Text txt = new Text(text);
		content.setContent(txt);
		content.setLang(lang);
		contentMngr.save(content);
		return "OK";
	}
	
	@RequestMapping("list")
	public ResponseEntity<String> list() {
		//find all programs, including the not public ones
		List<NnContent> list = contentMngr.findAll();
		String[] title = {"id", "key", "value"};		
		String output = "";
		for (NnContent c : list) {
			String[] ori = {String.valueOf(c.getKey().getId()),
							c.getItem(),
	                        String.valueOf(c.getContent().getValue())};	                        
			output = output + NnStringUtil.getDelimitedStr(ori) + "\n";			
		}
		String result = NnStringUtil.getDelimitedStr(title) + "\n" + output;
		return NnNetUtil.textReturn(result);
	}
	
	
}
