package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.AreaOwnershipDao;
import com.nnvmso.model.AreaOwnership;

@Service
public class AreaOwnershipManager {
	
	protected static final Logger logger = Logger.getLogger(AreaOwnershipManager.class.getName());
	
	private AreaOwnershipDao areaDao = new AreaOwnershipDao();
	
	public void create(AreaOwnership area) {
		Date now = new Date();
		area.setCreateDate(now);
		area.setUpdateDate(now);
		areaDao.save(area);
	}
	
	public void delete(AreaOwnership area) {
		areaDao.delete(area);
	}
	
	public AreaOwnership save(AreaOwnership area) {
		area.setUpdateDate(new Date());
		area = areaDao.save(area);
		return area;
	}
	
	public List<AreaOwnership> findByUserId(long userId) {
		return areaDao.findByUserId(userId);
	}
	
	public AreaOwnership findByUserIdAndAreaNo(long userId, short areaNo) {
		return areaDao.findByUserIdAndAreaNo(userId, areaNo);
	}
	
	public AreaOwnership findByUserIdAndSetId(long userId, long setId) {
		return areaDao.findByUserIdAndSetId(userId, setId);
	}	
	
}
