package com.softwaremagico.kt.core.controller.models;

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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(groups = {"scoreTests"})
public class ScoreOfCompetitorDTOTest {

    @Test
    public void shouldCreateDefaultInstance() {
        final ScoreOfCompetitorDTO dto = new ScoreOfCompetitorDTO();
        assertNull(dto.getCompetitor());
        assertNull(dto.getWonDuels());
        assertNull(dto.getDrawDuels());
        assertNull(dto.getUntieDuels());
        assertNull(dto.getHits());
        assertNull(dto.getHitsLost());
        assertNull(dto.getUntieHits());
        assertNull(dto.getDuelsDone());
        assertNull(dto.getWonFights());
        assertNull(dto.getDrawFights());
        assertNull(dto.getTotalFights());
        assertFalse(dto.isCountNotOver());
    }

    @Test
    public void shouldCreateInstanceWithCompetitorAndCountNotOver() {
        final ParticipantDTO participant = new ParticipantDTO();
        participant.setName("Taro");
        participant.setLastname("Yamada");

        final ScoreOfCompetitorDTO dto = new ScoreOfCompetitorDTO(participant, true);
        assertSame(dto.getCompetitor(), participant);
        assertTrue(dto.isCountNotOver());
    }

    @Test
    public void shouldSetAndGetAllFields() {
        final ParticipantDTO participant = new ParticipantDTO();
        participant.setName("Taro");
        participant.setLastname("Yamada");

        final ScoreOfCompetitorDTO dto = new ScoreOfCompetitorDTO();
        dto.setCompetitor(participant);
        dto.setWonDuels(3);
        dto.setDrawDuels(1);
        dto.setUntieDuels(2);
        dto.setHits(5);
        dto.setHitsLost(2);
        dto.setUntieHits(1);
        dto.setDuelsDone(4);
        dto.setWonFights(2);
        dto.setDrawFights(1);
        dto.setTotalFights(3);
        dto.setCountNotOver(true);

        assertSame(dto.getCompetitor(), participant);
        assertEquals((int) dto.getWonDuels(), 3);
        assertEquals((int) dto.getDrawDuels(), 1);
        assertEquals((int) dto.getUntieDuels(), 2);
        assertEquals((int) dto.getHits(), 5);
        assertEquals((int) dto.getHitsLost(), 2);
        assertEquals((int) dto.getUntieHits(), 1);
        assertEquals((int) dto.getDuelsDone(), 4);
        assertEquals((int) dto.getWonFights(), 2);
        assertEquals((int) dto.getDrawFights(), 1);
        assertEquals((int) dto.getTotalFights(), 3);
        assertTrue(dto.isCountNotOver());
    }

    @Test
    public void shouldProduceToStringWithCompetitor() {
        final ParticipantDTO participant = new ParticipantDTO();
        participant.setName("Taro");
        participant.setLastname("Yamada");

        final ScoreOfCompetitorDTO dto = new ScoreOfCompetitorDTO(participant, false);
        dto.setWonDuels(2);
        dto.setDrawDuels(1);
        dto.setHits(3);

        final String result = dto.toString();
        assertNotNull(result);
        assertTrue(result.contains("D:2/1"));
        assertTrue(result.contains("H:3"));
    }
}

