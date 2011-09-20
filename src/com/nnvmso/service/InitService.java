package com.nnvmso.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.jsr107cache.Cache;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.CacheFactory;
import com.nnvmso.model.*;

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
	
	public void initAll(boolean transcoding) {		
		deleteAll();		
		initMso();
		initChannels(transcoding);
		initSets();
		initCategories();
		initSetAndChannels();
		initCategoryAndSets();
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
		userMngr.create(user);
		//a user for testing
		NnUser a = new NnUser("a@a.com", "foobie", "a", NnUser.TYPE_NN);
		a.setMsoId(mso.getKey().getId()); //!!!
		userMngr.create(a);
		
		log.info("initializeMso1AndCategories is done");
	}				
			
	public void initCategoryAndSets() {
		CategoryChannelSetManager cscMngr = new CategoryChannelSetManager();
		ArrayList<String[]> list = this.getCategorySets();
		CategoryManager cMngr = new CategoryManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		String[] categories = (String[])list.get(0);
		String[] subcategories = (String[])list.get(1);
		String[] sets = (String[])list.get(2);
		Hashtable<String, ChannelSet> setTable = new Hashtable<String, ChannelSet>();
		for (int i=0; i<categories.length; i++) {
			Category c = cMngr.findCategory(categories[i], subcategories[i]);
			System.out.println("category:" + c.getName());			
			ChannelSet cs = setTable.get(sets[i]);
			if (cs == null)
				cs = csMngr.findByName(sets[i]);
			CategoryChannelSet csc = new CategoryChannelSet(c.getKey().getId(), cs.getKey().getId());
			cscMngr.save(csc);
		}
	}
	
	private ArrayList<String[]> getCategorySets() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		String[] categories = {
			"For Him",
			"For Him",
			"For Him",
			"For Her",
			"For Her",
			"For Her",
			"For Her",
			"For Work",
			"For Play",
			"For Play",
			"For Play",
			"For Family",
			"For College",
			"For College",			
			"工作誌",
			"工作誌",
			"姊妹淘",
			"玩樂咖",
			"男人幫",
			"男人幫",
			"玩樂咖",
			"玩樂咖",
			"姊妹淘",
			"姊妹淘",
			"男人幫",
			"童心園",
			"姊妹淘"	,		
		};
		
		String[] subcategories = {
				"The 20 Something Professionals", 
				"The 20 Something Professionals", 
				"The Family Man",
				"Glamorous Gal",
				"Forever Young",
				"Super Moms",
				"Change for the Better",
				"Go Big or Go Home",
				"Master Gamer",
				"Friday Night Fever",
				"Past Time Favorites",
				"The Family Man",
				"The 40 Minutes Study Break",
				"Hiliarious Dude!",
				"財經新知",
				"綜合新聞",
				"美容美體",
				"科技玩家",
				"科技玩家",
				"科技時尚",
				"科技時尚",
				"綜藝娛樂",
				"綜藝娛樂",
				"流行音樂",
				"流行音樂",
				"可愛動物",
				"可愛動物",
		};
		String[] sets = {
				"Pick Up Your Game",
				"Second Place is the First Loser",
				"Family Time",
				"Waking up Beautiful",
				"Dance Like No one is Watching",
				"Family Lovin'",
				"Revamp Your Lifestyle",
				"Play Hard, Work Hard",
				"King of Games",
				"Dance to These",
				"When You Have Nothing to Do",
				"Family Time",
				"A Break from Studying",
				"Learning Could Be Fun",
				"經理人聖經",
				"華語都會新聞",
				"美麗自我",
				"電玩競地",
				"電玩競地",
				"陽光潮流幫",
				"陽光潮流幫",
				"娛樂懶人包",
				"娛樂懶人包",
				"音樂頑童",
				"音樂頑童",
				"小小動物園",
				"小小動物園",
		};
		list.add(categories);
		list.add(subcategories);
		list.add(sets);
		return list;
	}
	
	public void initCategories() {		
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso();
		//categories
		String[] categoryStr = {
				"For Him", "For Her", "For Work", "For Play", "For Family", 
				"For College", "For Kids", "Recommended", Category.UNCATEGORIZED,
				"男人幫", "姊妹淘", "工作誌", "玩樂咖", "居家人", "校園瘋", "童心園", "精選推薦"  
		};
		String[][] subStr = new String[16][]; 
		subStr[0] = new String[] {"The 20 Something Professionals", "The Family Man"}; 			
		subStr[1] = new String[] {"Glamorous Gal", "Forever Young", "Super Moms", "Change for the Better"};
		subStr[2] = new String[] {"Go Big or Go Home"};
		subStr[3] = new String[] {"Master Gamer", "Friday Night Fever", "Past Time Favorites"};
		subStr[4] = new String[] {"The Family Man"};
		subStr[5] = new String[] {"The 40 Minutes Study Break", "Hiliarious Dude!"};
		subStr[6] = new String[] {""};
		subStr[7] = new String[] {""};
		subStr[8] = new String[] {"科技時尚", "流行音樂"}; //for him
		subStr[9] = new String[] {"美容美體", "綜藝娛樂", "流行音樂"}; //for her
		subStr[10] = new String[] {"財經新知", "綜合新聞"}; //for work
		subStr[11] = new String[] {"科技玩家", "科技時尚", "綜藝娛樂"}; //for play
		subStr[12] = new String[] {""};
		subStr[13] = new String[] {""};
		subStr[14] = new String[] {"可愛動物"};
		subStr[15] = new String[] {""};
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categories= new ArrayList<Category>();
		for (int i=0; i<categoryStr.length; i++) {
			Category c = new Category(categoryStr[i], true, mso.getKey().getId());
			c.setLang(LangTable.LANG_EN);
			if (i > 8)
				c.setLang(LangTable.LANG_ZH);
			if (c.getName().equals(Category.UNCATEGORIZED))
				c.setPublic(false);
			c.setSeq((short)(i+1));
			System.out.println("category name:" + c.getName() + ";lang:" + c.getLang());
			categoryMngr.create(c);
			categories.add(c);			
		}
		
		for (int i=0; i<subStr.length; i++) {	
			for (int j=0; j<subStr[i].length; j++) {
				if (subStr[j].length > 0) {
					if (subStr[i][j].length() > 0) {
						Category c = new Category(subStr[i][j], true, mso.getKey().getId());
						c.setLang(LangTable.LANG_EN);
						if (i > 7)
							c.setLang(LangTable.LANG_ZH);
						c.setParentId(categories.get(i).getKey().getId());
						System.out.println("sub category name:" + c.getName() + ";lang:" + c.getLang() + ";parentId:" + c.getParentId());
						c.setSeq((short)(j+1));
						categoryMngr.create(c);
					}
				}
			}
		}		
	}
	
	public void initChannels(boolean transcoding) {
		NnUserManager userMngr = new NnUserManager();
		user = userMngr.findByEmail(NNEMAIL);		
		String[] urls = this.getChannelUrls();
		MsoChannelManager channelMngr = new MsoChannelManager();
		TranscodingService tranService = new TranscodingService();
		for (String url : urls) {			
			MsoChannel c = channelMngr.findBySourceUrlSearch(url);
			if (c == null) {					
				c = new MsoChannel(url, user.getKey().getId());
				c.setStatus(MsoChannel.STATUS_PROCESSING);
				c.setContentType(channelMngr.getContentTypeByUrl(url));
				channelMngr.create(c);
				if (transcoding)
					tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);			
			} else {
				log.info("this channel existed:" + url);
				if (c.getStatus() == MsoChannel.STATUS_WAIT_FOR_APPROVAL) {
					log.info("mark the channel from waiting to approval to success");
					c.setStatus(MsoChannel.STATUS_SUCCESS);
				}
			}
		}		
	}

	public String[][] getSetChannels() {
		String sets[][] = new String[21][];		
		sets[0] = new String[] {
				"http://www.youtube.com/user/simplepickup",
				"http://www.youtube.com/user/sixpackshortcuts",
				"http://www.youtube.com/user/stanforduniversity",
				"http://www.youtube.com/user/mercedesbenztv",
				"http://www.youtube.com/user/nike",
				"http://www.youtube.com/user/tedtalksdirector"				
		};
		
		sets[1] = new String[] {
				"http://www.youtube.com/user/machinima",
				"http://www.youtube.com/user/teamflightbrothers",
				"http://www.youtube.com/user/mroperationsports",
		};		
		sets[2] = new String[] {
				"http://www.youtube.com/user/devinanderica",
				"http://www.youtube.com/user/visitvictoria",
				"http://www.youtube.com/user/disneyparks",
		};
		sets[3] = new String[] {
				"http://www.youtube.com/user/michellephan",
				"http://www.youtube.com/user/elletvfashion",
				"http://www.youtube.com/user/lorealparis",
		};
		sets[4] = new String[] {
				"http://www.youtube.com/user/billboardgoddess",
				"http://www.youtube.com/user/atlanticvideos",
				"http://www.youtube.com/user/armadamusic",
		};
		sets[5] = new String[] {
				"http://www.youtube.com/user/devinanderica",
				"http://www.youtube.com/user/visitvictoria",
				"http://www.youtube.com/user/disneyparks",
		};
		sets[6] = new String[] {
				"http://www.youtube.com/user/fashiontv",
				"http://www.youtube.com/user/getmarriedtv",
				"http://www.youtube.com/user/larrygreenberg",
		};
		sets[7] = new String[] {
				"http://www.youtube.com/user/tedtalksdirector",
				"http://www.youtube.com/user/tysiphonehelp",
				"http://www.youtube.com/user/stanfordbusiness",
				"http://www.youtube.com/user/a3network",
				"http://www.youtube.com/user/autoexpress",
		};
		sets[8] = new String[] {
				"http://www.youtube.com/user/machinima",
				"http://www.youtube.com/user/teamflightbrothers",
				"http://www.youtube.com/user/mroperationsports",
		};		
		sets[9] = new String[] {
				"http://www.youtube.com/user/billboardgoddess",
				"http://www.youtube.com/user/atlanticvideos",
				"http://www.youtube.com/user/armadamusic",
		};
		sets[10] = new String[] {
				"http://www.youtube.com/user/collegehumor",
				"http://www.youtube.com/user/break",
		};
		sets[11] = new String[] {
				"http://www.youtube.com/user/justkiddingfilms",
				"http://www.youtube.com/user/timothydelaghetto2",
				"http://www.youtube.com/user/simplepickup",
				"http://www.youtube.com/user/wongfuproductions",
		};
		sets[12] = new String[] {
				"http://www.youtube.com/user/collegehumor",
				"http://www.youtube.com/user/break",
		};				
		sets[13] = new String[] {
				"http://www.youtube.com/user/bwnet",
				"http://www.youtube.com/user/cwgv",
				"http://www.youtube.com/user/cwtv",
				"http://www.youtube.com/user/dxmonline",
				"http://www.youtube.com/user/taiwantrade",				
		};
		sets[14] = new String[] {
				"http://www.youtube.com/user/thechinesenews",
				"http://www.youtube.com/user/ptstalk",
				"http://www.youtube.com/user/chinatimes",
				"http://www.youtube.com/user/tvbs",
				"http://www.youtube.com/user/rthk",
				"http://www.youtube.com/user/hkbnnews",				
		};
		sets[15] = new String[] {
				"http://www.youtube.com/user/fashionguide",
				"http://www.youtube.com/user/sppweb",
				"http://www.youtube.com/user/beautyqq",
				"http://www.youtube.com/user/newapplearial",				
		};
		sets[16] = new String[] {
				"http://www.youtube.com/user/efuntv",
				"http://www.youtube.com/user/gamebasegnc",
				"http://www.youtube.com/user/bahamutgnn",                                                          
				"http://www.youtube.com/user/samgnn2",				
		};
		sets[17] = new String[] {
				"http://www.youtube.com/user/sheng98news",
				"http://www.youtube.com/user/taiwansr",
				"http://www.youtube.com/user/buycartv",
				"http://www.youtube.com/user/lioncyber",
				"http://www.youtube.com/user/tkbang",
				"http://www.youtube.com/user/udndigital",
				"http://www.youtube.com/user/dcviewno1",
				"http://www.youtube.com/user/sogitv",
				"http://www.youtube.com/user/sppweb",				
		};
		sets[18] = new String[] {
				"http://www.youtube.com/user/sinapremium",
				"http://www.youtube.com/user/roadshow68xchina",
				"http://www.youtube.com/user/chinesecivilization2",
				"http://www.youtube.com/user/twnexttv",
				"http://www.youtube.com/user/zimeitao",
				"http://www.youtube.com/user/taiwansugoi2",
				"http://www.youtube.com/user/gorgeousspace",
				"http://www.youtube.com/user/yattamovie2",
				"http://www.youtube.com/user/2cekidhk",				
		};
		sets[19] = new String[] {
				"http://www.youtube.com/user/universalmusichk",
				"http://www.youtube.com/user/universaltwn",
				"http://www.youtube.com/user/warnertaiwan",
				"http://www.youtube.com/user/kpopmv2011v2",
				"http://www.youtube.com/user/vul3a04snsding",
				"http://www.youtube.com/user/asiamuseentertainme",
				"http://www.youtube.com/user/thewalltw",
				"http://www.youtube.com/user/totororo0202",				
		};
		sets[20] = new String[] {
				"http://www.youtube.com/user/taipeizoo",
				"http://www.youtube.com/user/mmovies21",
				"http://www.youtube.com/user/shironekoshiro",
				"http://www.youtube.com/user/olivinej",				
		};		
		return sets;
	}
		
	public void initSetAndChannels() {
		String[][] sets = this.getSetChannels();
		Hashtable<String, MsoChannel> table = new Hashtable<String, MsoChannel>();
		MsoChannelManager channelMngr = new MsoChannelManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		ChannelSetChannelManager cscMngr = new ChannelSetChannelManager();		
		List<ChannelSet> cs = csMngr.findAll();
		
		for (int i=0; i<sets.length; i++) {
			for (int j=0; j<sets[i].length; j++) {
				MsoChannel c = table.get(sets[i][j]);
				if (c == null) {
					c = channelMngr.findBySourceUrlSearch(sets[i][j]);
					table.put(sets[i][j], c);
				}
				ChannelSetChannel csc = new ChannelSetChannel(cs.get(i).getKey().getId(), c.getKey().getId(), c.getSeq());				
				cscMngr.create(csc);
			}
		}
	}
	
	//13 English, 8 Chinese
	private String[] getSetNames() { 
		String[] names = {
				"Pick Up Your Game", 
				"Second Place is the First Loser",
				"Family Time", "Waking up Beautiful",
				"Dance Like No one is Watching",
				"Family Lovin'",
				"Revamp Your Lifestyle",
				"Play Hard, Work Hard",
				"King of Games",
				"Dance to These", 
				"When You Have Nothing to Do",
				"Family Time",
				"A Break from Studying",
				"Learning Could Be Fun",
				"經理人聖經", "華語都會新聞", "美麗自我", "電玩競地", "陽光潮流幫", 
				"娛樂懶人包", "音樂頑童", "小小動物園"  	
			};
		return names;
	}
	
	private String[] getSetIntros() {
		String[] intros = {
				"Tired of the same routine everyday? If these guys can do you, so can you!",
				"Stop losing.  Play like a man.",
				"Be the best dad there is.", 
				"Tips on how you can walk out of your house in the morning looking like a superstar!", 
				"Forget about the dancefloor, your living room is all yours.", 
				"Cute babies, romantic getaways, awesome places, need I say more?", 
				"Going through life-changing events? Even the smallest changes can make your life amazing",
				"Make that money, spend that money!",
				"Stop getting school'd.  Be that player killer.",
				"Too much work? Dance away your stress to these tunes.",
				"Tired of thinking? Give your brain a break with these!",
				"Be the best dad there is.",
				"Studyig? Pffff! Procrastinating is better.",
				"Duh Dude! Pranking on people is jokes.",	
				"都會經理人必備，掌握業界最新資訊與概念。",
				"推薦您中國大陸、台灣、香港的主流媒體都會新聞，歡迎訂閱",
				"姐姐妹妹站起來，美麗時尚不求人的秘方，盡在9x9美麗自我頻道網。",
				"電玩競地提供您第一手的電玩遊戲情報，每天更新。", 
				"給陽光熱血又潮流的你，受到眾人矚目的必看精選，歡迎訂閱。",  
				"匯集港中台娛樂頻道，提供您最新的娛樂資訊，每天更新",  
				"亞洲流行音樂情報站，提供您港中台日韓等地的最新流行音樂資訊",  
				"小小動物園就在你家，不用出門也可以和可愛動物們玩耍，歡迎訂閱。",  	
			};
		return intros;
	}
	
	public void initSets() {
		MsoManager msoMngr = new MsoManager();
		Mso mso = msoMngr.findNNMso(); 
		String[] names = this.getSetNames();
		String[] intros = this.getSetIntros();
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ChannelSetManager csMngr = new ChannelSetManager();
		for (int i=0; i<names.length; i++) {
			ChannelSet channelSet = new ChannelSet(mso.getKey().getId(), names[i], intros[i], true);			
			channelSet.setDefaultUrl(String.valueOf(i)); 
			channelSet.setBeautifulUrl(String.valueOf(i));
			channelSet.setFeatured(true);
			channelSet.setLang(LangTable.LANG_EN);
			channelSet.setSeq((short)(i+1));
			if (i > 12) {
				channelSet.setLang(LangTable.LANG_ZH);
				channelSet.setSeq((short)(i-12));
			}
			csMngr.create(channelSet);
			ownershipMngr.create(new ContentOwnership(), mso, channelSet);
		}
	}
	
	private String[] getChannelUrls() {
		String[] urls = {
				"http://www.youtube.com/user/2cekidhk",
				"http://www.youtube.com/user/a3network",
				"http://www.youtube.com/user/armadamusic",
				"http://www.youtube.com/user/asiamuseentertainme",
				"http://www.youtube.com/user/atlanticvideos",
				"http://www.youtube.com/user/autoexpress",
				"http://www.youtube.com/user/bahamutgnn",
				"http://www.youtube.com/user/beautyqq",
				"http://www.youtube.com/user/billboardgoddess",
				"http://www.youtube.com/user/break",
				"http://www.youtube.com/user/buycartv",
				"http://www.youtube.com/user/bwnet",				
				"http://www.youtube.com/user/chinatimes",
				"http://www.youtube.com/user/chinesecivilization2",
				"http://www.youtube.com/user/collegehumor",
				"http://www.youtube.com/user/cwgv",
				"http://www.youtube.com/user/cwtv",
				"http://www.youtube.com/user/dcviewno1",
				"http://www.youtube.com/user/devinanderica",
				"http://www.youtube.com/user/disneyparks",
				"http://www.youtube.com/user/dxmonline",
				"http://www.youtube.com/user/efuntv",
				"http://www.youtube.com/user/elletvfashion",
				"http://www.youtube.com/user/fashionguide",
				"http://www.youtube.com/user/fashiontv",
				"http://www.youtube.com/user/gamebasegnc",
				"http://www.youtube.com/user/getmarriedtv",
				"http://www.youtube.com/user/gorgeousspace",
				"http://www.youtube.com/user/hkbnnews",
				"http://www.youtube.com/user/justkiddingfilms",
				"http://www.youtube.com/user/kpopmv2011v2",
				"http://www.youtube.com/user/larrygreenberg",
				"http://www.youtube.com/user/lioncyber",
				"http://www.youtube.com/user/lorealparis",
				"http://www.youtube.com/user/machinima",
				"http://www.youtube.com/user/mercedesbenztv",
				"http://www.youtube.com/user/michellephan",
				"http://www.youtube.com/user/mmovies21",
				"http://www.youtube.com/user/mroperationsports",
				"http://www.youtube.com/user/newapplearial",
				"http://www.youtube.com/user/nike",
				"http://www.youtube.com/user/olivinej",
				"http://www.youtube.com/user/ptstalk",
				"http://www.youtube.com/user/roadshow68xchina",
				"http://www.youtube.com/user/rthk",
				"http://www.youtube.com/user/samgnn2",
				"http://www.youtube.com/user/sheng98news",
				"http://www.youtube.com/user/shironekoshiro",
				"http://www.youtube.com/user/simplepickup",
				"http://www.youtube.com/user/sinapremium",
				"http://www.youtube.com/user/sixpackshortcuts",
				"http://www.youtube.com/user/sogitv",
				"http://www.youtube.com/user/sppweb",
				"http://www.youtube.com/user/stanfordbusiness",
				"http://www.youtube.com/user/stanforduniversity",
				"http://www.youtube.com/user/taipeizoo",
				"http://www.youtube.com/user/taiwansr",
				"http://www.youtube.com/user/taiwansugoi2",
				"http://www.youtube.com/user/taiwantrade",
				"http://www.youtube.com/user/teamflightbrothers",
				"http://www.youtube.com/user/tedtalksdirector",
				"http://www.youtube.com/user/thechinesenews",
				"http://www.youtube.com/user/thewalltw",
				"http://www.youtube.com/user/timothydelaghetto2",
				"http://www.youtube.com/user/tkbang",
				"http://www.youtube.com/user/totororo0202",
				"http://www.youtube.com/user/tvbs",
				"http://www.youtube.com/user/twnexttv",
				"http://www.youtube.com/user/tysiphonehelp",
				"http://www.youtube.com/user/udndigital",
				"http://www.youtube.com/user/universalmusichk",
				"http://www.youtube.com/user/universaltwn",
				"http://www.youtube.com/user/visitvictoria",
				"http://www.youtube.com/user/vul3a04snsding",
				"http://www.youtube.com/user/warnertaiwan",
				"http://www.youtube.com/user/wongfuproductions",
				"http://www.youtube.com/user/yattamovie2",
				"http://www.youtube.com/user/zimeitao"
		};
		return urls;
	}

	public void deleteUrls() {
		String[] urls = this.getDeleteUrls();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoProgramManager programMngr = new MsoProgramManager();
		for (String url : urls) {
			MsoChannel c = channelMngr.findBySourceUrlSearch(url);
			if (c != null) {
				log.info("delete this url:" + url);
				List<MsoProgram> list = programMngr.findAllByChannelId(c.getKey().getId());
				for (MsoProgram p : list) {
					programMngr.delete(p);
				}
				channelMngr.delete(c);				
			}
		}

	}

	public void initSetImages() {
		ChannelSetManager csMngr = new ChannelSetManager();
		List<ChannelSet> list = csMngr.findAll();
		for (ChannelSet cs : list) {
			List<MsoChannel> channels = csMngr.findChannelsById(cs.getKey().getId());
			if (channels.size() == 0) { 
				log.info("no channels in this set" + cs.getName());
			} else { 
				cs.setImageUrl(channels.get(0).getImageUrl());
				csMngr.save(cs);
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
		CategoryManager categoryMngr = new CategoryManager();
		List<Category> categories= categoryMngr.findAll();
		for (Category c : categories) {
			c.setChannelCount(5);
			categoryMngr.save(c);
		}
	}
	
	public void initRecommended() {
		ChannelSetManager csMngr = new ChannelSetManager();
		List<ChannelSet> list = csMngr.findAll();
		for (int i=0; i<list.size(); i++) {
			if (i<10)
				list.get(i).setFeatured(true);
			if (i>12)
				list.get(i).setFeatured(true);
			csMngr.save(list.get(i));
		}				
	}
	
	private String[] getDeleteUrls() {
		String[] urls = {
				"http://www.youtube.com/user/break",
				"http://www.youtube.com/user/chinesecivilization2",
				"http://www.youtube.com/user/collegehumor",
				"http://www.youtube.com/user/hkbnnews",
				"http://www.youtube.com/user/justkiddingfilms",
				"http://www.youtube.com/user/kpopmv2011v2",
				"http://www.youtube.com/user/nike",
				"http://www.youtube.com/user/roadshow68xchina",
				"http://www.youtube.com/user/simplepickup",
				"http://www.youtube.com/user/sinapremium",
				"http://www.youtube.com/user/sixpackshortcuts",
				"http://www.youtube.com/user/taipeizoo",
				"http://www.youtube.com/user/thechinesenews",
				"http://www.youtube.com/user/timothydelaghetto2",
				"http://www.youtube.com/user/tkbang",
				"http://www.youtube.com/user/tvbs",
				"http://www.youtube.com/user/twnexttv",
				"http://www.youtube.com/user/universalmusichk",                                                                                  				
		};
		return urls;
	}
}
