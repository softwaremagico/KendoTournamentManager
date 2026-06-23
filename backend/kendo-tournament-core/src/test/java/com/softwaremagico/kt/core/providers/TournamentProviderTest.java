package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.tournaments.ITournamentManager;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentImageRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.core.tournaments.TournamentHandlerSelector;
import com.softwaremagico.kt.persistence.encryption.KeyProperty;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"tournamentProviderTests"})
public class TournamentProviderTest {

    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private TournamentExtraPropertyRepository tournamentExtraPropertyRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private FightRepository fightRepository;
    @Mock
    private DuelRepository duelRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TournamentHandlerSelector tournamentHandlerSelector;
    @Mock
    private TournamentImageRepository tournamentImageRepository;
    @Mock
    private AchievementRepository achievementRepository;

    private TournamentProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new TournamentProvider(tournamentRepository, tournamentExtraPropertyRepository,
                groupRepository, fightRepository, duelRepository, teamRepository, roleRepository,
                tournamentHandlerSelector, tournamentImageRepository, achievementRepository);
    }

    // ========== getPreviousTo Tests ==========

    @Test
    public void testGetPreviousToWithNullTournament() {
        final List<Tournament> result = provider.getPreviousTo(null);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetPreviousToWithNullCreatedAt() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        // createdAt is null by default
        final List<Tournament> result = provider.getPreviousTo(tournament);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetPreviousToWithNullTournamentAndLimit() {
        final List<Tournament> result = provider.getPreviousTo(null, 5);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetPreviousToWithNullCreatedAtAndLimit() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        final List<Tournament> result = provider.getPreviousTo(tournament, 5);
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetPreviousToReturnsOlderTournaments() {
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(2));
        final Tournament t3 = tournamentWithDate("T3", LocalDateTime.now().minusDays(3));

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(t1, t2, t3)));

        final List<Tournament> previous = provider.getPreviousTo(t1);

        assertThat(previous).containsExactly(t2, t3);
    }

    @Test
    public void testGetPreviousToWithLimitReturnsLimitedResults() {
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(2));
        final Tournament t3 = tournamentWithDate("T3", LocalDateTime.now().minusDays(3));
        final Tournament t4 = tournamentWithDate("T4", LocalDateTime.now().minusDays(4));

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(t1, t2, t3, t4)));

        final List<Tournament> previous = provider.getPreviousTo(t1, 2);

        assertThat(previous).hasSize(2).containsExactly(t2, t3);
    }

    // ========== markAsFinished Tests ==========

    @Test
    public void testMarkAsFinishedSetsFinishedAt() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);

        provider.markAsFinished(tournament, true);

        assertThat(tournament.getFinishedAt()).isNotNull();
        verify(tournamentRepository).save(tournament);
    }

    @Test
    public void testMarkAsFinishedDoesNotSetIfAlreadyFinished() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        final LocalDateTime alreadySet = LocalDateTime.now().minusHours(1);
        tournament.updateFinishedAt(alreadySet);

        provider.markAsFinished(tournament, true);

        assertThat(tournament.getFinishedAt()).isEqualTo(alreadySet);
        verify(tournamentRepository, never()).save(tournament);
    }

    @Test
    public void testMarkAsUnfinishedClearsFinishedAt() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        tournament.updateFinishedAt(LocalDateTime.now());

        provider.markAsFinished(tournament, false);

        assertThat(tournament.getFinishedAt()).isNull();
        verify(tournamentRepository).save(tournament);
    }

    @Test
    public void testMarkAsUnfinishedNoSaveWhenAlreadyNotFinished() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        // finishedAt already null

        provider.markAsFinished(tournament, false);

        verify(tournamentRepository, never()).save(tournament);
    }

    // ========== countTournamentsAfter Tests ==========

    @Test
    public void testCountTournamentsAfterWithNullDateUsesDefault() {
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusMonths(6));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusYears(2));

        when(tournamentRepository.findAll()).thenReturn(List.of(t1, t2));

        final long count = provider.countTournamentsAfter(null);

        assertThat(count).isEqualTo(1L); // Only t1 is within 1 year
    }

    @Test
    public void testCountTournamentsAfterWithSpecificDate() {
        final LocalDateTime cutoff = LocalDateTime.now().minusDays(10);
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(5));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(20));

        when(tournamentRepository.findAll()).thenReturn(List.of(t1, t2));

        final long count = provider.countTournamentsAfter(cutoff);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void testCountTournamentsAfterExcludesNullCreatedAt() {
        final Tournament t1 = new Tournament("T1", 1, 3, TournamentType.LEAGUE, "user");
        // t1.createdAt is null

        when(tournamentRepository.findAll()).thenReturn(List.of(t1));

        final long count = provider.countTournamentsAfter(LocalDateTime.now().minusDays(30));

        assertThat(count).isZero();
    }

    // ========== findLastByUnlocked Tests ==========

    @Test
    public void testFindLastByUnlockedReturnsLatestUnlocked() {
        final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));
        final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(2));

        when(tournamentRepository.findByLocked(false)).thenReturn(new ArrayList<>(List.of(t1, t2)));

        final Tournament result = provider.findLastByUnlocked();

        assertThat(result).isEqualTo(t1);
    }

    @Test
    public void testFindLastByUnlockedReturnsNullWhenEmpty() {
        when(tournamentRepository.findByLocked(false)).thenReturn(List.of());

        final Tournament result = provider.findLastByUnlocked();

        assertThat(result).isNull();
    }

    // ========== update Tests ==========

    @Test
    public void testUpdateSetsLockedAtWhenLocking() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        tournament.setLocked(true);
        // lockedAt is null

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        provider.update(tournament);

        assertThat(tournament.getLockedAt()).isNotNull();
    }

    @Test
    public void testUpdateClearsLockedAtWhenUnlocking() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        tournament.setLocked(false);
        tournament.setLockedAt(LocalDateTime.now());

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        provider.update(tournament);

        assertThat(tournament.getLockedAt()).isNull();
    }

    @Test
    public void testUpdateDoesNotOverwriteExistingLockedAt() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);
        tournament.setLocked(true);
        final LocalDateTime existingLockedAt = LocalDateTime.now().minusHours(2);
        tournament.setLockedAt(existingLockedAt);

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        provider.update(tournament);

        assertThat(tournament.getLockedAt()).isEqualTo(existingLockedAt);
    }

    // ========== delete Tests ==========

    @Test
    public void testDeleteNullDoesNothing() {
        provider.delete((Tournament) null);

        verify(tournamentRepository, never()).delete(any(Tournament.class));
    }

    @Test
    public void testDeleteTournamentCleansRelatedEntities() {
        final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
        tournament.setId(1);

        provider.delete(tournament);

        verify(tournamentExtraPropertyRepository).deleteByTournament(tournament);
        verify(groupRepository).deleteByTournament(tournament);
        verify(fightRepository).deleteByTournament(tournament);
        verify(duelRepository).deleteByTournament(tournament);
        verify(teamRepository).deleteByTournament(tournament);
        verify(roleRepository).deleteByTournament(tournament);
        verify(achievementRepository).deleteByTournament(tournament);
        verify(tournamentRepository).delete(tournament);
    }

    // ========== findByName Tests ==========

    @Test
    public void testFindByNameDelegatesToRepository() {
        final Tournament tournament = new Tournament("MyTournament", 1, 3, TournamentType.LEAGUE, "user");
        when(tournamentRepository.findByName("MyTournament")).thenReturn(Optional.of(tournament));

        final Optional<Tournament> result = provider.findByName("MyTournament");

        assertThat(result).isPresent().contains(tournament);
    }

     @Test
     public void testFindByNameReturnsEmptyWhenNotFound() {
         when(tournamentRepository.findByName("Unknown")).thenReturn(Optional.empty());

         final Optional<Tournament> result = provider.findByName("Unknown");

         assertThat(result).isEmpty();
     }

     // ========== save Tests ==========

     @Test
     public void testSaveNewTournamentCallsSetDefaultProperties() {
         final Tournament tournament = new Tournament("NewT", 1, 3, TournamentType.LEAGUE, "user");
         tournament.setId(null); // Ensure it's new

         when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
             Tournament t = invocation.getArgument(0);
             t.setId(1); // Simulate ID generation
             return t;
         });
         when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user"))
                 .thenReturn(new ArrayList<>());

         final Tournament result = provider.save(tournament);

         assertThat(result).isNotNull();
         assertThat(result.getId()).isEqualTo(1);
         verify(tournamentRepository).save(tournament);
         verify(tournamentExtraPropertyRepository).findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user");
     }

     @Test
     public void testSaveExistingTournamentDoesNotCallSetDefaultProperties() {
         final Tournament tournament = new Tournament("ExistingT", 1, 3, TournamentType.LEAGUE, "user");
         tournament.setId(1); // Already has an ID

         when(tournamentRepository.save(tournament)).thenReturn(tournament);

         provider.save(tournament);

         verify(tournamentRepository).save(tournament);
         verify(tournamentExtraPropertyRepository, never()).findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc(any());
     }

     @Test
     public void testSaveWithParameters() {
         final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
         tournament.setId(1);

         when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
             Tournament t = invocation.getArgument(0);
             t.setId(1);
             return t;
         });
         when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user"))
                 .thenReturn(new ArrayList<>());

         final Tournament result = provider.save("T", 2, 4, TournamentType.TREE, "user");

         assertThat(result.getName()).isEqualTo("T");
         assertThat(result.getShiaijos()).isEqualTo(2);
         assertThat(result.getTeamSize()).isEqualTo(4);
         assertThat(result.getType()).isEqualTo(TournamentType.TREE);
     }

     @Test
     public void testSaveWithDefaultValues() {
         when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
             Tournament t = invocation.getArgument(0);
             t.setId(1);
             return t;
         });
         when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user"))
                 .thenReturn(new ArrayList<>());

         final Tournament result = provider.save("T", null, null, null, "user");

         assertThat(result.getShiaijos()).isEqualTo(1);
         assertThat(result.getTeamSize()).isEqualTo(TournamentProvider.DEFAULT_TEAM_SIZE);
         assertThat(result.getType()).isEqualTo(TournamentType.LEAGUE);
     }

     // ========== create Tests ==========

     @Test
     public void testCreateTournamentWithDefaultGroup() {
         final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
         tournament.setId(1);

         when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
             Tournament t = invocation.getArgument(0);
             t.setId(1);
             return t;
         });
         when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user"))
                 .thenReturn(new ArrayList<>());

         final Tournament result = provider.create("T", 2, 4, TournamentType.TREE, "user");

         assertThat(result).isNotNull();
         assertThat(result.getName()).isEqualTo("T");
         verify(groupRepository).save(any(Group.class));
     }

     @Test
     public void testCreateTournamentWithNullParameters() {
         final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
         tournament.setId(1);

         when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
             Tournament t = invocation.getArgument(0);
             t.setId(1);
             return t;
         });
         when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user"))
                 .thenReturn(new ArrayList<>());

         provider.create("T", null, null, null, "user");

         verify(groupRepository).save(any(Group.class));
     }

     // ========== clone Tests ==========

     @Test
     public void testCloneTournamentCopiesAllEntities() {
         final Tournament source = new Tournament("Source", 1, 3, TournamentType.LEAGUE, "user1");
         source.setId(1);

         final Tournament cloned = new Tournament("Copy of Source", 1, 3, TournamentType.LEAGUE, "user2");
         cloned.setId(2);

         final Role sourceRole = new Role();
         sourceRole.setTournament(source);
         sourceRole.setId(1);

         final Team sourceTeam = new Team();
         sourceTeam.setTournament(source);
         sourceTeam.setId(1);
         sourceTeam.setMembers(new ArrayList<>());

         final TournamentExtraProperty sourceProp = new TournamentExtraProperty();
         sourceProp.setTournament(source);
         sourceProp.setId(1);

         final TournamentImage sourceImage = new TournamentImage();
         sourceImage.setTournament(source);
         sourceImage.setId(1);

         when(tournamentRepository.save(any(Tournament.class))).thenReturn(cloned);
         when(roleRepository.findByTournament(source)).thenReturn(new ArrayList<>(List.of(sourceRole)));
         when(teamRepository.findByTournament(source)).thenReturn(new ArrayList<>(List.of(sourceTeam)));
         when(tournamentExtraPropertyRepository.findByTournament(source)).thenReturn(new ArrayList<>(List.of(sourceProp)));
         when(tournamentImageRepository.findByTournament(source)).thenReturn(new ArrayList<>(List.of(sourceImage)));
         when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user2"))
                 .thenReturn(new ArrayList<>());

         final Tournament result = provider.clone(source, "user2");

         assertThat(result).isEqualTo(cloned);
         verify(roleRepository).saveAll(any());
         verify(teamRepository).saveAll(any());
         verify(tournamentExtraPropertyRepository).saveAll(any());
         verify(tournamentImageRepository).saveAll(any());
         verify(groupRepository).save(any(Group.class));
     }

     // ========== setNumberOfWinners Tests ==========

     @Test
     public void testSetNumberOfWinnersForTreeTournament() {
         final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
         tournament.setId(1);
         tournament.setType(TournamentType.TREE); // Tree tournament type

         final Group group = new Group();
         group.setTournament(tournament);
         group.setLevel(0);

         final TreeTournamentHandler handler = org.mockito.Mockito.mock(TreeTournamentHandler.class);

         when(provider.get(1)).thenReturn(Optional.of(tournament));
         when(tournamentHandlerSelector.selectManager(TournamentType.TREE)).thenReturn(handler);
         when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(new ArrayList<>(List.of(group)));

         provider.setNumberOfWinners(1, 4, "admin");

         verify(tournamentExtraPropertyRepository).deleteByTournamentAndPropertyKey(tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
         verify(tournamentExtraPropertyRepository).save(any(TournamentExtraProperty.class));
     }

     @Test
     public void testSetNumberOfWinnersThrowsExceptionWhenTournamentNotFound() {
         when(provider.get(999)).thenReturn(Optional.empty());

         assertThatThrownBy(() -> provider.setNumberOfWinners(999, 4, "admin"))
                 .isInstanceOf(TournamentNotFoundException.class);
     }

     @Test
     public void testSetNumberOfWinnersForNonTreeTournament() {
         final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
         tournament.setId(1);

         final ITournamentManager manager = org.mockito.Mockito.mock(ITournamentManager.class);

         when(provider.get(1)).thenReturn(Optional.of(tournament));
         when(tournamentHandlerSelector.selectManager(TournamentType.LEAGUE)).thenReturn(manager);

         provider.setNumberOfWinners(1, 4, "admin");

         verify(tournamentExtraPropertyRepository, never()).deleteByTournamentAndPropertyKey(any(), any());
     }

     // ========== findByName with Encryption Tests ==========

     @Test
     public void testFindByNameWithEncryptionEnabled() {
         final Tournament tournament1 = new Tournament("Tournament1", 1, 3, TournamentType.LEAGUE, "user");
         final Tournament tournament2 = new Tournament("Tournament2", 1, 3, TournamentType.LEAGUE, "user");

         try (MockedStatic<KeyProperty> keyProperty = mockStatic(KeyProperty.class)) {
             keyProperty.when(KeyProperty::getDatabaseEncryptionKey).thenReturn("encryptionKey");

             when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(tournament1, tournament2)));

             final Optional<Tournament> result = provider.findByName("Tournament1");

             assertThat(result).isPresent().contains(tournament1);
         }
     }

     @Test
     public void testFindByNameWithEncryptionEnabledCaseInsensitive() {
         final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "user");

         try (MockedStatic<KeyProperty> keyProperty = mockStatic(KeyProperty.class)) {
             keyProperty.when(KeyProperty::getDatabaseEncryptionKey).thenReturn("encryptionKey");

             when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(tournament)));

             final Optional<Tournament> result = provider.findByName("tournament");

             assertThat(result).isPresent().contains(tournament);
         }
     }

     @Test
     public void testFindByNameWithEncryptionEnabledNotFound() {
         final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "user");

         try (MockedStatic<KeyProperty> keyProperty = mockStatic(KeyProperty.class)) {
             keyProperty.when(KeyProperty::getDatabaseEncryptionKey).thenReturn("encryptionKey");

             when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(tournament)));

             final Optional<Tournament> result = provider.findByName("Unknown");

             assertThat(result).isEmpty();
         }
     }

      @Test
      public void testFindByNameWithoutEncryptionDelegatesToRepository() {
          final Tournament tournament = new Tournament("MyTournament", 1, 3, TournamentType.LEAGUE, "user");
          when(tournamentRepository.findByName("MyTournament")).thenReturn(Optional.of(tournament));

          try (MockedStatic<KeyProperty> keyProperty = mockStatic(KeyProperty.class)) {
              keyProperty.when(KeyProperty::getDatabaseEncryptionKey).thenReturn(null);

              final Optional<Tournament> result = provider.findByName("MyTournament");

              assertThat(result).isPresent().contains(tournament);
              verify(tournamentRepository).findByName("MyTournament");
          }
      }

      @Test
      public void testFindByNameWithoutEncryptionEmptyKey() {
          when(tournamentRepository.findByName("Unknown")).thenReturn(Optional.empty());

          try (MockedStatic<KeyProperty> keyProperty = mockStatic(KeyProperty.class)) {
              keyProperty.when(KeyProperty::getDatabaseEncryptionKey).thenReturn("");

              final Optional<Tournament> result = provider.findByName("Unknown");

              assertThat(result).isEmpty();
          }
      }

      @Test
      public void testSaveWithExistingPropertiesCallsSetDefaultProperties() {
          final Tournament tournament = new Tournament("NewT", 1, 3, TournamentType.LEAGUE, "user");
          tournament.setId(null);

          final TournamentExtraProperty prop1 = new TournamentExtraProperty();
          prop1.setId(1);
          prop1.setTournament(new Tournament("OldT", 1, 3, TournamentType.LEAGUE, "user"));

          when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
              Tournament t = invocation.getArgument(0);
              t.setId(1);
              return t;
          });
          when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user"))
                  .thenReturn(new ArrayList<>(List.of(prop1)));

          final Tournament result = provider.save(tournament);

          assertThat(result).isNotNull();
          verify(tournamentExtraPropertyRepository).saveAll(any());
      }

      @Test
      public void testCloneTournamentWithTournamentScore() {
          final Tournament source = new Tournament("Source", 1, 3, TournamentType.LEAGUE, "user1");
          source.setId(1);

          // Create a mock tournament score
          source.setTournamentScore(new org.mockito.Mockito().mock(com.softwaremagico.kt.persistence.entities.TournamentScore.class));

          final Tournament cloned = new Tournament("Copy of Source", 1, 3, TournamentType.LEAGUE, "user2");
          cloned.setId(2);

          when(tournamentRepository.save(any(Tournament.class))).thenReturn(cloned);
          when(roleRepository.findByTournament(source)).thenReturn(new ArrayList<>());
          when(teamRepository.findByTournament(source)).thenReturn(new ArrayList<>());
          when(tournamentExtraPropertyRepository.findByTournament(source)).thenReturn(new ArrayList<>());
          when(tournamentImageRepository.findByTournament(source)).thenReturn(new ArrayList<>());
          when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user2"))
                  .thenReturn(new ArrayList<>());

          final Tournament result = provider.clone(source, "user2");

          assertThat(result).isEqualTo(cloned);
          assertThat(cloned.getTournamentScore()).isNotNull();
      }

      @Test
      public void testCountTournamentsAfterMultipleVariations() {
          final LocalDateTime baseDate = LocalDateTime.now().minusDays(5);
          final Tournament t1 = tournamentWithDate("T1", baseDate.minusDays(2));
          final Tournament t2 = tournamentWithDate("T2", baseDate.plusDays(2));
          final Tournament t3 = new Tournament("T3", 1, 3, TournamentType.LEAGUE, "user");
          // t3 has null createdAt

          when(tournamentRepository.findAll()).thenReturn(List.of(t1, t2, t3));

          final long count = provider.countTournamentsAfter(baseDate);

          assertThat(count).isEqualTo(1L); // Only t2
      }

      @Test
      public void testGetPreviousToLimitExceedsAvailable() {
          final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));
          final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(2));

          when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(t1, t2)));

          final List<Tournament> previous = provider.getPreviousTo(t1, 100);

          assertThat(previous).hasSize(1).containsExactly(t2);
      }

      @Test
      public void testGetPreviousToWhenTournamentIsLast() {
          final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));
          final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusDays(2));

          when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(t1, t2)));

          final List<Tournament> previous = provider.getPreviousTo(t2);

          assertThat(previous).isEmpty();
      }

      @Test
      public void testUpdateLockedAtTransition() {
          final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
          tournament.setId(1);
          tournament.setLocked(false);
          tournament.setLockedAt(LocalDateTime.now().minusHours(1));

          when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

          provider.update(tournament);

          assertThat(tournament.getLockedAt()).isNull();
          verify(tournamentRepository).save(tournament);
      }

      @Test
      public void testMarkAsFinishedWhenAlreadyFinished() {
          final Tournament tournament = new Tournament("T", 1, 3, TournamentType.LEAGUE, "user");
          tournament.setId(1);
          final LocalDateTime finishedTime = LocalDateTime.now().minusHours(2);
          tournament.updateFinishedAt(finishedTime);

          provider.markAsFinished(tournament, true);

          assertThat(tournament.getFinishedAt()).isEqualTo(finishedTime);
          verify(tournamentRepository, never()).save(tournament);
      }

      @Test
      public void testSetNumberOfWinnersWithEmptyGroups() {
          final Tournament tournament = new Tournament("T", 1, 3, TournamentType.TREE, "user");
          tournament.setId(1);

          final TreeTournamentHandler handler = org.mockito.Mockito.mock(TreeTournamentHandler.class);

          when(provider.get(1)).thenReturn(Optional.of(tournament));
          when(tournamentHandlerSelector.selectManager(TournamentType.TREE)).thenReturn(handler);
          when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(new ArrayList<>());

          provider.setNumberOfWinners(1, 4, "admin");

          verify(tournamentExtraPropertyRepository).deleteByTournamentAndPropertyKey(tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
          verify(tournamentExtraPropertyRepository).save(any(TournamentExtraProperty.class));
      }

      @Test
      public void testCreateTournamentWithAllParameters() {
          final Tournament tournament = new Tournament("T", 2, 4, TournamentType.TREE, "user");
          tournament.setId(1);

          when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
              Tournament t = invocation.getArgument(0);
              t.setId(1);
              return t;
          });
          when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user"))
                  .thenReturn(new ArrayList<>());

          final Tournament result = provider.create("T", 2, 4, TournamentType.TREE, "user");

          assertThat(result).isNotNull();
          assertThat(result.getShiaijos()).isEqualTo(2);
          assertThat(result.getTeamSize()).isEqualTo(4);
          assertThat(result.getType()).isEqualTo(TournamentType.TREE);
          verify(groupRepository).save(any(Group.class));
      }

      @Test
      public void testCountTournamentsAfterWhenAllOlderThanYear() {
          final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusYears(2));
          final Tournament t2 = tournamentWithDate("T2", LocalDateTime.now().minusYears(3));

          when(tournamentRepository.findAll()).thenReturn(List.of(t1, t2));

          final long count = provider.countTournamentsAfter(null);

          assertThat(count).isZero();
      }

      @Test
      public void testGetPreviousToSingleElement() {
          final Tournament t1 = tournamentWithDate("T1", LocalDateTime.now().minusDays(1));

          when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(t1)));

          final List<Tournament> previous = provider.getPreviousTo(t1);

          assertThat(previous).isEmpty();
      }

      @Test
      public void testCloneTournamentWithEmptyCollections() {
          final Tournament source = new Tournament("Source", 1, 3, TournamentType.LEAGUE, "user1");
          source.setId(1);

          final Tournament cloned = new Tournament("Copy of Source", 1, 3, TournamentType.LEAGUE, "user2");
          cloned.setId(2);

          when(tournamentRepository.save(any(Tournament.class))).thenReturn(cloned);
          when(roleRepository.findByTournament(source)).thenReturn(new ArrayList<>());
          when(teamRepository.findByTournament(source)).thenReturn(new ArrayList<>());
          when(tournamentExtraPropertyRepository.findByTournament(source)).thenReturn(new ArrayList<>());
          when(tournamentImageRepository.findByTournament(source)).thenReturn(new ArrayList<>());
          when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user2"))
                  .thenReturn(new ArrayList<>());

          final Tournament result = provider.clone(source, "user2");

          assertThat(result).isEqualTo(cloned);
          verify(groupRepository).save(any(Group.class));
      }

      @Test
      public void testSaveNewTournamentCallsSetDefaultPropertiesWithProperties() {
          final Tournament tournament = new Tournament("NewT", 1, 3, TournamentType.LEAGUE, "user");
          tournament.setId(null);

          final TournamentExtraProperty prop = new TournamentExtraProperty();
          prop.setId(1);
          prop.setTournament(new Tournament("OldT", 1, 3, TournamentType.LEAGUE, "user"));

          when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> {
              Tournament t = invocation.getArgument(0);
              t.setId(1);
              return t;
          });
          when(tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByHashOrderByCreatedAtDesc("user"))
                  .thenReturn(new ArrayList<>(List.of(prop)));

          final Tournament result = provider.save(tournament);

          assertThat(result).isNotNull();
          verify(tournamentExtraPropertyRepository).saveAll(any());
      }

    private Tournament tournamentWithDate(String name, LocalDateTime createdAt) {
        final Tournament tournament = new Tournament(name, 1, 3, TournamentType.LEAGUE, "user");
        tournament.setCreatedAt(createdAt);
        return tournament;
    }
}

