package com.ks.tocho5.service.db;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ks.tocho5.model.AppUser;
import com.ks.tocho5.model.EquiposModel;
import com.ks.tocho5.model.TeamPhotoModel;
import com.ks.tocho5.repository.EquiposRepository;
import com.ks.tocho5.repository.TeamPhotoRepository;

@Service
public class TeamCarouselService {

    private final AppUserService appUserService;
    private final EquiposRepository equiposRepository;
    private final TeamPhotoRepository carouselRepository;
    private final R2StorageService r2StorageService;

    public TeamCarouselService(
            AppUserService appUserService,
            EquiposRepository equiposRepository,
            TeamPhotoRepository carouselRepository,
            R2StorageService r2StorageService
    ) {
        this.appUserService = appUserService;
        this.equiposRepository = equiposRepository;
        this.carouselRepository = carouselRepository;
        this.r2StorageService = r2StorageService;
    }

    private void assertCanManageTeam(AppUser user, EquiposModel team) {
        if (!user.isAdmin() &&
            (team.getCaptain() == null ||
             !Objects.equals(team.getCaptain().getId(), user.getId()))) {
            throw new RuntimeException("No puedes administrar este equipo");
        }
    }

    // Listar fotos del carrusel
    @Transactional(readOnly = true)
    public List<TeamPhotoModel> listPhotos(Long teamId) {
        return carouselRepository.findByTeam_TeamIdOrderBySortOrderAsc(teamId);
    }

    // Agregar nueva foto (respeta el máximo de 5)
    @Transactional
    public TeamPhotoModel addPhoto(
            Jwt jwt,
            Long teamId,
            MultipartFile file,
            Integer positionIndex
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La foto es obligatoria");
        }

        AppUser user = appUserService.syncFromJwt(jwt);
        EquiposModel team = equiposRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        assertCanManageTeam(user, team);

        long count = carouselRepository.countByTeam_TeamId(teamId);
        if (count >= 5) {
            throw new RuntimeException("Solo se permiten hasta 5 fotos en el carrusel");
        }

        // Subir al R2
        String url = r2StorageService.uploadTeamCarouselPhoto(teamId, file);

        TeamPhotoModel photo = new TeamPhotoModel();
        photo.setTeam(team);

        // Si no mandas positionIndex, usamos siguiente slot disponible 0..4
        Integer finalSlot = positionIndex != null ? positionIndex : (int) count;
        photo.setSortOrder(finalSlot);

        photo.setPhotoUrl(url);

        return carouselRepository.save(photo);
    }

    // Reemplazar UNA foto (borra el archivo viejo en R2)
    @Transactional
    public TeamPhotoModel updatePhoto(
            Jwt jwt,
            Long teamId,
            Long photoId,
            MultipartFile file,
            Integer positionIndex
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La nueva foto es obligatoria");
        }

        AppUser user = appUserService.syncFromJwt(jwt);
        EquiposModel team = equiposRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        assertCanManageTeam(user, team);

        TeamPhotoModel photo = carouselRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada"));

        Long photoTeamId = photo.getTeam().getTeamId() == null
                ? null
                : Long.valueOf(photo.getTeam().getTeamId());

        if (photoTeamId == null || !photoTeamId.equals(teamId)) {
            throw new RuntimeException("La foto no pertenece a este equipo");
        }

        // Borrar archivo viejo en R2
        r2StorageService.deleteByUrl(photo.getPhotoUrl());

        // Subir nuevo archivo
        String newUrl = r2StorageService.uploadTeamCarouselPhoto(teamId, file);
        photo.setPhotoUrl(newUrl);

        if (positionIndex != null) {
            photo.setSortOrder(positionIndex);
        }

        return carouselRepository.save(photo);
    }

    // Eliminar UNA foto (y su archivo en R2)
    @Transactional
    public void deletePhoto(
            Jwt jwt,
            Long teamId,
            Long photoId
    ) throws IOException {

        AppUser user = appUserService.syncFromJwt(jwt);
        EquiposModel team = equiposRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        assertCanManageTeam(user, team);

        TeamPhotoModel photo = carouselRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada"));

        Long photoTeamId = photo.getTeam().getTeamId() == null
                ? null
                : Long.valueOf(photo.getTeam().getTeamId());

        if (photoTeamId == null || !photoTeamId.equals(teamId)) {
            throw new RuntimeException("La foto no pertenece a este equipo");
        }

        // Borrar archivo en R2
        r2StorageService.deleteByUrl(photo.getPhotoUrl());

        // Borrar registro en BD
        carouselRepository.delete(photo);
    }
}
