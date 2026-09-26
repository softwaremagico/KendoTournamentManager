package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Tenant;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.repositories.AuthenticatedUserRepository;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupLinkRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.ParticipantImageRepository;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentImageRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentScoreRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Deletes every tenant-scoped row of a tenant without touching the rows of
 * other tenants. The tenant registration itself is preserved.
 * <p>
 * Tenant-scoped entity tables are removed through the {@code deleteByTenantId}
 * bulk delete provided by each repository. Bulk deletes are used so the
 * operation is atomic and independent of the request {@link TenantContext}
 * (the Hibernate tenant filter would otherwise scope queries to the
 * authenticated tenant, preventing an admin from cleaning another tenant).
 * The tenant context is temporarily switched to the target tenant so the
 * tenant filter, if applied, never contradicts the bulk delete condition.
 * </p>
 * <p>
 * The pure association join tables ({@code duels_by_fight}, {@code fights_by_group},
 * {@code teams_by_group}, {@code unties}, {@code members_of_team} and
 * {@code authenticated_user_roles}) carry no {@code tenant_id} and have no JPA
 * entity of their own, so they cannot be reached through a repository. Their
 * rows are matched through the tenant-scoped parent tables.
 * </p>
 */
@Service
public class TenantDataCleanupProvider {
    /**
     * Rows that reference the tenant are removed in child-before-parent order.
     */
    private static final List<String> JOIN_TABLE_DELETES = List.of(
            "DELETE FROM duels_by_fight WHERE fight_id IN (SELECT id FROM fights WHERE tenant_id = ?)",
            "DELETE FROM fights_by_group WHERE group_id IN (SELECT id FROM tournament_groups WHERE tenant_id = ?)",
            "DELETE FROM teams_by_group WHERE group_id IN (SELECT id FROM tournament_groups WHERE tenant_id = ?)",
            "DELETE FROM unties WHERE group_id IN (SELECT id FROM tournament_groups WHERE tenant_id = ?)",
            "DELETE FROM members_of_team WHERE team_id IN (SELECT id FROM teams WHERE tenant_id = ?)",
            "DELETE FROM authenticated_user_roles WHERE authenticated_user IN (SELECT id FROM authenticated_users WHERE tenant_id = ?)"
    );

    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManagerFactory entityManagerFactory;
    private final AchievementRepository achievementRepository;
    private final AuthenticatedUserRepository authenticatedUserRepository;
    private final ClubRepository clubRepository;
    private final DuelRepository duelRepository;
    private final FightRepository fightRepository;
    private final GroupRepository groupRepository;
    private final GroupLinkRepository groupLinkRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantImageRepository participantImageRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentExtraPropertyRepository tournamentExtraPropertyRepository;
    private final TournamentImageRepository tournamentImageRepository;
    private final TournamentScoreRepository tournamentScoreRepository;

    public TenantDataCleanupProvider(TenantRepository tenantRepository, JdbcTemplate jdbcTemplate,
                                     EntityManagerFactory entityManagerFactory,
                                     AchievementRepository achievementRepository,
                                     AuthenticatedUserRepository authenticatedUserRepository,
                                     ClubRepository clubRepository, DuelRepository duelRepository,
                                     FightRepository fightRepository, GroupRepository groupRepository,
                                     GroupLinkRepository groupLinkRepository,
                                     ParticipantRepository participantRepository,
                                     ParticipantImageRepository participantImageRepository,
                                     RoleRepository roleRepository, TeamRepository teamRepository,
                                     TournamentRepository tournamentRepository,
                                     TournamentExtraPropertyRepository tournamentExtraPropertyRepository,
                                     TournamentImageRepository tournamentImageRepository,
                                     TournamentScoreRepository tournamentScoreRepository) {
        this.tenantRepository = tenantRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.entityManagerFactory = entityManagerFactory;
        this.achievementRepository = achievementRepository;
        this.authenticatedUserRepository = authenticatedUserRepository;
        this.clubRepository = clubRepository;
        this.duelRepository = duelRepository;
        this.fightRepository = fightRepository;
        this.groupRepository = groupRepository;
        this.groupLinkRepository = groupLinkRepository;
        this.participantRepository = participantRepository;
        this.participantImageRepository = participantImageRepository;
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
        this.tournamentExtraPropertyRepository = tournamentExtraPropertyRepository;
        this.tournamentImageRepository = tournamentImageRepository;
        this.tournamentScoreRepository = tournamentScoreRepository;
    }

    /**
     * Removes all data belonging to the given tenant. The tenant registration
     * is kept so it can be reconfigured afterwards.
     *
     * @param tenantId the tenant whose data must be removed.
     * @return the total number of deleted rows.
     */
    @Transactional
    public long deleteAllData(Integer tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("A valid tenant is required.");
        }
        if (TenantContext.LEGACY_TENANT_ID == tenantId) {
            throw new IllegalArgumentException("The default tenant cannot be wiped.");
        }
        final Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found."));
        if (tenantRepository.count() == 1) {
            throw new IllegalArgumentException("The last remaining tenant cannot be wiped.");
        }
        TenantContext.setTenantId(tenantId);
        long deleted;
        try {
            deleted = deleteJoinTableRows(tenantId);
            deleted += groupLinkRepository.deleteByTenantId(tenantId);
            deleted += roleRepository.deleteByTenantId(tenantId);
            deleted += achievementRepository.deleteByTenantId(tenantId);
            deleted += fightRepository.deleteByTenantId(tenantId);
            deleted += duelRepository.deleteByTenantId(tenantId);
            deleted += groupRepository.deleteByTenantId(tenantId);
            deleted += teamRepository.deleteByTenantId(tenantId);
            deleted += participantImageRepository.deleteByTenantId(tenantId);
            deleted += participantRepository.deleteByTenantId(tenantId);
            deleted += clubRepository.deleteByTenantId(tenantId);
            deleted += authenticatedUserRepository.deleteByTenantId(tenantId);
            deleted += tournamentExtraPropertyRepository.deleteByTenantId(tenantId);
            deleted += tournamentImageRepository.deleteByTenantId(tenantId);
            deleted += tournamentRepository.deleteByTenantId(tenantId);
            deleted += tournamentScoreRepository.deleteByTenantId(tenantId);
        } finally {
            TenantContext.clear();
        }
        //Bulk SQL deletes bypass the second level cache (tournaments and users are cached).
        entityManagerFactory.getCache().evictAll();
        KendoTournamentLogger.info(this.getClass(), "All data of tenant '{}' has been deleted.", tenant.getName());
        return deleted;
    }

    private long deleteJoinTableRows(Integer tenantId) {
        long deleted = 0;
        for (String statement : JOIN_TABLE_DELETES) {
            deleted += jdbcTemplate.update(statement, tenantId);
        }
        return deleted;
    }
}
