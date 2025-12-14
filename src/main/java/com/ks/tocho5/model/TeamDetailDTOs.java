// src/main/java/com/ks/tocho5/dto/TeamDetailDTOs.java
package com.ks.tocho5.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TeamDetailDTOs {

    public record TeamCaptainDTO(
            Long id,
            String fullName
    ) {}

    public record SeasonInfoDTO(
            Integer id,
            String name
    ) {}

    public record CategoryInfoDTO(
            Integer id,
            String gender,
            String name,
            String code
    ) {}

    // 👇 CAMBIO: teamId ahora es Integer
    public record TeamBasicDTO(
            Integer teamId,
            SeasonInfoDTO season,
            CategoryInfoDTO category,
            String name,
            String shortName,
            String logoUrl,
            String colorPrimary,
            String colorSecondary,
            TeamCaptainDTO captain
    ) {}

    public record PlayerPublicDTO(
            Long id,
            String fullName,
            String curp,
            Integer jerseyNumber,
            LocalDate birthdate,
            String photoUrl
    ) {}

    public record TeamPhotoDTO(
            Long id,
            String photoUrl,
            Integer sortOrder
    ) {}

    public record GameSummaryDTO(
            Integer gameId,
            LocalDateTime matchDateUtc,
            String status,
            Integer homeTeamId,
            String homeTeamName,
            Integer awayTeamId,
            String awayTeamName
    ) {}

    public record TeamDetailDTO(
            TeamBasicDTO team,
            List<PlayerPublicDTO> players,
            List<GameSummaryDTO> lastGames,
            List<TeamPhotoDTO> gallery
    ) {}
}
