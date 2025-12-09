// src/main/java/com/ks/tocho5/service/db/TeamDetailService.java
package com.ks.tocho5.service.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ks.tocho5.model.TeamDetailDTOs.*;
import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.GameStatusModel;
import com.ks.tocho5.model.PlayerModel;
import com.ks.tocho5.model.TeamPhotoModel;
import com.ks.tocho5.model.TeamEnrollmentInfoProjection;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.EquiposFiltroRepository;
import com.ks.tocho5.repository.JuegoStatus;
import com.ks.tocho5.repository.PlayerRepository;
import com.ks.tocho5.repository.TeamPhotoRepository;

@Service
public class TeamDetailService {

    private final EquiposRepository equiposRepository;
    private final PlayerRepository playerRepository;
    private final TeamPhotoRepository teamPhotoRepository;
    private final JuegoStatus juegoStatus;
    private final EquiposFiltroRepository equiposFiltroRepository;

    public TeamDetailService(
            EquiposRepository equiposRepository,
            PlayerRepository playerRepository,
            TeamPhotoRepository teamPhotoRepository,
            JuegoStatus juegoStatus,
            EquiposFiltroRepository equiposFiltroRepository
    ) {
        this.equiposRepository = equiposRepository;
        this.playerRepository = playerRepository;
        this.teamPhotoRepository = teamPhotoRepository;
        this.juegoStatus = juegoStatus;
        this.equiposFiltroRepository = equiposFiltroRepository;
    }

    @Transactional(readOnly = true)
    public TeamDetailDTO getTeamDetail(Long teamId) {

        // 1) Equipo
        EquiposModel team = equiposRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        TeamBasicDTO teamDto = toTeamBasicDTO(team);

        // 2) Jugadores del equipo
        List<PlayerPublicDTO> players = playerRepository
                .findByTeam_TeamId(teamId)
                .stream()
                .map(this::toPlayerDTO)
                .toList();

        // 3) Galería (fotos)
        List<TeamPhotoDTO> gallery = teamPhotoRepository
                .findByTeam_TeamIdOrderBySortOrderAsc(teamId)
                .stream()
                .map(this::toPhotoDTO)
                .toList();

        // 4) Últimos 3 partidos
        List<GameSummaryDTO> lastGames = juegoStatus
                .findLastGamesForTeam(teamId.intValue(), PageRequest.of(0, 3))
                .stream()
                .map(this::toGameSummaryDTO)
                .toList();

        return new TeamDetailDTO(
                teamDto,
                players,
                lastGames,
                gallery
        );
    }

    // ============ MAPEOS ============

    private TeamBasicDTO toTeamBasicDTO(EquiposModel team) {

        // Buscar la última inscripción (season/category) del equipo
        Optional<TeamEnrollmentInfoProjection> enrollmentOpt =
                equiposFiltroRepository.findLatestEnrollmentForTeam(team.getTeamId().intValue());

        SeasonInfoDTO seasonInfo = null;
        CategoryInfoDTO categoryInfo = null;

        if (enrollmentOpt.isPresent()) {
            TeamEnrollmentInfoProjection enr = enrollmentOpt.get();
            seasonInfo = new SeasonInfoDTO(
                    enr.getSeasonId(),
                    enr.getSeasonName()
            );
            categoryInfo = new CategoryInfoDTO(
                    enr.getCategoryId(),
                    enr.getCategoryName()
            );
        }

        AppUser captain = team.getCaptain();

        TeamCaptainDTO captainDto = null;
        if (captain != null) {
            captainDto = new TeamCaptainDTO(
                    captain.getId(),
                    captain.getFullName()
            );
        }

        return new TeamBasicDTO(
                team.getTeamId(),
                seasonInfo,
                categoryInfo,
                team.getName(),
                team.getShortName(),
                team.getLogoUrl(),
                team.getColorPrimary(),
                team.getColorSecondary(),
                captainDto
        );
    }

    private PlayerPublicDTO toPlayerDTO(PlayerModel p) {
        return new PlayerPublicDTO(
                p.getPlayerId(),       // ajusta si tu id tiene otro nombre
                p.getFullName(),
                p.getCurp(),
                p.getJerseyNumber(),
                p.getBirthdate(),
                p.getPhotoUrl()
        );
    }

    private TeamPhotoDTO toPhotoDTO(TeamPhotoModel photo) {
        return new TeamPhotoDTO(
                photo.getId(),
                photo.getPhotoUrl(),
                photo.getSortOrder()
        );
    }

    private GameSummaryDTO toGameSummaryDTO(GameStatusModel g) {
        return new GameSummaryDTO(
                g.getGame_id(),
                g.getMatch_date_utc(),
                g.getStatus(),
                g.getHome_team_id(),
                g.getHomeTeamName(),
                g.getAway_team_id(),
                g.getAwayTeamName()
        );
    }
}
