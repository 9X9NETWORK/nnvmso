package com.nnvmso.lib;

public class PlayerLib {
	public static String getTabDelimitedStr(String[] ori) {
		String delimiter = "\t";
		StringBuilder result = new StringBuilder();
		if (ori.length > 0) {
			result.append(ori[0]);
		    for (int i=1; i<ori.length; i++) {
		       result.append(delimiter);
		       result.append(ori[i]);
		    }
		}
		return result.toString();
	}
}
