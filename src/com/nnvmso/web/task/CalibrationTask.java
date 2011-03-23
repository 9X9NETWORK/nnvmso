package com.nnvmso.web.task;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.service.CategoryChannelManager;
import com.nnvmso.service.CategoryManager;
import com.nnvmso.service.MsoChannelManager;

/**
 * temporary fix, to be removed
 *
 */
@Controller
@RequestMapping("task/calibration")
public class CalibrationTask {
	protected static final Logger log = Logger.getLogger(CalibrationTask.class.getName());
	
	@RequestMapping("categoryChannelCount")
	public ResponseEntity<String> categoryChannelCount() throws IOException {
		try {						
			QueueFactory.getDefaultQueue().add(
					TaskOptions.Builder.withUrl("/task/calibration/runCategories")
		    );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return NnNetUtil.textReturn("OK");
	}

	@RequestMapping(value="runCategories")
	public ResponseEntity<String> runCategories(HttpServletRequest req) {
		String output = "";
		CategoryManager cMngr = new CategoryManager();
		List<Category> categories = cMngr.findAll();
		for (Category c : categories) {
			try {						
				QueueFactory.getDefaultQueue().add(
						TaskOptions.Builder.withUrl("/task/calibration/runCategoryChannels")
						     .param("category", String.valueOf(c.getKey().getId()))
			    );
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return NnNetUtil.textReturn(output);
	}
	
	@RequestMapping(value="runCategoryChannels")
	public ResponseEntity<String> runCategoryChannels(HttpServletRequest req) {
		int categoryId = Integer.parseInt(req.getParameter("category"));
		CategoryManager cMngr = new CategoryManager();
		CategoryChannelManager ccMngr = new CategoryChannelManager();
		Category category = cMngr.findById(categoryId);
		List<CategoryChannel> list = ccMngr.findAllByCategoryId(categoryId);
		category.setChannelCount(list.size());
		cMngr.save(category);
		log.info("calibrat category:" + category.getName() + "(" + category.getKey().getId() + ") with " + list.size() + " channels.");
		return NnNetUtil.textReturn("OK");
	}	
	
}
