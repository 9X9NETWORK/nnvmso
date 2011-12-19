package com.nnvmso.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import com.nnvmso.lib.CacheFactory;
import com.nnvmso.lib.PiwikLib;
import com.nnvmso.lib.YouTubeLib;
import com.nnvmso.model.AreaOwnership;
import com.nnvmso.model.BrandAdmin;
import com.nnvmso.model.Captcha;
import com.nnvmso.model.Category;
import com.nnvmso.model.CategoryChannel;
import com.nnvmso.model.CategoryChannelSet;
import com.nnvmso.model.ChannelAutosharing;
import com.nnvmso.model.ChannelSet;
import com.nnvmso.model.ChannelSetAutosharing;
import com.nnvmso.model.ChannelSetChannel;
import com.nnvmso.model.ContentOwnership;
import com.nnvmso.model.Ipg;
import com.nnvmso.model.LangTable;
import com.nnvmso.model.Mso;
import com.nnvmso.model.MsoChannel;
import com.nnvmso.model.MsoConfig;
import com.nnvmso.model.MsoIpg;
import com.nnvmso.model.MsoProgram;
import com.nnvmso.model.NnGuest;
import com.nnvmso.model.NnUser;
import com.nnvmso.model.NnUserPref;
import com.nnvmso.model.NnUserShare;
import com.nnvmso.model.NnUserWatched;
import com.nnvmso.model.PdrRaw;
import com.nnvmso.model.SnsAuth;
import com.nnvmso.model.Subscription;
import com.nnvmso.model.SubscriptionLog;
import com.nnvmso.model.ViewLog;

/**
 * for testing, works only for small set of data
 */	
@Service
public class InitService {
	protected static final Logger log = Logger.getLogger(InitService.class.getName());		

	private Cache cache;
	private HttpServletRequest req;

	private Mso mso;
	private NnUser user;
	private static String NNEMAIL = "mso@9x9.tv";

	public void setRequest(HttpServletRequest req) {
		this.req = req;
	}
	
	public void initAll(boolean english, boolean devel) {
		deleteAll();		
		initMso();
		initCategories(english);
		initSets(english, devel);
		
		initChannels(english, devel);
		
		initSetAndChannels(english);		
		initCategoryAndSets(english);
		initRecommended(english);
		initCategoryCount();
		initMapelPrograms();
	}
		
