// src/main/java/com/ks/tocho5/service/TeamService.java
package com.ks.tocho5.service.db;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.service.db.AppUserService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    private final AppUserService appUserService;
    private final EquiposRepository teamRepository;

    public TeamService(AppUserService appUserService,
    		EquiposRepository teamRepository) {
        this.appUserService = appUserService;
        this.teamRepository = teamRepository;
    }

    /**
     * A partir del JWT:
     * - sincroniza/crea el AppUser en BD
     * - devuelve el AppUser actual
     */
    private AppUser getCurrentUser(Jwt jwt) {
        return appUserService.syncFromJwt(jwt);
    }

    @Transactional
    public EquiposModel createTeamForCurrentUser(Jwt jwt, String teamName) {
        AppUser user = getCurrentUser(jwt);

        if (!user.hasCaptainPrivileges()) {
            throw new RuntimeException("Solo capitanes o admins pueden crear equipos");
        }

        int currentTeams = teamRepository.countByCaptain(user);

        if (currentTeams >= user.getMaxTeamsAllowed()) {
            throw new RuntimeException("Ya alcanzaste tu límite de equipos (" + user.getMaxTeamsAllowed() + ")");
        }

        EquiposModel team = new EquiposModel();
        team.setName(teamName);
        team.setCaptain(user);  // esto llena captain_id en la tabla team

        return teamRepository.save(team);
    }

    @Transactional
    public EquiposModel updateTeamName(Jwt jwt, Long teamId, String newName) {
        AppUser user = getCurrentUser(jwt);

        EquiposModel team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // Solo capitán del equipo o admin puede editarlo
        if (!user.isAdmin() && (team.getCaptain() == null ||
                !team.getCaptain().getId().equals(user.getId()))) {
            throw new RuntimeException("No puedes editar un equipo que no es tuyo");
        }

        team.setName(newName);
        return teamRepository.save(team);
    }
}
