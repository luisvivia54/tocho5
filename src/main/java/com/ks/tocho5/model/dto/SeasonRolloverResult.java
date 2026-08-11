package com.ks.tocho5.model.dto;

/**
 * Respuesta de POST /api/seasons/rollover
 *
 * @param newSeasonId      id de la nueva temporada creada (ya activa)
 * @param equiposMovidos   inscripciones movidas a la nueva temporada
 * @param standingsCreados filas de standing_team sembradas en ceros
 */
public record SeasonRolloverResult(
        Long newSeasonId,
        int equiposMovidos,
        int standingsCreados
) {}
