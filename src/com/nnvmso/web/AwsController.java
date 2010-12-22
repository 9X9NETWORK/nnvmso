package com.nnvmso.web;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nnvmso.json.AwsMessage;
import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.NnLib;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.service.ProgramManager;

@Controller
@RequestMapping("aws")
public class AwsController {
	
	protected static final Logger logger = Logger.getLogger(AwsController.class.getName());

	private final ProgramManager programMngr;
		
	@Autowired
	public AwsController(ProgramManager programMngr) {
		this.programMngr = programMngr;
	}
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLib.logException(e);
		return "error/exception";				
	}		

	/**
	 * Notify AWS a file has been delivered
	 */
	@RequestMapping(value="contentDrop")
	public void contentDrop(HttpServletRequest req, HttpServletResponse resp) {		
        try {
    		//prepare data
        	String bucket = req.getParameter("bucket");
    		long id = Long.parseLong(req.getParameter("id"));
    		MsoProgram p = programMngr.findById(id);    		
    		String token = "";
    		Date createDate = new Date();
    		//prepare url fetch to aws api server, http://9x9cloud.tv/api/9x9encode.php	
        	String awsApiServer = "awsapi.9x9cloud.tv";
        	String awsApiPort = "80";
        	String filePath = "/api/9x9encode.php";
        	if (!DebugLib.LOCAL_TEST) {
        		awsApiServer = "localhost";
        		awsApiPort = "8888";
        		filePath = "/hello";
        	} 
    		String urlStr = "http://" + awsApiServer + ":" + awsApiPort + filePath;
    		logger.info("LOGGING: urlstr = " + urlStr);
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
    		AwsMessage msg = new AwsMessage(bucket, NnLib.getKeyStr(p.getKey()), createDate.toString(), token);
    		msg.setType(p.getType());
    		ObjectMapper mapper = new ObjectMapper();    		    		
            mapper.writeValue(writer, msg);
            logger.info("json:" + mapper.writeValueAsString(msg));
            writer.close();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.info("CONNECTION FAILED = " + connection.getResponseCode());
            }
        } catch (Exception e) {
        	logger.info("CONNECTION exception"); 
        	e.printStackTrace();
        }		
	}
	
	/**
	 * Receive transcoding status from AWS
	 */	
	@RequestMapping("contentUpdate")
	public @ResponseBody String contentUpdate(@RequestBody AwsMessage msg) {
		logger.info("aws content update1:" + ";bucketname=" + msg.getBucketName() + ";fileurl=" + msg.getFileUrl() + ";key=" + msg.getKey());
		logger.info("aws content update2:" + ";type=" + msg.getType() + ";error=" + msg.getErrorCode() + ";reason=" + msg.getErrorReason());
		ProgramManager pService = new ProgramManager();
		pService.saveViaAws(msg);
		return "";
	}
	
}
