package com.nnvmso.lib;

import com.nnvmso.model.Slideshow;

public class NnScriptLib {

	public static String generateSlideScript(Slideshow slide, String fileUrl) {
		String start = "\n[start]\n";
		String end = "\n[exit]";
		String verb = "slide ";
		String sleep = "sleep 6\n";
		String files[] = slide.getSlides();
		String body = "";
		for (int i=0; i< files.length; i++) {
			body = body + verb + fileUrl + files[i];
			if (i != files.length -1) {
				body = body + "\n" + sleep;
			}
		}
		String output = start + body + end;
		return output;
	}
}
