package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ClubConverter;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.ParticipantReducedConverter;
import com.softwaremagico.kt.core.converters.ScoreOfCompetitorConverter;
import com.softwaremagico.kt.core.converters.ScoreOfTeamConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.core.tournaments.BubbleSortTournamentHandler;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@Test(groups = "scoreTests")
public class RankingControllerTest {

	@Mock
	private GroupProvider groupProvider;
	@Mock
	private GroupConverter groupConverter;
	@Mock
	private TournamentConverter tournamentConverter;
	@Mock
	private FightConverter fightConverter;
	@Mock
	private TeamConverter teamConverter;
	@Mock
	private DuelConverter duelConverter;
	@Mock
	private ParticipantReducedConverter participantReducedConverter;
	@Mock
	private ParticipantConverter participantConverter;
	@Mock
	private RankingProvider rankingProvider;
	@Mock
	private ScoreOfCompetitorConverter scoreOfCompetitorConverter;
	@Mock
	private ScoreOfTeamConverter scoreOfTeamConverter;
	@Mock
	private ClubProvider clubProvider;
	@Mock
	private ParticipantProvider participantProvider;
	@Mock
	private ClubConverter clubConverter;
	@Mock
	private RoleProvider roleProvider;
	@Mock
	private TournamentProvider tournamentProvider;
	@Mock
	private BubbleSortTournamentHandler bubbleSortTournamentHandler;
	@Mock
	private SenbatsuTournamentHandler senbatsuTournamentHandler;

