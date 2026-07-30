package com.softwaremagico.kt.core.score;

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

import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentScore;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "scoreOfCompetitorCustomTests")
public class ScoreOfCompetitorCustomTest {

    private final ScoreOfCompetitorCustom comparator = new ScoreOfCompetitorCustom();

    private Fight fightWithScore(int pointsByVictory, int pointsByDraw) {
        final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "tester");
        tournament.setTournamentScore(new TournamentScore(ScoreType.INTERNATIONAL, pointsByVictory, pointsByDraw));
        final Team team1 = new Team("Team1", tournament);
        final Team team2 = new Team("Team2", tournament);
        return new Fight(tournament, team1, team2, 0, 0, "tester");
    }

    private ScoreOfCompetitor competitor(String name, Fight fight, int wonDuels, int drawDuels, int hits, int duelsDone) {
        final ScoreOfCompetitor scoreOfCompetitor = new ScoreOfCompetitor();
        final Participant participant = new Participant();
        participant.setName(name);
        participant.setLastname(name);
        scoreOfCompetitor.setCompetitor(participant);
        scoreOfCompetitor.setFights(List.of(fight));
        scoreOfCompetitor.setWonDuels(wonDuels);
        scoreOfCompetitor.setDrawDuels(drawDuels);
        scoreOfCompetitor.setHits(hits);
        scoreOfCompetitor.setDuelsDone(duelsDone);
        return scoreOfCompetitor;
    }

    @Test
    public void shouldRankHigherWinPointsFirst() {
        final Fight fight = this.fightWithScore(2, 1);
        final ScoreOfCompetitor first = this.competitor("Alice", fight, 2, 0, 0, 0);
        final ScoreOfCompetitor second = this.competitor("Bob", fight, 1, 0, 0, 0);

        assertTrue(this.comparator.compare(first, second) < 0);
        assertTrue(this.comparator.compare(second, first) > 0);
    }

    @Test
    public void shouldUseHitsAsTiebreakerWhenPointsAreEqual() {
        final Fight fight = this.fightWithScore(2, 1);
        final ScoreOfCompetitor first = this.competitor("Alice", fight, 1, 0, 5, 0);
        final ScoreOfCompetitor second = this.competitor("Bob", fight, 1, 0, 3, 0);

        assertTrue(this.comparator.compare(first, second) < 0);
    }

    @Test
    public void shouldUseDuelsDoneAsTiebreakerWhenHitsAreEqual() {
        final Fight fight = this.fightWithScore(2, 1);
        final ScoreOfCompetitor first = this.competitor("Alice", fight, 1, 0, 4, 5);
        final ScoreOfCompetitor second = this.competitor("Bob", fight, 1, 0, 4, 2);

        // More duels done with the same score ranks higher (negative result for first).
        assertTrue(this.comparator.compare(first, second) < 0);
    }

    @Test
    public void shouldOrderByNameWhenEverythingElseIsEqual() {
        final Fight fight = this.fightWithScore(2, 1);
        final ScoreOfCompetitor first = this.competitor("Alice", fight, 1, 0, 4, 2);
        final ScoreOfCompetitor second = this.competitor("Bob", fight, 1, 0, 4, 2);

        assertTrue(this.comparator.compare(first, second) < 0);
        assertTrue(this.comparator.compare(second, first) > 0);
    }

    @Test
    public void shouldOrderByNameWhenNoFights() {
        final ScoreOfCompetitor first = new ScoreOfCompetitor();
        final Participant participant1 = new Participant();
        participant1.setName("Alice");
        participant1.setLastname("Alice");
        first.setCompetitor(participant1);
        first.setFights(List.of());

        final ScoreOfCompetitor second = new ScoreOfCompetitor();
        final Participant participant2 = new Participant();
        participant2.setName("Bob");
        participant2.setLastname("Bob");
        second.setCompetitor(participant2);
        second.setFights(List.of());

        assertTrue(this.comparator.compare(first, second) < 0);
    }

    @Test
    public void shouldReturnZeroWhenEqualCompetitors() {
        final Fight fight = this.fightWithScore(2, 1);
        final ScoreOfCompetitor first = this.competitor("Alice", fight, 1, 0, 4, 2);
        final ScoreOfCompetitor second = this.competitor("Alice", fight, 1, 0, 4, 2);

        assertEquals(this.comparator.compare(first, second), 0);
    }
}

