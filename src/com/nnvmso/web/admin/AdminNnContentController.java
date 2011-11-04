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
import org.springframework.web.servlet.ModelAndView;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.nnvmso.lib.NnLogUtil;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.lib.NnStringUtil;
import com.nnvmso.model.LangTable;
import com.nnvmso.model.NnContent;
import com.nnvmso.model.NnUserReport;
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
	
	@RequestMapping(value="form", method=RequestMethod.GET)
	public ModelAndView formEdit(
			@RequestParam(value="id", required=false)String id,
			HttpServletRequest req) {	
		ModelAndView mv = new ModelAndView("admin/static");
		if (id != null) {
			NnContent content = contentMngr.findById(Long.parseLong(id));
			mv.addObject("key", content.getItem());
			mv.addObject("lang", content.getLang());
			mv.addObject("text", content.getContent().getValue());
		}
		return mv;
	}

	@RequestMapping(value="form", method=RequestMethod.POST)
	public String formPost(HttpServletRequest req) {			
		String key = req.getParameter("key");
		String lang = req.getParameter("lang");
		String text = req.getParameter("text");
		NnContent content = contentMngr.findByItemAndLang(key, lang);
		if (content == null) {
			Text txt = new Text(text);
			content = new NnContent(key, txt, lang);
		}
		if (lang == null)
			lang = LangTable.LANG_EN;
		if (!lang.equals(LangTable.LANG_EN) && !lang.equals(LangTable.LANG_ZH))
			lang = LangTable.LANG_EN;	
		
		Text txt = new Text(text);
		content.setContent(txt);
		content.setLang(lang);
		contentMngr.save(content);
		return "redirect:/admin/content/list";
	}
		
	@RequestMapping("list")
	public ResponseEntity<String> list() {
		//find all programs, including the not public ones
		List<NnContent> list = contentMngr.findAll();				
		String output = "<p><INPUT TYPE='button' onClick=\"parent.location='form'\" value='create'></p>";
		for (NnContent c : list) {
			output += "<p>" + "<a href='form?id=" + c.getKey().getId() + "'>" +
			          c.getKey().getId() + "<a>\t" + c.getItem() + "\t" +
			          c.getLang() + "</p>";
		}
		return NnNetUtil.htmlReturn(output);
	}	
	
}
