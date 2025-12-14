package com.ks.tocho5.service.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.repository.JuegoStatus;
import com.ks.tocho5.repository.JuegosRepository;
import com.ks.tocho5.repository.StandingTeamRepository;

@Service
public class GameService {
	
	@Autowired
	private StandingTeamRepository standrepo;
	
	@Autowired
	private JuegosRepository juegosrepo;
	
	@Autowired
	private JuegoStatus juegostatus;
	
	public String saveGame(GameModel gamemodel) {
		try {
			juegosrepo.save(gamemodel);
			juegostatus.finalById(gamemodel.getGame_id());
			standrepo.addOneToGp(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
			standrepo.addOneToGp(juegostatus.findAwayTeamId(gamemodel.getGame_id()));
			
			if (gamemodel.getHome_score() > gamemodel.getAway_score()){
				standrepo.addOneToWins(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
				standrepo.addOneToLosses(juegostatus.findAwayTeamId(gamemodel.getGame_id()));
				standrepo.addPointsFor(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
				standrepo.addPointsFor(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
				standrepo.addPointsAgainst(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
				standrepo.addPointsAgainst(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
				standrepo.addTablePoints(juegostatus.findHomeTeamId(gamemodel.getGame_id()), 3);
			}else if(gamemodel.getHome_score() == gamemodel.getAway_score()) {
				standrepo.addOneToDraws(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
				standrepo.addOneToDraws(juegostatus.findAwayTeamId(gamemodel.getGame_id()));
				standrepo.addPointsFor(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
				standrepo.addPointsFor(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
				standrepo.addPointsAgainst(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
				standrepo.addPointsAgainst(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
				standrepo.addTablePoints(juegostatus.findHomeTeamId(gamemodel.getGame_id()), 1);
				standrepo.addTablePoints(juegostatus.findAwayTeamId(gamemodel.getGame_id()), 1);
			}else if(gamemodel.getHome_score() < gamemodel.getAway_score()) {
				standrepo.addOneToWins(juegostatus.findAwayTeamId(gamemodel.getGame_id()));
				standrepo.addOneToLosses(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
				standrepo.addPointsFor(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
				standrepo.addPointsFor(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
				standrepo.addPointsAgainst(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
				standrepo.addPointsAgainst(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
				standrepo.addTablePoints(juegostatus.findAwayTeamId(gamemodel.getGame_id()), 3);
			}
		}catch(Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return "OK";
	}
	
}

//me quede en con el game id consigue el id de los dos equipos participantes y aumento sus partidos jugados,
//falta que aumente win losse draws, points_for, points_againts y los puntos totales por equipo y hay 
//que hacer algo para cuando se cancelen