package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.UserTenant;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface UserTenantRepository extends JpaRepository<UserTenant, Integer> {
    boolean existsByAuthenticatedUserIdAndTenantId(Integer authenticatedUserId, Integer tenantId);

    List<UserTenant> findAllByAuthenticatedUserId(Integer authenticatedUserId);

    List<UserTenant> findAllByTenantId(Integer tenantId);

    @Modifying
    @Query("DELETE FROM UserTenant u WHERE u.authenticatedUserId = :userId AND u.tenantId = :tenantId")
    int deleteByAuthenticatedUserIdAndTenantId(@Param("userId") Integer userId, @Param("tenantId") Integer tenantId);

    @Modifying
    @Query("DELETE FROM UserTenant u WHERE u.tenantId = :tenantId")
    long deleteByTenantId(@Param("tenantId") Integer tenantId);

    @Query("SELECT u.authenticatedUserId FROM UserTenant u GROUP BY u.authenticatedUserId HAVING COUNT(u) = 1 AND MAX(u.tenantId) = :tenantId")
    List<Integer> findUserIdsOnlyAssignedToTenant(@Param("tenantId") Integer tenantId);
}
