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

import com.softwaremagico.kt.core.controller.models.RegistrationClubDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationConfigurationDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationRequestDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationResponseDTO;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.services.RegistrationTeamService;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Domain operations behind the credential-free registration form.
 * <p>
 * The invitation itself is validated by the REST layer, which also sets the
 * tenant before any repository is touched. This controller only accepts
 * tournament ids that came from a validated invitation.
 * </p>
 */
@Controller
public class RegistrationController {

    private static final List<RoleType> PUBLIC_ROLES = List.of(RoleType.COMPETITOR, RoleType.REFEREE);

    private final TournamentProvider tournamentProvider;

    private final ClubProvider clubProvider;

    private final ParticipantProvider participantProvider;

    private final RoleProvider roleProvider;

    private final RegistrationTeamService registrationTeamService;

    @Autowired
    public RegistrationController(TournamentProvider tournamentProvider, ClubProvider clubProvider,
                                  ParticipantProvider participantProvider, RoleProvider roleProvider,
                                  RegistrationTeamService registrationTeamService) {
        this.tournamentProvider = tournamentProvider;
        this.clubProvider = clubProvider;
        this.participantProvider = participantProvider;
        this.roleProvider = roleProvider;
        this.registrationTeamService = registrationTeamService;
    }

    public RegistrationConfigurationDTO getConfiguration(Integer tournamentId) {
        final Tournament tournament = getOpenTournament(tournamentId);
        final RegistrationConfigurationDTO configuration = new RegistrationConfigurationDTO();
        configuration.setTournamentName(tournament.getName());
        configuration.setTeamSize(tournament.getTeamSize());
        configuration.setTeamSelectionEnabled(isTeamSelectionEnabled(tournament));
        configuration.setAvailableRoles(PUBLIC_ROLES);
        configuration.setClubs(clubProvider.getAll().stream().map(this::toPublicClub).toList());
        return configuration;
    }

    @Transactional
    public RegistrationResponseDTO register(Integer tournamentId, RegistrationRequestDTO request, String createdBy) {
        final Tournament tournament = getOpenTournament(tournamentId);
        validate(request);
        final String normalizedIdCard = normalizeIdCard(request.getIdCard());

        // Reuse an existing record by normalized id card. Its personal data is
        // never overwritten by an anonymous registration, and no new club is
        // created just because the form offered one.
        final Optional<Participant> existing = participantProvider.findByIdCard(normalizedIdCard);
        final boolean isExisting = existing.isPresent();
        final Participant participant = existing.orElseGet(() -> {
            final Club club = resolveClub(request, createdBy);
            final Participant newParticipant = new Participant(normalizedIdCard, request.getName(), request.getLastname(), club);
            newParticipant.setCreatedBy(createdBy);
            return participantProvider.save(newParticipant);
        });

        final String teamPreference = isTeamSelectionEnabled(tournament) ? trimToNull(request.getTeamPreference()) : null;
        final List<Role> roles = roleProvider.createOrUpdateRoles(tournament, participant, request.getRoles(), teamPreference, true);
        final String teamName = isTeamSelectionEnabled(tournament)
                ? registrationTeamService.assignParticipant(tournament, participant, teamPreference, createdBy) : null;
        return toResponse(participant, roles, teamName, isExisting);
    }

    private Tournament getOpenTournament(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."));
        if (tournament.isLocked()) {
            throw new ValidateBadRequestException(getClass(), "Registration is closed for this tournament.");
        }
        return tournament;
    }

    private boolean isTeamSelectionEnabled(Tournament tournament) {
        final Integer teamSize = tournament.getTeamSize();
        return teamSize != null && teamSize > 1;
    }

    private void validate(RegistrationRequestDTO request) {
        if (request == null || isBlank(request.getName()) || isBlank(request.getLastname()) || isBlank(request.getIdCard())) {
            throw new ValidateBadRequestException(getClass(), "Participant data is missing.");
        }
        if (request.getRoles() == null || request.getRoles().isEmpty()
                || request.getRoles().stream().anyMatch(role -> role == null || !PUBLIC_ROLES.contains(role))) {
            throw new ValidateBadRequestException(getClass(), "At least one public tournament role is required.");
        }
        if (request.getClubId() == null
                && (isBlank(request.getNewClubName()) || isBlank(request.getNewClubCity()) || isBlank(request.getNewClubCountry()))) {
            throw new ValidateBadRequestException(getClass(), "Select an existing club or enter the new club data.");
        }
    }

    private Club resolveClub(RegistrationRequestDTO request, String createdBy) {
        if (request.getClubId() != null) {
            return clubProvider.get(request.getClubId()).orElseThrow(() ->
                    new ValidateBadRequestException(getClass(), "The selected club does not exist."));
        }
        final Optional<Club> existing = clubProvider.findBy(request.getNewClubName().trim(), request.getNewClubCity().trim());
        if (existing.isPresent()) {
            return existing.orElseThrow();
        }
        final Club club = new Club(request.getNewClubName(), request.getNewClubCountry(), request.getNewClubCity());
        club.setCreatedBy(createdBy);
        return clubProvider.save(club);
    }

    private String normalizeIdCard(String idCard) {
        final Participant participant = new Participant();
        participant.setIdCard(idCard);
        return participant.getIdCard();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private RegistrationClubDTO toPublicClub(Club club) {
        final RegistrationClubDTO dto = new RegistrationClubDTO();
        dto.setId(club.getId());
        dto.setName(club.getName());
        dto.setCity(club.getCity());
        dto.setCountry(club.getCountry());
        return dto;
    }

    private RegistrationResponseDTO toResponse(Participant participant, List<Role> roles, String teamName, boolean existing) {
        final RegistrationResponseDTO response = new RegistrationResponseDTO();
        response.setId(participant.getId());
        response.setName(participant.getName());
        response.setLastname(participant.getLastname());
        response.setExistingParticipant(existing);
        response.setPendingReview(roles.stream().anyMatch(Role::isPendingReview));
        // Only acknowledge the public roles granted by this registration. An
        // existing participant may also hold organizer roles that must not be
        // echoed back to an anonymous caller.
        response.setRoles(roles.stream()
                .map(Role::getRoleType)
                .filter(PUBLIC_ROLES::contains)
                .distinct()
                .toList());
        response.setTeamName(teamName);
        return response;
    }
}
