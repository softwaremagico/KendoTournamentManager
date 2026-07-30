package com.softwaremagico.kt.core.converters;

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

import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.models.ScoreOfTeamConverterRequest;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(groups = "scoreOfTeamConverter")
public class ScoreOfTeamConverterTest {

    @Mock
    private TeamConverter mockTeamConverter;

    @Mock
    private FightConverter mockFightConverter;

    @Mock
    private DuelConverter mockDuelConverter;

    @Mock
    private TournamentConverter mockTournamentConverter;

    @Mock
    private Team mockTeam;

    @Mock
    private Tournament mockTournament;

    @Mock
    private Fight mockFight;

    @Mock
    private Duel mockDuel;

    private ScoreOfTeamConverter converter;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new ScoreOfTeamConverter(mockTeamConverter, mockFightConverter, mockDuelConverter, mockTournamentConverter);
        when(mockTeam.getTournament()).thenReturn(mockTournament);
    }

    @Test
    public void convert_expectDelegationToAllConverters() {
        final ScoreOfTeam scoreOfTeam = new ScoreOfTeam();
        scoreOfTeam.setTeam(mockTeam);
        scoreOfTeam.setFights(List.of(mockFight));
        scoreOfTeam.setUnties(List.of(mockDuel));
        scoreOfTeam.setHits(10);
        scoreOfTeam.setHitsLost(4);

        final TournamentDTO tournamentDTO = new TournamentDTO();
        final TeamDTO teamDTO = new TeamDTO();
        final FightDTO fightDTO = new FightDTO();

        when(mockTournamentConverter.convertElement(any())).thenReturn(tournamentDTO);
        when(mockTeamConverter.convert(any())).thenReturn(teamDTO);
        when(mockFightConverter.convertAll(any())).thenReturn(List.of(fightDTO));
        when(mockDuelConverter.convertAll(any())).thenReturn(List.of());

        final ScoreOfTeamDTO result = converter.convert(new ScoreOfTeamConverterRequest(scoreOfTeam));

        assertSame(result.getTeam(), teamDTO);
        assertEquals(result.getFights(), List.of(fightDTO));
        assertTrue(result.getUnties().isEmpty());
        assertEquals(result.getHits().intValue(), 10);
        assertEquals(result.getHitsLost().intValue(), 4);
    }

    @Test
    public void reverse_withNull_expectNull() {
        assertNull(converter.reverse(null));
    }

    @Test
    public void reverse_expectFightsAndUntiesReversed() {
        final ScoreOfTeamDTO dto = new ScoreOfTeamDTO();
        final TeamDTO teamDTO = new TeamDTO();
        final FightDTO fightDTO = new FightDTO();
        dto.setTeam(teamDTO);
        dto.setFights(List.of(fightDTO));
        dto.setUnties(List.of());
        dto.setHits(6);

        when(mockTeamConverter.reverse(teamDTO)).thenReturn(mockTeam);
        when(mockFightConverter.reverse(fightDTO)).thenReturn(mockFight);

        final ScoreOfTeam result = converter.reverse(dto);

        assertSame(result.getTeam(), mockTeam);
        assertEquals(result.getFights(), List.of(mockFight));
        assertTrue(result.getUnties().isEmpty());
        assertEquals(result.getHits().intValue(), 6);
    }
}

