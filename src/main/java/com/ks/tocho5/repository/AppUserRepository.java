// src/main/java/com/ks/tocho5/repository/AppUserRepository.java
package com.ks.tocho5.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.ks.tocho5.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByKeycloakId(String keycloakId);

    @Query("""
        select u
          from AppUser u
         where (:role is null or upper(u.role) = :role)
           and (:active is null or u.active = :active)
           and (
                :q is null or :q = '' or
                lower(coalesce(u.fullName,'')) like lower(concat('%', :q, '%')) or
                lower(coalesce(u.email,'')) like lower(concat('%', :q, '%')) or
                lower(coalesce(u.keycloakId,'')) like lower(concat('%', :q, '%'))
           )
        """)
    Page<AppUser> adminSearch(
            @Param("q") String q,
            @Param("role") String role,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
