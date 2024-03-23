package com.softwaremagico.kt.pdf.controller;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantImageController;
import com.softwaremagico.kt.core.controller.QrController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.TournamentImageController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.controller.models.QrCodeDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.controller.models.TournamentImageDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.exceptions.NoContentException;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.pdf.accreditations.TournamentAccreditationCards;
import com.softwaremagico.kt.pdf.diplomas.DiplomaPDF;
import com.softwaremagico.kt.pdf.lists.CompetitorsScoreList;
import com.softwaremagico.kt.pdf.lists.FightSummary;
import com.softwaremagico.kt.pdf.lists.FightsList;
import com.softwaremagico.kt.pdf.lists.GroupList;
import com.softwaremagico.kt.pdf.lists.RoleList;
import com.softwaremagico.kt.pdf.lists.TeamList;
import com.softwaremagico.kt.pdf.lists.TeamsScoreList;
import com.softwaremagico.kt.pdf.qr.TournamentQr;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class PdfController {
    private static final float DEFAULT_NAME_POSITION = 0.5f;
    private final MessageSource messageSource;

    private final RoleController roleController;

    private final TeamController teamController;

    private final GroupController groupController;

    private final TournamentImageController tournamentImageController;

    private final ParticipantImageController participantImageController;

    private final TournamentExtraPropertyController tournamentExtraPropertyController;

    private final ParticipantProvider participantProvider;
    private final ParticipantConverter participantConverter;

    private final QrController qrController;

    public PdfController(MessageSource messageSource, RoleController roleController, TeamController teamController, GroupController groupController,
                         TournamentImageController tournamentImageController, ParticipantImageController participantImageController,
                         TournamentExtraPropertyController tournamentExtraPropertyController, ParticipantProvider participantProvider,
                         ParticipantConverter participantConverter, QrController qrController) {
        this.messageSource = messageSource;
        this.roleController = roleController;
        this.teamController = teamController;
        this.groupController = groupController;
        this.tournamentImageController = tournamentImageController;
        this.participantImageController = participantImageController;
        this.tournamentExtraPropertyController = tournamentExtraPropertyController;
        this.participantProvider = participantProvider;
        this.participantConverter = participantConverter;
        this.qrController = qrController;
    }

    public CompetitorsScoreList generateCompetitorsScoreList(Locale locale, TournamentDTO tournament, List<ScoreOfCompetitorDTO> competitorTopTen) {
        return new CompetitorsScoreList(messageSource, locale, tournament, competitorTopTen, true);
    }

    public TeamsScoreList generateTeamsScoreList(Locale locale, TournamentDTO tournament, List<ScoreOfTeamDTO> teamsTopTen) {
        return new TeamsScoreList(messageSource, locale, tournament, teamsTopTen);
    }

    public RoleList generateClubList(Locale locale, TournamentDTO tournamentDTO) {
        final List<RoleDTO> roles = roleController.get(tournamentDTO);

        //Gets all participants with clubs.
        final List<ParticipantDTO> participants = participantConverter.convertAll(participantProvider
                .findByIdIn(roles.stream().map(roleDTO -> roleDTO.getParticipant().getId())
                        .collect(Collectors.toSet())).stream().map(ParticipantConverterRequest::new).toList());

        final Map<Integer, ParticipantDTO> participantsById = participants.stream()
                .collect(Collectors.toMap(ParticipantDTO::getId, Function.identity()));

        final Map<ClubDTO, List<RoleDTO>> rolesByClub = roles.stream().collect(
                Collectors.groupingBy(roleDTO -> participantsById.get(roleDTO.getParticipant().getId()).getClub())
        );
        return new RoleList(messageSource, locale, tournamentDTO, rolesByClub);
    }

    public TeamList generateTeamList(TournamentDTO tournamentDTO) {
        final List<TeamDTO> teams = teamController.getAllByTournament(tournamentDTO, null);
        teams.sort(Comparator.comparing(TeamDTO::getName));
        return new TeamList(tournamentDTO, teams);
    }

    public GroupList generateGroupList(Locale locale, TournamentDTO tournamentDTO) {
        return new GroupList(messageSource, locale, tournamentDTO, groupController.get(tournamentDTO));
    }

    public FightsList generateFightsList(Locale locale, TournamentDTO tournamentDTO) {
        return new FightsList(messageSource, locale, tournamentDTO, groupController.get(tournamentDTO));
    }

    public FightSummary generateFightsSummaryList(Locale locale, TournamentDTO tournamentDTO) {
        return new FightSummary(messageSource, locale, tournamentDTO, groupController.get(tournamentDTO), null);
    }

    public TournamentAccreditationCards generateTournamentAccreditations(Locale locale, TournamentDTO tournamentDTO, Boolean onlyNews,
                                                                         String username, RoleType... roleTypes) throws NoContentException {
        final List<RoleDTO> roleDTOS = roleController.getForAccreditations(tournamentDTO, onlyNews,
                roleTypes != null ? Arrays.asList(roleTypes) : new ArrayList<>());
        if (roleDTOS.isEmpty()) {
            throw new NoContentException(this.getClass(), "No roles matching this criteria are found");
        }
        final TournamentImageDTO accreditationBackground = tournamentImageController.get(tournamentDTO, TournamentImageType.ACCREDITATION);
        final TournamentImageDTO banner = tournamentImageController.get(tournamentDTO, TournamentImageType.BANNER);
        final TournamentImageDTO defaultPhoto = tournamentImageController.get(tournamentDTO, TournamentImageType.PHOTO);
        final List<ParticipantDTO> participantDTOS = roleDTOS.stream().map(RoleDTO::getParticipant).toList();
        final List<ParticipantImageDTO> participantImageDTOS = participantImageController.get(participantDTOS);
        final Map<ParticipantDTO, ParticipantImageDTO> participantImages = participantImageDTOS.stream()
                .collect(Collectors.toMap(ParticipantImageDTO::getParticipant, Function.identity()));
        try {
            return new TournamentAccreditationCards(messageSource, locale, tournamentDTO, roleDTOS.stream()
                    .collect(Collectors.toMap(RoleDTO::getParticipant, Function.identity())), participantImages,
                    banner != null ? banner.getData() : null,
                    accreditationBackground != null ? accreditationBackground.getData() : null,
                    defaultPhoto != null ? defaultPhoto.getData() : null);
        } finally {
            roleDTOS.forEach(roleDTO -> roleDTO.setAccreditationPrinted(true));
            roleController.updateAll(roleDTOS, username);
        }
    }

    public TournamentAccreditationCards generateTournamentAccreditations(Locale locale, TournamentDTO tournamentDTO,
                                                                         ParticipantDTO participantDTO, RoleType type, String username) {
        if (type == null) {
            type = RoleType.COMPETITOR;
        }
        return generateTournamentAccreditations(locale, tournamentDTO, participantDTO, new RoleDTO(tournamentDTO, participantDTO, type), username);
    }

    public TournamentAccreditationCards generateTournamentAccreditations(Locale locale, TournamentDTO tournamentDTO,
                                                                         ParticipantDTO participantDTO, RoleDTO roleDTO, String username) {
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
        try {
            return new TournamentAccreditationCards(messageSource, locale, tournamentDTO, competitorsRoles, participantImages,
                    banner != null ? banner.getData() : null,
                    accreditationBackground != null ? accreditationBackground.getData() : null,
                    defaultPhoto != null ? defaultPhoto.getData() : null);
        } finally {
            if (roleDTO.getId() != null) {
                roleDTO.setAccreditationPrinted(true);
                roleController.update(roleDTO, username);
            }
        }
    }

    public DiplomaPDF generateTournamentDiplomas(TournamentDTO tournamentDTO, Boolean onlyNews, String username, RoleType... roleTypes)
            throws NoContentException {
        final List<RoleDTO> roleDTOS = roleController.getForDiplomas(tournamentDTO, onlyNews,
                roleTypes != null ? Arrays.asList(roleTypes) : new ArrayList<>());
        if (roleDTOS.isEmpty()) {
            throw new NoContentException(this.getClass(), "No roles matching this criteria are found");
        }
        final TournamentImageDTO diploma = tournamentImageController.get(tournamentDTO, TournamentImageType.DIPLOMA);
        final List<ParticipantDTO> participantDTOS = roleDTOS.stream().map(RoleDTO::getParticipant).toList();
        try {
            return new DiplomaPDF(participantDTOS, diploma != null ? diploma.getData() : null, getNamePosition(tournamentDTO));
        } finally {
            roleDTOS.forEach(roleDTO -> roleDTO.setDiplomaPrinted(true));
            roleController.updateAll(roleDTOS, username);
        }
    }

    public DiplomaPDF generateTournamentDiploma(TournamentDTO tournamentDTO, ParticipantDTO participantDTO) {
        final TournamentImageDTO diploma = tournamentImageController.get(tournamentDTO, TournamentImageType.DIPLOMA);
        return new DiplomaPDF(Collections.singletonList(participantDTO), diploma != null ? diploma.getData() : null, getNamePosition(tournamentDTO));
    }

    private float getNamePosition(TournamentDTO tournamentDTO) {
        final TournamentExtraPropertyDTO tournamentExtraPropertyDTO = tournamentExtraPropertyController
                .getByTournamentAndProperty(tournamentDTO.getId(), TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT);
        if (tournamentExtraPropertyDTO == null) {
            return DEFAULT_NAME_POSITION;
        }
        try {
            return Float.parseFloat(tournamentExtraPropertyDTO.getPropertyValue());
        } catch (Exception e) {
            return DEFAULT_NAME_POSITION;
        }
    }

    public TournamentQr generateTournamentQr(Locale locale, Integer tournamentId) {
        final QrCodeDTO qrCodeDTO = qrController.generateGuestQrCodeForTournamentFights(tournamentId);
        return new TournamentQr(messageSource, locale, qrCodeDTO.getTournament(), qrCodeDTO.getData(), null);
    }
}
