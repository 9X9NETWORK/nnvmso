package com.nnvmso.lib;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class NnLib {
	public static String getKeyStr(Key key) {
		return KeyFactory.keyToString(key);
	}

}
