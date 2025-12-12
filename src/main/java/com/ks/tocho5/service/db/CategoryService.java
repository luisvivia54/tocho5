package com.ks.tocho5.service.db;

import com.ks.tocho5.model.CategoryDto;
import com.ks.tocho5.model.CategoryModel;
import com.ks.tocho5.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
                .collect(Collectors.toList());
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()
                || "all".equalsIgnoreCase(gender)) {
            return null;
        }
        return gender.toUpperCase(Locale.ROOT);
    }
}
