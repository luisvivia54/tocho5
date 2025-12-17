package com.ks.tocho5.service.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ks.tocho5.model.GameScoreUpdateRequest;
import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.model.GameStatusModel;
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

    // =========================
    // 1) Endpoint viejo: 1 partido con GameModel
    // =========================
    @Transactional
    public String saveGame(GameModel input) {
        try {
            if (input == null) throw new IllegalArgumentException("Body vacío");
            saveGameScore(input.getGame_id(), input.getHome_score(), input.getAway_score());
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // =========================
    // 2) Endpoint nuevo: batch con DTO
    // =========================
    @Transactional
    public BatchResult saveGames(List<GameScoreUpdateRequest> batch) {
        BatchResult result = new BatchResult();

        if (batch == null || batch.isEmpty()) {
            result.errors.put(null, "Batch vacío");
            return result;
        }

        for (GameScoreUpdateRequest item : batch) {
            Integer gid = (item != null) ? item.getGame_id() : null;
            try {
                if (item == null) throw new IllegalArgumentException("Item null en batch");
                saveGameScore(item.getGame_id(), item.getHome_score(), item.getAway_score());
                result.ok.add(gid);
            } catch (Exception e) {
                e.printStackTrace();
                result.errors.put(gid, e.getMessage());
            }
        }

        return result;
    }

    // =========================
    // Lógica segura: NO pisa nulls (carga de BD y solo actualiza scores)
    // Evita duplicar standings si ya estaba FINAL
    // =========================
    @Transactional
    public void saveGameScore(Integer gameId, Integer homeScore, Integer awayScore) {
        if (gameId == null) throw new IllegalArgumentException("game_id es requerido");

        if (homeScore != null && homeScore < 0) throw new IllegalArgumentException("home_score no puede ser negativo");
        if (awayScore != null && awayScore < 0) throw new IllegalArgumentException("away_score no puede ser negativo");

        // 1) Traer juego real
        GameModel game = juegosrepo.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("No existe game_id " + gameId));

        // 2) Actualizar SOLO marcador
        game.setHome_score(homeScore);
        game.setAway_score(awayScore);
        juegosrepo.save(game);

        // 3) Si no hay marcador completo, NO finaliza ni toca standings
        if (homeScore == null || awayScore == null) return;

        // 4) Evitar duplicar standings si ya era FINAL
        String currentStatus = juegostatus.findById(gameId)
                .map(GameStatusModel::getStatus)
                .orElse(null);

        if (currentStatus != null && "FINAL".equalsIgnoreCase(currentStatus)) {
            return; // ya estaba final, no sumes otra vez
        }

        // 5) Marcar FINAL
        juegostatus.finalById(gameId);

        // 6) Equipos
        Integer homeTeamId = juegostatus.findHomeTeamId(gameId);
        Integer awayTeamId = juegostatus.findAwayTeamId(gameId);

        if (homeTeamId == null || awayTeamId == null) {
            throw new IllegalStateException("No se pudo resolver homeTeamId/awayTeamId para game_id " + gameId);
        }

        // 7) GP
        standrepo.addOneToGp(homeTeamId);
        standrepo.addOneToGp(awayTeamId);

        // 8) PF / PA
        standrepo.addPointsFor(homeTeamId, homeScore);
        standrepo.addPointsFor(awayTeamId, awayScore);
        standrepo.addPointsAgainst(homeTeamId, awayScore);
        standrepo.addPointsAgainst(awayTeamId, homeScore);

        // 9) W/L/D + table points
        if (homeScore > awayScore) {
            standrepo.addOneToWins(homeTeamId);
            standrepo.addOneToLosses(awayTeamId);
            standrepo.addTablePoints(homeTeamId, 3);
        } else if (homeScore.equals(awayScore)) {
            standrepo.addOneToDraws(homeTeamId);
            standrepo.addOneToDraws(awayTeamId);
            standrepo.addTablePoints(homeTeamId, 1);
            standrepo.addTablePoints(awayTeamId, 1);
        } else {
            standrepo.addOneToWins(awayTeamId);
            standrepo.addOneToLosses(homeTeamId);
            standrepo.addTablePoints(awayTeamId, 3);
        }
    }

    // Respuesta batch
    public static class BatchResult {
        public List<Integer> ok = new ArrayList<>();
        public Map<Integer, String> errors = new LinkedHashMap<>();
    }
}
