package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
