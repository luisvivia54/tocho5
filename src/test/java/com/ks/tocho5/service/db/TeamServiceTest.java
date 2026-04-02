package com.ks.tocho5.service.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ks.tocho5.model.TeamSearchProjection;
import com.ks.tocho5.repository.EquiposFiltroRepository;
import com.ks.tocho5.repository.EquiposRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private EquiposRepository equiposRepository;

    @Mock
    private EquiposFiltroRepository equiposFiltroRepository;

    @InjectMocks
    private TeamService service;

    @Test
    void searchTeamsForAgentReturnsEmptyListWhenQueryIsBlank() {
        List<TeamSearchProjection> result = service.searchTeamsForAgent("   ", null, null, null, null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(equiposRepository);
    }

    @Test
    void searchTeamsForAgentClampsLimitBeforeCallingRepository() {
        when(equiposRepository.searchActiveTeamsByName("Pandas", 5, "MIX", "F", 25))
                .thenReturn(List.of());

        List<TeamSearchProjection> result = service.searchTeamsForAgent("Pandas", 5, "MIX", "F", 99);

        assertEquals(0, result.size());
        verify(equiposRepository).searchActiveTeamsByName("Pandas", 5, "MIX", "F", 25);
    }
}
