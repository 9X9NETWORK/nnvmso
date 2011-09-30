package com.nnvmso.service;

import java.util.Date;

import com.nnvmso.dao.NnGuestDao;
import com.nnvmso.model.NnGuest;

public class NnGuestManager {

	private NnGuestDao guestDao = new NnGuestDao();

	public void create(NnGuest guest) {
		guest.setCreateDate(new Date());
		guestDao.save(guest);
	}
	
	public void save(NnGuest guest) {
		guestDao.save(guest);
	}

	public void delete(NnGuest guest) {
		guestDao.delete(guest);
	}
	
	public NnGuest findByToken(String token) {
		return guestDao.findByToken(token);
	}
}
