package com.softwaremagico.kt.persistence.entities;

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

import com.softwaremagico.kt.persistence.values.Score;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {"duelEntity"})
public class DuelEntityCoverageTest {

    // ---------- getCompetitors() ----------

    @Test
    public void when_bothCompetitorSet_expect_setOfTwo() {
        final Duel duel = new Duel();
        final Participant p1 = new Participant();
        p1.setId(1);
        final Participant p2 = new Participant();
        p2.setId(2);
        duel.setCompetitor1(p1);
        duel.setCompetitor2(p2);

        final Set<Participant> result = duel.getCompetitors();

        assertThat(result).hasSize(2).contains(p1, p2);
    }

    @Test
    public void when_competitor1Null_expect_setOfOne() {
        final Duel duel = new Duel();
        final Participant p2 = new Participant();
        p2.setId(2);
        duel.setCompetitor1(null);
        duel.setCompetitor2(p2);

        final Set<Participant> result = duel.getCompetitors();

        assertThat(result).hasSize(1).contains(p2);
    }

    @Test
    public void when_competitor2Null_expect_setOfOne() {
        final Duel duel = new Duel();
        final Participant p1 = new Participant();
        p1.setId(1);
        duel.setCompetitor1(p1);
        duel.setCompetitor2(null);

        final Set<Participant> result = duel.getCompetitors();

        assertThat(result).hasSize(1).contains(p1);
    }

    @Test
    public void when_bothCompetitorsNull_expect_emptySet() {
        final Duel duel = new Duel();
        duel.setCompetitor1(null);
        duel.setCompetitor2(null);

        final Set<Participant> result = duel.getCompetitors();

        assertThat(result).isEmpty();
    }

    // ---------- addCompetitor1Score() / addCompetitor2Score() null list init ----------

    @Test
    public void when_addCompetitor1Score_nullList_expect_initialized() {
        final Duel duel = new Duel();
        duel.setCompetitor1Score(null);

        duel.addCompetitor1Score(Score.MEN);

        assertThat(duel.getCompetitor1Score()).containsExactly(Score.MEN);
    }

    @Test
    public void when_addCompetitor2Score_nullList_expect_initialized() {
        final Duel duel = new Duel();
        duel.setCompetitor2Score(null);

        duel.addCompetitor2Score(Score.KOTE);

        assertThat(duel.getCompetitor2Score()).containsExactly(Score.KOTE);
    }

    @Test
    public void when_addCompetitor1Score_existingList_expect_appended() {
        final Duel duel = new Duel();
        duel.addCompetitor1Score(Score.MEN);
        duel.addCompetitor1Score(Score.DO);

        assertThat(duel.getCompetitor1Score()).containsExactly(Score.MEN, Score.DO);
    }

    // ---------- getCompetitor1ScoreValue() / getCompetitor2ScoreValue() ----------

    @Test
    public void when_noScores_expect_zeroScoreValue() {
        final Duel duel = new Duel();

        assertThat(duel.getCompetitor1ScoreValue()).isZero();
        assertThat(duel.getCompetitor2ScoreValue()).isZero();
    }

    @Test
    public void when_validPointsAdded_expect_correctScoreValue() {
        final Duel duel = new Duel();
        duel.addCompetitor1Score(Score.MEN);
        duel.addCompetitor1Score(Score.KOTE);
        duel.addCompetitor2Score(Score.DO);

        assertThat(duel.getCompetitor1ScoreValue()).isEqualTo(2);
        assertThat(duel.getCompetitor2ScoreValue()).isEqualTo(1);
    }

    @Test
    public void when_invalidPointsAdded_expect_notCountedInScoreValue() {
        final Duel duel = new Duel();
        duel.addCompetitor1Score(Score.EMPTY);
        duel.addCompetitor1Score(Score.DRAW);

        assertThat(duel.getCompetitor1ScoreValue()).isZero();
    }

    // ---------- isOver() ----------

    @Test
    public void when_competitor1HasTwoPoints_expect_isOver() {
        final Duel duel = new Duel();
        duel.addCompetitor1Score(Score.MEN);
        duel.addCompetitor1Score(Score.KOTE);

        assertThat(duel.isOver()).isTrue();
    }

    @Test
    public void when_competitor2HasTwoPoints_expect_isOver() {
        final Duel duel = new Duel();
        duel.addCompetitor2Score(Score.MEN);
        duel.addCompetitor2Score(Score.KOTE);

        assertThat(duel.isOver()).isTrue();
    }

    @Test
    public void when_finishedFlag_expect_isOver() {
        final Duel duel = new Duel();
        duel.setFinished(true);

        assertThat(duel.isOver()).isTrue();
    }

    @Test
    public void when_substitute_expect_isOver() {
        final Duel duel = new Duel();
        duel.setSubstitute(true);

        assertThat(duel.isOver()).isTrue();
    }

    @Test
    public void when_neitherConditionMet_expect_notOver() {
        final Duel duel = new Duel();

        assertThat(duel.isOver()).isFalse();
    }

    // ---------- getSubstitute() null guard ----------

