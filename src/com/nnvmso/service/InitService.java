package com.nnvmso.service;

import java.util.ArrayList;
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

	private HttpServletRequest req;
	private Cache cache;
	
	public void setRequest(HttpServletRequest req) {
		this.req = req;
	}

	public void initAll(boolean devel, boolean debug, boolean trans) {		
		deleteAll();		
		initializeMso1AndCategories(debug);
		initializeMso2AndCategories(debug);	
		initializeMso3AndCategories(debug); //Mso3 is daai 3x3 owner
		createMso1DefaultChannels(devel, trans);
		createMso2DefaultChannels(devel, trans);	
		createMso3OwnedChannels(devel, trans);
		createMso1DefaultIpg(devel);
		createMso2DefaultIpg(devel);		
		createMso3ChannelSet(devel);
	}
	
	public void initMsoAndCategories(boolean debug) {
		initializeMso1AndCategories(debug);
		initializeMso2AndCategories(debug);	
	}	
	
	public void deleteAll() {
		cache = CacheFactory.get();
		if (cache != null) {
			cache.clear();
		}
		
		DbDumper dumper = new DbDumper();
		@SuppressWarnings("rawtypes")
		List list = dumper.findAll(Category.class, "createDate");
		dumper.deleteAll(Category.class, list);
		
		list = dumper.findAll(CategoryChannel.class, "createDate");
		dumper.deleteAll(CategoryChannel.class, list);
		
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
		
		list = dumper.findAll(PdrRaw.class, "createDate");
		dumper.deleteAll(PdrRaw.class, list);

		list = dumper.findAll(Subscription.class, "createDate");
		dumper.deleteAll(Subscription.class, list);
		
		list = dumper.findAll(SubscriptionLog.class, "createDate");
		dumper.deleteAll(SubscriptionLog.class, list);

		list = dumper.findAll(ViewLog.class, "createDate");
		dumper.deleteAll(ViewLog.class, list);
		
		list = dumper.findAll(AreaOwnership.class, "createDate");
		dumper.deleteAll(AreaOwnership.class, list);
		
		list = dumper.findAll(BrandAdmin.class, "createDate");
		dumper.deleteAll(BrandAdmin.class, list);
		
		list = dumper.findAll(ChannelAutosharing.class, "createDate");
		dumper.deleteAll(ChannelAutosharing.class, list);
		
		list = dumper.findAll(ChannelSetAutosharing.class, "createDate");
		dumper.deleteAll(ChannelSetAutosharing.class, list);
		
		list = dumper.findAll(SnsAuth.class, "createDate");
		dumper.deleteAll(SnsAuth.class, list);
		
		list = dumper.findAll(CategoryChannelSet.class, "createDate");
		dumper.deleteAll(CategoryChannelSet.class, list);
		
		list = dumper.findAll(ChannelSet.class, "createDate");
		dumper.deleteAll(ChannelSet.class, list);
		
		list = dumper.findAll(ChannelSetChannel.class, "createDate");
		dumper.deleteAll(ChannelSetChannel.class, list);
		
		list = dumper.findAll(ContentOwnership.class, "createDate");
		dumper.deleteAll(ContentOwnership.class, list);
		
		log.info("delete all is done");
	}
	
	private void initializeMso1AndCategories(boolean debug) {
		//a default MSO
		MsoManager msoMngr = new MsoManager();
		Mso mso = new Mso("9x9", "9x9", "mso@9x9.tv", Mso.TYPE_NN);
		mso.setTitle("9x9.tv");
		mso.setPreferredLangCode(Mso.LANG_EN);
		mso.setJingleUrl("/WEB-INF/../videos/opening.swf");
		mso.setLogoUrl("/WEB-INF/../images/logo_9x9.png");
		mso.setLogoClickUrl("/");
		msoMngr.create(mso);
		
		//config
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig config = new MsoConfig(mso.getKey().getId(), MsoConfig.CDN, MsoConfig.CDN_AKAMAI);
		configMngr.create(config);
		String debugStr = "1";
		if (!debug) {debugStr = "0";}
		MsoConfig config1 = new MsoConfig(mso.getKey().getId(), MsoConfig.DEBUG, debugStr);
		configMngr.create(config1);
		
		//a default MSO user
		NnUserManager userMngr = new NnUserManager();
		NnUser user = new NnUser("mso@9x9.tv", "9x9mso", "9x9 mso", NnUser.TYPE_NN);
		user.setMsoId(mso.getKey().getId()); //!!!
		userMngr.create(user);
		
		//initialize default categories
		String[] categoryStr = {
			"Activism", "Automotive", "Comedy", "Entertainment", "Finance", "Food & Wine",
			"Gaming", "Gay & Lesbian", "Health & Fitness", "How to", 
			"Lifestyle", "Music", "News & Politics", "Outdoor",
			"People", "Pets & Animals", "Religion", "Sports", "Tech & Science",
			"Travel"
		};
				
		CategoryManager categoryMngr = new CategoryManager();
		for (String name : categoryStr) {			
			categoryMngr.create(new Category(name, true, mso.getKey().getId()));
		}
		log.info("initializeMso1AndCategories is done");
	}

	private void initializeMso2AndCategories(boolean debug) {
		//a default MSO
		MsoManager msoMngr = new MsoManager();
		Mso mso = new Mso("5f", "5f", "mso@5f.tv", Mso.TYPE_MSO);
		mso.setTitle("5f.tv");
		mso.setPreferredLangCode(Mso.LANG_ZH_TW);
		mso.setJingleUrl("/WEB-INF/../videos/opening.swf");
		mso.setLogoUrl("/WEB-INF/../images/logo_9x9.png");
		mso.setLogoClickUrl("/");
		msoMngr.create(mso);
		
		//config
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig config = new MsoConfig(mso.getKey().getId(), MsoConfig.CDN, MsoConfig.CDN_AKAMAI);
		configMngr.create(config);
		String debugStr = "1";
		if (!debug) {debugStr = "0";}
		MsoConfig config1 = new MsoConfig(mso.getKey().getId(), MsoConfig.DEBUG, debugStr);
		configMngr.create(config1);		
		
		//a default MSO user
		NnUserManager userMngr = new NnUserManager();
		NnUser user = new NnUser("mso@5f.tv", "5ffmso", "5f mso", NnUser.TYPE_TBC);		
		user.setMsoId(mso.getKey().getId()); //!!! constructor, or create
		userMngr.create(user);
		
		//initialize default categories
		String[] categoryStr = {
			"活動中心", "視聽劇場", "數位高手", "ACG夢工廠", "生活娛樂館", "國家研究院", "國家體育場", "文創藝廊", "影音實驗室"
		};
		
		CategoryManager categoryMngr = new CategoryManager();
		for (String name : categoryStr) {			
			categoryMngr.create(new Category(name, true, mso.getKey().getId()));
		}
		log.info("initializeMso2AndCategories is done");
	}		
	
	public void initializeMso3AndCategories(boolean debug) {
		//a default 3x3 owner (MSO)
		MsoManager msoMngr = new MsoManager();
		Mso mso = new Mso("daai", "daai", "daai@9x9.tv", Mso.TYPE_3X3);
		mso.setTitle("Da Ai TV");
		mso.setPreferredLangCode(Mso.LANG_ZH_TW);
		mso.setJingleUrl("/WEB-INF/../videos/opening.swf");
		mso.setLogoUrl("http://9x9ui.s3.amazonaws.com/9x9playerV52/images/logo_tzuchi.png");
		mso.setLogoClickUrl("/");
		msoMngr.create(mso);
		
		//config
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig config = new MsoConfig(mso.getKey().getId(), MsoConfig.CDN, MsoConfig.CDN_AKAMAI);
		configMngr.create(config);
		String debugStr = "1";
		if (!debug) {debugStr = "0";}
		MsoConfig config1 = new MsoConfig(mso.getKey().getId(), MsoConfig.DEBUG, debugStr);
		configMngr.create(config1);		
		
		//a default 3x3 user (MSO)
		NnUserManager userMngr = new NnUserManager();
		NnUser user = new NnUser("daai@9x9.tv", "daaimso", "daai", NnUser.TYPE_3X3);		
		user.setMsoId(mso.getKey().getId()); //!!! constructor, or create
		userMngr.create(user);
		
		//initialize default categories
		String[] categoryStr = {
			"慈濟大愛電視"
		};
		
		CategoryManager categoryMngr = new CategoryManager();
		for (String name : categoryStr) {			
			categoryMngr.create(new Category(name, true, mso.getKey().getId()));
		}
		log.info("initializeMso3AndCategories is done");
	}
		
	public void createMso1DefaultChannels(boolean devel, boolean trans){
		//prepare data
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findNNUser();
		List<Category> categories = new ArrayList<Category>();
		CategoryManager categoryMngr = new CategoryManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoProgramManager programMngr = new MsoProgramManager();
		Category category = categoryMngr.findByName("Activism");
		categories.add(category);

		if (devel) {
			//create channel		
			MsoChannel channel1 = new MsoChannel("Etsy", "Etsy.com", "http://s3.amazonaws.com/9x9chthumb/54e2967caf4e60fe9bc19ef1920997977eae1578.gif", user.getKey().getId());
			channel1.setSourceUrl("http://feeds.feedburner.com/etsyetsyetsy");
			channel1.setPublic(true);
			channelMngr.create(channel1, categories);
			
			MsoProgram program1 = new MsoProgram("Handmade Confessional: Eli Dlugach", "Eli Dlugach gives a testimonial on why he loves handmade", "http://s3.amazonaws.com/9x9cache/005a69b4431d521e39534431254d81a211ebefc7_1227739497_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program1.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/005a69b4431d521e39534431254d81a211ebefc7_1227739497_thumbLarge.jpg");
			program1.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/005a69b4431d521e39534431254d81a211ebefc7_1227739497.m4v");
			program1.setWebMFileUrl("http://s3.amazonaws.com/9x9cache/005a69b4431d521e39534431254d81a211ebefc7_1227739497.webm");	
			program1.setPublic(true);
			
			programMngr.create(channel1, program1);	
			MsoProgram program2 = new MsoProgram("How-Tuesday: Needle Felted Eyeballs", "Read the full Etsy blog post", "http://s3.amazonaws.com/9x9cache/5a14e5502fd5ab6b26e7f11f2a38ee718bc06eea_1288043514_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program2.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/5a14e5502fd5ab6b26e7f11f2a38ee718bc06eea_1288043514_thumbLarge.jpg");
			program2.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/5a14e5502fd5ab6b26e7f11f2a38ee718bc06eea_1288043514.m4v");	
			program2.setPublic(true);
			programMngr.create(channel1, program2);
		
			//create channel
			MsoChannel channel2 = new MsoChannel("TEDTalks (hd)", "TED", "http://s3.amazonaws.com/9x9chthumb/f14a9bb972adfefab1c9c4f0ec44f251686d655a.jpg", user.getKey().getId());		
			channel2.setSourceUrl("http://feeds.feedburner.com/tedtalksHD");
			channel2.setPublic(true);
			channelMngr.create(channel2, categories);

			MsoProgram program3 = new MsoProgram("TEDTalks : Beverly + Dereck", "Beverly + Dereck Joubert live in the bush", "http://s3.amazonaws.com/9x9cache/8ad69b8dcbd0edd516c4f6bd530390d9f640de45_1292858280_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program3.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/8ad69b8dcbd0edd516c4f6bd530390d9f640de45_1292858280_thumbLarge.jpg");
			program3.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/8ad69b8dcbd0edd516c4f6bd530390d9f640de45_1292858280.mp4");	
			program3.setPublic(true);
			programMngr.create(channel2, program3);
	
			MsoProgram program4 = new MsoProgram("TEDTalks : Peter Molyneux", "Peter Molyneux demos Milo", "http://s3.amazonaws.com/9x9cache/5716619074068502b91f5f9668cf906a6702078b_1282119180_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program4.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/5716619074068502b91f5f9668cf906a6702078b_1282119180_thumbLarge.jpg");
			program4.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/5716619074068502b91f5f9668cf906a6702078b_1282119180.mp4");	
			program4.setPublic(true);
			programMngr.create(channel2, program4);
	
			MsoProgram program8 = new MsoProgram("TEDTalks : Hans Rosling", "Hans Rosling reframes 10 years of UN", "http://s3.amazonaws.com/9x9cache/5ee1ea7ea93d6703c90fb4dc00188f4e5619ee1f_1286442720_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program8.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/5ee1ea7ea93d6703c90fb4dc00188f4e5619ee1f_1286442720_thumbLarge.jpg");
			program8.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/5ee1ea7ea93d6703c90fb4dc00188f4e5619ee1f_1286442720.mp4");	
			program8.setPublic(true);
			programMngr.create(channel2, program8);
	
			MsoProgram program9 = new MsoProgram("TEDTalks : Zainab Salbi: Women", "In war we often see only the frontline stories of soldiers and combat", "http://s3.amazonaws.com/9x9cache/6e0c1a48b2a5b8b9253b25885a2cce9586564366_1290523500_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program9.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/6e0c1a48b2a5b8b9253b25885a2cce9586564366_1290523500_thumbLarge.jpg");
			program9.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/6e0c1a48b2a5b8b9253b25885a2cce9586564366_1290523500.mp4");
			program9.setWebMFileUrl("http://s3.amazonaws.com/9x9cache/6e0c1a48b2a5b8b9253b25885a2cce9586564366_1290523500.webm");
			program9.setPublic(true);
			programMngr.create(channel2, program9);
		
			//create channel				
			MsoChannel channel5 = new MsoChannel("System Channel", "System Channel", "/WEB-INF/../images/logo_9x9.png", user.getKey().getId());
			channel5.setPublic(true);
			Category system = categoryMngr.findByName("Tech & Science");
			List<Category> systemCategories = new ArrayList<Category>();
			systemCategories.add(system);
			channelMngr.create(channel5, categories);
	
			MsoProgram program7 = new MsoProgram("System Program", "", "/WEB-INF/../images/logo_9x9.png", MsoProgram.TYPE_VIDEO);
			program7.setPublic(true);
			program7.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/default.mp4");
			program7.setWebMFileUrl("http://9x9pod.s3.amazonaws.com/default.mp4");	
			programMngr.create(channel5, program7);
					
			//create a channel, but status set to error
			MsoChannel channel3 = new MsoChannel("Vegan A Go-Go", "A simple vegan cooking show.", "http://s3.amazonaws.com/9x9chthumb/6bb992aafe18c3054ca30035d7e5fe7cc9394d37.jpg", user.getKey().getId());		
			channel3.setSourceUrl("http://feeds.feedburner.com/veganagogo");
			channel3.setStatus(MsoChannel.STATUS_ERROR);
			channel3.setPublic(true);
			channelMngr.create(channel3, categories);
					
			MsoProgram program5 = new MsoProgram("EP 40: Caramelized Rosemary Pears", "Caramelized Rosemary Pears Serves: 6-8", "http://s3.amazonaws.com/9x9cache/a023cd7cafa3b9d18d3e48274b5994c3cbacd759_1218075566_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program5.setImageLargeUrl("	http://s3.amazonaws.com/9x9cache/a023cd7cafa3b9d18d3e48274b5994c3cbacd759_1218075566_thumbLarge.jpg");
			program5.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/a023cd7cafa3b9d18d3e48274b5994c3cbacd759_1218075566.mp4");
			program5.setWebMFileUrl("http://s3.amazonaws.com/9x9cache/a023cd7cafa3b9d18d3e48274b5994c3cbacd759_1218075566.webm");
			program5.setPublic(true);
			programMngr.create(channel3, program5);
	
			MsoProgram program6 = new MsoProgram("EP 55: Herbed Fruit Salad", "Herbed Fruit Salad Serves: 10 Difficulty", "http://s3.amazonaws.com/9x9cache/0047ece77a9dcdce929d69be889d3f4258e98b38_1245713603_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program6.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/0047ece77a9dcdce929d69be889d3f4258e98b38_1245713603_thumbLarge.jpg");
			program6.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/0047ece77a9dcdce929d69be889d3f4258e98b38_1245713603.mp4");
			program6.setWebMFileUrl("http://s3.amazonaws.com/9x9cache/0047ece77a9dcdce929d69be889d3f4258e98b38_1245713603.webm");
			program6.setPublic(true);
			programMngr.create(channel3, program6);
	
			//create a channel, but no programs
			MsoChannel channel4 = new MsoChannel("Comedy Central's Jokes.com", "", "http://s3.amazonaws.com/9x9cache/1b2885a8ba30ee692b56fd0e9c9128995473367e_1199163600_thumbnail.jpg", user.getKey().getId());		
			channel4.setSourceUrl("http://feeds.feedburner.com/comedycentral/standup");
			channel4.setPublic(true);
			channelMngr.create(channel4, categories);
		} else {						
			categories.clear();
			Category c = categoryMngr.findByName("Activism");
			categories.add(c);
			String[] urls1 = this.getMso1ActivismChannels();
			this.channelsCreate(urls1, categories, user.getKey().getId(), trans);
						
			categories.clear();
			c = categoryMngr.findByName("Automotive");
			categories.add(c);
			String[] urls2 = this.getMso1AutomativeChannels();
			this.channelsCreate(urls2, categories, user.getKey().getId(), trans);

			categories.clear();
			c = categoryMngr.findByName("Comedy");
			categories.add(c);
			String[] urls3 = this.getMso1ComedyChannels();
			this.channelsCreate(urls3, categories, user.getKey().getId(), trans);

			categories.clear();
			c = categoryMngr.findByName("Entertainment");
			categories.add(c);
			String[] urls4 = this.getMso1EntertainmentChannels();
			this.channelsCreate(urls4, categories, user.getKey().getId(), trans);
			
			categories.clear();			
			c = categoryMngr.findByName("Finance");
			categories.add(c);
			String[] urls5 = this.getMso1FinanceChannels();
			this.channelsCreate(urls5, categories, user.getKey().getId(), trans);
			
			categories.clear();			
			c = categoryMngr.findByName("Food & Wine");
			categories.add(c);
			String[] urls6 = this.getMso1FoodWineChannels();
			this.channelsCreate(urls6, categories, user.getKey().getId(), trans);
						
			categories.clear();
			c = categoryMngr.findByName("Gaming");
			categories.add(c);
			String[] urls7 = this.getMso1GamingChannels();
			this.channelsCreate(urls7, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Gay & Lesbian");
			categories.add(c);
			String[] urls8 = this.getMso1GLChannels();
			this.channelsCreate(urls8, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Health & Fitness");
			categories.add(c);
			String[] urls9 = this.getMso1HFChannels();
			this.channelsCreate(urls9, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("How to");
			categories.add(c);
			String[] urls10 = this.getMso1HowToChannels();
			this.channelsCreate(urls10, categories, user.getKey().getId(), trans);
			
			categories.clear();			
			c = categoryMngr.findByName("Lifestyle");
			categories.add(c);
			String[] urls11 = this.getMso1LifeStyleChannels();
			this.channelsCreate(urls11, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Music");
			categories.add(c);
			String[] urls12 = this.getMso1MusicChannels();
			this.channelsCreate(urls12, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("News & Politics");
			categories.add(c);
			String[] urls13 = this.getMso1NPChannels();
			this.channelsCreate(urls13, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Outdoor");
			categories.add(c);
			String[] urls20 = this.getMso1OutdoorChannels();
			this.channelsCreate(urls20, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("People");
			categories.add(c);
			String[] urls14 = this.getMso1PeopleChannels();
			this.channelsCreate(urls14, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Pets & Animals");
			categories.add(c);
			String[] urls15 = this.getMso1PAChannels();
			this.channelsCreate(urls15, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Religion");
			categories.add(c);
			String[] urls16 = this.getMso1ReligionChannels();
			this.channelsCreate(urls16, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Sports");
			categories.add(c);
			String[] urls17 = this.getMso1SportsChannels();
			this.channelsCreate(urls17, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Tech & Science");
			categories.add(c);
			String[] urls18 = this.getMso1TSChannels();
			this.channelsCreate(urls18, categories, user.getKey().getId(), trans);
			
			categories.clear();
			c = categoryMngr.findByName("Travel");
			categories.add(c);
			String[] urls19 = this.getMso1TravelChannels();
			this.channelsCreate(urls19, categories, user.getKey().getId(), trans);
															
			MsoChannel channel5 = new MsoChannel("System Channel", "System Channel", "/WEB-INF/../images/logo_9x9.png", user.getKey().getId());
			channel5.setPublic(true);
			channel5.setSourceUrl("http://9x9pod.s3.amazonaws.com/default.mp4"); //to avoid the duplication
			Category system = categoryMngr.findByName("Tech & Science");
			List<Category> systemCategories = new ArrayList<Category>();
			systemCategories.add(system);
			channelMngr.create(channel5, categories);
	
			MsoProgram program7 = new MsoProgram("System Program", "", "/WEB-INF/../images/logo_9x9.png", MsoProgram.TYPE_VIDEO);
			program7.setPublic(true);
			program7.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/default.mp4");
			program7.setWebMFileUrl("http://9x9pod.s3.amazonaws.com/default.mp4");	
			programMngr.create(channel5, program7);			
		}
		log.info("prepareMso1DefaultChannels is done");
	}	
	
	public void createMso2DefaultChannels(boolean devel, boolean trans){
		//prepare data
		Mso mso = new MsoManager().findByName("5f");
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmailAndMso("mso@5f.tv", mso);
		List<Category> categories = new ArrayList<Category>();
				
		if (devel) {
			//create channel1
			CategoryManager categoryMngr = new CategoryManager();
			Category category = categoryMngr.findByName("活動中心");		
			MsoChannelManager channelMngr = new MsoChannelManager();
			MsoChannel channel1 = new MsoChannel("中文伊特", "中文伊特.com", "http://s3.amazonaws.com/9x9chthumb/54e2967caf4e60fe9bc19ef1920997977eae1578.gif", user.getKey().getId());
			channel1.setSourceUrl("http://feeds.feedburner.com/etsyetsyetsy");
			channel1.setPublic(true);
			categories.add(category);
			channelMngr.create(channel1, categories);
			
			// -- create program1
			MsoProgramManager programMngr = new MsoProgramManager();
			MsoProgram program1 = new MsoProgram("手作", "手作", "http://s3.amazonaws.com/9x9cache/005a69b4431d521e39534431254d81a211ebefc7_1227739497_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program1.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/005a69b4431d521e39534431254d81a211ebefc7_1227739497_thumbLarge.jpg");
			program1.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/005a69b4431d521e39534431254d81a211ebefc7_1227739497.m4v");
			program1.setWebMFileUrl("http://s3.amazonaws.com/9x9cache/005a69b4431d521e39534431254d81a211ebefc7_1227739497.webm");	
			program1.setPublic(true);
			programMngr.create(channel1, program1);
		
			MsoProgram program2 = new MsoProgram("禮拜二", "詳情請讀部落格", "http://s3.amazonaws.com/9x9cache/5a14e5502fd5ab6b26e7f11f2a38ee718bc06eea_1288043514_thumbnail.jpg", MsoProgram.TYPE_VIDEO);
			program2.setImageLargeUrl("http://s3.amazonaws.com/9x9cache/5a14e5502fd5ab6b26e7f11f2a38ee718bc06eea_1288043514_thumbLarge.jpg");
			program2.setMpeg4FileUrl("http://s3.amazonaws.com/9x9pod/5a14e5502fd5ab6b26e7f11f2a38ee718bc06eea_1288043514.m4v");	
			program2.setPublic(true);
			programMngr.create(channel1, program2);
		} else {
			CategoryManager categoryMngr = new CategoryManager();
			Category category = categoryMngr.findByName("活動中心");
			categories.add(category);
			String[] urls1 = {"http://www.youtube.com/user/taiwanroc100", "http://www.youtube.com/user/msfhk",
					 "http://www.youtube.com/user/lisahou62", "http://www.youtube.com/user/savedogs2009",
					 "http://www.youtube.com/user/twtati", "http://www.youtube.com/user/twchannel",
					 "http://www.youtube.com/user/Twimitv"};
			this.channelsCreate(urls1, categories, user.getKey().getId(), trans);
			categories.clear();
			category = categoryMngr.findByName("視聽劇場");
			categories.add(category);
			String[] urls2 = this.getShowUrl();
			this.channelsCreate(urls2, categories, user.getKey().getId(), trans);

			categories.clear();
			category = categoryMngr.findByName("數位高手");
			categories.add(category);
			String[] urls3 = this.getDigitalUrl();
			this.channelsCreate(urls3, categories, user.getKey().getId(), trans);

			categories.clear();
			category = categoryMngr.findByName("ACG夢工廠");
			if (category == null) {categoryMngr.findByName("Acg夢工廠");}
			categories.add(category);
			String[] urls4 = this.getDreamUrl();
			this.channelsCreate(urls4, categories, user.getKey().getId(), trans);

			categories.clear();
			category = categoryMngr.findByName("生活娛樂館");
			categories.add(category);
			String[] urls5 = this.getUrlEntertainment();
			this.channelsCreate(urls5, categories, user.getKey().getId(), trans);

			categories.clear();
			category = categoryMngr.findByName("國家研究院");
			categories.add(category);
			String[] urls6 = this.getUrlStudy();
			this.channelsCreate(urls6, categories, user.getKey().getId(), trans);

			categories.clear();
			category = categoryMngr.findByName("國家體育場");
			categories.add(category);
			String[] urls7 = this.getUrlSports();
			this.channelsCreate(urls7, categories, user.getKey().getId(), trans);

			categories.clear();
			category = categoryMngr.findByName("文創藝廊");
			categories.add(category);
			String[] urls8 = this.getUrlArt();
			this.channelsCreate(urls8, categories, user.getKey().getId(), trans);

			categories.clear();
			category = categoryMngr.findByName("影音實驗室");
			categories.add(category);
			String[] urls9 = this.getUrlMovie();
			this.channelsCreate(urls9, categories, user.getKey().getId(), trans);			
		}
		log.info("prepareMso2DefaultChannels is done");		
	}
	
	public void createMso3OwnedChannels(boolean devel, boolean trans) {
		//prepare data
		Mso mso = new MsoManager().findByName("daai");
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmailAndMso("daai@9x9.tv", mso);
		List<Category> categories = new ArrayList<Category>();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		
		if (devel) {
			//create channel1
			CategoryManager categoryMngr = new CategoryManager();
			Category category = categoryMngr.findByName("慈濟大愛電視");
			MsoChannelManager channelMngr = new MsoChannelManager();
			MsoChannel channel1 = new MsoChannel("大愛電視", "慈濟大愛電視台", "http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/da_ai_dian_shi_files/shapeimage_3.png", user.getKey().getId());
			channel1.setSourceUrl("http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/rss.xml");
			channel1.setPublic(true);
			channel1.setContentType(MsoChannel.CONTENTTYPE_PODCAST);
			categories.add(category);
			channelMngr.create(channel1, categories);
			
			//channel1 ownership
			ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
			channel1 = channelMngr.findByName("大愛電視");
			ownershipMngr.create(new ContentOwnership(), mso, channel1);
			channels.add(channel1);
			
			//create channel2
			MsoChannel channel2 = new MsoChannel("靜思語", "大愛靜思語", "http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/jing_si_yu_files/shapeimage_4.png", user.getKey().getId());
			channel2.setSourceUrl("http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/rss.xml");
			channel2.setPublic(true);
			channel2.setContentType(MsoChannel.CONTENTTYPE_PODCAST);
			channelMngr.create(channel2, categories);
			
			//channel2 ownership
			channel2 = channelMngr.findByName("靜思語");
			ownershipMngr.create(new ContentOwnership(), mso, channel2);
			channels.add(channel2);
			
			// -- create program1
			MsoProgramManager programMngr = new MsoProgramManager();
			MsoProgram program1 = new MsoProgram("環保迎新春", "環保迎新春", "http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/Media/DaAiTV_2011newyear_children.jpg", MsoProgram.TYPE_VIDEO);
			program1.setImageLargeUrl("http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/Media/DaAiTV_2011newyear_children.jpg");
			program1.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/8c5f1a679bfff2359465a2af93c519bbbada8568_1296553894.m4v");
			program1.setPublic(true);
			programMngr.create(channel1, program1);
			
			MsoProgram program2 = new MsoProgram("初一特別節目", "初一特別節目", "http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/Media/DaAiTV_2011newyear_0101.jpg", MsoProgram.TYPE_VIDEO);
			program2.setImageLargeUrl("http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/Media/DaAiTV_2011newyear_0101.jpg");
			program2.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/3b9cfb9c433a6b06c54612dbaeeb5bf08b6469b7_1296458603.m4v");
			program2.setPublic(true);
			programMngr.create(channel1, program2);
			
			MsoProgram program3 = new MsoProgram("365天", "365天", "http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/Media/DaAiTV_2011newyear_MV.jpg", MsoProgram.TYPE_VIDEO);
			program3.setImageLargeUrl("http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/Media/DaAiTV_2011newyear_MV.jpg");
			program3.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/3b9cfb9c433a6b06c54612dbaeeb5bf08b6469b7_1296458603.m4v");
			program3.setPublic(true);
			programMngr.create(channel1, program3);
			
			MsoProgram program4 = new MsoProgram("遇事 若能平心面對", "遇事，若能平心面對，很快就會度過。", "http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/Media/STILL_20100601_40.jpg", MsoProgram.TYPE_VIDEO);
			program4.setImageLargeUrl("http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/Media/STILL_20100601_40.jpg");
			program4.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/70c1e2711487e956509ddbbf8c4e8134bc98dfbb_1280201357.m4v");
			program4.setPublic(true);
			programMngr.create(channel2, program4);
			
			MsoProgram program5 = new MsoProgram("面對困難 當下盡心", "面對困難，當下盡心、盡力、盡人事就對了。", "http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/Media/STILL_20100601_32.jpg", MsoProgram.TYPE_VIDEO);
			program5.setImageLargeUrl("http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/Media/STILL_20100601_32.jpg");
			program5.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/d17ffaf0fbb1d46d44cb3004478846af245c1f20_1279520261.m4v");
			program5.setPublic(true);
			programMngr.create(channel2, program5);
			
			MsoProgram program6 = new MsoProgram("心無雜念 凡事樂觀", "心無雜念、凡事樂觀、踏實做事，就會有智慧。", "http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/Media/STILL_20100601_29.jpg", MsoProgram.TYPE_VIDEO);
			program6.setImageLargeUrl("http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/Media/STILL_20100601_29.jpg");
			program6.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/e871b2fa5c68425b96fe2875b51e299161e9fc3b_1279258807.m4v");
			program6.setPublic(true);
			programMngr.create(channel2, program6);
		} else {
			CategoryManager categoryMngr = new CategoryManager();
			MsoChannelManager channelMngr = new MsoChannelManager();
			ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
			Category category = categoryMngr.findByName("慈濟大愛電視");
			categories.add(category);
			String[] urls = this.getMso3OwnedChannels();
			this.channelsCreate(urls, categories, user.getKey().getId(), trans);
			for (String url : urls) {
				url = channelMngr.verifyUrl(url);
				MsoChannel channel = channelMngr.findBySourceUrlSearch(url);
				ownershipMngr.create(new ContentOwnership(), mso, channel);
			}
		}
		log.info("prepareMso3DefaultChannels is done");
	}
	
	private void channelsCreate(String[] urls, List<Category>categories, long userId, boolean trans) {
		TranscodingService tranService = new TranscodingService();
		MsoChannelManager channelMngr = new MsoChannelManager();
		CategoryManager categoryMngr = new CategoryManager();
		for (String url : urls) {
			log.info(url);
			url = channelMngr.verifyUrl(url);
			MsoChannel channel = channelMngr.findBySourceUrlSearch(url);
			if (channel != null) {
				categoryMngr.changeCategory(channel.getKey().getId(), categories);				
			} else {
				MsoChannel c = new MsoChannel(url, userId);
				c.setContentType(channelMngr.getContentTypeByUrl(url));
				channelMngr.create(c, categories);
				System.out.println(c.getSourceUrl());
				System.out.println(c.getKey().getId());
				if (req == null) {System.out.println("error");}
				if (trans) {
					tranService.submitToTranscodingService(c.getKey().getId(), c.getSourceUrl(), req);
				}			
			}			
		}		
	}
	
	public void createMso1DefaultIpg(boolean devel) {
		MsoIpgManager msoIpgMngr = new MsoIpgManager();
		Mso mso = new MsoManager().findNNMso();
		MsoChannelManager channelMngr = new MsoChannelManager();
		CategoryManager categoryMngr = new CategoryManager();
		if (devel) {
			Category c = categoryMngr.findByName("Activism");
			List<MsoChannel> channels = new MsoChannelManager().findPublicChannelsByCategoryId(c.getKey().getId());
			int counter = 1;
			int limit = 4;
			for (int i=0; i<channels.size(); i++) {
				if (counter > limit) {break;}	
				MsoChannel chn = channels.get(i);
				if (counter < 2 && !chn.getName().equals("System Channel") && channelMngr.isCounterQualified(chn)) {
					MsoIpg msoIpg = new MsoIpg(mso.getKey().getId(), chn.getKey().getId(), counter, MsoIpg.TYPE_READONLY);			
					msoIpgMngr.create(msoIpg);
					counter++;			
				} else if (counter < 3 && !chn.getName().equals("System Channel") && channelMngr.isCounterQualified(chn)) {
					MsoIpg msoIpg = new MsoIpg(mso.getKey().getId(), chn.getKey().getId(), counter, MsoIpg.TYPE_GENERAL);			
					msoIpgMngr.create(msoIpg);
					counter++;
				}						
				if (chn.getName().equals("System Channel")) {
					MsoIpg msoIpg = new MsoIpg(mso.getKey().getId(), chn.getKey().getId(), 81, MsoIpg.TYPE_READONLY);
					msoIpgMngr.create(msoIpg);
					counter++;
				}
			}
		} else {
			String[] urls = this.getMso1DefaultIpg();
			for (int i=0; i< urls.length; i++) {
				MsoChannel c = channelMngr.findBySourceUrlSearch(channelMngr.verifyUrl(urls[i]));
				System.out.println("i=" + i + ";" + urls[i]);
				System.out.println("channel=" + c.getName() + ";");
				MsoIpg msoIpg = new MsoIpg(mso.getKey().getId(), c.getKey().getId(), i+1, MsoIpg.TYPE_GENERAL);			
				msoIpgMngr.create(msoIpg);							
			}			
			MsoChannel channel = new MsoChannelManager().findByName("System Channel");
			MsoIpg msoIpg = new MsoIpg(mso.getKey().getId(), channel.getKey().getId(), 81, MsoIpg.TYPE_READONLY);
			msoIpgMngr.create(msoIpg);	
			System.out.println("mso1 ipg:" + urls.length);
		}
		log.info("prepareMso1DefaultIpg is done");
	}

	public void createMso2DefaultIpg(boolean devel) {
		MsoIpgManager msoIpgMngr = new MsoIpgManager();
		MsoChannelManager cMngr = new MsoChannelManager();
		Mso mso = new MsoManager().findByName("5f");
		
		if (devel) {
			Category c = new CategoryManager().findByName("活動中心");
			List<MsoChannel> channels = new MsoChannelManager().findPublicChannelsByCategoryId(c.getKey().getId());
			MsoIpg msoIpg = new MsoIpg(mso.getKey().getId(), channels.get(0).getKey().getId(), 1, MsoIpg.TYPE_READONLY);					
			msoIpgMngr.create(msoIpg);		
		} else {		
			String[] urls = this.getMso2IpgUrls();
			String[] seqs = this.getMso2IpgSeq();
			System.out.println(urls.length + seqs.length);
			for (int i=0; i<urls.length; i++) {
				MsoChannel c = cMngr.findBySourceUrlSearch(cMngr.verifyUrl(urls[i]));
				System.out.println("i=" + i + ";" + urls[i]);
				System.out.println("channel=" + c.getName() + ";");				
				MsoIpg msoIpg = new MsoIpg(mso.getKey().getId(), c.getKey().getId(), Integer.parseInt(seqs[i]), MsoIpg.TYPE_GENERAL);					
				msoIpgMngr.create(msoIpg);					
			}
			System.out.println("mso2 ipg:" + urls.length);
		}
		log.info("prepareMso2DefaultIpg is done");
	}
	
	public void createMso3ChannelSet(boolean devel) {
		
		Mso mso = new MsoManager().findByName("daai");
		ChannelSetManager channelSetMngr = new ChannelSetManager();
		List<MsoChannel> channels = new ArrayList<MsoChannel>();
		
		if (devel) {
			CategoryManager categoryMngr = new CategoryManager();
			Category category = categoryMngr.findByName("慈濟大愛電視");
			channels = new MsoChannelManager().findPublicChannelsByCategoryId(category.getKey().getId());
			channels.get(0).setSeq(1);
			channels.get(1).setSeq(2);
			/*
			for (int i = 0; i < channels.size(); i++) {
				channels.get(i).setSeq(i+1);
			}
			*/
		} else {
			MsoChannelManager channelMngr = new MsoChannelManager();
			String[] urls = this.getMso3ChannelSetUrls();
			String[] seqs = this.getMso3ChannelSetSeqs();
			log.info(urls.length + " " + seqs.length);
			for (int i = 0; i < urls.length; i++) {
				MsoChannel channel = channelMngr.findBySourceUrlSearch(channelMngr.verifyUrl(urls[i]));
				log.info("i=" + i + ";" + urls[i]);
				log.info("channel=" + channel.getName() + ";");
				channel.setSeq(Integer.parseInt(seqs[i]));
				
				channels.add(channel);
			}
			log.info("channels length: " + channels.size());
		}
		
		ChannelSet channelSet = new ChannelSet(mso.getKey().getId(), "大愛3x3", "大愛3x3", true);
		channelSet.setDefaultUrl("12345678"); // ugly url http://9x9.tv/12345678
		channelSet.setBeautifulUrl("daai"); // beautiful url http://9x9.tv/daai
		channelSetMngr.create(channelSet, channels);
		
		//channelSet ownership
		ContentOwnershipManager ownershipMngr = new ContentOwnershipManager();
		ownershipMngr.create(new ContentOwnership(), mso, channelSet);
		
		log.info("prepareMso3ChannelSet is done");
	}
	
	private String[] getUrlMovie() {
		String[] url = {
				"http://www.youtube.com/user/rtvshow19",
				"http://www.youtube.com/user/bigbirdasa",
				"http://www.youtube.com/user/yan27149",
				"http://www.youtube.com/user/ca12fju",
				"http://www.youtube.com/user/wowwanwan",
				"http://www.youtube.com/user/journalismshu",
				"http://www.youtube.com/user/pccujnn9th",
				"http://www.youtube.com/user/cyberTKU",
				"http://www.youtube.com/user/DmaTut",
				"http://www.youtube.com/user/fcutv",
				"http://www.youtube.com/user/fcumlc",
				"http://www.youtube.com/user/ttudesign",
				"http://www.youtube.com/user/boyandgirlcomehere",
				"http://www.youtube.com/user/tr908325",
				"http://www.youtube.com/user/kevinjason03",
				"http://www.youtube.com/user/kinkuanc",
				"http://www.youtube.com/user/annie020612",
				"http://www.youtube.com/user/kimzzchizz",
				"http://www.youtube.com/user/catlocker",
				"http://www.youtube.com/user/rtvshow19",
				"http://www.youtube.com/user/bigbirdasa",
				"http://www.youtube.com/user/yan27149",
				"http://www.youtube.com/user/ca12fju",
				"http://www.youtube.com/user/wowwanwan",
				"http://www.youtube.com/user/journalismshu",
				"http://www.youtube.com/user/pccujnn9th",
				"http://www.youtube.com/user/cyberTKU",
				"http://www.youtube.com/user/DmaTut",
				"http://www.youtube.com/user/fcutv",
				"http://www.youtube.com/user/fcumlc",
				"http://www.youtube.com/user/ttudesign",
				"http://www.youtube.com/user/boyandgirlcomehere",
				"http://www.youtube.com/user/tr908325",
				"http://www.youtube.com/user/kevinjason03",
				"http://www.youtube.com/user/kinkuanc",
				"http://www.youtube.com/user/annie020612",
				"http://www.youtube.com/user/kimzzchizz",
				"http://www.youtube.com/user/catlocker"				
		};
		return url;
	}
	
	private String[] getUrlArt() {
		String[] url = {
				"http://www.youtube.com/user/CCANEWS",
				"http://www.youtube.com/user/dxmonline",
				"http://www.youtube.com/user/ntcharts",
				"http://www.youtube.com/user/khamtw",
				"http://www.youtube.com/user/TheNextBigThingTW",
				"http://www.youtube.com/user/WINYEDA",
				"http://www.youtube.com/user/awakeningtw"				
		};
		return url;
	}
	
	private String[] getUrlSports() {
		String[] url = {
				"http://www.youtube.com/user/NBA",
				"http://www.youtube.com/user/NBAHiighlights",
				"http://www.youtube.com/user/NBAErik",
				"http://www.youtube.com/user/lalakersfan88",
				"http://www.youtube.com/user/HeatTeo",
				"http://www.youtube.com/user/ATPWorldTour",
				"http://www.youtube.com/user/TennisAustralia",
				"http://www.youtube.com/user/yowmingchen",
				"http://www.youtube.com/user/JRSportBrief",
				"http://www.youtube.com/user/sheng98news",
				"http://www.youtube.com/user/taiwansr",
				"http://www.youtube.com/user/jimmylin121",
				"http://www.youtube.com/user/bostonceltics", 
				"http://www.youtube.com/user/YESNetwork",
				"http://www.youtube.com/user/wwedivax5",
				"http://www.youtube.com/user/djscratch2008",
				"http://www.youtube.com/user/theF1com",
				"http://www.youtube.com/user/secondsout",
				"http://www.youtube.com/user/lisachen110",
				"http://www.youtube.com/user/ilovebaseballforever",
				"http://www.youtube.com/user/topmma",
				"http://www.youtube.com/user/UFC",
				"http://www.youtube.com/user/TNAwrestling",
				"http://www.youtube.com/user/glyphmedia",
				"http://www.youtube.com/user/fcbarcelona",
				"http://www.youtube.com/user/NHLVideo",
				"http://www.youtube.com/user/redbull",
				"http://www.youtube.com/user/nikefootball",
				"http://www.youtube.com/user/Hoopmixtape",
				"http://www.youtube.com/user/TeamFlightBrothers",
				"http://www.youtube.com/user/wrc",
				"http://www.youtube.com/user/LoadedNewsletter",
				"http://www.youtube.com/user/enminem",
				"http://www.youtube.com/user/ThrasherMagazine",
				"http://www.youtube.com/user/XTremeVideo",
				"http://www.youtube.com/user/golf",
				"http://www.youtube.com/user/AmericanParkour",
				"http://www.youtube.com/user/nkalexander7",
				"http://www.youtube.com/user/NJPW",
				"http://www.youtube.com/user/letsKENDO",
				"http://www.youtube.com/user/IKIKAZEBOXING",
				"http://www.youtube.com/user/Happymagicskateboard", 
				"http://www.youtube.com/user/adblitz",
				"http://www.youtube.com/user/adidasoriginals", 
				"http://www.youtube.com/user/adidasfootballtv",
				"http://www.youtube.com/user/nikesoccer",                              
				"http://www.youtube.com/user/jumpman23"
		};
		return url;
	}
	
	private String[] getUrlStudy() {
		String url[] = {
				"http://www.youtube.com/user/Taiwantrade",
				"http://www.youtube.com/user/ChurchofScientology",
				"http://www.youtube.com/user/CambridgeUniversity",
				"http://www.youtube.com/user/MIT", 
				"http://www.youtube.com/user/StanfordUniversity",
				"http://www.youtube.com/user/UCBerkeley",
				"http://www.youtube.com/user/UCtelevision",
				"http://www.youtube.com/user/YaleUniversity",
				"http://www.youtube.com/user/UCLA",
				"http://www.youtube.com/user/UCBerkeleyEvents",
				"http://www.youtube.com/user/stanfordbusiness",
				"http://www.youtube.com/user/arirangkorean",
				"http://www.youtube.com/user/japanesepod101",
				"http://www.youtube.com/user/CWTV",
				"http://www.youtube.com/user/cwgv",
				"http://www.youtube.com/user/eballgogogo",
				"http://www.youtube.com/user/presidentialoffice",
				"http://www.youtube.com/user/bwnet"				
		};
		return url;
	}
	
	private String[] getUrlEntertainment() {
		String url[] = {
				"http://www.youtube.com/user/gorgeousspace",
				"http://www.youtube.com/user/IKEAmeatball",
				"http://www.youtube.com/user/fashionguide",
				"http://www.youtube.com/user/CWNTV",
				"http://www.youtube.com/user/elletvfashion",
				"http://www.youtube.com/user/vogueTV",
				"http://www.youtube.com/user/fashionTV",
				"http://www.youtube.com/user/mctw1",
				"http://www.youtube.com/user/sppweb",
				"http://www.youtube.com/user/catwalk2009",
				"http://www.youtube.com/user/beauty321",
				"http://www.youtube.com/user/fuzkittie",
				"http://www.youtube.com/user/pitin999",
				"http://www.youtube.com/user/lohas88louis",
				"http://www.youtube.com/user/webwave970",
				"http://www.youtube.com/user/lioncyber",
				"http://www.youtube.com/user/TLC",                                                                     
				"http://www.youtube.com/user/clys23",
				"http://www.youtube.com/user/starfunTV",
				"http://www.youtube.com/user/happy100stay",
				"http://www.youtube.com/user/thegrandformosa",
				"http://www.youtube.com/user/chiayigov",
				"http://www.youtube.com/user/ipapr",
				"http://www.youtube.com/user/tw078413",
				"http://www.youtube.com/user/parispeetee",
				"http://www.youtube.com/user/weatherrisk",
				"http://www.youtube.com/user/WestJet",
				"http://www.youtube.com/user/Budgetplaces",
				"http://www.youtube.com/user/Visitvictoria",
				"http://www.youtube.com/user/EmiratesExperience",
				"http://www.youtube.com/user/DisneyParks",
				"http://www.youtube.com/user/gionettaiwan",
				"http://www.youtube.com/user/seouldreamseries ",
				"http://www.youtube.com/user/visitkorea ",
				"http://www.youtube.com/user/spain",
				"http://www.youtube.com/user/machilogmovie",
				"http://www.youtube.com/user/drivemovie",
				"http://www.youtube.com/user/tontantin",
				"http://www.youtube.com/user/shitemita",
				"http://www.youtube.com/user/syaso",
				"http://www.youtube.com/user/nimo5",
				"http://www.youtube.com/user/ddloveaa",
				"http://www.youtube.com/user/wwwBeauTubecc",
				"http://www.youtube.com/user/qwq33",
				"http://www.youtube.com/user/ikkiknoles",
				"http://www.youtube.com/user/momallnet",
				"http://www.youtube.com/user/tw078413",
				"http://www.youtube.com/user/2ojux",
				"http://www.youtube.com/user/bikinicom",
				"http://www.youtube.com/user/FHMTW",
				"http://www.youtube.com/user/VICTORIASSECRET",
				"http://www.youtube.com/user/A3Network",
				"http://www.youtube.com/user/starbucks",
				"http://www.youtube.com/user/BMW",
				"http://www.youtube.com/user/MercedesBenzTV",
				"http://www.youtube.com/user/UnitedStatesNavy ",
				"http://www.youtube.com/user/porsche",
				"http://www.youtube.com/user/Audi",
				"http://www.youtube.com/user/Honda",
				"http://www.youtube.com/user/hennesandmauritz",
				"http://www.youtube.com/user/DisneyParks",
				"http://www.youtube.com/user/dolcegabbanachannel",
				"http://www.youtube.com/user/Burberry ",
				"http://www.youtube.com/user/myvolkswagen ",
				"http://www.youtube.com/user/LexusVehicles ",
				"http://www.youtube.com/user/MINI",
				"http://www.youtube.com/user/thejeepchannel",
				"http://www.youtube.com/user/chevrolet",
				"http://www.youtube.com/user/Ford",
				"http://www.youtube.com/user/Peugeot",
				"http://www.youtube.com/user/LorealParis",
				"http://www.youtube.com/user/Pepsi",
				"http://www.youtube.com/user/Bundeswehr",
				"http://www.youtube.com/user/Lowes",
				"http://www.youtube.com/user/LOUISVUITTON",
				"http://www.youtube.com/user/namgeun",
				"http://www.youtube.com/user/sun57500",
				"http://www.youtube.com/user/olivinej",
				"http://www.youtube.com/user/Mmovies21",
				"http://www.youtube.com/user/gardea23",
				"http://www.youtube.com/user/dogs101tw",
				"http://www.youtube.com/user/Wearefordogs",
				"http://www.youtube.com/user/TEAMJGY",
				"http://www.youtube.com/user/sppa105986",
				"http://www.youtube.com/user/mimi74812",
				"http://www.youtube.com/user/88100dog",
				"http://www.youtube.com/user/ca8207",
				"http://www.youtube.com/user/brunello1997",
				"http://www.youtube.com/user/haijiq",
				"http://www.youtube.com/user/ShippoTV",
				"http://www.youtube.com/user/buycartv",
				"http://www.youtube.com/user/bk20185",
				"http://www.youtube.com/user/motousa",
				"http://www.youtube.com/user/RevZillaTV",
				"http://www.youtube.com/user/autocar",
				"http://www.youtube.com/user/TopGear",
				"http://www.youtube.com/user/motorstelevision",
				"http://www.youtube.com/user/motorcyclenewsdotcom",
				"http://www.youtube.com/user/Autoexpress",
				"http://www.youtube.com/user/Shmee150",
				"http://www.youtube.com/user/Motorscouk",
				"http://www.youtube.com/user/carshowclassic",
				"http://www.youtube.com/user/BEAUTYQQ",
				"http://www.youtube.com/user/michellephan",
				"http://www.youtube.com/user/makeupbytiffanyD",
				"http://www.youtube.com/user/ilovemakeup",
				"http://www.youtube.com/user/qafbeijing",
				"http://www.youtube.com/user/TheDailyGays",
				"http://www.youtube.com/user/thatgaybunch",
				"http://www.youtube.com/user/beaverbunch",
				"http://www.youtube.com/user/johnpatton15",
				"http://www.youtube.com/user/devinanderica",
				"http://www.youtube.com/user/wearnNews",
				"http://www.youtube.com/user/chung1219",                                                        
				"http://www.youtube.com/user/chri5784 ",
				"http://www.youtube.com/user/Dulan9 ",
				"http://www.youtube.com/user/ma19ko",
				"http://www.youtube.com/user/gd1104",
				"http://www.youtube.com/user/3rdid8487",
				"http://www.youtube.com/user/pepsi",
				"http://www.youtube.com/user/Otakuarmy"				
		};
		return url;
	}
	
	private String[] getDreamUrl() {
		String url[] = {
				"http://www.youtube.com/user/efunTV",
				"http://www.youtube.com/user/RUGNN",
				"http://www.youtube.com/user/BahamutGNN",
				"http://www.youtube.com/user/gamebasegnc",           
				"http://www.youtube.com/user/SuperEAMAN",
				"http://www.youtube.com/user/huskystarcraft",
				"http://www.youtube.com/user/hdstarcraft",
				"http://www.youtube.com/user/EASPORTS",
				"http://www.youtube.com/user/MrOperationSports",
				"http://www.youtube.com/user/yaoilover019",
				"http://www.youtube.com/user/rrobbert184",
				"http://www.youtube.com/user/shingin",
				"http://www.youtube.com/user/NextGenTactics",
				"http://www.youtube.com/user/IGNentertainment",
				"http://www.youtube.com/user/HazardCinema",
				"http://www.youtube.com/user/PS3Comedy",
				"http://www.youtube.com/user/Games",
				"http://www.youtube.com/user/InecomCompany",
				"http://www.youtube.com/user/pilicreateworld",
				"http://www.youtube.com/user/Fujiigumi",
				"http://www.youtube.com/user/xbox",
				"http://www.youtube.com/user/LittleBigPlanetUK",
				"http://www.youtube.com/user/ElectronicArtsDE",				
		};
		return url;
	}
	
	private String[] getDigitalUrl() {
		String url[] = {
				"http://www.youtube.com/user/lifesforsharing",
				"http://www.youtube.com/user/bing",
				"http://www.youtube.com/user/IBM",
				"http://www.youtube.com/user/BlackBerry",
				"http://www.youtube.com/user/terry28853669",
				"http://www.youtube.com/user/nokia",
				"http://www.youtube.com/user/sonyericsson",
				"http://www.youtube.com/user/Google",
				"http://www.youtube.com/user/apple",
				"http://www.youtube.com/user/WindowsVideos",
				"http://www.youtube.com/user/sonyelectronics",
				"http://www.youtube.com/user/AtGoogleTalks",
				"http://www.youtube.com/user/Googledevelopers",
				"http://www.youtube.com/user/EngadgetChinese",
				"http://www.youtube.com/user/15fun",
				"http://www.youtube.com/user/CNETTV",
				"http://www.youtube.com/user/jon4lakers",                                                                       
				"http://www.youtube.com/user/UDNDigital",
				"http://www.youtube.com/user/iphone4tw",
				"http://www.youtube.com/user/jima6636",
				"http://www.youtube.com/user/tapcritic",
				"http://www.youtube.com/user/TEDtalksDirector",
				"http://www.youtube.com/user/tysiphonehelp",
				"http://www.youtube.com/user/DCviewNO1",
				"http://www.youtube.com/user/androidcentral",
				"http://www.youtube.com/user/salesforce",
				"http://www.youtube.com/user/sogitv"			
		};
		return url;
	}
	
	private String[] getShowUrl() {
		String[] url = {
				"http://www.youtube.com/user/aff0021",
				"http://www.youtube.com/user/thaicraze",
				"http://www.youtube.com/user/ptscc",
				"http://www.youtube.com/user/ChinaTimes",
				"http://www.youtube.com/user/ttvnewsview",
				"http://www.youtube.com/user/NMANews",
				"http://www.youtube.com/user/ETTVnews",
				"http://www.youtube.com/user/asahicom",
				"http://www.youtube.com/user/peoponews",
				"http://www.youtube.com/user/InDeepCloud",
				"http://www.youtube.com/user/PTSTalk",
				"http://www.youtube.com/user/hakkatv",
				"http://www.youtube.com/user/appleactionews",
				"http://www.youtube.com/user/FTVCP",
				"http://www.youtube.com/user/FMTV168",
				"http://www.youtube.com/user/ctitv",
				"http://www.youtube.com/user/TBSCTS",
				"http://www.youtube.com/user/chinatv",
				"http://www.youtube.com/user/pts",                                                                                            
				"http://www.youtube.com/user/zimeitao",
				// "http://www.youtube.com/user/trailers", // can not pass formatCheck
				"http://www.youtube.com/user/twfoxmovies",
				"http://www.youtube.com/user/2010jaychou",
				"http://www.youtube.com/user/FoxBroadcasting",
				"http://www.youtube.com/user/elura2009",
				"http://www.youtube.com/user/NextTVent",
				"http://www.youtube.com/user/nagootv",
				"http://www.youtube.com/user/SETTV",
				"http://www.youtube.com/user/RTHK",
				"http://www.youtube.com/user/hollywoodstreams",
				"http://www.youtube.com/user/NationalGeographic",
				"http://www.youtube.com/user/kbsworld",
				"http://www.youtube.com/user/arirangworld",
				"http://www.youtube.com/user/ystarchannel2",
				"http://www.youtube.com/user/NHKonline",
				"http://www.youtube.com/user/ystarchannel4",
				"http://www.youtube.com/user/achun5",
				"http://www.youtube.com/user/khalilfongfanclub",
				"http://www.youtube.com/user/goodtv",
				"http://www.youtube.com/user/bearchen000",
				"http://www.youtube.com/user/DaAiVideo",
				"http://www.youtube.com/user/BillboardGoddess",
				"http://www.youtube.com/user/avexnetwork",
				"http://www.youtube.com/user/Rina93",
				"http://www.youtube.com/user/kpopmv2011",
				"http://www.youtube.com/user/AsiaHolicKpop",
				"http://www.youtube.com/user/AsianMusicWorldHD",
				"http://www.youtube.com/user/sment",
				"http://www.youtube.com/user/kbsworld",
				"http://www.youtube.com/user/musiciansinstitute",
				"http://www.youtube.com/user/universalmusicjapan",
				"http://www.youtube.com/user/musiciansinstituteja",
				"http://www.youtube.com/user/vul3a04SNSDing",
				"http://www.youtube.com/user/KPOPMV020",
				"http://www.youtube.com/user/mamonuser",
				"http://www.youtube.com/user/TonyKPOPMV",
				"http://www.youtube.com/user/rockhall",
				"http://www.youtube.com/user/rockitoutblog",
				"http://www.youtube.com/user/KpopNET4",
				"http://www.youtube.com/user/GuitarCenterTV",
				"http://www.youtube.com/user/AKB48",
				"http://www.youtube.com/user/FueledByRamen",
				"http://www.youtube.com/user/ahmirTV",
				"http://www.youtube.com/user/officialtiesto",
				"http://www.youtube.com/user/UltraRecords",
				"http://www.youtube.com/user/JRAquinomusic",
				"http://www.youtube.com/user/atlanticvideos",
				"http://www.youtube.com/user/armadamusic",
				"http://www.youtube.com/user/datarecordsuk",
				"http://www.youtube.com/user/scantraxxrecordz",
				"http://www.youtube.com/user/ortoPilot",
				"http://www.youtube.com/user/warnertaiwan",
				"http://www.youtube.com/user/universaltwn",
				"http://www.youtube.com/user/ystarchannel3",
				"http://www.youtube.com/user/PRINTGAKUFU",
				"http://www.youtube.com/user/discoverynetworks",
				"http://www.youtube.com/user/nationalgeographic",
				"http://www.youtube.com/user/animalplanetTV",
		};
		return url;
	}
	
	private String[] getMso2IpgUrls() {
		String[] urls = {
				"http://www.youtube.com/user/nmanews",
				"http://www.youtube.com/user/InDeepCloud",
				"http://www.youtube.com/user/2ojux",
				"http://www.youtube.com/user/NBA",
				"http://www.youtube.com/user/yowmingchen",
				"http://www.youtube.com/user/jimmylin121",
				"http://www.youtube.com/user/YESNetwork",
				"http://www.youtube.com/user/TLC",
				"http://www.youtube.com/user/clys23",
				"http://www.youtube.com/user/achun5",
				"http://www.youtube.com/user/gorgeousspace",
				"http://www.youtube.com/user/michellephan",     
				"http://www.youtube.com/user/beauty321",
				"http://www.youtube.com/user/fashionTV",
				"http://www.youtube.com/user/fashionguide",
				"http://www.youtube.com/user/FTVCP",
				"http://www.youtube.com/user/ctitv",
				"http://www.youtube.com/user/chinatv",
				"http://www.youtube.com/user/pts",
				"http://www.youtube.com/user/ChinaTimes",
				"http://www.youtube.com/user/SETTV",
				"http://www.youtube.com/user/weatherrisk",
				"http://www.youtube.com/user/kbsworld",
				"http://www.youtube.com/user/arirangworld",
				"http://www.youtube.com/user/vul3a04SNSDing",
				"http://www.youtube.com/user/KPOPMV020",
				"http://www.youtube.com/user/KpopNET4",
				"http://www.youtube.com/user/brunello1997",
				"http://www.youtube.com/user/EngadgetChinese",
				"http://www.youtube.com/user/efunTV",
				"http://www.youtube.com/user/gamebasegnc",
				"http://www.youtube.com/user/wwwBeauTubecc",
				"http://www.youtube.com/user/BMW",
				"http://www.youtube.com/user/buycartv"
		};
		return urls;
	}
	
	private String[] getMso2IpgSeq() {
		String[] seq = {
				"4", "5", "6", 
				"10", "11", "12", "13", "15", "16", "17", "18",
				"21", "22", "23", "24", "26", "27",
				"28", "29", "30", "31", "32", "33", "34", "35",
				"47", "48", "49", "50", "51", "52",
				"64", "65", "66", "67", "68", "69"
		};
	    return seq;
	}
	
	private String[] getMso3ChannelSetUrls() {
		String[] urls = {
				"http://podcast.daaitv.org/Daai_TV_Podcast/ren_jian_pu_ti/rss.xml",
				"http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_chen_yu/rss.xml",
				"http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/rss.xml",
				"http://podcast.daaitv.org/Daai_TV_Podcast/fa_pi_ru_shui/rss.xml",
				"http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/rss.xml",
				"http://www.youtube.com/view_play_list?p=236DA856894AFC8E",
				"http://www.youtube.com/user/DaAiVideo"
		};
		return urls;
	}
	
	private String[] getMso3ChannelSetSeqs() {
		String[] seqs ={
				"1", "2", "3", "4", "5", "6", "7"
		};
		return seqs;
	}
	
	public String[] getMso1DefaultIpg() {
		String[] urls = {
				"http://feeds.visionontv.net/visionontv/Olympics?format=xml",
				"http://feeds.feedburner.com/cnet/cartechpodcastvideo?format=xml",
				"http://www.youtube.com/user/huluDotCom",
				"http://feeds.feedburner.com/tedtalks_video",
				"http://feeds.feedburner.com/cnet/buzzreport?format=xml",
				"http://feeds.visionontv.net/Livinginthefuture?format=xml",
				"http://revision3.com/hak5/feed/MP4-Large",
				"http://anyonebutme.blip.tv/rss/itunes",
				"http://feeds.feedburner.com/fitlife",				
				"http://www.youtube.com/user/LoveSystems",				
				"http://www.youtube.com/user/maaximumseduction",
				"http://feeds.feedburner.com/rocknrolltv",
				"http://www.democracynow.org/podcast-video.xml",
				"http://feeds.feedburner.com/earth-touch_featured_720p?format=xml",
				"http://www.youtube.com/user/SHAYTARDS?feature=chclk",
				"http://www.discovery.com/radio/xml/discovery_video.xml",
				"http://www.youtube.com/user/nba",
				"http://feeds.feedburner.com/caliextralarge?format=xml",
				"http://lltv.libsyn.com/rss"				
		};
		return urls;
	}
	
	public String[] getMso1ActivismChannels() {
		String[] urls = {
				"http://feeds.visionontv.net/visionontv/Olympics?format=xml",
				"http://www.democracynow.org/podcast-video.xml",
				"http://www.youtube.com/profile?user=TheRealNews"				
		};
		return urls;
	}
	
	public String[] getMso1AutomativeChannels() {
		String[] urls = {
				"http://feeds.feedburner.com/cnet/cartechpodcastvideo?format=xml",
				"http://www.mevio.com/feeds/hdv.xml",
				"http://feeds.drivingsports.com/dstv-extra?format=xml",
				"http://www.youtube.com/user/TopGear",
				"http://www.youtube.com/user/mercedesbenztv",
				"http://www.youtube.com/user/iconmotosports"				
		};
		return urls;
	}
	
	public String[] getMso1ComedyChannels() {
		String[] urls = {
				"http://www.youtube.com/user/huluDotCom",
				"http://www.youtube.com/user/BestofYTChannel",
				"http://revision3.com/scamschool/feed/xvid-large/"				
		};
		return urls;
	}
	
	public String[] getMso1EntertainmentChannels() {
		String[] urls = {	
				"http://feeds.feedburner.com/tedtalks_video",
				"http://www.youtube.com/user/HBO",
				"http://revision3.com/hdnation/feed/Quicktime-High-Definition",
				"http://feeds.feedburner.com/imoviesblogspot?format=xml",
				"http://feeds.feedburner.com/macappguide-hd?format=xml",
				"http://www.hbo.com/podcasts/true_blood/podcast.xml",
				"http://podcast.msnbc.com/audio/podcast/MSNBC-MADDOW-NETCAST-M4V.xml",
				"http://www.youtube.com/user/richarddawkinsdotnet"				
		};
		return urls;
	}

	public String[] getMso1FinanceChannels() {
		String[] urls = {				
				"http://feeds.feedburner.com/cnet/buzzreport?format=xml",
				"http://www.youtube.com/user/TheInnovationNetwork",
				"http://feeds.harvardbusiness.org/harvardbusiness/videoideacast",
				"http://feeds.tvo.org/tvobigideasVideo?format=xml",				
		};
		return urls;
	}

	public String[] getMso1FoodWineChannels() {
		String[] urls = {				
				"http://feeds.visionontv.net/Livinginthefuture?format=xml",
				"http://delicioustv.libsyn.com/rss",
				"http://simplyming.dreamhosters.com/rss/vodcast.xml",
				"http://feeds2.feedburner.com/lfsn-cooking",
				"http://legourmettv.blip.tv/rss",
				"http://nytsynvideo.com/itunes/1",
				"http://feeds.feedburner.com/WinelibraryTv",
				"http://feeds.feedburner.com/eddVideo?format=xml",
				"http://wineweek.com.au/bm/rss.php?i=2"				
		};
		return urls;
	}

	public String[] getMso1GamingChannels() {
		String[] urls = {	
				"http://revision3.com/hak5/feed/MP4-Large",
				"http://feeds.gametrailers.com/rss_ipod_gen.php?source=xb360",
				"http://feeds.gametrailers.com/rss_ipod_gen.php?source=ps3",
				"http://www.g4tv.com/xplay/podcasts/6/G4_TV__XPlay_Video_Podcast.xml",
				"http://www.g4tv.com/attackoftheshow/podcasts/5/Attack_of_the_Show_Daily_Video_Podcast__G4_TV.xml",
				"http://www.youtube.com/user/freddiew",
				"http://www.youtube.com/user/huskystarcraft",				
		};
		return urls;
	}

	public String[] getMso1GLChannels() {
		String[] urls = {		
				"http://anyonebutme.blip.tv/rss/itunes",
				"http://jengotv.com/player-content/video-myd.xml", //!!!
				"http://www.youtube.com/user/kateclinton",
				"http://robsfeedtoday.blip.tv/rss"				
		};
		return urls;
	}

	public String[] getMso1HFChannels() {
		String[] urls = {
				"http://feeds.feedburner.com/fitlife",
				"http://feeds.feedburner.com/PilatesOnFifth?format=xml",
				"http://urbansustainableliv.blip.tv/rss",
				"http://www.diet.com/videos/rss-vodcast.php",				
		};
		return urls;
	}

	public String[] getMso1HowToChannels() {
		String[] urls = {	
				"http://www.youtube.com/user/LoveSystems",
				"http://feeds.feedburner.com/thewoodwhisperer",
				"http://www.basicbrewing.com/radio/video.rss",
				"http://www.youtube.com/user/MichellePhan",
				"http://www.youtube.com/user/AuntiesBeads",
				"http://www.youtube.com/user/spacepainter",				
		};
		return urls;
	}

	public String[] getMso1LifeStyleChannels() {
		String[] urls = {
				"http://feeds.feedburner.com/rocknrolltv",
				"http://www.youtube.com/user/maaximumseduction",
				"http://feeds.feedburner.com/fashionrocks",
				"http://www.designfix.tv/design_fix.xml",
				"http://downloads.designforlife.ie/podcasts/dfl_wmv/designforlife_windowsfeed.xml",
				"http://feeds.feedburner.com/YdnDesignGuide?format=xml",				
		};
		return urls;
	}

	public String[] getMso1MusicChannels() {
		String[] urls = {
				"http://feeds.feedburner.com/rocknrolltv",
				"http://www.youtube.com/user/OkGo",
				"http://www.youtube.com/user/stlhiphop",
				"http://caribbeanbeats.blip.tv/rss",
				"http://feeds.feedburner.com/djvibe_tv"				
		};
		return urls;
	}
	
	public String[] getMso1NPChannels() {
		String[] urls = {			
				"http://www.democracynow.org/podcast-video.xml",
				"http://feeds.theonion.com/OnionNewsNetwork",
				"http://feeds.feedburner.com/Euronews-NoComment?format=xml"				
		};
		return urls;
	}

	public String[] getMso1OutdoorChannels() {
		String[] urls = {	
				"http://feeds.feedburner.com/earth-touch_featured_720p?format=xml",
				"http://feeds.feedburner.com/alaskahdtv?format=xml",
				"http://feeds.visionontv.net/A-zOfBushcraft?format=xml"				
		};
		return urls;
	}

	public String[] getMso1PeopleChannels() {
		String[] urls = {				
				"http://www.youtube.com/user/SHAYTARDS?feature=chclk",
				"http://www.youtube.com/user/charlieissocoollike",
				"http://www.youtube.com/user/CiNNtv1",
				"http://www.youtube.com/user/mahmoodkhanpictures",
				"http://www.youtube.com/user/PhilipDeFranco"				
		};
		return urls;
	}

	public String[] getMso1PAChannels() {
		String[] urls = {				
				"http://www.discovery.com/radio/xml/discovery_video.xml",
				"http://feeds.pbs.org/pbs/wnet/nature-video",
				"http://feeds.feedburner.com/KittyCast?format=xml",				
		};
		return urls;
	}
	public String[] getMso1ReligionChannels() {
		String[] urls = {		
				"http://www.youtube.com/user/patcondell",
				"http://feeds.churchmediadesign.tv/churchmediadesign?format=xml",
				"http://ishafoundation.blip.tv/rss",
				"http://feeds.harvest.org/greglaurietv?format=xml",
				"http://rss.marshillchurch.org/mhcsermonvideo"				
		};
		return urls;
	}
	public String[] getMso1SportsChannels() {
		String[] urls = {
				"http://www.youtube.com/user/nba",
				"http://www.youtube.com/user/BigBlueChat",
				"http://www.youtube.com/user/EpicMealTime",
				"http://www.youtube.com/user/fcbarcelona",
				"http://sports.espn.go.com/espnradio/podcast/feeds/itunes/podCast?id=3403194",
				"http://www.youtube.com/user/UFC",
				"http://www.youtube.com/user/wwwtvgolocom",
				"http://www.youtube.com/user/hockeyfightsdotcom?feature=chclk"				
		};
		return urls;
	}
	public String[] getMso1TSChannels() {
		String[] urls = {	
				"http://feeds.feedburner.com/caliextralarge?format=xml",
				"http://www.youtube.com/user/richarddawkinsdotnet",
				"http://www.discovery.com/radio/xml/sciencevideo.xml",
				"http://chandra.harvard.edu/resources/podcasts/hd/podcasts.xml",
				"http://www.youtube.com/user/amzertech",
				"http://linuxjournal.blip.tv/rss/itunes/",
				"http://feeds.feedburner.com/cnet/applebytehd?format=xml",
				"http://feeds.feedburner.com/doctype/episodes?format=xml",
				"http://wordpress.tv/feed/",
				"http://revision3.com/tekzilla/feed/quicktime-high-definition?subshow=false",
				"http://www.engadget.com/engadgetshow.xml"				
		};
		return urls;
	}
	public String[] getMso1TravelChannels() {
		String[] urls = {		
				"http://lltv.libsyn.com/rss",
				"http://feeds.feedburner.com/Mayda3000"				
		};
		return urls;
	}
	
	public String[] getMso1DefaultChannels() { // it seems not used ?
		String[] urls = {
				"http://feeds.visionontv.net/visionontv/Olympics?format=xml",
				"http://feeds.feedburner.com/cnet/cartechpodcastvideo?format=xml",
				"http://www.youtube.com/user/huluDotCom",
				"http://feeds.feedburner.com/tedtalks_video",
				"http://feeds.feedburner.com/cnet/buzzreport?format=xml",
				"http://feeds.visionontv.net/Livinginthefuture?format=xml",
				"http://revision3.com/hak5/feed/MP4-Large",
				"http://anyonebutme.blip.tv/rss/itunes",
				"http://feeds.visionontv.net/Livinginthefuture?format=xml",
				"http://www.youtube.com/user/LoveSystems",
				"http://feeds.feedburner.com/rocknrolltv",
				"http://www.youtube.com/user/maaximumseduction",
				"http://feeds.feedburner.com/cnet/buzzreport?format=xml",
				"http://feeds.feedburner.com/earth-touch_featured_720p?format=xml",
				"http://www.youtube.com/user/SHAYTARDS?feature=chclk",
				"http://www.youtube.com/user/nba",
				"http://feeds.feedburner.com/caliextralarge?format=xml",
				"http://lltv.libsyn.com/rss"
		};
		
		return urls;
	}
	
	public String[] getMso3OwnedChannels() {
		String[] urls = {
				"http://podcast.daaitv.org/Daai_TV_Podcast/ren_jian_pu_ti/rss.xml",
				"http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_chen_yu/rss.xml",
				"http://podcast.daaitv.org/Daai_TV_Podcast/jing_si_yu/rss.xml",
				"http://podcast.daaitv.org/Daai_TV_Podcast/fa_pi_ru_shui/rss.xml",
				"http://podcast.daaitv.org/Daai_TV_Podcast/da_ai_dian_shi/rss.xml",
				"http://www.youtube.com/view_play_list?p=236DA856894AFC8E",
				"http://www.youtube.com/watch?v=bldfbrAp4hU&playnext=1&list=PL40E8E32DDA356BD2",
				"http://www.youtube.com/user/bearchen000#grid/user/B061246345E7F5C3",
				"http://www.youtube.com/user/bearchen000#grid/user/4DD808A8C9595946",
				"http://www.youtube.com/user/DaAiVideo",
				"http://www.youtube.com/user/TzuChiUSA",
				"http://www.youtube.com/user/happyshanshia",
				"http://www.youtube.com/user/tzuchicanada",
				"http://www.youtube.com/daaitvnews",
		};
		return urls;
	}
	
}
