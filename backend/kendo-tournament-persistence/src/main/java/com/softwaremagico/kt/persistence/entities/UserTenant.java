package com.softwaremagico.kt.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_tenants", uniqueConstraints = @UniqueConstraint(columnNames = {"authenticated_user_id", "tenant_id"}))
public class UserTenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "authenticated_user_id", nullable = false)
    private Integer authenticatedUserId;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    public UserTenant() {
    }

    public UserTenant(Integer authenticatedUserId, Integer tenantId) {
        this.authenticatedUserId = authenticatedUserId;
        this.tenantId = tenantId;
    }

    public Integer getAuthenticatedUserId() {
        return authenticatedUserId;
    }

    public Integer getTenantId() {
        return tenantId;
    }
}
