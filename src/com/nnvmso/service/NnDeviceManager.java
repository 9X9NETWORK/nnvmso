package com.nnvmso.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nnvmso.dao.NnDeviceDao;
import com.nnvmso.model.NnDevice;
import com.nnvmso.model.NnUser;

@Service
public class NnDeviceManager {
	protected static final Logger log = Logger.getLogger(NnDeviceManager.class.getName());
	
	private NnDeviceDao deviceDao = new NnDeviceDao();
		
	public void save(NnDevice device) {
		device.setUpdateDate(new Date());
		deviceDao.save(device);
	}
	
	public NnDevice create(NnDevice device, NnUser user, String type) {
		if (device != null && user != null) {
			NnDevice existed = this.findByTokenAndUser(device.getToken(), user); 
			if (existed != null)
				return existed;
		}
		
		if (device == null)
			device = new NnDevice();
		if (device.getToken() == null)
			device.setToken(NnUserManager.generateToken());		
		if (user != null)
			device.setUserId(user.getKey().getId());
		device.setType(type);
		Date now = new Date();
		if (device.getCreateDate() == null)
			device.setCreateDate(now);
		device.setUpdateDate(now);
		deviceDao.save(device);		
		return device;
	}
	
	public NnDevice findByTokenAndUser(String token, NnUser user) {
		return deviceDao.findByTokenAndUser(token, user.getKey().getId());
	}

	public NnDevice findDeviceOpenToken(String token) {
		return deviceDao.findDeviceOpenToken(token);
	}
	
	public List<NnDevice> findByToken(String token) {
		return deviceDao.findByToken(token);
	}

	public List<NnDevice> findByUser(NnUser user) {
		return deviceDao.findByUser(user.getKey().getId());
	}
	
	public NnDevice addUser(String token, NnUser user) {
		List<NnDevice> devices = this.findByToken(token);
		if (devices.size() == 0)
			return null;
		NnDevice existed = this.findDeviceOpenToken(token);
		if (existed != null) {
			System.out.println("enter 2");
			existed.setUserId(user.getKey().getId());
			deviceDao.save(existed);
			return existed;
		}
		existed = this.findByTokenAndUser(token, user);
		if (existed != null) {
			System.out.println("enter 1");
			return existed;
		}		
		System.out.println("new add user:" + user.getKey().getId());
		NnDevice device = new NnDevice();
		device.setToken(devices.get(0).getToken());
		this.create(device, user, null);
		deviceDao.save(device);
		return device;
	}	
	
	public void delete(NnDevice device) {
		deviceDao.delete(device);
	}
	
	public boolean removeUser(String token, NnUser user) {
		List<NnDevice> devices = this.findByToken(token);
		if (devices.size() == 0)
			return false;
		NnDevice existed = this.findByTokenAndUser(token, user);
		if (existed == null)
			return false;
		this.delete(existed);
		return true;
	}
}
