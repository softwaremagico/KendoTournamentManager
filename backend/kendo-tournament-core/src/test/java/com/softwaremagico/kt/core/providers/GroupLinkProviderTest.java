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

import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.GroupLinkRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "groupLinkProviderTests")
public class GroupLinkProviderTest {

    @Mock
    private GroupLinkRepository groupLinkRepository;

    @Mock
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    @Mock
    private GroupProvider groupProvider;

    private GroupLinkProvider groupLinkProvider;
    private Tournament tournament;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        groupLinkProvider = new GroupLinkProvider(groupLinkRepository, tournamentExtraPropertyProvider, groupProvider);
        tournament = new Tournament("Test", 1, 3, TournamentType.LEAGUE, "tester");
        tournament.setId(1);
    }

    // ── getGroupLinks ──────────────────────────────────────────────────────────

    @Test
    public void shouldReturnStoredLinksWhenRepositoryHasData() {
        final GroupLink link = new GroupLink();
        when(groupLinkRepository.findByTournament(tournament)).thenReturn(List.of(link));

        final List<GroupLink> result = groupLinkProvider.getGroupLinks(tournament);

        assertEquals(result.size(), 1);
        verify(groupLinkRepository).findByTournament(tournament);
    }

    // ── deleteByTournament ─────────────────────────────────────────────────────

    @Test
    public void shouldDeleteLinksByTournament() {
        groupLinkProvider.deleteByTournament(tournament);

        verify(groupLinkRepository).deleteByTournament(tournament);
    }

    // ── generateLinks – standard binary tree ──────────────────────────────────

    @Test
    public void shouldGenerateLinksForStandardBinaryTreeWithOneWinner() {
        // 4 groups at level 0 → 2 groups at level 1 (standard binary tree)
        final List<Group> groups = List.of(
                group(0, 0), group(0, 1), group(0, 2), group(0, 3),
                group(1, 0), group(1, 1));

        mockAsapFalse();

        final List<GroupLink> links = groupLinkProvider.generateLinks(groups, 1, 1, 0);

        assertEquals(links.size(), 4);
    }

    // ── getDestination – binary tree level > 0, winnerOrder == 1 ──────────────

    @Test
    public void shouldCoverWinnerOrder1AtLevel1InBinaryTree() {
        // Level 0: 2, Level 1: 2, Level 2: 1
        // Call getDestination(level1_group0, 2, 1, allGroups)
        // → obtainPositionOfWinnerAsBinaryTree(groups, 0, 2, 2, 1, 1)
        // → winnerOrder==1 && sourceLevel>0 → (2-0+1)/2-1 = 0 → level2_group0
        final Group l0g0 = group(0, 0);
        final Group l0g1 = group(0, 1);
        final Group l1g0 = group(1, 0);
        final Group l1g1 = group(1, 1);
        final Group l2g0 = group(2, 0);
        final List<Group> groups = List.of(l0g0, l0g1, l1g0, l1g1, l2g0);

        mockAsapFalse();

        final Group result = groupLinkProvider.getDestination(l1g0, 2, 1, groups);

        assertEquals(result, l2g0);
    }

    // ── getDestination – catch IndexOutOfBoundsException, return null ──────────

    @Test
    public void shouldReturnNullWhenBinaryTreeWinnerOrderTooHighCausingIndexOutOfBounds() {
        // ASAP=false, 4 groups level 0, 2 groups level 1
        // numberOfWinners=3, winnerOrder=2 →
        //   obtainPositionOfWinnerAsBinaryTree returns -1 →
        //   nextLevelGroups.get(-1) throws IndexOutOfBoundsException → null
        final List<Group> groups = List.of(
                group(0, 0), group(0, 1), group(0, 2), group(0, 3),
                group(1, 0), group(1, 1));

        mockAsapFalse();

        final Group result = groupLinkProvider.getDestination(groups.get(0), 3, 2, groups);

        assertNull(result);
    }

    // ── generateLinks – ASAP + odd 5→3, winnerOrder 0 and 1 ─────────────────

    @Test
    public void shouldGenerateLinksForOddPoolsAsapModeWithTwoWinners() {
        // 5 groups level 0 → 3 groups level 1
        // ASAP=true, 2 winners → obtainPositionOfWinnerNonBinaryTreeOddSize for all indices
        // covers winnerOrder==1 first-half and second-half branches
        final Group g0 = group(0, 0);
        final Group g1 = group(0, 1);
        final Group g2 = group(0, 2);
        final Group g3 = group(0, 3);
        final Group g4 = group(0, 4);
        final Group ng0 = group(1, 0);
        final Group ng1 = group(1, 1);
        final Group ng2 = group(1, 2);
        final List<Group> groups = List.of(g0, g1, g2, g3, g4, ng0, ng1, ng2);

        mockAsapTrue();

        final List<GroupLink> links = groupLinkProvider.generateLinks(groups, 2, 1, 0);

        assertNotNull(links);
        assertTrue(links.size() > 0);
    }

    // ── getDestination – obtainPositionOfWinnerNonBinaryTreeOddSize returns -1

    @Test
    public void shouldReturnNullWhenNonBinaryOddWinnerOrderTooHigh() {
        // 5 groups level 0 → 3 groups level 1
        // ASAP=true, winnerOrder=2 →
        //   obtainPositionOfWinnerNonBinaryTreeOddSize(0, 5, 3, 2) → -1 →
        //   nextLevelGroups.get(-1) throws IndexOutOfBoundsException → null
        final List<Group> groups = List.of(
                group(0, 0), group(0, 1), group(0, 2), group(0, 3), group(0, 4),
                group(1, 0), group(1, 1), group(1, 2));

        mockAsapTrue();

        final Group result = groupLinkProvider.getDestination(groups.get(0), 3, 2, groups);

        assertNull(result);
    }

    // ── getDestination – spreadWinnersOnTreeAsMuchAsPossible returns -1 ────────

    @Test
    public void shouldReturnNullWhenSpreadWinnersReturnsNegativeOne() {
        // 3 groups level 0 → 4 groups level 1
        // ASAP=true, 3 < 4 && 3>1 && 3%2==1 → spreadWinnersOnTreeAsMuchAsPossible(0, 3, 4, 2) → -1 → null
        final List<Group> groups = List.of(
                group(0, 0), group(0, 1), group(0, 2),
                group(1, 0), group(1, 1), group(1, 2), group(1, 3));

        mockAsapTrue();

        final Group result = groupLinkProvider.getDestination(groups.get(0), 3, 2, groups);

        assertNull(result);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Group group(int level, int index) {
        return new Group(tournament, level, index);
    }

    private void mockAsapFalse() {
        final TournamentExtraProperty prop = new TournamentExtraProperty(
                tournament, TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "false");
        when(tournamentExtraPropertyProvider.getByTournamentAndProperty(
                eq(tournament), eq(TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP), any()))
                .thenReturn(prop);
    }

    private void mockAsapTrue() {
        final TournamentExtraProperty prop = new TournamentExtraProperty(
                tournament, TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "true");
        when(tournamentExtraPropertyProvider.getByTournamentAndProperty(
                eq(tournament), eq(TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP), any()))
                .thenReturn(prop);
    }
}

