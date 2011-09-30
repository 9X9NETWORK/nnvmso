package com.nnvmso.web;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.nnvmso.lib.NnNetUtil;
import com.nnvmso.model.Captcha;
import com.nnvmso.service.CaptchaManager;

@Controller
@RequestMapping("captcha")
public class CaptchaController {

	protected static final Logger log = Logger.getLogger(CaptchaController.class.getName());		
	
	@RequestMapping("test")
	public ResponseEntity<String> test(@RequestParam(value="file", required=false) String file,			
				                         HttpServletRequest req) {
		String sample1 = "1\ttuscan order\t1/a6273b589df2dfdbd8fe35b1011e3183.jpg\n";
		String sample2 = "2\toopps\t1/b6273b589df2dfdbd8fe35b1011e3183.jpg\n";
		String sample3 = "2\twhat\t1/c6273b589df2dfdbd8fe35b1011e3183.jpg\n";
		String input = sample1 + sample2 + sample3;		
		String[] line = input.split("\n");
		ArrayList<Captcha> list = new ArrayList<Captcha>();
		for (String l : line) {
			String[] data = l.split("\t");
			long batch = Long.parseLong(data[2].substring(0, 1));
			System.out.println("batch:" + batch + "name:");
			Captcha c = new Captcha(batch, data[1], data[2]);
			list.add(c);
		}
		CaptchaManager mngr = new CaptchaManager();
		mngr.saveAll(list);
		return NnNetUtil.textReturn("upload");
	}
	
	@RequestMapping("uploadToTask")
	public ResponseEntity<String> uploadToTask(
			@RequestParam(value="file", required=false) String file,	
			HttpServletRequest req) {
		QueueFactory.getDefaultQueue().add(
			      TaskOptions.Builder.withUrl("/captcha/upload")
			        .param("file", String.valueOf(file)));		
		return NnNetUtil.textReturn("queued in the task");
	}
	
	@RequestMapping("upload")
	public ResponseEntity<String> upload(@RequestParam(value="file", required=false) String file,			
				                         HttpServletRequest req) {		

		if (file == null)
			return NnNetUtil.textReturn("data missing");
		String[] line = file.split("\n");
		ArrayList<Captcha> list = new ArrayList<Captcha>();
		try {
			for (String l : line) {
				String[] data = l.split("\t");
				long batch = Long.parseLong(data[2].substring(0, 1));
				Captcha c = new Captcha(batch, data[1], data[2]);
				list.add(c);
			}
			log.info("lines processed:" + line.length);
		} catch (ArrayIndexOutOfBoundsException e) {
			log.info("data form not expected");
		}
		CaptchaManager mngr = new CaptchaManager();
		mngr.saveAll(list);
		return NnNetUtil.textReturn("upload");
	}
	
}
