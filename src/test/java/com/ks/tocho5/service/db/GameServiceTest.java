package com.ks.tocho5.service.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ks.tocho5.model.GameModel;
import com.ks.tocho5.model.GameStatusModel;
import com.ks.tocho5.model.SeasonModel;
import com.ks.tocho5.model.dto.GameCreateRequest;
import com.ks.tocho5.repository.JuegoStatus;
import com.ks.tocho5.repository.JuegosRepository;
import com.ks.tocho5.repository.StandingTeamRepository;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private StandingTeamRepository standrepo;

    @Mock
    private JuegosRepository juegosrepo;

    @Mock
    private JuegoStatus juegostatus;

    @Mock
    private EntityManager em;

    @InjectMocks
    private GameService service;

    @Test
    void createScheduledGamePersistsVenueWhenProvided() {
        SeasonModel season = new SeasonModel();
        season.setSeasonId(3L);

        when(em.getReference(SeasonModel.class, 3)).thenReturn(season);
        when(juegostatus.save(any(GameStatusModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameStatusModel created = service.createScheduledGame(new GameCreateRequest(
                null,
                3L,
                11,
                7,
                9,
                "2026-01-24T18:00:00.000Z",
                "J1",
                "Cancha 1"
        ));

        assertEquals("SCHEDULED", created.getStatus());
        assertEquals("Cancha 1", created.getVenue());
    }

    @Test
    void createScheduledGameAllowsVenueToBeOmitted() {
        SeasonModel season = new SeasonModel();
        season.setSeasonId(3L);

        when(em.getReference(SeasonModel.class, 3)).thenReturn(season);
        when(juegostatus.save(any(GameStatusModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameStatusModel created = service.createScheduledGame(new GameCreateRequest(
                null,
                3L,
                11,
                7,
                9,
                "2026-01-24T18:00:00.000Z",
                "J1",
                null
        ));

        assertEquals("SCHEDULED", created.getStatus());
        assertNull(created.getVenue());
    }

    @Test
    void saveGameFinalizesScheduledMatchUsingScopedStandingsOnly() {
        GameStatusModel status = scheduledGame(100, 7, 9, 11, 3L);
        GameModel incomingScore = score(100, 21, 14);

        when(juegostatus.findById(100)).thenReturn(Optional.of(status));
        when(juegosrepo.findByIdForUpdate(100)).thenReturn(Optional.empty());
        when(juegosrepo.save(any(GameModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(juegostatus.save(any(GameStatusModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(standrepo.incGpScoped(3, 11, 7, 1)).thenReturn(1);
        when(standrepo.incGpScoped(3, 11, 9, 1)).thenReturn(1);
        when(standrepo.incWinsScoped(3, 11, 7, 1)).thenReturn(1);
        when(standrepo.incLossesScoped(3, 11, 9, 1)).thenReturn(1);
        when(standrepo.incPointsForScoped(3, 11, 7, 21)).thenReturn(1);
        when(standrepo.incPointsForScoped(3, 11, 9, 14)).thenReturn(1);
        when(standrepo.incPointsAgainstScoped(3, 11, 7, 14)).thenReturn(1);
        when(standrepo.incPointsAgainstScoped(3, 11, 9, 21)).thenReturn(1);
        when(standrepo.incTablePointsScoped(3, 11, 7, 3)).thenReturn(1);

        String result = service.saveGame(incomingScore);

        assertEquals("OK", result);
        assertEquals("FINAL", status.getStatus());
        verify(standrepo).incGpScoped(3, 11, 7, 1);
        verify(standrepo).incGpScoped(3, 11, 9, 1);
        verify(standrepo).incWinsScoped(3, 11, 7, 1);
        verify(standrepo).incLossesScoped(3, 11, 9, 1);
        verify(standrepo, never()).incrementGp(anyInt(), anyInt());
        verify(standrepo, never()).incrementWins(anyInt(), anyInt());
        verify(standrepo, never()).incrementLosses(anyInt(), anyInt());
    }

    @Test
    void saveGameDoesNotDoubleCountWhenFinalScoreIsResentUnchanged() {
        GameStatusModel status = finalGame(100, 7, 9, 11, 3L);
        GameModel storedScore = score(100, 21, 14);

        when(juegostatus.findById(100)).thenReturn(Optional.of(status));
        when(juegosrepo.findByIdForUpdate(100)).thenReturn(Optional.of(storedScore));

        String result = service.saveGame(score(100, 21, 14));

        assertEquals("OK", result);
        verifyNoInteractions(standrepo);
        verify(juegosrepo, never()).save(any(GameModel.class));
        verify(juegostatus, never()).save(any(GameStatusModel.class));
    }

    @Test
    void saveGameAdjustsDeltaWhenFinalScoreChangesInsteadOfAddingAnotherMatch() {
        GameStatusModel status = finalGame(100, 7, 9, 11, 3L);
        GameModel storedScore = score(100, 21, 14);

        when(juegostatus.findById(100)).thenReturn(Optional.of(status));
        when(juegosrepo.findByIdForUpdate(100)).thenReturn(Optional.of(storedScore));
        when(juegosrepo.save(any(GameModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(standrepo.incWinsScoped(3, 11, 7, -1)).thenReturn(1);
        when(standrepo.incDrawsScoped(3, 11, 7, 1)).thenReturn(1);
        when(standrepo.incPointsAgainstScoped(3, 11, 7, 7)).thenReturn(1);
        when(standrepo.incTablePointsScoped(3, 11, 7, -2)).thenReturn(1);

        when(standrepo.incLossesScoped(3, 11, 9, -1)).thenReturn(1);
        when(standrepo.incDrawsScoped(3, 11, 9, 1)).thenReturn(1);
        when(standrepo.incPointsForScoped(3, 11, 9, 7)).thenReturn(1);
        when(standrepo.incTablePointsScoped(3, 11, 9, 1)).thenReturn(1);

        String result = service.saveGame(score(100, 21, 21));

        assertEquals("OK", result);
        assertEquals(21, storedScore.getHome_score());
        assertEquals(21, storedScore.getAway_score());
        verify(standrepo, never()).incGpScoped(anyInt(), anyInt(), anyInt(), anyInt());
        verify(standrepo).incWinsScoped(3, 11, 7, -1);
        verify(standrepo).incDrawsScoped(3, 11, 7, 1);
        verify(standrepo).incPointsAgainstScoped(3, 11, 7, 7);
        verify(standrepo).incTablePointsScoped(3, 11, 7, -2);
        verify(standrepo).incLossesScoped(3, 11, 9, -1);
        verify(standrepo).incDrawsScoped(3, 11, 9, 1);
        verify(standrepo).incPointsForScoped(3, 11, 9, 7);
        verify(standrepo).incTablePointsScoped(3, 11, 9, 1);
    }

    @Test
    void saveGameReturnsExplicitErrorWhenStandingsHasDuplicateRows() {
        GameStatusModel status = scheduledGame(100, 7, 9, 11, 3L);

        when(juegostatus.findById(100)).thenReturn(Optional.of(status));
        when(juegosrepo.findByIdForUpdate(100)).thenReturn(Optional.empty());
        when(juegosrepo.save(any(GameModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(juegostatus.save(any(GameStatusModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(standrepo.incGpScoped(3, 11, 7, 1)).thenReturn(3);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.saveGame(score(100, 1, 0)));

        assertEquals("Hay 3 filas standings duplicadas para teamId=7 seasonId=3 categoryId=11 al actualizar gp", ex.getReason());
        verify(standrepo, never()).incrementGp(anyInt(), anyInt());
    }

    @Test
    void deleteGameAndRevertFailsInsteadOfTouchingLegacyRowsWhenScopedStandingIsMissing() {
        GameStatusModel status = finalGame(100, 7, 9, 11, 3L);
        GameModel storedScore = score(100, 21, 14);

        when(juegostatus.findById(100)).thenReturn(Optional.of(status));
        when(juegosrepo.findByIdForUpdate(100)).thenReturn(Optional.of(storedScore));
        when(standrepo.incGpScoped(3, 11, 7, -1)).thenReturn(0);

        assertThrows(ResponseStatusException.class, () -> service.deleteGameAndRevert(100L));

        verify(standrepo, never()).incrementGp(anyInt(), anyInt());
        verify(standrepo, never()).incrementWins(anyInt(), anyInt());
        verify(standrepo, never()).incrementLosses(anyInt(), anyInt());
    }

    private GameStatusModel scheduledGame(int gameId, int homeTeamId, int awayTeamId, int categoryId, long seasonId) {
        GameStatusModel game = baseGame(gameId, homeTeamId, awayTeamId, categoryId, seasonId);
        game.setStatus("SCHEDULED");
        return game;
    }

    private GameStatusModel finalGame(int gameId, int homeTeamId, int awayTeamId, int categoryId, long seasonId) {
        GameStatusModel game = baseGame(gameId, homeTeamId, awayTeamId, categoryId, seasonId);
        game.setStatus("FINAL");
        return game;
    }

    private GameStatusModel baseGame(int gameId, int homeTeamId, int awayTeamId, int categoryId, long seasonId) {
        SeasonModel season = new SeasonModel();
        season.setSeasonId(seasonId);

        GameStatusModel game = new GameStatusModel();
        game.setGame_id(gameId);
        game.setHome_team_id(homeTeamId);
        game.setAway_team_id(awayTeamId);
        game.setCategory_id(categoryId);
        game.setSeason(season);
        return game;
    }

    private GameModel score(int gameId, int home, int away) {
        GameModel game = new GameModel();
        game.setGame_id(gameId);
        game.setHome_score(home);
        game.setAway_score(away);
        return game;
    }
}
