package com.ks.tocho5.service.db;

import com.ks.tocho5.model.TeamStatsFilterDTO;
import com.ks.tocho5.repository.EquiposFiltroRepository;
import com.ks.tocho5.model.EquiposStatsDTO;
import com.ks.tocho5.model.EquiposProjection;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class EquipoStatsService {

  private final EquiposFiltroRepository repo;

  public EquipoStatsService(EquiposFiltroRepository repo) {
    this.repo = repo;
  }

  public Page<EquiposStatsDTO> search(TeamStatsFilterDTO f) {
    int page = (f.page == null) ? 0 : Math.max(0, f.page);
    int size = (f.size == null) ? 20 : Math.min(100, Math.max(1, f.size));
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

    String name = (f.name != null && !f.name.isBlank()) ? f.name.trim() : null;

    Page<EquiposProjection> p = repo.searchTeams(
        name, f.category_id, f.season_id, pageable
    );

    return p.map(r -> new EquiposStatsDTO(
        r.getTeam_id(), r.getName(), r.getCategory_id(), r.getSeason_id()
    ));
  }
}
