package com.ks.tocho5.service.db;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.PlayerSeasonStatsProjection;
import com.ks.tocho5.model.StatPlayerGameModel;
import com.ks.tocho5.model.PlayerGameStatsUpsertDTO;
import com.ks.tocho5.model.PlayerSeasonStatsDTO;
import com.ks.tocho5.repository.StatPlayerGameRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerStatsService {

    private final StatPlayerGameRepository spgRepo;
    private final SeasonService seasonService;
    private final AppUserService userService;

    public PlayerStatsService(
            StatPlayerGameRepository spgRepo,
            SeasonService seasonService,
            AppUserService userService
    ) {
        this.spgRepo = spgRepo;
        this.seasonService = seasonService;
        this.userService = userService;
    }

    // ✅ PUT batch: upsert por juego
    @Transactional
    public void upsertStatsForGame(Jwt jwt, Long gameId, List<PlayerGameStatsUpsertDTO> items) {
        AppUser user = userService.syncFromJwt(jwt);
        if (!user.isAdmin()) {
            throw new RuntimeException("Solo admin puede subir stats");
        }

        for (PlayerGameStatsUpsertDTO r : items) {
            StatPlayerGameModel row = spgRepo
                    .findByGameIdAndTeamIdAndPlayerId(gameId, r.teamId(), r.playerId())
                    .orElseGet(() -> {
                        StatPlayerGameModel x = new StatPlayerGameModel();
                        x.setGameId(gameId);
                        x.setTeamId(r.teamId());
                        x.setPlayerId(r.playerId());
                        return x;
                    });

            // SOLO tocamos las 4 stats
            row.setTd(nz(r.td()));
            row.setPassTd(nz(r.passTd()));
            row.setIntercep(nz(r.intercep()));
            row.setSacks(nz(r.sacks()));

            spgRepo.save(row);
        }
    }

    // ✅ GET: leaderboard por season (si no mandas seasonId => season activa)
    @Transactional(readOnly = true)
    public List<PlayerSeasonStatsDTO> getLeaderboard(Long leagueId, Long seasonIdOrNull) {
        Long seasonId = (seasonIdOrNull != null)
                ? seasonIdOrNull
                : seasonService.getCurrentSeasonId(leagueId);

        List<PlayerSeasonStatsProjection> rows = spgRepo.leaderboardBySeason(seasonId);

        return rows.stream()
                .map(r -> new PlayerSeasonStatsDTO(
                        r.getPersonKey(),
                        r.getPlayerId(),
                        r.getTeamId(),
                        r.getFullName(),
                        nz(r.getTd()),
                        nz(r.getPassTd()),
                        nz(r.getIntercep()),
                        nz(r.getSacks())
                ))
                .toList();
    }

    private int nz(Integer v) { return v == null ? 0 : v; }
}
