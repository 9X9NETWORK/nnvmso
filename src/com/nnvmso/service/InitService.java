package com.nnvmso.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.nnvmso.model.*;

/**
 * for testing, works only for small set of data
 */	
@Service
public class InitService {
	
	public void deleteAll() {
		DbDumper dumper = new DbDumper();
		@SuppressWarnings("rawtypes")
		List list = dumper.findAll(Category.class, "createDate");
		dumper.deleteAll(Category.class, list);
		
		list = dumper.findAll(CategoryChannel.class, "createDate");
		dumper.deleteAll(CategoryChannel.class, list);
		
		list = dumper.findAll(Mso.class, "createDate");
		dumper.deleteAll(Mso.class, list);
		
		list = dumper.findAll(MsoIpg.class, "createDate");
		dumper.deleteAll(MsoIpg.class, list);		

		list = dumper.findAll(MsoChannel.class, "createDate");
		dumper.deleteAll(MsoChannel.class, list);
				
		list = dumper.findAll(MsoProgram.class, "createDate");
		dumper.deleteAll(MsoProgram.class, list);
		
		list = dumper.findAll(NnUser.class, "createDate");
		dumper.deleteAll(NnUser.class, list);
		
		list = dumper.findAll(Subscription.class, "createDate");
		dumper.deleteAll(Subscription.class, list);
	}
	
	private void createMso2DefaultChannels(){
		//prepare data
		Mso mso = new MsoManager().findByName("5f");
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findByEmailAndMso("mso@5f.tv", mso);
		List<Category> categories = new ArrayList<Category>();
				
		//create channel1
		CategoryManager categoryMngr = new CategoryManager();
		Category category = categoryMngr.findByName("喜劇");		
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoChannel channel1 = new MsoChannel("中文伊特", "中文伊特.com", "http://s3.amazonaws.com/9x9chthumb/54e2967caf4e60fe9bc19ef1920997977eae1578.gif", user.getKey());
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
		
	}
	
	private void createMso1DefaultIpg() {
		MsoIpgManager msoIpgMngr = new MsoIpgManager();
		Mso mso = new MsoManager().findNNMso();
		Category c = new CategoryManager().findByName("Activism");
		List<MsoChannel> channels = new MsoChannelManager().findPublicChannelsByCategoryId(c.getKey().getId());
		for (int i=0; i<2; i++) {
			MsoIpg msoIpg = new MsoIpg(mso.getKey(), channels.get(i).getKey(), i+1, MsoIpg.TYPE_READONLY);
			System.out.println("before name:" + channels.get(i).getName());			
			msoIpgMngr.create(msoIpg);
		}
		MsoIpg msoIpg = new MsoIpg(mso.getKey(), channels.get(2).getKey(), 81, MsoIpg.TYPE_GENERAL);
		msoIpgMngr.create(msoIpg);
	}

	private void createMso2DefaultIpg() {
		MsoIpgManager msoIpgMngr = new MsoIpgManager();
		Mso mso = new MsoManager().findByName("5f");
		Category c = new CategoryManager().findByName("喜劇");
		List<MsoChannel> channels = new MsoChannelManager().findPublicChannelsByCategoryId(c.getKey().getId());
		MsoIpg msoIpg = new MsoIpg(mso.getKey(), channels.get(0).getKey(), 1, MsoIpg.TYPE_READONLY);					
		msoIpgMngr.create(msoIpg);
	}
	
