package com.nnadmin.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
 
@Controller
@RequestMapping("hello")
public class HelloController {

	//protected static final Logger log = Logger.getLogger(HelloController.class.getName());
	protected static final Logger log = Logger.getLogger(HelloController.class);
    
	//basic test
    @RequestMapping("world")
    @ResponseBody
    public String world(HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession();
		session.setMaxInactiveInterval(1);
        String message = "Hello NnCloudTv";
        return message;
    }    

	//basic test
    @RequestMapping("view")
    public ModelAndView view(HttpServletRequest req) throws Exception {
		HttpSession session = req.getSession();
		session.setMaxInactiveInterval(1);
        String message = "Hello NnCloudTv";
        return new ModelAndView("hello", "message", message);
    }    
    
}
    
