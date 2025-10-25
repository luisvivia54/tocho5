package com.ks.tocho5.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.repository.JuegosRepository;

@Service
public class GameService {
	
	@Autowired
	private JuegosRepository juegosrepo;
	
	public String saveGame(GameModel gamemodel) {
		try {
			juegosrepo.save(gamemodel);
		}catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return "OK";
	}
}