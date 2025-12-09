// src/main/java/com/ks/tocho5/service/db/PlayerService.java
package com.ks.tocho5.service.db;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.PlayerModel;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.PlayerRepository;
import com.ks.tocho5.service.db.R2StorageService; // usa el package donde realmente tienes esta clase

@Service
public class PlayerService {

    private final AppUserService appUserService;
    private final EquiposRepository equiposRepository;
    private final PlayerRepository playerRepository;
    private final R2StorageService r2StorageService;

    public PlayerService(AppUserService appUserService,
                         EquiposRepository equiposRepository,
                         PlayerRepository playerRepository,
                         R2StorageService r2StorageService) {
        this.appUserService = appUserService;
        this.equiposRepository = equiposRepository;
        this.playerRepository = playerRepository;
        this.r2StorageService = r2StorageService;
    }

    private void assertCanManageTeam(AppUser user, EquiposModel team) {
        if (!user.isAdmin() &&
            (team.getCaptain() == null ||
             !Objects.equals(team.getCaptain().getId(), user.getId()))) {
            throw new RuntimeException("No puedes administrar este equipo");
        }
    }

    // Listar jugadores de un equipo
    @Transactional(readOnly = true)
    public List<PlayerModel> getPlayersByTeam(Long teamId) {
        return playerRepository.findByTeam_TeamId(teamId);
    }

    // Crear jugador
    @Transactional
    public PlayerModel createPlayerForTeam(
            Jwt jwt,
            Long teamId,                     // 👈 LONG
            String fullName,
            String curp,
            Integer jerseyNumber,
            LocalDate birthdate,
            MultipartFile photo
    ) throws IOException {

        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("La foto del jugador es obligatoria");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        if (curp == null || curp.isBlank()) {
            throw new IllegalArgumentException("La CURP es obligatoria");
        }

        AppUser user = appUserService.syncFromJwt(jwt);

        // 👇 ahora pasamos Long, coincide con CrudRepository<EquiposModel, Long>
        EquiposModel team = equiposRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        // Validar que sea capitán de ese equipo o admin
        assertCanManageTeam(user, team);

        // Subir foto a R2
        String photoUrl = r2StorageService.uploadPlayerPhoto(teamId, photo);

        // Crear jugador
        PlayerModel player = new PlayerModel();
        player.setTeam(team);
        player.setFullName(fullName);
        player.setCurp(curp);
        player.setJerseyNumber(jerseyNumber);
        player.setBirthdate(birthdate);
        player.setPhotoUrl(photoUrl);

        return playerRepository.save(player);
    }

    // Actualizar jugador
    @Transactional
    public PlayerModel updatePlayer(
            Jwt jwt,
            Long teamId,                     // 👈 también LONG
            Long playerId,
            String fullName,
            String curp,
            Integer jerseyNumber,
            LocalDate birthdate,
            MultipartFile newPhoto // puede venir null
    ) throws IOException {

        AppUser user = appUserService.syncFromJwt(jwt);

        EquiposModel team = equiposRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        assertCanManageTeam(user, team);

        PlayerModel player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        // Normalizamos el tipo del ID del equipo del jugador a Long,
        // aunque internamente sea Integer o Long.
        Long playerTeamId = player.getTeam().getTeamId() == null
                ? null
                : Long.valueOf(player.getTeam().getTeamId());

        if (playerTeamId == null || !playerTeamId.equals(teamId)) {
            throw new RuntimeException("El jugador no pertenece a este equipo");
        }

        if (fullName != null && !fullName.isBlank()) {
            player.setFullName(fullName);
        }
        if (curp != null && !curp.isBlank()) {
            player.setCurp(curp);
        }
        if (jerseyNumber != null) {
            player.setJerseyNumber(jerseyNumber);
        }
        if (birthdate != null) {
            player.setBirthdate(birthdate);
        }

        if (newPhoto != null && !newPhoto.isEmpty()) {
            String newPhotoUrl = r2StorageService.uploadPlayerPhoto(teamId, newPhoto);
            player.setPhotoUrl(newPhotoUrl);
        }

        return playerRepository.save(player);
    }
}
