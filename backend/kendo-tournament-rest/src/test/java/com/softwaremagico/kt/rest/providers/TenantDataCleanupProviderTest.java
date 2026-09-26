package com.softwaremagico.kt.rest.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.providers.TenantDataCleanupProvider;
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
import jakarta.persistence.Cache;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = "tenantDataCleanupProvider")
public class TenantDataCleanupProviderTest {
    private static final int TENANT_ID = 42;
    private static final int JOIN_TABLE_DELETES = 6;

    private TenantRepository tenantRepository;
    private JdbcTemplate jdbcTemplate;
    private EntityManagerFactory entityManagerFactory;
    private Cache cache;
    private AchievementRepository achievementRepository;
    private AuthenticatedUserRepository authenticatedUserRepository;
    private ClubRepository clubRepository;
    private DuelRepository duelRepository;
    private FightRepository fightRepository;
    private GroupLinkRepository groupLinkRepository;
    private GroupRepository groupRepository;
    private ParticipantRepository participantRepository;
    private ParticipantImageRepository participantImageRepository;
    private RoleRepository roleRepository;
    private TeamRepository teamRepository;
    private TournamentRepository tournamentRepository;
    private TournamentExtraPropertyRepository tournamentExtraPropertyRepository;
    private TournamentImageRepository tournamentImageRepository;
    private TournamentScoreRepository tournamentScoreRepository;
    private TenantDataCleanupProvider provider;

    @BeforeMethod
    public void setUp() {
        tenantRepository = mock(TenantRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        entityManagerFactory = mock(EntityManagerFactory.class);
        cache = mock(Cache.class);
        when(entityManagerFactory.getCache()).thenReturn(cache);
        achievementRepository = mock(AchievementRepository.class);
        authenticatedUserRepository = mock(AuthenticatedUserRepository.class);
        clubRepository = mock(ClubRepository.class);
        duelRepository = mock(DuelRepository.class);
        fightRepository = mock(FightRepository.class);
        groupLinkRepository = mock(GroupLinkRepository.class);
        groupRepository = mock(GroupRepository.class);
        participantRepository = mock(ParticipantRepository.class);
        participantImageRepository = mock(ParticipantImageRepository.class);
        roleRepository = mock(RoleRepository.class);
        teamRepository = mock(TeamRepository.class);
        tournamentRepository = mock(TournamentRepository.class);
        tournamentExtraPropertyRepository = mock(TournamentExtraPropertyRepository.class);
        tournamentImageRepository = mock(TournamentImageRepository.class);
        tournamentScoreRepository = mock(TournamentScoreRepository.class);
        provider = new TenantDataCleanupProvider(tenantRepository, jdbcTemplate, entityManagerFactory,
                achievementRepository, authenticatedUserRepository, clubRepository, duelRepository, fightRepository,
                groupRepository, groupLinkRepository, participantRepository, participantImageRepository, roleRepository,
                teamRepository, tournamentRepository, tournamentExtraPropertyRepository, tournamentImageRepository,
                tournamentScoreRepository);
    }

    @Test
    public void nullTenantIsRejected() {
        Assert.assertThrows(IllegalArgumentException.class, () -> provider.deleteAllData(null));
    }

    @Test
    public void legacyTenantIsProtected() {
        Assert.assertThrows(IllegalArgumentException.class, () -> provider.deleteAllData(TenantContext.LEGACY_TENANT_ID));
    }

    @Test
    public void unknownTenantIsRejected() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.empty());
        Assert.assertThrows(IllegalArgumentException.class, () -> provider.deleteAllData(TENANT_ID));
    }

    @Test
    public void lastRemainingTenantIsProtected() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(new Tenant("Only Tenant")));
        when(tenantRepository.count()).thenReturn(1L);
        Assert.assertThrows(IllegalArgumentException.class, () -> provider.deleteAllData(TENANT_ID));
    }

    @Test
    public void deletesAllTenantScopedRowsAndEvictsCache() {
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(new Tenant("Target Tenant")));
        when(tenantRepository.count()).thenReturn(2L);
        when(jdbcTemplate.update(anyString(), anyInt())).thenReturn(1);
        when(achievementRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(authenticatedUserRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(clubRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(duelRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(fightRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(groupLinkRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(groupRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(participantRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(participantImageRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(roleRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(teamRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(tournamentRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(tournamentExtraPropertyRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(tournamentImageRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);
        when(tournamentScoreRepository.deleteByTenantId(TENANT_ID)).thenReturn(1L);

        final long deleted = provider.deleteAllData(TENANT_ID);

        verify(jdbcTemplate, times(JOIN_TABLE_DELETES)).update(anyString(), anyInt());
        verify(cache).evictAll();
        Assert.assertEquals(deleted, JOIN_TABLE_DELETES + 15L);
        Assert.assertNull(TenantContext.getTenantId(), "Tenant context must be restored after the wipe.");
    }
}