package com.softwaremagico.kt.pdf.controller;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantImageController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TournamentImageController;
import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.pdf.accreditations.TournamentAccreditationCards;
import com.softwaremagico.kt.pdf.diplomas.DiplomaPDF;
import com.softwaremagico.kt.pdf.lists.*;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class PdfController {
    private final MessageSource messageSource;

    private final RoleController roleController;

    private final GroupController groupController;

    private final TournamentImageController tournamentImageController;

    private final ParticipantImageController participantImageController;

    public PdfController(MessageSource messageSource, RoleController roleController, GroupController groupController,
                         TournamentImageController tournamentImageController, ParticipantImageController participantImageController) {
        this.messageSource = messageSource;
        this.roleController = roleController;
        this.groupController = groupController;
        this.tournamentImageController = tournamentImageController;
        this.participantImageController = participantImageController;
    }

    public CompetitorsScoreList generateCompetitorsScoreList(Locale locale, TournamentDTO tournament, List<ScoreOfCompetitor> competitorTopTen) {
        return new CompetitorsScoreList(messageSource, locale, tournament, competitorTopTen);
    }

    public TeamsScoreList generateTeamsScoreList(Locale locale, TournamentDTO tournament, List<ScoreOfTeam> teamsTopTen) {
        return new TeamsScoreList(messageSource, locale, tournament, teamsTopTen);
    }

    public RoleList generateClubList(Locale locale, TournamentDTO tournamentDTO) {
        final List<RoleDTO> roles = roleController.get(tournamentDTO);
        final Map<ClubDTO, List<RoleDTO>> rolesByClub = roles.stream().collect(
                Collectors.groupingBy(roleDTO -> roleDTO.getParticipant().getClub())
        );
        return new RoleList(messageSource, locale, tournamentDTO, rolesByClub);
    }

    public FightsList generateFightsList(Locale locale, TournamentDTO tournamentDTO) {
        return new FightsList(messageSource, locale, tournamentDTO, groupController.get(tournamentDTO));
    }

    public FightSummaryPDF generateFightsSummaryList(Locale locale, TournamentDTO tournamentDTO) {
        return new FightSummaryPDF(messageSource, locale, tournamentDTO, groupController.get(tournamentDTO), null);
    }

    public TournamentAccreditationCards generateTournamentAccreditations(Locale locale, TournamentDTO tournamentDTO) {
        final List<RoleDTO> roleDTOS = roleController.get(tournamentDTO);
        final TournamentImageDTO accreditationBackground = tournamentImageController.get(tournamentDTO, TournamentImageType.ACCREDITATION);
        final TournamentImageDTO banner = tournamentImageController.get(tournamentDTO, TournamentImageType.BANNER);
        final TournamentImageDTO defaultPhoto = tournamentImageController.get(tournamentDTO, TournamentImageType.PHOTO);
        final List<ParticipantDTO> participantDTOS = roleDTOS.stream().map(RoleDTO::getParticipant).collect(Collectors.toList());
        final List<ParticipantImageDTO> participantImageDTOS = participantImageController.get(participantDTOS);
        final Map<ParticipantDTO, ParticipantImageDTO> participantImages = participantImageDTOS.stream()
                .collect(Collectors.toMap(ParticipantImageDTO::getParticipant, Function.identity()));
        return new TournamentAccreditationCards(messageSource, locale, tournamentDTO, roleDTOS.stream()
                .collect(Collectors.toMap(RoleDTO::getParticipant, Function.identity())), participantImages,
                banner != null ? banner.getData() : null,
                accreditationBackground != null ? accreditationBackground.getData() : null,
                defaultPhoto != null ? defaultPhoto.getData() : null);
    }

    public TournamentAccreditationCards generateTournamentAccreditations(Locale locale, TournamentDTO tournamentDTO,
                                                                         ParticipantDTO participantDTO, RoleType type) {
        if (type == null) {
            type = RoleType.COMPETITOR;
        }
        return generateTournamentAccreditations(locale, tournamentDTO, participantDTO, new RoleDTO(tournamentDTO, participantDTO, type));
    }

    public TournamentAccreditationCards generateTournamentAccreditations(Locale locale, TournamentDTO tournamentDTO,
                                                                         ParticipantDTO participantDTO, RoleDTO roleDTO) {
        final TournamentImageDTO accreditationBackground = tournamentImageController.get(tournamentDTO, TournamentImageType.ACCREDITATION);
        final TournamentImageDTO banner = tournamentImageController.get(tournamentDTO, TournamentImageType.BANNER);
        final TournamentImageDTO defaultPhoto = tournamentImageController.get(tournamentDTO, TournamentImageType.PHOTO);
        final List<ParticipantDTO> participantDTOS = Collections.singletonList(participantDTO);
        final Map<ParticipantDTO, ParticipantImageDTO> participantImages;
        if (participantDTO.getId() != null) {
            final List<ParticipantImageDTO> participantImageDTOS = participantImageController.get(participantDTOS);
            participantImages = participantImageDTOS.stream()
                    .collect(Collectors.toMap(ParticipantImageDTO::getParticipant, Function.identity()));
        } else {
            participantImages = new HashMap<>();
        }
        final Map<ParticipantDTO, RoleDTO> competitorsRoles = new HashMap<>();
        competitorsRoles.put(participantDTO, roleDTO);
        return new TournamentAccreditationCards(messageSource, locale, tournamentDTO, competitorsRoles, participantImages,
                banner != null ? banner.getData() : null,
                accreditationBackground != null ? accreditationBackground.getData() : null,
                defaultPhoto != null ? defaultPhoto.getData() : null);
    }

    public DiplomaPDF generateTournamentDiplomas(TournamentDTO tournamentDTO) {
        final List<RoleDTO> roleDTOS = roleController.get(tournamentDTO);
        final TournamentImageDTO diploma = tournamentImageController.get(tournamentDTO, TournamentImageType.DIPLOMA);
        final List<ParticipantDTO> participantDTOS = roleDTOS.stream().map(RoleDTO::getParticipant).collect(Collectors.toList());
        return new DiplomaPDF(participantDTOS, diploma != null ? diploma.getData() : null);
    }

    public DiplomaPDF generateTournamentDiplomas(TournamentDTO tournamentDTO, ParticipantDTO participantDTO) {
        final TournamentImageDTO diploma = tournamentImageController.get(tournamentDTO, TournamentImageType.DIPLOMA);
        return new DiplomaPDF(Collections.singletonList(participantDTO), diploma != null ? diploma.getData() : null);
    }
}
