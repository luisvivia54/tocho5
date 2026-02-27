package com.ks.tocho5.service.db;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.model.GameStatusModel;
import com.ks.tocho5.model.SeasonModel;
import com.ks.tocho5.model.dto.GameCreateRequest;
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

    @PersistenceContext
    private EntityManager em;

    // =========================
    // ✅ TU MÉTODO ORIGINAL (igual)
    // =========================
    @Transactional
    public String saveGame(GameModel gamemodel) {
        try {
            juegosrepo.save(gamemodel);

            // cambia a FINAL en la tabla/vista donde vive el status
            juegostatus.finalById(gamemodel.getGame_id());

            // suma GP
            standrepo.addOneToGp(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
            standrepo.addOneToGp(juegostatus.findAwayTeamId(gamemodel.getGame_id()));

            // gana local
            if (gamemodel.getHome_score() > gamemodel.getAway_score()) {
                standrepo.addOneToWins(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
                standrepo.addOneToLosses(juegostatus.findAwayTeamId(gamemodel.getGame_id()));

                standrepo.addPointsFor(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
                standrepo.addPointsFor(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());

                standrepo.addPointsAgainst(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
                standrepo.addPointsAgainst(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());

                standrepo.addTablePoints(juegostatus.findHomeTeamId(gamemodel.getGame_id()), 3);

            // empate
            } else if (gamemodel.getHome_score() == gamemodel.getAway_score()) {
                standrepo.addOneToDraws(juegostatus.findHomeTeamId(gamemodel.getGame_id()));
                standrepo.addOneToDraws(juegostatus.findAwayTeamId(gamemodel.getGame_id()));

                standrepo.addPointsFor(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());
                standrepo.addPointsFor(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());

                standrepo.addPointsAgainst(juegostatus.findHomeTeamId(gamemodel.getGame_id()), gamemodel.getAway_score());
                standrepo.addPointsAgainst(juegostatus.findAwayTeamId(gamemodel.getGame_id()), gamemodel.getHome_score());

                standrepo.addTablePoints(juegostatus.findHomeTeamId(gamemodel.getGame_id()), 1);
                standrepo.addTablePoints(juegostatus.findAwayTeamId(gamemodel.getGame_id()), 1);

            // gana visita
            } else {
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

    // =========================
    // ✅ NUEVO: BATCH (varios juegos)
    // =========================
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

    public static class BatchResult {
        public List<Integer> ok = new ArrayList<>();
        public Map<Integer, String> errors = new LinkedHashMap<>();
    }

    // =========================
    // ✅ CREAR SCHEDULED
    // =========================
    @Transactional
    public GameStatusModel createScheduledGame(GameCreateRequest req) {
        if (req == null) throw new IllegalArgumentException("Body vacío");

        Integer incomingId = req.gameId();
        if (incomingId != null && incomingId > 0) {
            throw new IllegalArgumentException("Para crear NO mandes game_id (debe venir null/0)");
        }

        if (req.seasonId() == null) throw new IllegalArgumentException("Falta seasonId/season_id");
        if (req.categoryId() == null || req.categoryId() < 1) throw new IllegalArgumentException("Falta categoryId/category_id");
        if (req.homeTeamId() == null) throw new IllegalArgumentException("Falta homeTeamId/home_team_id");
        if (req.awayTeamId() == null) throw new IllegalArgumentException("Falta awayTeamId/away_team_id");
        if (req.homeTeamId().equals(req.awayTeamId())) throw new IllegalArgumentException("Local y visitante no pueden ser el mismo equipo");
        if (req.matchDateUtc() == null || req.matchDateUtc().isBlank()) throw new IllegalArgumentException("Falta matchDateUtc/match_date_utc");

        GameStatusModel g = new GameStatusModel();

        SeasonModel seasonRef = em.getReference(SeasonModel.class, req.seasonId());
        g.setSeason(seasonRef);

        g.setCategory_id(req.categoryId());
        g.setHome_team_id(req.homeTeamId());
        g.setAway_team_id(req.awayTeamId());

        g.setStatus("SCHEDULED");
        g.setRoundLabel(req.roundLabel());
        g.setMatch_date_utc(parseToLocalDateTime(req.matchDateUtc()));
        g.setUpdated_at(LocalDateTime.now());

        return juegostatus.save(g);
    }

    // =========================
    // ✅ NUEVO: BORRAR SCHEDULED (hard delete)
    // =========================
    @Transactional
    public void deleteScheduledGame(Long gameId) {
        if (gameId == null) throw new IllegalArgumentException("Falta gameId");

        Integer id;
        try {
            id = Math.toIntExact(gameId);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("gameId fuera de rango: " + gameId);
        }

        GameStatusModel game = juegostatus.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado"));

        String status = game.getStatus();
        if (status == null || !status.equalsIgnoreCase("SCHEDULED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede borrar si está SCHEDULED");
        }

        try {
            juegostatus.delete(game);
        } catch (DataIntegrityViolationException fk) {
            // por si existen FKs (stats, eventos, etc.)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo borrar por relaciones en BD. Mejor cámbialo a CANCELLED (borrado lógico)."
            );
        }
    }

    // =========================
    // ✅ NUEVO: CANCELAR SCHEDULED (soft delete recomendado)
    // =========================
    @Transactional
    public void cancelScheduledGame(Long gameId) {
        if (gameId == null) throw new IllegalArgumentException("Falta gameId");

        Integer id;
        try {
            id = Math.toIntExact(gameId);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("gameId fuera de rango: " + gameId);
        }

        GameStatusModel game = juegostatus.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado"));

        if (!"SCHEDULED".equalsIgnoreCase(game.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede cancelar si está SCHEDULED");
        }

        game.setStatus("CANCELLED");
        game.setUpdated_at(LocalDateTime.now());
        juegostatus.save(game);
    }

    // =========================
    // helpers
    // =========================
    private LocalDateTime parseToLocalDateTime(String iso) {
        // acepta 2026-01-24T18:00:00.000Z
        try {
            return OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (DateTimeParseException ignore) {
            // acepta 2026-01-24T18:00:00
            return LocalDateTime.parse(iso);
        }
    }
}