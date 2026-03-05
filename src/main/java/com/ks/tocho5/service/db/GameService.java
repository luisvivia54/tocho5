// src/main/java/com/ks/tocho5/service/db/GameService.java
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

        SeasonModel seasonRef = em.getReference(SeasonModel.class, req.seasonId().intValue());
        g.setSeason(seasonRef);

        g.setCategory_id(req.categoryId());
        g.setHome_team_id(req.homeTeamId());
        g.setAway_team_id(req.awayTeamId());

        g.setStatus("SCHEDULED");
        g.setRoundLabel(req.roundLabel());
        g.setMatch_date_utc(parseToLocalDateTime(req.matchDateUtc()));

        // ✅ NUEVO: cancha (venue) — ya existe en tu DTO y en tu entity
        if (req.venue() != null && !req.venue().trim().isEmpty()) {
            g.setVenue(req.venue().trim());
        }

        g.setUpdated_at(LocalDateTime.now());
        return juegostatus.save(g);
    }

    // =========================
    // ✅ BORRAR SCHEDULED (hard delete)
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
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo borrar por relaciones en BD. Mejor cámbialo a CANCELLED (borrado lógico)."
            );
        }
    }

    // =========================
    // ✅ CANCELAR SCHEDULED (soft delete recomendado)
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
    // ✅ NUEVO: Editar score FINAL + corregir standings
    // =========================
    @Transactional
    public String editFinalScore(Integer gameId, int newHomeScore, int newAwayScore) {
        if (gameId == null) throw new IllegalArgumentException("Falta gameId");
        if (newHomeScore < 0 || newAwayScore < 0) throw new IllegalArgumentException("Scores no pueden ser negativos");

        GameStatusModel statusRow = juegostatus.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado"));

        if (!"FINAL".equalsIgnoreCase(statusRow.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede editar si el partido está FINAL");
        }

        Integer homeTeamId = statusRow.getHome_team_id();
        Integer awayTeamId = statusRow.getAway_team_id();
        Integer categoryId = statusRow.getCategory_id();

        if (homeTeamId == null || awayTeamId == null || categoryId == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Faltan datos (home/away/category) en GameStatusModel");
        }

        Integer seasonId = extractSeasonId(statusRow);
        if (seasonId == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No pude obtener seasonId del partido (GameStatusModel.season)");
        }

        GameModel scoreRow = juegosrepo.findByIdForUpdate(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe score guardado para ese gameId"));

        Integer oldHome = scoreRow.getHome_score();
        Integer oldAway = scoreRow.getAway_score();
        if (oldHome == null || oldAway == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El score viejo está null (inconsistencia)");
        }

        if (oldHome == newHomeScore && oldAway == newAwayScore) {
            return "OK (sin cambios)";
        }

        Contribution oldHomeC = contributionForHome(oldHome, oldAway);
        Contribution oldAwayC = contributionForAway(oldHome, oldAway);

        Contribution newHomeC = contributionForHome(newHomeScore, newAwayScore);
        Contribution newAwayC = contributionForAway(newHomeScore, newAwayScore);

        Contribution dHome = newHomeC.minus(oldHomeC);
        Contribution dAway = newAwayC.minus(oldAwayC);

        applyDeltaScopedStrict(seasonId, categoryId, homeTeamId, dHome);
        applyDeltaScopedStrict(seasonId, categoryId, awayTeamId, dAway);

        scoreRow.setHome_score(newHomeScore);
        scoreRow.setAway_score(newAwayScore);
        juegosrepo.save(scoreRow);

        return "OK";
    }

    // =========================
    // ✅ NUEVO: Eliminar partido y revertir standings
    // - Si está SCHEDULED: hard delete
    // - Si está FINAL: revierte GP/W/D/L/PF/PA/TP y lo marca CANCELLED
    // =========================
    @Transactional
    public String deleteGameAndRevert(Long gameId) {
        if (gameId == null) throw new IllegalArgumentException("Falta gameId");

        Integer id;
        try {
            id = Math.toIntExact(gameId);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("gameId fuera de rango: " + gameId);
        }

        GameStatusModel statusRow = juegostatus.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partido no encontrado"));

        String status = (statusRow.getStatus() == null) ? "" : statusRow.getStatus().toUpperCase();

        if ("SCHEDULED".equals(status)) {
            deleteScheduledGame(gameId);
            return "OK (SCHEDULED eliminado)";
        }

        if (!"FINAL".equals(status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo puedo revertir si está FINAL (o borrar si está SCHEDULED). Status=" + status);
        }

        Integer homeTeamId = statusRow.getHome_team_id();
        Integer awayTeamId = statusRow.getAway_team_id();
        Integer categoryId = statusRow.getCategory_id();
        if (homeTeamId == null || awayTeamId == null || categoryId == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Faltan datos (home/away/category) en GameStatusModel");
        }

        Integer seasonId = extractSeasonId(statusRow);
        if (seasonId == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No pude obtener seasonId del partido");
        }

        GameModel scoreRow = juegosrepo.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe score guardado para ese gameId"));

        Integer oldHome = scoreRow.getHome_score();
        Integer oldAway = scoreRow.getAway_score();
        if (oldHome == null || oldAway == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El score viejo está null (inconsistencia)");
        }

        Contribution homeC = contributionForHome(oldHome, oldAway).negate();
        Contribution awayC = contributionForAway(oldHome, oldAway).negate();

        applyDeltaScopedOrLegacy(seasonId, categoryId, homeTeamId, homeC, -1);
        applyDeltaScopedOrLegacy(seasonId, categoryId, awayTeamId, awayC, -1);

        statusRow.setStatus("CANCELLED");
        statusRow.setUpdated_at(LocalDateTime.now());
        juegostatus.save(statusRow);

        try {
            juegosrepo.delete(scoreRow);
        } catch (Exception ignore) {}

        return "OK (FINAL revertido y cancelado)";
    }

    // =========================
    // helpers
    // =========================
    private LocalDateTime parseToLocalDateTime(String iso) {
        try {
            return OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (DateTimeParseException ignore) {
            return LocalDateTime.parse(iso);
        }
    }

    private Integer extractSeasonId(GameStatusModel statusRow) {
        if (statusRow == null || statusRow.getSeason() == null) return null;

        Object pk = em.getEntityManagerFactory()
                      .getPersistenceUnitUtil()
                      .getIdentifier(statusRow.getSeason());

        if (pk instanceof Number) return ((Number) pk).intValue();
        return null;
    }

    // ---------- helpers internos (standings deltas) ----------
    private static class Contribution {
        int wins;
        int draws;
        int losses;
        int pointsFor;
        int pointsAgainst;
        int tablePoints;

        Contribution(int w, int d, int l, int pf, int pa, int tp) {
            this.wins = w; this.draws = d; this.losses = l;
            this.pointsFor = pf; this.pointsAgainst = pa; this.tablePoints = tp;
        }

        Contribution minus(Contribution other) {
            return new Contribution(
                this.wins - other.wins,
                this.draws - other.draws,
                this.losses - other.losses,
                this.pointsFor - other.pointsFor,
                this.pointsAgainst - other.pointsAgainst,
                this.tablePoints - other.tablePoints
            );
        }

        Contribution negate() {
            return new Contribution(-wins, -draws, -losses, -pointsFor, -pointsAgainst, -tablePoints);
        }
    }

    private Contribution contributionForHome(int homeScore, int awayScore) {
        if (homeScore > awayScore) return new Contribution(1,0,0, homeScore, awayScore, 3);
        if (homeScore == awayScore) return new Contribution(0,1,0, homeScore, awayScore, 1);
        return new Contribution(0,0,1, homeScore, awayScore, 0);
    }

    private Contribution contributionForAway(int homeScore, int awayScore) {
        if (awayScore > homeScore) return new Contribution(1,0,0, awayScore, homeScore, 3);
        if (awayScore == homeScore) return new Contribution(0,1,0, awayScore, homeScore, 1);
        return new Contribution(0,0,1, awayScore, homeScore, 0);
    }

    private void applyDeltaScopedStrict(Integer seasonId, Integer categoryId, Integer teamId, Contribution d) {
        if (d.wins != 0) {
            int rows = standrepo.incWinsScoped(seasonId, categoryId, teamId, d.wins);
            if (rows == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "No existe fila standings (wins) para teamId=" + teamId);
        }
        if (d.losses != 0) {
            int rows = standrepo.incLossesScoped(seasonId, categoryId, teamId, d.losses);
            if (rows == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "No existe fila standings (losses) para teamId=" + teamId);
        }
        if (d.draws != 0) {
            int rows = standrepo.incDrawsScoped(seasonId, categoryId, teamId, d.draws);
            if (rows == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "No existe fila standings (draws) para teamId=" + teamId);
        }
        if (d.pointsFor != 0) {
            int rows = standrepo.incPointsForScoped(seasonId, categoryId, teamId, d.pointsFor);
            if (rows == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "No existe fila standings (pointsFor) para teamId=" + teamId);
        }
        if (d.pointsAgainst != 0) {
            int rows = standrepo.incPointsAgainstScoped(seasonId, categoryId, teamId, d.pointsAgainst);
            if (rows == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "No existe fila standings (pointsAgainst) para teamId=" + teamId);
        }
        if (d.tablePoints != 0) {
            int rows = standrepo.incTablePointsScoped(seasonId, categoryId, teamId, d.tablePoints);
            if (rows == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "No existe fila standings (tablePoints) para teamId=" + teamId);
        }
    }

    private void applyDeltaScopedOrLegacy(Integer seasonId, Integer categoryId, Integer teamId, Contribution d, int gpDelta) {
        if (gpDelta != 0) {
            int rows = standrepo.incGpScoped(seasonId, categoryId, teamId, gpDelta);
            if (rows == 0) standrepo.incrementGp(teamId, gpDelta);
        }

        if (d.wins != 0) {
            int rows = standrepo.incWinsScoped(seasonId, categoryId, teamId, d.wins);
            if (rows == 0) standrepo.incrementWins(teamId, d.wins);
        }
        if (d.losses != 0) {
            int rows = standrepo.incLossesScoped(seasonId, categoryId, teamId, d.losses);
            if (rows == 0) standrepo.incrementLosses(teamId, d.losses);
        }
        if (d.draws != 0) {
            int rows = standrepo.incDrawsScoped(seasonId, categoryId, teamId, d.draws);
            if (rows == 0) standrepo.incrementDraws(teamId, d.draws);
        }
        if (d.pointsFor != 0) {
            int rows = standrepo.incPointsForScoped(seasonId, categoryId, teamId, d.pointsFor);
            if (rows == 0) standrepo.incrementPointsFor(teamId, d.pointsFor);
        }
        if (d.pointsAgainst != 0) {
            int rows = standrepo.incPointsAgainstScoped(seasonId, categoryId, teamId, d.pointsAgainst);
            if (rows == 0) standrepo.incrementPointsAgainst(teamId, d.pointsAgainst);
        }
        if (d.tablePoints != 0) {
            int rows = standrepo.incTablePointsScoped(seasonId, categoryId, teamId, d.tablePoints);
            if (rows == 0) standrepo.incrementTablePoints(teamId, d.tablePoints);
        }
    }
}