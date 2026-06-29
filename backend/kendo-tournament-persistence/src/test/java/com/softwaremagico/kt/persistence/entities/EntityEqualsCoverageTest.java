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

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {"entityEquals"})
public class EntityEqualsCoverageTest {

    // --- Element.equals() branch coverage ---

    @Test
    public void when_equalsItself_expect_true() {
        final Club club = new Club();
        club.setId(1);
        assertThat(club.equals(club)).isTrue();
    }

    @Test
    public void when_equalsNull_expect_false() {
        final Club club = new Club();
        club.setId(1);
        assertThat(club.equals(null)).isFalse();
    }

    @Test
    public void when_equalsDifferentClass_expect_false() {
        final Club club = new Club();
        club.setId(1);
        assertThat(club.equals("notAnEntity")).isFalse();
    }

    @Test
    public void when_equalsSameId_expect_true() {
        final Club club1 = new Club();
        club1.setId(42);
        final Club club2 = new Club();
        club2.setId(42);
        assertThat(club1.equals(club2)).isTrue();
    }

    @Test
    public void when_equalsDifferentId_expect_false() {
        final Club club1 = new Club();
        club1.setId(1);
        final Club club2 = new Club();
        club2.setId(2);
        assertThat(club1.equals(club2)).isFalse();
    }

    @Test
    public void when_equalsBothNullId_expect_true() {
        final Club club1 = new Club();
        final Club club2 = new Club();
        assertThat(club1.equals(club2)).isTrue();
    }

    @Test
    public void when_equalsOneNullId_expect_false() {
        final Club club1 = new Club();
        final Club club2 = new Club();
        club2.setId(1);
        assertThat(club1.equals(club2)).isFalse();
    }

    // --- Element.hashCode() ---

    @Test
    public void when_hashCodeWithId_expect_consistent() {
        final Club club = new Club();
        club.setId(5);
        assertThat(club.hashCode()).isEqualTo(club.hashCode());
    }

    @Test
    public void when_hashCodeNullId_expect_nonException() {
        final Club club = new Club();
        assertThat(club.hashCode()).isEqualTo(club.hashCode());
    }

    // --- Element setCreatedBy/updatedBy also set hash ---

    @Test
    public void when_setCreatedBy_expect_hashAlsoSet() {
        final Club club = new Club();
        club.setCreatedBy("admin");
        assertThat(club.getCreatedBy()).isEqualTo("admin");
        assertThat(club.getCreatedByHash()).isEqualTo("admin");
    }

    @Test
    public void when_setUpdatedBy_expect_hashAlsoSet() {
        final Club club = new Club();
        club.setUpdatedBy("editor");
        assertThat(club.getUpdatedBy()).isEqualTo("editor");
        assertThat(club.getUpdatedByHash()).isEqualTo("editor");
    }

    @Test
    public void when_setCreatedByNull_expect_noException() {
        final Club club = new Club();
        club.setCreatedBy(null);
        assertThat(club.getCreatedBy()).isNull();
    }

    // --- Equals for different entity types to ensure cross-type returns false ---

    @Test
    public void when_equalsClubVsParticipant_sameId_expect_false() {
        final Club club = new Club();
        club.setId(10);
        final Participant participant = new Participant();
        participant.setId(10);
        assertThat(club.equals(participant)).isFalse();
    }

    // --- Tournament.equals() ---

    @Test
    public void when_tournamentEqualsSameId_expect_true() {
        final Tournament t1 = new Tournament();
        t1.setId(7);
        final Tournament t2 = new Tournament();
        t2.setId(7);
        assertThat(t1.equals(t2)).isTrue();
    }

    @Test
    public void when_tournamentEqualsDifferentId_expect_false() {
        final Tournament t1 = new Tournament();
        t1.setId(7);
        final Tournament t2 = new Tournament();
        t2.setId(8);
        assertThat(t1.equals(t2)).isFalse();
    }

    // --- Fight.equals() ---

    @Test
    public void when_fightEqualsItself_expect_true() {
        final Fight fight = new Fight();
        fight.setId(3);
        assertThat(fight.equals(fight)).isTrue();
    }

    // --- Team.equals() ---

    @Test
    public void when_teamEqualsNull_expect_false() {
        final Team team = new Team();
        team.setId(1);
        assertThat(team.equals(null)).isFalse();
    }
}

