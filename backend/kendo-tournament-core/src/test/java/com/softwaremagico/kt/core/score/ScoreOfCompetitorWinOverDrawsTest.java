package com.softwaremagico.kt.core.score;

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

import com.softwaremagico.kt.persistence.entities.Participant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;

/**
 * Unit tests for {@link ScoreOfCompetitorWinOverDraws}, the {@link java.util.Comparator}
 * used to rank competitors giving priority to won duels, then hits, then draws, then
 * number of duels done and finally alphabetical order of the competitor's name.
 */
@Test(groups = "scoreOfCompetitorWinOverDrawsTests")
public class ScoreOfCompetitorWinOverDrawsTest {

    private final ScoreOfCompetitorWinOverDraws comparator = new ScoreOfCompetitorWinOverDraws();

    private ScoreOfCompetitor createScore(String name, String lastname, Integer wonDuels, Integer hits,
                                           Integer drawDuels, Integer duelsDone) {
        final Participant participant = new Participant();
        participant.setName(name);
        participant.setLastname(lastname);

        final ScoreOfCompetitor score = new ScoreOfCompetitor();
        score.setFights(new ArrayList<>());
        score.setUnties(new ArrayList<>());
        score.setCompetitor(participant);
        score.setWonDuels(wonDuels);
        score.setHits(hits);
        score.setDrawDuels(drawDuels);
        score.setDuelsDone(duelsDone);
        return score;
    }

    @Test
    public void shouldRankMoreWonDuelsFirst() {
        final ScoreOfCompetitor better = createScore("A", "A", 3, 0, 0, 0);
        final ScoreOfCompetitor worse = createScore("B", "B", 1, 0, 0, 0);

        Assert.assertTrue(comparator.compare(better, worse) < 0);
        Assert.assertTrue(comparator.compare(worse, better) > 0);
    }

    @Test
    public void shouldUseHitsAsTieBreakerWhenWonDuelsAreEqual() {
        final ScoreOfCompetitor better = createScore("A", "A", 2, 5, 0, 0);
        final ScoreOfCompetitor worse = createScore("B", "B", 2, 2, 0, 0);

        Assert.assertTrue(comparator.compare(better, worse) < 0);
    }

    @Test
    public void shouldUseDrawDuelsAsTieBreakerWhenWonDuelsAndHitsAreEqual() {
        final ScoreOfCompetitor better = createScore("A", "A", 2, 5, 3, 0);
        final ScoreOfCompetitor worse = createScore("B", "B", 2, 5, 1, 0);

        Assert.assertTrue(comparator.compare(better, worse) < 0);
    }

    @Test
    public void shouldPreferMoreDuelsDoneWhenAllPreviousStatsAreEqual() {
        final ScoreOfCompetitor better = createScore("A", "A", 2, 5, 1, 5);
        final ScoreOfCompetitor worse = createScore("B", "B", 2, 5, 1, 2);

        Assert.assertTrue(comparator.compare(better, worse) < 0);
    }

    @Test
    public void shouldFallBackToNameOrderingWhenAllStatsAreEqual() {
        final ScoreOfCompetitor abel = createScore("Abel", "Zeta", 1, 1, 1, 1);
        final ScoreOfCompetitor bruno = createScore("Bruno", "Zeta", 1, 1, 1, 1);

        Assert.assertTrue(comparator.compare(abel, bruno) < 0);
        Assert.assertTrue(comparator.compare(bruno, abel) > 0);
    }

    @Test
    public void shouldReturnZeroForIdenticalScores() {
        final ScoreOfCompetitor first = createScore("Ana", "Perez", 2, 4, 1, 3);
        final ScoreOfCompetitor second = createScore("Ana", "Perez", 2, 4, 1, 3);

        Assert.assertEquals(comparator.compare(first, second), 0);
    }
}

