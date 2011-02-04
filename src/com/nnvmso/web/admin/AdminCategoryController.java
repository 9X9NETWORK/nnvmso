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
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.CategoryChannelManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.MsoChannelManager;
import com.nnvmso.service.MsoManager;

@Controller
@RequestMapping("admin/category")
public class AdminCategoryController {
	
	protected static final Logger logger = Logger.getLogger(AdminCategoryController.class.getName());
	
	private final CategoryManager categoryMngr;	
	
	@Autowired
	public AdminCategoryController(CategoryManager categoryMngr) {
		this.categoryMngr = categoryMngr;
	}	

	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/exception";				
	}
	
	//list every channel under a category
	@RequestMapping(value="channelList")
	public ResponseEntity<String> categoryCount(@RequestParam("category")long category) {		
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		MsoChannelManager cMngr = new MsoChannelManager();
		List<CategoryChannel> ccs = ccMngr.findAllByCategoryId(category);
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (CategoryChannel cc : ccs) {
			channels.add(cMngr.findById(cc.getChannelId()));
		}
		String output = "";
		output = "category count = " + new CategoryManager().findById(category).getChannelCount() + "\n";
		output = output + "id \t name \t program count \t status \t isPublic\n";
		for (MsoChannel c : channels) {
			output = output + c.getKey().getId() + "\t" + c.getName() + "\t" + + c.getProgramCount() + "\t" + c.getStatus() + "\t" + c.isPublic() + "\n";
		}
		return NnNetUtil.textReturn(output);
	}
	
	
	@RequestMapping("list")
	public ResponseEntity<String> list(@RequestParam(required=false)String id) {
		
		List<Category> categories = null;
		if (id == null) {
			categories = categoryMngr.findAll();
		} else {	
			categories = categoryMngr.findAllByMsoId(Long.parseLong(id));
		}
		
		String[] title = {"id", "msoId", "isPublic", "channelCount", "name"};
		String result = "";
		for (Category c:categories) {
			String[] ori = {String.valueOf(c.getKey().getId()),
			                Long.toString(c.getMsoId()),
			                Boolean.toString(c.isPublic()),
			                String.valueOf(c.getChannelCount()),
			                c.getName()};
			result = result + NnStringUtil.getDelimitedStr(ori);
			result = result + "\n";
		}
		String output = NnStringUtil.getDelimitedStr(title) + "\n" + result;
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping("create")
	public @ResponseBody String create(@RequestParam(required=true)String name,
	                                   @RequestParam(required=true)String msoId) {
		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findById(Long.parseLong(msoId));
		if (mso == null)
			return "Invalid msoId";
		categoryMngr.create(new Category(name, true, Long.parseLong(msoId)));
		return "OK";
	}
	
	@RequestMapping("modify")
	public @ResponseBody String modify(@RequestParam(required=true)  long id,
	                                   @RequestParam(required=false) String name,
	                                   @RequestParam(required=false) String isPublic,
	                                   @RequestParam(required=false) String channelCount) {
		
		logger.info("name: " + name + " isPublic: " + isPublic + " channelCount: " + channelCount + " id: " + id);
		Category category = categoryMngr.findById(id);
		if (category == null)
			return "Category Not Found";
		
		if (name != null)
			category.setName(name);
		if (isPublic != null)
			category.setPublic(Boolean.valueOf(isPublic));
		if (channelCount != null)
			category.setChannelCount(Integer.parseInt(channelCount));
		
		categoryMngr.save(category);
		return "OK";
	}
}