	private RankingController controller;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new RankingController(groupProvider, groupConverter, tournamentConverter, fightConverter,
				teamConverter, duelConverter, participantReducedConverter, participantConverter, rankingProvider,
				scoreOfCompetitorConverter, scoreOfTeamConverter, clubProvider, participantProvider, clubConverter,
				roleProvider, tournamentProvider, bubbleSortTournamentHandler, senbatsuTournamentHandler);
	}

	@Test
	public void shouldReturnBubbleSortTeamsRankingFromTournament() {
		final Tournament tournament = tournament(1, TournamentType.BUBBLE_SORT);
		final Team team1 = team(1, "One", tournament);
		final Team team2 = team(2, "Two", tournament);
		final ScoreOfTeamDTO dto1 = scoreOfTeamDTO(teamDTO(1, "One", tournamentDTO(1, TournamentType.BUBBLE_SORT)));
		final ScoreOfTeamDTO dto2 = scoreOfTeamDTO(teamDTO(2, "Two", tournamentDTO(1, TournamentType.BUBBLE_SORT)));

		when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(rankingProvider.getTeamsScoreRankingFromTournament(1))
				.thenReturn(List.of(mock(ScoreOfTeam.class), mock(ScoreOfTeam.class)));
		when(scoreOfTeamConverter.convertAll(any())).thenReturn(List.of(dto1, dto2));
		when(bubbleSortTournamentHandler.getTeamsOrderedByRanks(tournament))
				.thenReturn(new ArrayList<>(List.of(team1, team2)));

		final List<ScoreOfTeamDTO> result = controller.getTeamsScoreRankingFromTournament(1);

		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getTeam().getId(), Integer.valueOf(2));
		assertEquals(result.get(1).getTeam().getId(), Integer.valueOf(1));
		assertEquals(result.get(0).getSortingIndex(), Integer.valueOf(0));
		assertEquals(result.get(1).getSortingIndex(), Integer.valueOf(1));
	}

	@Test
	public void shouldReturnFirstTeamDrawSetWhenAnyWinningPositionHasTie() {
		final GroupDTO groupDTO = new GroupDTO();
		final Group group = new Group();
		final TeamDTO team1 = teamDTO(1, "A", tournamentDTO(1, TournamentType.LEAGUE));
		final TeamDTO team2 = teamDTO(2, "B", tournamentDTO(1, TournamentType.LEAGUE));
		final TeamDTO team3 = teamDTO(3, "C", tournamentDTO(1, TournamentType.LEAGUE));

		when(groupConverter.reverse(groupDTO)).thenReturn(group);
		final Map<Integer, List<Team>> byPosition = new java.util.LinkedHashMap<>();
		byPosition.put(0, List.of(team(1, "A", tournament(1, TournamentType.LEAGUE))));
		byPosition.put(1, List.of(team(2, "B", tournament(1, TournamentType.LEAGUE)),
				team(3, "C", tournament(1, TournamentType.LEAGUE))));
		when(rankingProvider.getTeamsByPosition(group)).thenReturn(byPosition);
		when(teamConverter.convertAll(any())).thenReturn(List.of(team1), List.of(team2, team3));

		final List<TeamDTO> drawTeams = controller.getFirstTeamsWithDrawScore(groupDTO, 3);

		assertEquals(drawTeams.size(), 2);
		assertEquals(drawTeams.get(0).getId(), Integer.valueOf(2));
		assertEquals(drawTeams.get(1).getId(), Integer.valueOf(3));
	}

	@Test
	public void shouldReturnCompetitorsGlobalRankingWithClubContext() {
		final ClubDTO clubDTO = clubDTO(11, "ClubA");
		final ParticipantDTO participantDTO = participantDTO(7, "Name", "Lastname", clubDTO);
		final Club club = club(11, "ClubA");
		final Participant participant = participant(7, club);
		final ScoreOfCompetitor score = new ScoreOfCompetitor();
		score.setCompetitor(participant);
		final ScoreOfCompetitorDTO dto = new ScoreOfCompetitorDTO();
		dto.setCompetitor(participantDTO);

		when(rankingProvider.getCompetitorsGlobalScoreRanking(any(), eq(ScoreType.DEFAULT), eq(30)))
				.thenReturn(List.of(score));
		when(scoreOfCompetitorConverter.convertAll(any())).thenReturn(List.of(dto));

		final List<ScoreOfCompetitorDTO> result = controller.getCompetitorsGlobalScoreRanking(List.of(participantDTO),
				ScoreType.DEFAULT, 30);

		assertEquals(result.size(), 1);
		assertSame(result.get(0), dto);
		assertEquals(result.get(0).getCompetitor().getClub().getId(), Integer.valueOf(11));
	}

	@Test
	public void shouldReturnNullForMissingCompetitorAndEmptyLists() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final GroupDTO groupDTO = new GroupDTO();
		groupDTO.setTournament(tournamentDTO);
		groupDTO.setTeams(new ArrayList<>());
		groupDTO.setFights(new ArrayList<>());
		groupDTO.setUnties(new ArrayList<>());

		when(participantReducedConverter.convert(any())).thenReturn(null);
		when(groupConverter.reverse(groupDTO)).thenReturn(new Group());
		when(rankingProvider.getTeamsByPosition(any())).thenReturn(Map.of());
		when(rankingProvider.getTeamsRanking(any(Group.class))).thenReturn(List.of());
		when(rankingProvider.getCompetitor(any(), eq(0))).thenReturn(null);

		assertTrue(controller.getTeamsScoreRanking((GroupDTO) null).isEmpty());
		assertNull(controller.getTeam(groupDTO, 3));
		assertNull(controller.getCompetitorRanking(null));
		assertFalse(controller.getTeamsByPosition(groupDTO).containsKey(0));
		assertNull(controller.getCompetitor(groupDTO, 0));
	}

	private Tournament tournament(int id, TournamentType type) {
		final Tournament tournament = new Tournament("Tournament", 1, 1, type, "tester");
		tournament.setId(id);
		return tournament;
	}

	private TournamentDTO tournamentDTO(int id, TournamentType type) {
		final TournamentDTO tournamentDTO = new TournamentDTO("Tournament", 1, 1, type);
		tournamentDTO.setId(id);
		return tournamentDTO;
	}

	private Team team(int id, String name, Tournament tournament) {
		final Team team = new Team(name, tournament);
		team.setId(id);
		return team;
	}

	private TeamDTO teamDTO(int id, String name, TournamentDTO tournament) {
		final TeamDTO teamDTO = new TeamDTO(name, tournament);
		teamDTO.setId(id);
		return teamDTO;
	}

	private ScoreOfTeamDTO scoreOfTeamDTO(TeamDTO teamDTO) {
		final ScoreOfTeamDTO dto = new ScoreOfTeamDTO();
		dto.setTeam(teamDTO);
		return dto;
	}

	private ClubDTO clubDTO(int id, String name) {
		final ClubDTO clubDTO = new ClubDTO(name, "City");
		clubDTO.setId(id);
		return clubDTO;
	}

	private ParticipantDTO participantDTO(int id, String name, String lastname, ClubDTO club) {
		final ParticipantDTO participantDTO = new ParticipantDTO();
		participantDTO.setId(id);
		participantDTO.setName(name);
		participantDTO.setLastname(lastname);
		participantDTO.setClub(club);
		return participantDTO;
	}

	private Club club(int id, String name) {
		final Club club = new Club(name, "ES", "City");
		club.setId(id);
		return club;
	}

	private Participant participant(int id, Club club) {
		final Participant participant = new Participant();
		participant.setId(id);
		participant.setClub(club);
		return participant;
	}
}
