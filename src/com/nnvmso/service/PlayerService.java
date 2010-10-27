package com.nnvmso.service;

import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.springframework.stereotype.Service;

import com.nnvmso.lib.DebugLib;
import com.nnvmso.lib.PMF;
import com.nnvmso.model.Mso;
import com.nnvmso.model.Player;

@Service
public class PlayerService {
	// ============================================================
	// find
	// ============================================================		
	public Player findByMsoKey(String msoKey) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Mso mso = pm.getObjectById(Mso.class, msoKey);
		Player player = mso.getPlayer();
		player.getMso(); //touch
		pm.close();		
		return player;
	}
	
	// ============================================================
	// c.u.d
	// ============================================================		
	public void create(String msoKey, Player player) {
		this.save(msoKey, player);
	}
	
	//save based on mso key string
	public void save(String msoKey, Player player) {		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			Mso mso = pm.getObjectById(Mso.class, msoKey);					
			Player old = mso.getPlayer();
			if (old != null) {				
				old.setLogoUrl(player.getLogoUrl());
				old.setCode(player.getCode());
				old.setUpdateDate(new Date());
				System.out.println("old:" + mso.getKey());
				System.out.println("old:" + old.getMso().getKey());				
			} else {
				mso.setPlayer(player);
				player.setMso(mso);
			}
			pm.makePersistent(mso);
			tx.commit();
		} finally {
			if (tx.isActive()) { tx.rollback(); }
		}
		pm.close();
	}
}
