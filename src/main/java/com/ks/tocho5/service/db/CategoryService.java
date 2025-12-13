package com.ks.tocho5.service.db;

import com.ks.tocho5.model.CategoryDto;
import com.ks.tocho5.model.CategoryModel;
import com.ks.tocho5.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> getCategories(Long leagueId, String gender) {
        String normalizedGender = normalizeGender(gender);

        List<CategoryModel> categories =
                categoryRepository.findFiltered(leagueId, normalizedGender);

        return categories.stream()
                .map(c -> new CategoryDto(
                        c.getId(),
                        c.getLeagueId(),
                        c.getName(),
                        c.getCode(),
                        c.getGender()
                ))
                .toList();
    }

    private String normalizeGender(String gender) {
        if (gender == null) return null;
        String g = gender.trim();
        if (g.isEmpty() || "all".equalsIgnoreCase(g)) return null;

        g = g.toUpperCase(Locale.ROOT);

        return switch (g) {
            case "V", "VARONIL" -> "VARONIL";
            case "F", "FEMENIL" -> "FEMENIL";
            case "M", "MIXTO"   -> "MIXTO";
            default -> g; // por si mandas algo distinto
        };
    }
}
