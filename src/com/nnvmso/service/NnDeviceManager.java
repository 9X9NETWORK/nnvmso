package com.nnvmso.service;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.NnDeviceDao;
import com.nnvmso.model.NnDevice;

@Service
public class NnDeviceManager {
	protected static final Logger log = Logger.getLogger(NnDeviceManager.class.getName());
	
	private NnDeviceDao deviceDao = new NnDeviceDao();
		
	public void save(NnDevice device) {
		device.setUpdateDate(new Date());
		deviceDao.save(device);
	}
	public NnDevice create(NnDevice device, long userId) {
		if (device == null)
			device = new NnDevice();
		if (device.getToken() == null)
			device.setToken(NnUserManager.generateToken());
		System.out.println("userId:" + userId);
		
		if (userId != 0) {
			Set<Long> users = new TreeSet<Long>();
			users.add(userId);
			device.setUserIds(users);
		}
		Date now = new Date();
		if (device.getCreateDate() == null)
			device.setCreateDate(now);
		device.setUpdateDate(now);
		deviceDao.save(device);		
		return device;
	}
	
	public NnDevice findByToken(String token) {
		return deviceDao.findByToken(token);
	}
	
	public NnDevice addUser(String token, long userId) {
		NnDevice device = this.findByToken(token);
		if (device == null)
			return null;
		Set<Long> users = device.getUserIds();
		users.add(userId);
		deviceDao.save(device);
		return device;
	}	
	
	public NnDevice removeUser(String token, long userId) {
		NnDevice device = this.findByToken(token);
		if (device == null)
			return null;
		Set<Long> users = device.getUserIds();
		users.remove(userId);
		deviceDao.save(device);
		return device;		
	}
}
