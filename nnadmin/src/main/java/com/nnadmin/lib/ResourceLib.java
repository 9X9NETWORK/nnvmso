package com.nnadmin.lib;

import java.io.IOException;
import java.util.Properties;

public class ResourceLib {
	
	static public String getExternalRootPath() {
		Properties properties = new Properties();
		String result = "";
		try {
			properties.load(ResourceLib.class.getClassLoader().getResourceAsStream("resource.properties"));
			result = properties.getProperty("static_file_root_path");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	static public String getApiUrlRootPath() {
		Properties properties = new Properties();
		String result = "";
		try {
			properties.load(ResourceLib.class.getClassLoader().getResourceAsStream("resource.properties"));
			result = properties.getProperty("api_url_path");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
