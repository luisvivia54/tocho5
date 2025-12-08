package com.ks.tocho5.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 191)
    private String keycloakId;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
    
    @Column(name = "role", nullable = false)
    private String role;   // "USER", "CAPTAIN", "ADMIN"

    /**
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * @return the maxTeamsAllowed
	 */
	public Integer getMaxTeamsAllowed() {
		return maxTeamsAllowed;
	}

	/**
	 * @param maxTeamsAllowed the maxTeamsAllowed to set
	 */
	public void setMaxTeamsAllowed(Integer maxTeamsAllowed) {
		this.maxTeamsAllowed = maxTeamsAllowed;
	}
	
	@Column(name = "max_teams_allowed", nullable = false)
    private Integer maxTeamsAllowed;

    // Opcional: mapear timestamps (los maneja la BD, por eso insertable=false/updatable=false)
    @Column(name = "created_at", insertable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.time.OffsetDateTime updatedAt;

    // getters y setters...

    public Long getId() { return id; }

    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public java.time.OffsetDateTime getCreatedAt() { return createdAt; }
    public java.time.OffsetDateTime getUpdatedAt() { return updatedAt; }
    

    // ===== helpers =====
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isCaptain() {
        return "CAPTAIN".equalsIgnoreCase(role);
    }

    public boolean hasCaptainPrivileges() {
        return isCaptain() || isAdmin();
    }
}