    @Test
    public void when_substituteNull_expect_false() {
        final Duel duel = new Duel();
        duel.setSubstitute(null);

        assertThat(duel.getSubstitute()).isFalse();
    }

    @Test
    public void when_substituteTrue_expect_true() {
        final Duel duel = new Duel();
        duel.setSubstitute(true);

        assertThat(duel.getSubstitute()).isTrue();
    }

    // ---------- getWinner() / getCompetitorWinner() / getCompetitorLooser() ----------

    @Test
    public void when_competitor1Wins_expect_winnerMinusOne() {
        final Duel duel = new Duel();
        duel.addCompetitor1Score(Score.MEN);

        assertThat(duel.getWinner()).isNegative();
    }

    @Test
    public void when_competitor2Wins_expect_winnerPositive() {
        final Duel duel = new Duel();
        duel.addCompetitor2Score(Score.KOTE);

        assertThat(duel.getWinner()).isPositive();
    }

    @Test
    public void when_draw_expect_winnerZero() {
        final Duel duel = new Duel();

        assertThat(duel.getWinner()).isZero();
    }

    @Test
    public void when_competitor1Wins_expect_getCompetitorWinnerReturnsCompetitor1() {
        final Duel duel = new Duel();
        final Participant p1 = new Participant();
        p1.setId(1);
        duel.setCompetitor1(p1);
        duel.addCompetitor1Score(Score.MEN);

        assertThat(duel.getCompetitorWinner()).isEqualTo(p1);
    }

    @Test
    public void when_competitor2Wins_expect_getCompetitorWinnerReturnsCompetitor2() {
        final Duel duel = new Duel();
        final Participant p2 = new Participant();
        p2.setId(2);
        duel.setCompetitor2(p2);
        duel.addCompetitor2Score(Score.TSUKI);

        assertThat(duel.getCompetitorWinner()).isEqualTo(p2);
    }

    @Test
    public void when_draw_expect_getCompetitorWinnerNull() {
        final Duel duel = new Duel();

        assertThat(duel.getCompetitorWinner()).isNull();
    }

    @Test
    public void when_competitor1Wins_expect_getCompetitorLooserReturnsCompetitor2() {
        final Duel duel = new Duel();
        final Participant p1 = new Participant();
        p1.setId(1);
        final Participant p2 = new Participant();
        p2.setId(2);
        duel.setCompetitor1(p1);
        duel.setCompetitor2(p2);
        duel.addCompetitor1Score(Score.MEN);

        assertThat(duel.getCompetitorLooser()).isEqualTo(p2);
    }

    @Test
    public void when_competitor2Wins_expect_getCompetitorLooserReturnsCompetitor1() {
        final Duel duel = new Duel();
        final Participant p1 = new Participant();
        p1.setId(1);
        final Participant p2 = new Participant();
        p2.setId(2);
        duel.setCompetitor1(p1);
        duel.setCompetitor2(p2);
        duel.addCompetitor2Score(Score.DO);

        assertThat(duel.getCompetitorLooser()).isEqualTo(p1);
    }

    @Test
    public void when_draw_expect_getCompetitorLooserNull() {
        final Duel duel = new Duel();

        assertThat(duel.getCompetitorLooser()).isNull();
    }

    // ---------- remaining getters/setters ----------

    @Test
    public void when_setAllFields_expect_correctGetters() {
        final Duel duel = new Duel();
        final LocalDateTime now = LocalDateTime.now();

        duel.setDuration(120);
        duel.setTotalDuration(180);
        duel.setFinished(true);
        duel.setStartedAt(now);
        duel.setFinishedAt(now.plusMinutes(3));
        duel.setCompetitor1Fault(true);
        duel.setCompetitor2Fault(false);
        duel.setCompetitor1FaultTime(30);
        duel.setCompetitor2FaultTime(60);
        duel.setCompetitor1ScoreTime(List.of(10, 50));
        duel.setCompetitor2ScoreTime(List.of(20));
        duel.setType(DuelType.UNDRAW);

        assertThat(duel.getDuration()).isEqualTo(120);
        assertThat(duel.getTotalDuration()).isEqualTo(180);
        assertThat(duel.isFinished()).isTrue();
        assertThat(duel.getStartedAt()).isEqualTo(now);
        assertThat(duel.getFinishedAt()).isEqualTo(now.plusMinutes(3));
        assertThat(duel.getCompetitor1Fault()).isTrue();
        assertThat(duel.getCompetitor2Fault()).isFalse();
        assertThat(duel.getCompetitor1FaultTime()).isEqualTo(30);
        assertThat(duel.getCompetitor2FaultTime()).isEqualTo(60);
        assertThat(duel.getCompetitor1ScoreTime()).containsExactly(10, 50);
        assertThat(duel.getCompetitor2ScoreTime()).containsExactly(20);
        assertThat(duel.getType()).isEqualTo(DuelType.UNDRAW);
    }

    @Test
    public void when_newDuel_expect_defaultTypeIsStandard() {
        final Duel duel = new Duel();
        assertThat(duel.getType()).isEqualTo(DuelType.STANDARD);
    }
}