	public void mapleNames(boolean english, boolean devel) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		String[] entries = this.getChannelUrlsFromExcel(english);
		int cnt = 0;
		for (String entry : entries) {	
			String[] data = entry.split(",");
			String url = data[0];
			String name = null;
			if (data.length == 2)
				name = data[1];
			MsoChannel c = channelMngr.findBySourceUrlSearch(url);
			if (c != null) {
				if (c.getName() == null) {
					c.setName(name);
					channelMngr.save(c);					
					cnt++;
				}
			}
		}
		log.info("channel modified:" + cnt);
	}
	
	 //for local testing only
	public void initMapelPrograms() {
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmail(NNEMAIL); 
		MsoChannelManager channelMngr = new MsoChannelManager();		
		MsoChannel soap = new MsoChannel("mapel soap", "mapel soap", "", user.getKey().getId());
		soap.setContentType(MsoChannel.CONTENTTYPE_MAPLE_SOAP);
		channelMngr.create(soap, null);
		MsoChannel variety = new MsoChannel("mapel variety", "mapel variety", "", user.getKey().getId());
		variety.setContentType(MsoChannel.CONTENTTYPE_MAPLE_VARIETY);
		channelMngr.create(variety, null);
		
		MsoProgramManager programMngr = new MsoProgramManager();
		MsoProgram s1 = new MsoProgram("s1", "s1", "", MsoProgram.TYPE_VIDEO);
		s1.setPublic(true);
		s1.setSeq("1");
		programMngr.create(soap, s1);
		MsoProgram s3 = new MsoProgram("s3", "s3", "", MsoProgram.TYPE_VIDEO);
		s3.setPublic(true);
		s3.setSeq("3");
		programMngr.create(soap, s3);
		MsoProgram s2 = new MsoProgram("s2", "s2", "", MsoProgram.TYPE_VIDEO);
		s2.setPublic(true);
		s2.setSeq("2");
		programMngr.create(soap, s2);
		MsoProgram s4 = new MsoProgram("s4", "s4", "", MsoProgram.TYPE_VIDEO);
		s4.setPublic(true);
		s4.setSeq("4");
		programMngr.create(soap, s4);		
		MsoProgram v11 = new MsoProgram("v11", "v11", "", MsoProgram.TYPE_VIDEO);
		v11.setPublic(true);
		v11.setSeq("1");
		v11.setSubSeq("1");
		programMngr.create(variety, v11);
		MsoProgram v13 = new MsoProgram("v13", "v13", "", MsoProgram.TYPE_VIDEO);
		v13.setPublic(true);
		v13.setSeq("1");
		v13.setSubSeq("3");
		programMngr.create(variety, v13);
		MsoProgram v2 = new MsoProgram("v2", "v2", "", MsoProgram.TYPE_VIDEO);
		v2.setPublic(true);
		v2.setSeq("2");
		programMngr.create(variety, v2);
		MsoProgram v3 = new MsoProgram("v3", "v3", "", MsoProgram.TYPE_VIDEO);
		v3.setPublic(true);
		v3.setSeq("3");
		programMngr.create(variety, v3);		
		MsoProgram v12 = new MsoProgram("v12", "v12", "", MsoProgram.TYPE_VIDEO);
		v12.setPublic(true);
		v12.setSeq("1");
		v12.setSubSeq("2");
		programMngr.create(variety, v12);
	}
	public void deleteAll() {
		cache = CacheFactory.get();
		if (cache != null) {
			cache.clear();
		}		
		DbDumper dumper = new DbDumper();
		@SuppressWarnings("rawtypes")
		
		List list = dumper.findAll(AreaOwnership.class, "createDate");
		dumper.deleteAll(AreaOwnership.class, list);

		list = dumper.findAll(BrandAdmin.class, "createDate");
		dumper.deleteAll(BrandAdmin.class, list);
		
		list = dumper.findAll(Category.class, "createDate");
		dumper.deleteAll(Category.class, list);

		list = dumper.findAll(Captcha.class, "createDate");
		dumper.deleteAll(Captcha.class, list);
		
		list = dumper.findAll(CategoryChannel.class, "createDate");
		dumper.deleteAll(CategoryChannel.class, list);
		
		list = dumper.findAll(CategoryChannelSet.class, "createDate");
		dumper.deleteAll(CategoryChannelSet.class, list);

		list = dumper.findAll(ChannelSet.class, "createDate");
		dumper.deleteAll(ChannelSet.class, list);
		
		list = dumper.findAll(ChannelAutosharing.class, "createDate");
		dumper.deleteAll(ChannelAutosharing.class, list);
		
		list = dumper.findAll(ChannelSetAutosharing.class, "createDate");
		dumper.deleteAll(ChannelSetAutosharing.class, list);
				
		list = dumper.findAll(ChannelSetChannel.class, "createDate");
		dumper.deleteAll(ChannelSetChannel.class, list);
								
		list = dumper.findAll(ContentOwnership.class, "createDate");
		dumper.deleteAll(ContentOwnership.class, list);
				
		list = dumper.findAll(Ipg.class, "createDate");
		dumper.deleteAll(Ipg.class, list);

		list = dumper.findAll(Mso.class, "createDate");
		dumper.deleteAll(Mso.class, list);
		
		list = dumper.findAll(MsoChannel.class, "createDate");
		dumper.deleteAll(MsoChannel.class, list);

		list = dumper.findAll(MsoConfig.class, "createDate");
		dumper.deleteAll(MsoConfig.class, list);
		
		list = dumper.findAll(MsoIpg.class, "createDate");
		dumper.deleteAll(MsoIpg.class, list);		
		
		list = dumper.findAll(MsoProgram.class, "createDate");
		dumper.deleteAll(MsoProgram.class, list);

		list = dumper.findAll(NnGuest.class, "createDate");
		dumper.deleteAll(NnGuest.class, list);
		
		list = dumper.findAll(NnUser.class, "createDate");
		dumper.deleteAll(NnUser.class, list);

		list = dumper.findAll(NnUserPref.class, "createDate");
		dumper.deleteAll(NnUserPref.class, list);

		list = dumper.findAll(NnUserShare.class, "createDate");
		dumper.deleteAll(NnUserShare.class, list);

		list = dumper.findAll(NnUserWatched.class, "createDate");
		dumper.deleteAll(NnUserWatched.class, list);
		
		list = dumper.findAll(PdrRaw.class, "createDate");
		dumper.deleteAll(PdrRaw.class, list);

		list = dumper.findAll(Subscription.class, "createDate");
		dumper.deleteAll(Subscription.class, list);
		
		list = dumper.findAll(SubscriptionLog.class, "createDate");
		dumper.deleteAll(SubscriptionLog.class, list);

		list = dumper.findAll(ViewLog.class, "createDate");
		dumper.deleteAll(ViewLog.class, list);
						
		list = dumper.findAll(SnsAuth.class, "createDate");
		dumper.deleteAll(SnsAuth.class, list);
		log.info("delete all is done");
	}

	public String reportBadChannels() {
		ChannelSetManager csMngr = new ChannelSetManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		List<ChannelSet> sets = csMngr.findAll();
		HashMap<Long, MsoChannel> map = new HashMap<Long, MsoChannel>();
		List<ChannelSetChannel> list = new ArrayList<ChannelSetChannel>();
		for (ChannelSet cs : sets) {
			list.addAll(cscMngr.findByChannelSetId(cs.getKey().getId()));
		}
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		for (ChannelSetChannel csc : list) {
			MsoChannelManager channelMngr = new MsoChannelManager();
			MsoChannel c = map.get(csc.getChannelId());
			if (c == null) {
				c = channelMngr.findById(csc.getChannelId());
				map.put(csc.getChannelId(), c);
				channels.add(c);
			}						
		}
		String report = "";
		for (MsoChannel c : channels) {
			if (c.getStatus() != MsoChannel.STATUS_SUCCESS) {
				String output = c.getKey().getId() + "\t" + c.getName() + "\t" + c.getSourceUrl() + "\t" + c.getStatus() + "\t" + c.getErrorReason() + "\n";
				report += output;
			}
		}
		return report;
	}
 	
	public void initMso() {
		MsoManager msoMngr = new MsoManager();
		mso = new Mso("9x9", "9x9", NNEMAIL, Mso.TYPE_NN);
		mso.setTitle("9x9.tv");
		mso.setPreferredLangCode(LangTable.LANG_EN);
		mso.setJingleUrl("http://s3.amazonaws.com/9x9ui/videos/opening.swf");
		mso.setLogoUrl("http://s3.amazonaws.com/9x9ui/images/logo_9x9.png");
		msoMngr.create(mso);
				
		//config
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig configCdn = new MsoConfig(mso.getKey().getId(), MsoConfig.CDN, MsoConfig.CDN_AKAMAI);
		configMngr.create(configCdn);
		MsoConfig configDebug = new MsoConfig(mso.getKey().getId(), MsoConfig.DEBUG, "1");		
		configMngr.create(configDebug);
		MsoConfig configFb = new MsoConfig(mso.getKey().getId(), MsoConfig.FBTOKEN, "");
		configMngr.create(configFb);
		MsoConfig configRo = new MsoConfig(mso.getKey().getId(), MsoConfig.RO, "0");
		configMngr.create(configRo);
		
		//a default MSO user
		NnUserManager userMngr = new NnUserManager();
		user = new NnUser(NNEMAIL, "9x9mso", "9x9 mso", NnUser.TYPE_NN);  
		user.setMsoId(mso.getKey().getId());
		userMngr.create(user, null);
		//a user for testing
		NnUser a = new NnUser("a@a.com", "foobie", "a", NnUser.TYPE_NN);
		a.setMsoId(mso.getKey().getId()); //!!!
		userMngr.create(a, null);
		
		log.info("initializeMso1AndCategories is done");
	}				

	public void initCategoryAndSets(boolean english) {
		CategoryChannelSetManager cscMngr = new CategoryChannelSetManager();
		CategoryManager cMngr = new CategoryManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		List<Category> categories = new ArrayList<Category>();
		try {
			InputStream input;
			if (english)
				input = new FileInputStream("WEB-INF/views/admin/ESets.xlsx");
			else
				input = new FileInputStream("WEB-INF/views/admin/CSets.xlsx");
			Workbook wb = WorkbookFactory.create(input);
			Sheet sheet = wb.getSheetAt(2);			
			Row row = sheet.getRow(0);
			//get categories
			for (int i=0; i<row.getLastCellNum(); i++) {
				Cell cell = row.getCell(i);
				String name = cell.getStringCellValue();
				Category c = cMngr.findByName(name);
				if (c == null) {
					log.severe("category not found:" + name);
					return;
				}
				categories.add(c);
				System.out.println(c.getName());
			}
		    ArrayList<List<ChannelSet>> channelSets = new ArrayList<List<ChannelSet>>();
		    int rows = sheet.getPhysicalNumberOfRows();
			for (int r=1; r<rows; r++) {
			    row = sheet.getRow(r);
		    	int col = row.getLastCellNum();
		    	int seq = 0;
			    for (int c=0; c<col; c++) {
			    	Cell cell = row.getCell(c);
			    	if (cell != null) {
			    		String setName = cell.getStringCellValue();
			    		if (setName != null && setName.length() > 0) {
				    		ChannelSet cs = csMngr.findByName(setName);
			    			if (cs == null) {
								log.severe("channel set not found:" + setName + ";r=" + r + ";c=" + c);
								return;		    				
			    			}
			    			List<ChannelSet> list = new ArrayList<ChannelSet>();		    			
				    		if (seq < channelSets.size()) { 
				    			list = channelSets.get(seq);
				    		} else {
				    			channelSets.add(list);
				    		}			    						    			
		    				list.add(cs);
			    		}
			    	}
			    	seq++;
			    }
			}
			System.out.println("category size:" + categories.size());
			System.out.println("channel set size:" + channelSets.size());
			for (int i=0; i<categories.size(); i++) {
				for (int j=0; j<channelSets.get(i).size(); j++) {
					CategoryChannelSet csc = new CategoryChannelSet(
							categories.get(i).getKey().getId(), 
							channelSets.get(i).get(j).getKey().getId());  
					cscMngr.save(csc);
				}
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
			
	public void initCategories(boolean english) {
		int rows = 0;
		ArrayList<String> list = new ArrayList<String>();
		try {
			InputStream input;
			if (english)
				input = new FileInputStream("WEB-INF/views/admin/ESets.xlsx");
			else 
				input = new FileInputStream("WEB-INF/views/admin/CSets.xlsx");
			Workbook wb = WorkbookFactory.create(input);
			Sheet category = wb.getSheetAt(0);
			rows = category.getPhysicalNumberOfRows();
			for (int i=1; i<rows; i++) {
			    Row row = category.getRow(i);
			    Cell cell = row.getCell(0);
			    list.add(cell.getStringCellValue());
			}
			if (english)
				list.add(Category.UNCATEGORIZED);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso();
		CategoryManager categoryMngr = new CategoryManager();
		int i=1;
		for (String l : list) {
			Category c = new Category(l, true, mso.getKey().getId());
			if (english)
				c.setLang(LangTable.LANG_EN);
			else
				c.setLang(LangTable.LANG_ZH);			
			if (c.getName().equals(Category.UNCATEGORIZED))
				c.setPublic(false);
			c.setSeq((short)i);
			c.setSubCategoryCnt(0);
			categoryMngr.create(c);
			i++;
		}
	}		
	
	public String[] getChannelUrlsFromExcel(boolean english) {
		InputStream input;
		Workbook wb;
		int rows = 0;
		Set<String> list = new TreeSet<String>(); 
		try {
			if (english)
				input = new FileInputStream("WEB-INF/views/admin/ESets.xlsx");
			else 
				input = new FileInputStream("WEB-INF/views/admin/CSets.xlsx");
			wb = WorkbookFactory.create(input);
			Sheet sets = wb.getSheetAt(3);			
			rows = sets.getPhysicalNumberOfRows();						
		    int maple = 0;
			for (int r=1; r<rows; r++) {
			    Row row = sets.getRow(r);
			    if (row != null) {
				    int col = row.getLastCellNum();
				    for (int c=1; c<col; c+=2) {
				    	Cell cell = row.getCell(c);
				    	if (cell != null) {
				    		String url = cell.getStringCellValue();
				    		if (url != null && url.length() > 0) {
					    		if (!url.contains("maplestage")) {  
						    		String checkedUrl = YouTubeLib.formatCheck(url);
						    		if (checkedUrl != null) {
						    			Cell nameCell = row.getCell(c-1);
						    			String name = "";
						    			if (nameCell != null) {
						    				name = nameCell.getStringCellValue();
						    			}
						    			list.add(checkedUrl + "," + name);
						    		} else {
						    			log.info("url not passed youtube lib check:" + url);
						    		}
					    		} else {
					    			Cell nameCell = row.getCell(c-1);
					    			String name = "";
					    			if (nameCell != null) {
					    				name = nameCell.getStringCellValue();
					    			}					    			
					    			list.add(url + "," + name);
					    			maple++;
					    		}
				    		}
				    	}
				    }
			    }
			}
			log.info("final channel size:" + list.size());
    		System.out.println("mapel channels:" + maple);			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return list.toArray(new String[0]);
		//return ;		
	}

	public void initYoutubeOriName(boolean english, boolean devel) {
		String[] entries = this.getChannelUrlsFromExcel(english);
		TranscodingService tranService = new TranscodingService();
		MsoChannelManager channelMngr = new MsoChannelManager();
		for (String entry : entries) {
			String[] data = entry.split(",");
			String url = data[0];
			MsoChannel c = channelMngr.findBySourceUrlSearch(url);
			if (c == null) {
				log.info("something wrong:" + url);
			} else {
				if (c.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL || 
					c.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {
					if (c.getOriName() == null) {
						log.info("re-submit youtube channel:" + c.getSourceUrl());
						if (!devel)
							tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);						
					}
				}
			}			
		}
	}
	
	public void initChannels(boolean english, boolean devel) {
		NnUserManager userMngr = new NnUserManager();
		user = userMngr.findByEmail(NNEMAIL);		
		String[] entries = this.getChannelUrlsFromExcel(english);
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso();
		MsoChannelManager channelMngr = new MsoChannelManager();		
		TranscodingService tranService = new TranscodingService();
		boolean piwik = true;
		int zeroProgramCnt = 0;
		for (String entry : entries) {	
			String[] data = entry.split(",");
			String url = data[0];
			String name = null;
			if (data.length == 2) {
				name = data[1];
			}
			MsoChannel c = channelMngr.findBySourceUrlSearch(url);
			if (c == null) {
				c = new MsoChannel(url, user.getKey().getId());
				c.setName(name);
				c.setStatus(MsoChannel.STATUS_PROCESSING);	
				c.setContentType(channelMngr.getContentTypeByUrl(url));
				channelMngr.create(c, null);
				ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
				ownershipMngr.create(new ContentOwnership(), mso, c);
				if (!devel) {
					tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
					channelMngr.save(c);
				} else {
					piwik = false; //local testing, no piwik creation
				}
			} else {
				if (c.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE_CHANNEL || 
					c.getContentType() == MsoChannel.CONTENTTYPE_YOUTUBE_PLAYLIST) {
					/*
					if (c.getOriName() == null) {						
						log.info("re-submit youtube channel:" + c.getSourceUrl());
						if (!devel)
							tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);												
					}
					*/
				}				
				if (c.getStatus() == MsoChannel.STATUS_WAIT_FOR_APPROVAL) {
					log.info("mark the channel from waiting to approval to success");
					c.setStatus(MsoChannel.STATUS_SUCCESS);
				} else if (c.getStatus() == MsoChannel.STATUS_PROCESSING){
					if (!devel)
						tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
					log.info("was in processing mode, going to submit again");
				} else if (c.getContentType() == MsoChannel.CONTENTTYPE_MAPLE_SOAP && c.getProgramCount() < 5) {
					zeroProgramCnt++;
					log.info("maple soap program count < 5; re-send:" + c.getSourceUrlSearch());
					if (!devel)
						tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
				} else if (c.getContentType() == MsoChannel.CONTENTTYPE_MAPLE_VARIETY && c.getProgramCount() < 5) {
					zeroProgramCnt++;
					log.info("maple variety program count < 5; re-send:" + c.getSourceUrlSearch());
					if (!devel)
						tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
				} else if (c.getStatus() != MsoChannel.STATUS_SUCCESS){
					log.info("wanted channel but not success");					
				}
				if (c.getPiwik() != null)
					piwik = false;
				if (c.getPiwik() == null && devel)
					piwik = false;
			}
			if (piwik) {
				String piwikId = PiwikLib.createPiwikSite(0, c.getKey().getId(), req);
				c.setPiwik(piwikId);
			}
			c.setName(name);
			channelMngr.save(c);				
		}
		log.info("< 5 program count:" + zeroProgramCnt); 
	}
		
	public void initSetAndChannels(boolean english) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();		
		InputStream input;
		Workbook wb;
		List<ChannelSet> setList = new ArrayList<ChannelSet>();
		try {
			if (english)
				input = new FileInputStream("WEB-INF/views/admin/ESets.xlsx");
			else
				input = new FileInputStream("WEB-INF/views/admin/CSets.xlsx");
			wb = WorkbookFactory.create(input);
			Sheet sheet = wb.getSheetAt(3);			
			Row row = sheet.getRow(0);						
			//get sets from excel
			System.out.println("last cell num:" + row.getLastCellNum());
			for (int i=1; i<row.getLastCellNum(); i+=2) {
				Cell cell = row.getCell(i);
				if (cell != null) {
					String name = cell.getStringCellValue();
					if (name != null && name.length() > 0) {
						name = name.trim();
						ChannelSet cs = csMngr.findByName(name);
						if (cs != null) {
							setList.add(cs);
					    } else {
					    	log.severe("this set is not in db:" + name);
					    }
					}
				}
			}
			//compare list with database
			String lang = "en";
			if (!english)
				lang = "zh";
			List<ChannelSet> setAll = csMngr.findAllByLang(lang);
			for (ChannelSet all : setAll) {
				boolean found = false;
				for (ChannelSet sl : setList) {
					if (sl.getName().equals(all.getName())) {
						found = true;
						break;
					}						
				}
				if (!found) {
					System.out.println("not found this:" + all.getName());
				}
			}
			if (setAll.size() != setList.size()) {
				log.severe("information inconsistent. set all has " + setAll.size() + ";setList here = " + setList.size());
				//return;
			}
			//put all the channels into a string table for set, channel lookup
			int rows = sheet.getPhysicalNumberOfRows();			
		    ArrayList<List<String>> channelSetList = new ArrayList<List<String>>();
			for (int r=1; r<rows; r++) {
			    row = sheet.getRow(r);
			    if (row != null) {
			    	int col = row.getLastCellNum(); 
			    	int seq = 0;
				    for (int c=1; c<col; c+=2) {
				    	Cell cell = row.getCell(c);
				    	if (cell != null) {
				    		String url = cell.getStringCellValue();
				    		String checkedUrl = url;
				    		if (!url.contains("maplestage"))
				    			checkedUrl = YouTubeLib.formatCheck(url);
			    			List<String> list = new ArrayList<String>();		    			
			    			if (checkedUrl != null) {
					    		if (seq < channelSetList.size()) { 
					    			list = channelSetList.get(seq);
					    		} else {
					    			channelSetList.add(list);
					    		}			    						    			
			    				list.add(checkedUrl);
			    			}
				    	}
				    	seq++;
				    }
			    }
			}
			if (channelSetList.size() != setList.size()) {
				log.severe("set found from db and channel data are not consistent:" + setList.size() + ";" + channelSetList.size());
				//return;
			}

			//real work
			Hashtable<String, MsoChannel> table = new Hashtable<String, MsoChannel>();
 			for (int i=0; i<setList.size(); i++) {
 				System.out.println(setList.get(i).getName() + channelSetList.get(i));
 				int j=1;
 				for (String url : channelSetList.get(i)) {
					MsoChannel c = table.get(url);
 					if (c == null)
 	 					c = channelMngr.findBySourceUrlSearch(url);
 					if (c == null) {
 						log.severe("channel unfound:" + url);
 						//return;
 					} else { 
 						table.put(url, c);
 						ChannelSet cs = setList.get(i);
 						ChannelSetChannel csc = cscMngr.findBySetAndChannel(cs.getKey().getId(), c.getKey().getId());
 						//avoid the duplication
 						if (csc == null) {
 							csc = new ChannelSetChannel(cs.getKey().getId(), c.getKey().getId(), j);				
 							cscMngr.create(csc);
 							j++;
 						}
 					} 						
 				}
 			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
				
	public List<String> getSetNamesFromExcel(boolean english) {		
		ArrayList<String> list = new ArrayList<String>();
		try {
			InputStream input;
			if (english)
				input = new FileInputStream("WEB-INF/views/admin/ESets.xlsx");
			else 
				input = new FileInputStream("WEB-INF/views/admin/CSets.xlsx");
			Workbook wb = WorkbookFactory.create(input);
			Sheet sets = wb.getSheetAt(1);
			int rows = sets.getPhysicalNumberOfRows();
			for (int i=0; i<rows; i++) {
			    Row row = sets.getRow(i);			    
			    Cell cellName = row.getCell(0);
			    Cell cellDesc = row.getCell(1);
			    if (cellName != null) {
				    if (cellName.getStringCellValue() != null && cellName.getStringCellValue().length() > 0)
				    	list.add(cellName.getStringCellValue().trim() + ";" + cellDesc.getStringCellValue().trim());
			    }
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public void initSets(boolean english, boolean devel) {
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso(); 
		List<String> list = this.getSetNamesFromExcel(english);
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		for (int i=0; i<list.size(); i++) {
			String[] value = list.get(i).split(";");
			String name = value[0];
			String intro = value[1];
			ChannelSet channelSet = new ChannelSet(mso.getKey().getId(), name, intro, true);			
			channelSet.setDefaultUrl(String.valueOf(i)); 
			channelSet.setBeautifulUrl(String.valueOf(i));
			if (english)
				channelSet.setLang(LangTable.LANG_EN);
			else 
				channelSet.setLang(LangTable.LANG_ZH);
			csMngr.create(channelSet);
			if (!devel) {
				String piwik = PiwikLib.createPiwikSite(channelSet.getKey().getId(), 0, req);
				log.info("piwik id:" + piwik);
				channelSet.setPiwik(piwik);
				csMngr.save(channelSet);
			}
			ownershipMngr.create(new ContentOwnership(), mso, channelSet);
		}
		log.info("set size:" + list.size());
	}
	
	public void initSetImages() {
		ChannelSetManager csMngr = new ChannelSetManager();
		List<ChannelSet> list = csMngr.findAll();
		for (ChannelSet cs : list) {
			List<MsoChannel> channels = csMngr.findChannelsById(cs.getKey().getId());
			if (channels.size() == 0) { 
				log.info("no channels in this set" + cs.getName());
			} else { 
				for (int i=0; i<channels.size(); i++) {
					String imageUrl = channels.get(i).getImageUrl();
					if (imageUrl != null && imageUrl.length() > 0) {
						cs.setImageUrl(imageUrl);
						csMngr.save(cs);
						i = channels.size();
					}
				}
			}
		}
	}

	public void addMsoConfig() {
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso();
		//config
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig configRo = new MsoConfig(mso.getKey().getId(), MsoConfig.RO, "0");
		configMngr.create(configRo);		
	}
	
	public void initCategoryCount() {
		ChannelSetManager csMngr = new ChannelSetManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();
		List<ChannelSet> sets = csMngr.findAll();
		HashMap<Long, Integer> map = new HashMap<Long, Integer>();
		for (ChannelSet cs : sets) {
			List<ChannelSetChannel> list = cscMngr.findByChannelSetId(cs.getKey().getId());
			cs.setChannelCount(list.size());
			System.out.println("cs name:" + cs.getName() + ";size:" + list.size());
			csMngr.save(cs);
			map.put(cs.getKey().getId(), list.size());
		}						
		CategoryManager categoryMngr = new CategoryManager();
		CategoryChannelSetManager ccsMngr = new CategoryChannelSetManager();
		List<Category> categories= categoryMngr.findAll();
		for (Category c : categories) {
			List<CategoryChannelSet> list = ccsMngr.findAllByCategoryId(c.getKey().getId());
			int cnt = 0;
			for (CategoryChannelSet l : list) {		
				cnt += map.get(l.getChannelSetId());
			}			
			c.setChannelCount(cnt);
			categoryMngr.save(c);			
		}
	}
	
	public void initRecommended(boolean english) {		
		ChannelSetManager csMngr = new ChannelSetManager();
		try {
			InputStream input; 
			if (english)
				input = new FileInputStream("WEB-INF/views/admin/ESets.xlsx");
			else 
				input = new FileInputStream("WEB-INF/views/admin/CSets.xlsx");
			Workbook wb = WorkbookFactory.create(input);
			Sheet sheet = wb.getSheetAt(4);
		    int rows = sheet.getPhysicalNumberOfRows();
			for (int r=0; r<rows; r++) {
				Row row = sheet.getRow(r); 
				Cell cell = row.getCell(0);
				String name = cell.getStringCellValue();
				if (name != null && name.length() > 0) {
					ChannelSet cs = csMngr.findByName(name); 
					if (cs == null) {
						log.severe("set not found:" + name);
						return;
					}
					cs.setFeatured(true);
					cs.setSeq((short)(r+1));
					csMngr.save(cs);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void initPiwikSet(boolean english, boolean devel) {
		ChannelSetManager csMngr = new ChannelSetManager();
		List<ChannelSet> sets = csMngr.findAll();
		int cnt = 0;
		for (ChannelSet cs : sets) {
			//if (cs.getPiwik() == null) {
				String piwikId = PiwikLib.createPiwikSite(cs.getKey().getId(), 0, req);
				cs.setPiwik(piwikId);
				csMngr.save(cs);
				cnt++;
			//}
		}
		log.info("init piwik set count = " + cnt);
	}
	
	public void initChannelPiwik(boolean english, boolean devel) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		String[] entries = this.getChannelUrlsFromExcel(english);
		int cnt = 0;
		for (String entry : entries) {	
			String[] data = entry.split(",");
			String url = data[0];
			MsoChannel c = channelMngr.findBySourceUrlSearch(url);
			if (c != null) {
				//if (c.getPiwik() == null) {
					String piwikId = PiwikLib.createPiwikSite(0, c.getKey().getId(), req);
					c.setPiwik(piwikId);
					channelMngr.save(c);					
					cnt++;
				//}
			}
		}
		log.info("init piwik channel count = " + cnt);
	}

	public void initNnChannelsPiwik(boolean english, boolean devel) {
		MsoChannelManager channelMngr = new MsoChannelManager();
		List<MsoChannel> channels = channelMngr.findChannelsByType(MsoChannel.CONTENTTYPE_MIXED);
		int cnt = 0;
		for (MsoChannel c : channels) {
			//if (c.getPiwik() == null || c.getPiwik().length() == 0) {
				String piwikId = PiwikLib.createPiwikSite(0, c.getKey().getId(), req);
				c.setPiwik(piwikId);
				channelMngr.save(c);					
				cnt++;
			//}
			
		}
		log.info("init piwik channel count = " + cnt);
	}
	
	public String[] excelTest(boolean english) {
		InputStream input;
		Workbook wb;
		Set<String> list = new TreeSet<String>(); 
		try {
			if (english)
				input = new FileInputStream("WEB-INF/views/admin/ESets.xlsx");
			else 
				input = new FileInputStream("WEB-INF/views/admin/CSets.xlsx");
			wb = WorkbookFactory.create(input);
			Sheet sets = wb.getSheetAt(3);			
			Row row = sets.getRow(2);
			Cell cell = row.getCell(8);
			Cell cell1 = row.getCell(9);
    		System.out.println("name=" + cell.getStringCellValue());
    		System.out.println("url=" + cell1.getStringCellValue());			    		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return list.toArray(new String[0]);
		//return ;		
	}
	
}
