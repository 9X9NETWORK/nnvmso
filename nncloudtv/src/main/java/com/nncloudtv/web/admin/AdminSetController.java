package com.nncloudtv.web.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.JqgridHelper;
import com.nncloudtv.model.Category;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnSet;
import com.nncloudtv.model.NnSetToNnChannel;
import com.nncloudtv.service.CategoryManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnSetManager;
import com.nncloudtv.service.NnSetToNnChannelManager;

@Controller
@RequestMapping("admin/set")
public class AdminSetController {
	protected static final Logger logger = Logger.getLogger(AdminSetController.class.getName());
	
	public final NnSetManager setMngr;
	
	@Autowired
	public AdminSetController(NnSetManager setMngr) {
		this.setMngr = setMngr;		
	}	

	@RequestMapping(value = "list", params = {"page", "rows", "sidx", "sord"})
	public void list(@RequestParam(value = "page")   Integer      currentPage,
	                 @RequestParam(value = "rows")   Integer      rowsPerPage,
	                 @RequestParam(value = "sidx")   String       sortIndex,
	                 @RequestParam(value = "sord")   String       sortDirection,
	                 @RequestParam(required = false) String       searchField,
	                 @RequestParam(required = false) String       searchOper,
	                 @RequestParam(required = false) String       searchString,
	                 @RequestParam(required = false) String       search,
	                 @RequestParam(required = false) boolean      notify,
	                 OutputStream out) {
						
		NnSetManager csMngr = new NnSetManager();
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> dataRows = new ArrayList<Map<String, Object>>();

		String filter = "";
		if (searchField != null && searchOper != null && searchString != null && !searchString.isEmpty()) {			
			Map<String, String> opMap = JqgridHelper.getOpMap();
			if (opMap.containsKey(searchOper)) {
				filter = searchField + " " + opMap.get(searchOper) + " " + searchString;
				logger.info("filter: " + filter);
				sortIndex = "updateDate";
				sortDirection = "desc";
			}
		}

		int totalRecords = csMngr.total(filter);
		int totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
		if (currentPage > totalPages)
			currentPage = totalPages;		
		List<NnSet> results = csMngr.list(currentPage, rowsPerPage, sortIndex, sortDirection, filter);
		NnSetToNnChannelManager cscMngr = new NnSetToNnChannelManager();
		for (NnSet cs : results) {			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			boolean qualified = true;
			if (notify) {
				qualified = false;
				Calendar cal = Calendar.getInstance();		
				cal.add(Calendar.DAY_OF_MONTH, - 14);
				Date d = cal.getTime();
				if (cs.getCreateDate().after(d)) {
					if (!cs.isPublic()) {
						qualified = true;
					} else {
						List<NnSetToNnChannel> cscs = cscMngr.findBySet(cs.getId());
						for (NnSetToNnChannel csc : cscs) {
							if (csc.getCreateDate().after(d)) {
								qualified = true;
							}
						}
					}
				}
			}
			if (qualified) {
				cell.add(cs.getId());
				cell.add(cs.getName());
				cell.add(cs.getIntro());
				cell.add(cs.isFeatured());
				cell.add(cs.isPublic());
				cell.add(cs.getLang());
				cell.add(cs.getSeq());
				cell.add(cs.getBeautifulUrl());
				cell.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cs.getUpdateDate()));
				cell.add(cs.getImageUrl());
				map.put("id", cs.getId());
				map.put("cell", cell);
				dataRows.add(map);
			}
		}
		try {
			mapper.writeValue(out, JqgridHelper.composeJqgridResponse(currentPage, totalPages, totalRecords, dataRows));			
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}				
	}

	public void channelSeqAdjust(
			NnSet set, NnChannel channel, 
			short seq, boolean delete, boolean edit) {
		if (!set.isFeatured())
			return;
		NnSetToNnChannelManager cscMngr = new NnSetToNnChannelManager();
		List<NnSetToNnChannel> list = cscMngr.findBySet(set.getId());		
		for (NnSetToNnChannel csc : list) {
			if (!delete && csc.getSeq() >= seq) {
				System.out.println("enter correct adjustment:" + csc.getSeq() + ";" + csc.getSeq()+1);
				csc.setSeq((short)(csc.getSeq() + 1));
				cscMngr.save(csc);
			}			
			if (delete) {
				if (csc.getSeq() >= seq) {
					if (csc != null) {
						csc.setSeq((short)(csc.getSeq() - 1));
						cscMngr.save(csc);
					}
				}				
			}
			
		}
		if (!delete) {
			NnSetToNnChannel csc = cscMngr.findBySetAndChannel(set.getId(), channel.getId());
			if (csc == null) { //add
				csc = new NnSetToNnChannel(set.getId(), channel.getId(), (short)seq);
				cscMngr.create(csc);
			} else {
				if (!edit) {
					csc.setSeq((short)(csc.getSeq() + 1));
				} else {
					csc.setSeq(csc.getSeq());
				}
				cscMngr.save(csc);
			}
		}
	}
	
	@RequestMapping(value="deleteCh", params = {"id", "set"})
	public @ResponseBody String deleteCh(@RequestParam(required = false) long set,	                 
			             @RequestParam(required = false) long id,
	                     OutputStream out) {		
		NnSetToNnChannelManager rMngr = new NnSetToNnChannelManager();
		NnSetToNnChannel sToC = rMngr.findBySetAndChannel(set, id);
		if (sToC != null) {
			rMngr.delete(sToC);
			NnSet s = new NnSetManager().findById(set);
			if (s.isFeatured()) {
				List<NnSetToNnChannel> list = rMngr.findBySet(set);
				List<NnSetToNnChannel> toBeSaved = new ArrayList<NnSetToNnChannel>();
				for (int i=0; i<list.size(); i++) {				
					NnSetToNnChannel t = list.get(i);
					int sequence = i+1;
					if (t.getSeq() != sequence) {				
						t.setSeq((short)sequence);
						toBeSaved.add(t);
					}
				}
				rMngr.saveAll(toBeSaved);
			}
		}
		return "OK";		
	}

	@RequestMapping(value="addCh")
	public @ResponseBody String addCh(
			             @RequestParam(required = false) long channel,
			             @RequestParam(required = false) long set,	                 
			             @RequestParam(required = false) String seq,
	                     OutputStream out) {
		NnSetManager setMngr = new NnSetManager();
		NnChannelManager cMngr = new NnChannelManager();
		NnChannel c = cMngr.findById(channel);
		if (c == null)
			return "Channel does not exist";
		NnSet s = setMngr.findById(set);
		if (s == null)
			return "Set does not exist";
		List<NnChannel> channels = new ArrayList<NnChannel>();
		c.setSeq(Short.parseShort(seq));
		channels.add(c);
		setMngr.addChannels(s, channels);		
//		if (s.isFeatured()) {
//			this.adjustSeq(s, c, Short.parseShort(seq));
//			//this.channelSeqAdjust(s, c, Short.parseShort(seq), false, false);
//		}
		List<NnChannel> existing = setMngr.findPlayerChannels(s);		
		if (existing.get(0).getImageUrl() != null)		 {	
			s.setImageUrl(existing.get(0).getImageUrl());
			new NnSetManager().save(s);
		}
		return "OK";
	}		
			
	@RequestMapping(value="editCh")
	public @ResponseBody String editCh(
			             @RequestParam(required = false) long id,
			             @RequestParam(required = false) long channel,
			             @RequestParam(required = false) long set,	                 
			             @RequestParam(required = false) short seq,
	                     OutputStream out) {
		setMngr.editChannel(set, channel, seq);
		return "OK";		
	}
		
	@RequestMapping(value = "listCh", params = {"page", "rows", "sidx", "sord", "set"})
	public @ResponseBody String listCh(
			         @RequestParam(value = "page")   Integer      currentPage,
	                 @RequestParam(value = "rows")   Integer      rowsPerPage,
	                 @RequestParam(value = "sidx")   String       sortIndex,
	                 @RequestParam(value = "sord")   String       sortDirection,
	                 @RequestParam(required = false) String       searchField,
	                 @RequestParam(required = false) String       searchOper,
	                 @RequestParam(required = false) String       searchString,
	                 @RequestParam(required = false) long       set,
	                 OutputStream out) {
		System.out.println("current page:" + currentPage);
		System.out.println("sort direction:" + sortDirection);
		System.out.println("sort index:" + sortIndex);
		NnSetManager setMngr = new NnSetManager();
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> dataRows = new ArrayList<Map<String, Object>>();
		NnSet s = setMngr.findById(set);
		List<NnChannel> totalResults = setMngr.findChannels(s);
		int totalRecords = totalResults.size();
		int totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
		//totalRecords = totalResults.size();
		//totalPages = (int)Math.ceil((double)totalRecords / rowsPerPage);
		
		List<NnChannel> results = new ArrayList<NnChannel>();		
		if(totalPages==0) {
			currentPage = 1;
			totalPages = 1;
		} else if(currentPage > totalPages)		
			currentPage = totalPages;
		
		if (totalRecords>0) {
			for (int i=(currentPage-1)*rowsPerPage;i<currentPage*rowsPerPage;i++) {			
				if(i<totalRecords) {
					results.add(totalResults.get(i));
				}
			}
		}
		for (NnChannel c : results) {			
			Map<String, Object> map = new HashMap<String, Object>();
			List<Object> cell = new ArrayList<Object>();
			cell.add(c.getId());
			cell.add(c.getName());
			cell.add(c.getSeq());
			cell.add(c.getContentType());
			map.put("id", c.getId());
			map.put("cell", cell);
			dataRows.add(map);
		}

		
		if (currentPage > totalPages)	
			currentPage = totalPages;		
		try {
			mapper.writeValue(out, JqgridHelper.composeJqgridResponse(currentPage, totalPages, totalRecords, dataRows));
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}
		return "OK";		
	}

	@RequestMapping("delete")
	public @ResponseBody String delete(@RequestParam(required=false) long id) {
		NnSetManager setMngr = new NnSetManager();
		NnSetToNnChannelManager sToNMngr = new NnSetToNnChannelManager();
		NnSet cs = setMngr.findById(id);		
		List<NnSetToNnChannel> list = sToNMngr.findBySet(cs.getId());
		sToNMngr.deleteAll(list);	
		setMngr.delete(cs);
		return "OK";
	}

	@RequestMapping("edit")
	public @ResponseBody String edit(
			@RequestParam(required=false) long id,
			@RequestParam(required=false) String name,
            @RequestParam(required=false) String intro,
            @RequestParam(required=false) String lang,
            @RequestParam(required=false) String imageUrl,
            @RequestParam(required=false) String beautifulUrl,
            @RequestParam(required=false) String isPublic,
            @RequestParam(required=false) String featured,
            @RequestParam(required=false) String channelIds,
            @RequestParam(required=false) String seq) {
		NnSetManager csMngr = new NnSetManager();
		NnChannelManager cMngr = new NnChannelManager();
		NnSetToNnChannelManager cscMngr = new NnSetToNnChannelManager();
		NnSet cs = csMngr.findById(id);
		if (name != null) cs.setName(name);
		if (intro != null) cs.setIntro(intro);
		if (isPublic != null)
			cs.setPublic(Boolean.parseBoolean(isPublic));
		if (featured != null)			
			cs.setFeatured(Boolean.parseBoolean(featured));
		if (imageUrl != null)
			cs.setImageUrl(imageUrl);
		if (beautifulUrl != null)
			cs.setBeautifulUrl(beautifulUrl);
		cs.setLang(lang);
		if (seq != null) {
			/*
			System.out.println("seqs:" + seq);
			cs.setSeq(Short.parseShort(seq));
			*/
		}		
		csMngr.save(cs);
		//this.adjustSeq(cs, seq);
		this.addRecCategory(cs);

		if (channelIds == null) 
			return "OK";
		
		String[] chId = channelIds.split(",");
		String[] chSeq = seq.split(",");		

		List<NnSetToNnChannel> list = cscMngr.findBySet(cs.getId());
		cscMngr.deleteAll(list);
		list = new ArrayList<NnSetToNnChannel>();
		for (int i=0; i<chId.length; i++) {
			NnChannel c = cMngr.findById(Long.parseLong(chId[i]));			
			NnSetToNnChannel csc = new NnSetToNnChannel(cs.getId(), c.getId(), Short.parseShort(chSeq[i]));
			csc.setCreateDate(new Date());
			list.add(csc);
		}
		cscMngr.saveAll(list);
		
		CategoryManager catMngr = new CategoryManager();		
		if (cs.getLang().equals("zh") && cs.getName().equals("綜合推薦")) {
			Category cat = catMngr.findByName("推薦頻道");
			if (cat != null) {
				catMngr.addSet(cat, cs);
			}				
		}
		if (cs.getLang().equals("en") && cs.getName().equals("Recommended")) {
			Category cat = catMngr.findByName("Recommended");
			if (cat != null) {
				if (cat != null) {
				   catMngr.addSet(cat, cs);
				}
			}				
		}
		//this.adjustSeq(cs, seq);		
		return "OK";
	}
		
	private void addRecCategory(NnSet cs) {
		CategoryManager catMngr = new CategoryManager();
		if (cs.getLang().equals("zh") && cs.isFeatured()) {
			Category cat = catMngr.findByName("推薦頻道");
			if (cat != null) {
				catMngr.addSet(cat, cs);
			}				
		}
		if (cs.getLang().equals("en") && cs.isFeatured()) {
			Category cat = catMngr.findByName("Recommended");
			if (cat != null) {
				if (cat != null) {
					catMngr.addSet(cat, cs);
				}
			}				
		}
	}
	
	@RequestMapping(value = "create", params = {"name", "intro", "featured", "lang", "imageUrl", "beautifulUrl", "seq"})
	public @ResponseBody String create(@RequestParam String name,	                                   
	                                   @RequestParam String intro,
	                                   @RequestParam boolean featured,
	                                   @RequestParam String imageUrl,
	                                   @RequestParam String beautifulUrl,
	                                   @RequestParam String seq,
	                                   @RequestParam String lang) {
		NnSetManager setMngr = new NnSetManager();
		NnSet set = new NnSet(name, intro, true);
		set.setBeautifulUrl(name);
		set.setLang(lang);		
		set.setBeautifulUrl(beautifulUrl);
		set.setImageUrl(imageUrl);
		set.setFeatured(featured);
		setMngr.create(set, null);
		//this.addRecCategory(set);
		//this.adjustSeq(set, seq);
		return "OK";		
	}
		
}
