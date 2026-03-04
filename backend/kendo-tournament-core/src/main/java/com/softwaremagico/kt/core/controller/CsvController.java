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
import com.softwaremagico.kt.core.controller.models.GroupLinkDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.converters.ClubConverter;
import com.softwaremagico.kt.core.converters.GroupLinkConverter;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.core.converters.models.GroupLinkConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.csv.ClubCsv;
import com.softwaremagico.kt.core.csv.GroupLinkCsv;
import com.softwaremagico.kt.core.csv.ParticipantCsv;
import com.softwaremagico.kt.core.csv.TeamCsv;
import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.GroupLinkProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class CsvController {

    private final ClubCsv clubCsv;
    private final ClubProvider clubProvider;
    private final ClubConverter clubConverter;

    private final ParticipantCsv participantCsv;
    private final ParticipantProvider participantProvider;
    private final ParticipantConverter participantConverter;

    private final TeamCsv teamCsv;
    private final TeamProvider teamProvider;
    private final TeamConverter teamConverter;

    private final GroupLinkCsv groupLinkCsv;
    private final GroupLinkProvider groupLinkProvider;
    private final GroupLinkConverter groupLinkConverter;

    private final RoleProvider roleProvider;
    private final TournamentProvider tournamentProvider;
    private final GroupProvider groupProvider;

    public CsvController(ClubCsv clubCsv, ClubProvider clubProvider, ClubConverter clubConverter,
                         ParticipantCsv participantCsv, ParticipantProvider participantProvider, ParticipantConverter participantConverter,
                         TeamCsv teamCsv, TeamProvider teamProvider, TeamConverter teamConverter, GroupLinkCsv groupLinkCsv,
                         GroupLinkProvider groupLinkProvider, GroupLinkConverter groupLinkConverter, RoleProvider roleProvider,
                         TournamentProvider tournamentProvider, GroupProvider groupProvider) {
        this.clubCsv = clubCsv;
        this.clubProvider = clubProvider;
        this.clubConverter = clubConverter;
        this.participantCsv = participantCsv;
        this.participantProvider = participantProvider;
        this.participantConverter = participantConverter;
        this.teamCsv = teamCsv;
        this.teamProvider = teamProvider;
        this.teamConverter = teamConverter;
        this.groupLinkCsv = groupLinkCsv;
        this.groupLinkProvider = groupLinkProvider;
        this.groupLinkConverter = groupLinkConverter;
        this.roleProvider = roleProvider;
        this.tournamentProvider = tournamentProvider;
        this.groupProvider = groupProvider;
    }


    public List<ClubDTO> addClubs(String csvContent, String uploadedBy) {
        final List<Club> clubs = clubCsv.readCSV(csvContent);
        final List<ClubDTO> failedClubs = new ArrayList<>();
        for (Club club : clubs) {
            try {
                if (club.getName() != null && club.getCity() != null) {
                    final Optional<Club> storedClub = clubProvider.findBy(club.getName(), club.getCity());
                    if (storedClub.isPresent()) {
                        KendoTournamentLogger.warning(this.getClass(), "Club '" + club.getName() + "' from '"
                                + club.getCity() + "' already exists. Will be updated.");
                        club.setId(storedClub.get().getId());
                        club.setUpdatedBy(uploadedBy);
                    } else {
                        club.setCreatedBy(uploadedBy);
                    }
                    clubProvider.save(club);
                } else {
                    KendoTournamentLogger.warning(this.getClass(), "Club with invalid name and/or city.");
                    failedClubs.add(clubConverter.convert(new ClubConverterRequest(club)));
                }
            } catch (Exception e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                failedClubs.add(clubConverter.convert(new ClubConverterRequest(club)));
            }
        }
        return failedClubs;
    }


    public List<ParticipantDTO> addParticipants(String csvContent, String uploadedBy) {
        final List<Participant> participants = participantCsv.readCSV(csvContent);
        final List<ParticipantDTO> failedParticipants = new ArrayList<>();
        for (Participant participant : participants) {
            try {
                if (participantProvider.findByIdCard(participant.getIdCard()).isPresent()) {
                    KendoTournamentLogger.severe(this.getClass().getName(), "Participant '" + participant.getIdCard() + "' with name '"
                            + participant.getName() + " " + participant.getLastname() + "' already exists.");
                    participant.setUpdatedBy(uploadedBy);
                    failedParticipants.add(participantConverter.convert(new ParticipantConverterRequest(participant)));
                } else {
                    participant.setCreatedBy(uploadedBy);
                    participantProvider.save(participant);
                }
            } catch (Exception e) {
                KendoTournamentLogger.severe(this.getClass().getName(), "Error when inserting '" + participant + "'.");
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                failedParticipants.add(participantConverter.convert(new ParticipantConverterRequest(participant)));
            }
        }
        return failedParticipants;
    }


    public List<TeamDTO> addTeams(String csvContent, String uploadedBy) {
        final List<Team> teams = teamCsv.readCSV(csvContent);
        final List<TeamDTO> failedTeams = new ArrayList<>();
        for (Team team : teams) {
            if (team.getTournament() == null) {
                KendoTournamentLogger.severe(this.getClass().getName(), "Team '" + team.getName() + "' has assigned a tournament that does not exists.");
                failedTeams.add(teamConverter.convert(new TeamConverterRequest(team)));
                continue;
            }
            if (team.getMembers().size() > team.getTournament().getTeamSize()) {
                throw new InvalidCsvFieldException(this.getClass(), "Team size is incorrect!", null);
            }
            setTeamMemberRoles(team);
            try {
                final Optional<Team> storedTeam = teamProvider.get(team.getTournament(), team.getName());
                if (storedTeam.isPresent()) {
                    KendoTournamentLogger.warning(this.getClass(), "Team '" + team.getName() + "' already exists on tournament '"
                            + team.getTournament().getName() + "'. Will be updated.");
                    team.setId(storedTeam.get().getId());
                    team.setUpdatedBy(uploadedBy);
                } else {
                    team.setCreatedBy(uploadedBy);
                }
                teamProvider.save(team);
            } catch (Exception e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                failedTeams.add(teamConverter.convert(new TeamConverterRequest(team)));
            }
        }
        return failedTeams;
    }

    private void setTeamMemberRoles(Team team) {
        if (team.getTournament() == null) {
            return;
        }
        //Define roles for team members.
        team.getMembers().forEach(member -> {
            final Role role = roleProvider.get(team.getTournament(), member);
            if (role == null) {
                roleProvider.save(new Role(team.getTournament(), member, RoleType.COMPETITOR));
            } else {
                role.setRoleType(RoleType.COMPETITOR);
                roleProvider.save(role);
            }
        });
    }

    public List<GroupLinkDTO> addGroupLinks(Integer tournamentId, String csvContent, String uploadedBy) {
        final Tournament tournament = tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO));

        //Define groups of the tournament.
        final int totalSourceGroups = groupLinkCsv.getSourceGroupSize(csvContent);
        final int totalDestinationGroups = groupLinkCsv.getDestinationGroupSize(csvContent);
        final int levels = generateCustomGroupTree(tournament, totalSourceGroups, totalDestinationGroups);

        //Assign the links
        groupLinkProvider.deleteByTournament(tournament);
        final List<GroupLink> groupLinks = groupLinkCsv.readCSV(tournament, csvContent);
        final List<GroupLinkDTO> failedGroupLinks = new ArrayList<>();

        //Set the links.
        for (GroupLink groupLink : groupLinks) {
            groupLink.setTournament(tournament);
            groupLink.setUpdatedBy(uploadedBy);
            try {
                groupLinkProvider.save(groupLink);
            } catch (Exception e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                failedGroupLinks.add(groupLinkConverter.convert(new GroupLinkConverterRequest(groupLink)));
            }
        }

        generateInnerLevelLinks(tournament, levels);

        return failedGroupLinks;
    }

    private List<GroupLink> generateInnerLevelLinks(Tournament tournament, int totalLevels) {
        //Update other levels links.
        final List<Group> groupsOfTournament = groupProvider.getGroups(tournament);
        return groupLinkProvider.save(groupLinkProvider.generateLinks(
                groupsOfTournament,
                1, totalLevels, 1));
    }


    private int generateCustomGroupTree(Tournament tournament, int levelZeroSize, int levelOneSize) {
        groupProvider.delete(tournament);

        //Define Level 0
        for (int i = 0; i < levelZeroSize; i++) {
            final Group levelGroup = new Group(tournament, 0, i);
            groupProvider.addGroup(tournament, levelGroup);
        }

        //Define Level 1
        for (int i = 0; i < levelOneSize; i++) {
            final Group levelGroup = new Group(tournament, 1, i);
            groupProvider.addGroup(tournament, levelGroup);
        }

        //Define other levels groups.
        int previousLevelSize = levelOneSize;
        int newLevel = 2;
        while (previousLevelSize > 1) {
            final List<Group> generatedGroups = generateGroupsOnLevel(tournament, previousLevelSize, newLevel);
            previousLevelSize = generatedGroups.size();
            newLevel++;
        }
        return newLevel - 1;
    }

    private List<Group> generateGroupsOnLevel(Tournament tournament, int lastLevelSize, int currentLevel) {
        if (lastLevelSize <= 1) {
            return List.of();
        }

        //On inner tree, always one winner by group.
        final int groupSize = (lastLevelSize + 1) / 2;

        final List<Group> groups = new ArrayList<>();
        for (int i = 0; i < groupSize; i++) {
            final Group levelGroup = new Group(tournament, currentLevel, i);
            groups.add(groupProvider.addGroup(tournament, levelGroup));
        }
        return groups;
    }
}