	private void createMso1DefaultChannels(){
		//prepare data
		NnUserManager userMngr = new NnUserManager();
		NnUser user = userMngr.findNNUser();
		List<Category> categories = new ArrayList<Category>();
		CategoryManager categoryMngr = new CategoryManager();
		MsoChannelManager channelMngr = new MsoChannelManager();
		MsoProgramManager programMngr = new MsoProgramManager();
		Category category = categoryMngr.findByName("Activism");				
		categories.add(category);
		
		//create channel		
		MsoChannel channel1 = new MsoChannel("Etsy", "Etsy.com", "http://s3.amazonaws.com/9x9chthumb/54e2967caf4e60fe9bc19ef1920997977eae1578.gif", user.getKey());
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
		MsoChannel channel2 = new MsoChannel("TEDTalks (hd)", "TED", "http://s3.amazonaws.com/9x9chthumb/f14a9bb972adfefab1c9c4f0ec44f251686d655a.jpg", user.getKey());		
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
		MsoChannel channel5 = new MsoChannel("System Channel", "System Channel", "/WEB-INF/../images/logo_9x9.png", user.getKey());
		channel5.setPublic(true);		
		channelMngr.create(channel5, categories);

		MsoProgram program7 = new MsoProgram("System Program", "", "/WEB-INF/../images/logo_9x9.png", MsoProgram.TYPE_VIDEO);
		program7.setPublic(true);
		program7.setMpeg4FileUrl("http://9x9pod.s3.amazonaws.com/default.mp4");
		program7.setWebMFileUrl("http://9x9pod.s3.amazonaws.com/default.mp4");	
		programMngr.create(channel5, program7);		
				
		//create a channel, but status set to error
		MsoChannel channel3 = new MsoChannel("Vegan A Go-Go", "A simple vegan cooking show.", "http://s3.amazonaws.com/9x9chthumb/6bb992aafe18c3054ca30035d7e5fe7cc9394d37.jpg", user.getKey());		
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
		MsoChannel channel4 = new MsoChannel("Comedy Central's Jokes.com", "", "http://s3.amazonaws.com/9x9cache/1b2885a8ba30ee692b56fd0e9c9128995473367e_1199163600_thumbnail.jpg", user.getKey());		
		channel4.setSourceUrl("http://feeds.feedburner.com/comedycentral/standup");
		channel4.setPublic(true);
		channelMngr.create(channel4, categories);				
	}
	
	private void initializeMso1AndCategories() {
		//a default MSO
		MsoManager msoMngr = new MsoManager();
		Mso mso = new Mso("9x9", "9x9", "mso@9x9.tv", Mso.TYPE_NN);
		mso.setPreferredLangCode(MsoChannel.LANG_EN);
		mso.setJingleUrl("/WEB-INF/../videos/logo2.swf");
		mso.setLogoUrl("/WEB-INF/../images/logo_9x9.png");
		mso.setLogoClickUrl("/");
		msoMngr.create(mso);
		
		//config
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig config = new MsoConfig(mso.getKey(), MsoConfig.CDN, MsoConfig.CDN_AKAMAI);
		configMngr.save(config);
		
		//a default MSO user
		NnUserManager userMngr = new NnUserManager();
		NnUser user = new NnUser("mso@9x9.tv", "9x9mso", "9x9 mso", NnUser.TYPE_NN);
		user.setMsoKey(mso.getKey());
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
			categoryMngr.create(new Category(name, true, mso.getKey()));
		}
	}

	private void initializeMso2AndCategories() {
		//a default MSO
		MsoManager msoMngr = new MsoManager();
		Mso mso = new Mso("5f", "5f", "mso@5f.tv", Mso.TYPE_MSO);
		mso.setPreferredLangCode(MsoChannel.LANG_ZH_TW);
		mso.setJingleUrl("/WEB-INF/../videos/logo2.swf");
		mso.setLogoUrl("/WEB-INF/../images/5 floor.jpg");
		mso.setLogoClickUrl("/");
		msoMngr.create(mso);
		
		//config
		MsoConfigManager configMngr = new MsoConfigManager();
		MsoConfig config = new MsoConfig(mso.getKey(), MsoConfig.CDN, MsoConfig.CDN_AKAMAI);
		configMngr.save(config);
		
		//a default MSO user
		NnUserManager userMngr = new NnUserManager();
		NnUser user = new NnUser("mso@5f.tv", "5ffmso", "5f mso", NnUser.TYPE_TBC);
		user.setMsoKey(mso.getKey());
		userMngr.create(user);
		
		//initialize default categories
		String[] categoryStr = {
				"喜劇", "動物", "藝術", "部落格"
		};
		
		CategoryManager categoryMngr = new CategoryManager();
		for (String name : categoryStr) {			
			categoryMngr.create(new Category(name, true, mso.getKey()));
		}
	}		
	
	public void initTestData() {
		createMso1DefaultChannels();
		createMso2DefaultChannels();	
		createMso1DefaultIpg();
		createMso2DefaultIpg();
	}

	public void initMsoAndCategories() {
		initializeMso1AndCategories();
		initializeMso2AndCategories();	
	}	
	
	public void initAll() {
		deleteAll();		
		initializeMso1AndCategories();
		initializeMso2AndCategories();	
		createMso1DefaultChannels();
		createMso2DefaultChannels();	
		createMso1DefaultIpg();
		createMso2DefaultIpg();		
	}
	
}