package com.ks.tocho5.service.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ks.tocho5.model.SiteConfigModel;
import com.ks.tocho5.model.dto.SiteConfigResponseDTO;
import com.ks.tocho5.model.dto.SiteConfigUpsertRequestDTO;
import com.ks.tocho5.repository.SiteConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteConfigService {

    private final SiteConfigRepository repo;
    private final ObjectMapper mapper;

    public SiteConfigService(SiteConfigRepository repo, ObjectMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    private JsonNode homeDefaults() {
        // ✅ default home (sin location y sin cta1/cta2)
        String json = """
        {
          "schemaVersion": 1,
          "hero": {
            "title": "Temporada 2026",
            "subtitle": "Resultados, posiciones y registros en un solo lugar.",
            "images": [
              { "id": "carrusel-1", "src": "/img/carrusel1.jpg" },
              { "id": "carrusel-2", "src": "/img/carrusel2.jpg" },
              { "id": "carrusel-3", "src": "/img/carrusel3.jpg" }
            ]
          },
          "intro": {
            "title": "TOCHERO5LIGA",
            "subtitle": "ENTÉRATE DE TODO LO QUE ESTÁ PASANDO EN EL TORNEO."
          },
          "sponsors": [
            {
              "id": "dicass",
              "name": "DICASS",
              "logo": "/img/sponsors/dicass-logo.png",
              "url": "https://dicass.com.mx/",
              "tagline": "Innovación para el juego y el bienestar.",
              "description": "Dicass acompaña a jugadores y familias con activaciones, alimentos y experiencias dentro del deportivo.",
              "label": "Patrocinador principal"
            }
          ]
        }
        """;
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Defaults JSON inválido", e);
        }
    }

    @Transactional
    public SiteConfigResponseDTO getHome() {
        SiteConfigModel cfg = repo.findByKey("home").orElseGet(() -> {
            SiteConfigModel created = new SiteConfigModel();
            created.setKey("home");
            created.setSchemaVersion(1);
            created.setData(homeDefaults());
            return repo.save(created);
        });

        return new SiteConfigResponseDTO(
                cfg.getKey(),
                cfg.getSchemaVersion(),
                cfg.getData(),
                cfg.getUpdatedAt()
        );
    }

    @Transactional
    public SiteConfigResponseDTO putHome(SiteConfigUpsertRequestDTO req) {
        if (req == null || req.data() == null) {
            throw new IllegalArgumentException("Falta body.data (JSON)");
        }

        SiteConfigModel cfg = repo.findByKey("home").orElseGet(() -> {
            SiteConfigModel c = new SiteConfigModel();
            c.setKey("home");
            return c;
        });

        int ver = req.schemaVersion() > 0 ? req.schemaVersion() : 1;

        cfg.setSchemaVersion(ver);
        cfg.setData(req.data());

        SiteConfigModel saved = repo.save(cfg);

        return new SiteConfigResponseDTO(
                saved.getKey(),
                saved.getSchemaVersion(),
                saved.getData(),
                saved.getUpdatedAt()
        );
    }
}