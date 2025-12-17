package com.ks.tocho5.service.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // ✅ TU MÉTODO ORIGINAL (igual)
    public String saveGame(GameModel gamemodel) {
        try {
            juegosrepo.save(gamemodel);
            juegostatus.finalById(gamemodel.getGame_id());
            standrepo.addOneToGp(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
            standrepo.addOneToGp(juegostatus.findAwayTeamId(gamemodel.getGame_id()));

            if (gamemodel.getHome_score() > gamemodel.getAway_score()) {
                standrepo.addOneToWins(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
                standrepo.addOneToLosses(juegostatus.findAwayTeamId(gamemodel.getGame_id()));
                standrepo.addPointsFor(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
                standrepo.addPointsFor(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
                standrepo.addPointsAgainst(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
                standrepo.addPointsAgainst(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
                standrepo.addTablePoints(juegostatus.findHomeTeamId(gamemodel.getGame_id()), 3);

            } else if (gamemodel.getHome_score() == gamemodel.getAway_score()) {
                standrepo.addOneToDraws(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
                standrepo.addOneToDraws(juegostatus.findAwayTeamId(gamemodel.getGame_id()));
                standrepo.addPointsFor(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
                standrepo.addPointsFor(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
                standrepo.addPointsAgainst(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
                standrepo.addPointsAgainst(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
                standrepo.addTablePoints(juegostatus.findHomeTeamId(gamemodel.getGame_id()), 1);
                standrepo.addTablePoints(juegostatus.findAwayTeamId(gamemodel.getGame_id()), 1);

            } else if (gamemodel.getHome_score() < gamemodel.getAway_score()) {
                standrepo.addOneToWins(juegostatus.findAwayTeamId(gamemodel.getGame_id()));
                standrepo.addOneToLosses(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
                standrepo.addPointsFor(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
                standrepo.addPointsFor(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
                standrepo.addPointsAgainst(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
                standrepo.addPointsAgainst(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
                standrepo.addTablePoints(juegostatus.findAwayTeamId(gamemodel.getGame_id()), 3);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return "OK";
    }

    // ✅ NUEVO: BATCH (varios juegos)
    public BatchResult saveGames(List<GameModel> games) {
        BatchResult result = new BatchResult();

        if (games == null || games.isEmpty()) {
            result.errors.put(null, "Batch vacío");
            return result;
        }

        for (GameModel g : games) {
            Integer id = (g != null) ? g.getGame_id() : null;
            try {
                if (g == null) throw new IllegalArgumentException("Item null en batch");
                String resp = saveGame(g);
                if (!"OK".equals(resp)) {
                    result.errors.put(id, resp);
                } else {
                    result.ok.add(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.errors.put(id, e.getMessage());
            }
        }

        return result;
    }

    // Respuesta para batch
    public static class BatchResult {
        public List<Integer> ok = new ArrayList<>();
        public Map<Integer, String> errors = new LinkedHashMap<>();
    }
}
