package com.nnvmso.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.DatastoreNeedIndexException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.nnvmso.lib.SearchJanitorUtils;
import com.nnvmso.model.MsoChannel;

public class SearchJanitor {
	
	private static final Logger log = Logger.getLogger(SearchJanitor.class.getName());
	
	public static final int MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH = 5;
	
	public static final int MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX = 200;
	
	public static List<MsoChannel> searchChannelEntries(
			String queryString, 
			PersistenceManager pm) {

		StringBuffer queryBuffer = new StringBuffer();

		queryBuffer.append("SELECT FROM " + MsoChannel.class.getName() + " WHERE ");

		Set<String> queryTokens = SearchJanitorUtils
				.getTokensForIndexingOrQuery(queryString,
						MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH);

		List<String> parametersForSearch = new ArrayList<String>(queryTokens);

		StringBuffer declareParametersBuffer = new StringBuffer();

		int parameterCounter = 0;

		while (parameterCounter < queryTokens.size()) {

			queryBuffer.append("fts == param" + parameterCounter);
			declareParametersBuffer.append("String param" + parameterCounter);

			if (parameterCounter + 1 < queryTokens.size()) {
				queryBuffer.append(" && ");
				declareParametersBuffer.append(", ");

			}
			parameterCounter++;
		}
	
		Query query = pm.newQuery(queryBuffer.toString());
		query.declareParameters(declareParametersBuffer.toString());
		List<MsoChannel> result = null;
		
		try {
			result = (List<MsoChannel>) query.executeWithArray(parametersForSearch.toArray());
		} catch (DatastoreTimeoutException e) {
			log.severe(e.getMessage());
			log.severe("datastore timeout at: " + queryString);// + " - timestamp: " + discreteTimestamp);
		} catch(DatastoreNeedIndexException e) {
			log.severe(e.getMessage());
			log.severe("datastore need index exception at: " + queryString);// + " - timestamp: " + discreteTimestamp);
		}

		return result;
	}
	
	public static void updateFTSStuffForMsoChannel(MsoChannel channel) {			
		StringBuffer sb = new StringBuffer();		
		sb.append(channel.getName() + " " + channel.getIntro());			
		Set<String> new_ftsTokens = SearchJanitorUtils.getTokensForIndexingOrQuery(
				sb.toString(),
				MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX);				
		Set<String> ftsTokens = channel.getFts();
		ftsTokens.clear();
		for (String token : new_ftsTokens) {
			ftsTokens.add(token);
		}		
	}

	public static Set<String> getFtsTokens(String name, String intro) {			
		StringBuffer sb = new StringBuffer();		
		sb.append(name + " " + intro);			
		Set<String> new_ftsTokens = SearchJanitorUtils.getTokensForIndexingOrQuery(
				sb.toString(),
				MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX);
		Set<String> ftsTokens = new HashSet<String>();
		ftsTokens.clear();
		for (String token : new_ftsTokens) {
			ftsTokens.add(token);
		}
		return ftsTokens;
	}
	
}