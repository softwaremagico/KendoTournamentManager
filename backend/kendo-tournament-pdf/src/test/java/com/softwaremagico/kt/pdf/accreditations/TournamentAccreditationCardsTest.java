package com.softwaremagico.kt.pdf.accreditations;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.context.MessageSource;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"listsUnitTests"})
public class TournamentAccreditationCardsTest {

    private MessageSource mockMessageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    private byte[] validImageBytes() throws Exception {
        return getClass().getResourceAsStream("/kendo-tournament-manager-logo.png").readAllBytes();
    }

    @Test
    public void invalidImageAssets_expectCaughtExceptionsAndNullImages() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participant = new ParticipantDTO("1", "Name", "Lastname", club);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        final Map<ParticipantDTO, RoleDTO> competitorsRoles = new HashMap<>();
        competitorsRoles.put(participant, role);

        final byte[] invalidBytes = "not-an-image".getBytes(StandardCharsets.UTF_8);
        final TournamentAccreditationCards.ImageAssets imageAssets = new TournamentAccreditationCards.ImageAssets(invalidBytes, invalidBytes, invalidBytes);

        final TournamentAccreditationCards cards = new TournamentAccreditationCards(mockMessageSource(), Locale.getDefault(), tournament,
                competitorsRoles, new HashMap<>(), imageAssets);

        cards.generate();
    }

    @Test
    public void validBackgroundImageAndInvalidParticipantImage_expectFallbackToDefault() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participant = new ParticipantDTO("1", "Name", "Lastname", club);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        final Map<ParticipantDTO, RoleDTO> competitorsRoles = new HashMap<>();
        competitorsRoles.put(participant, role);

        final ParticipantImageDTO participantImageDTO = new ParticipantImageDTO();
        participantImageDTO.setParticipant(participant);
        participantImageDTO.setData("invalid".getBytes(StandardCharsets.UTF_8));
        final Map<ParticipantDTO, ParticipantImageDTO> participantImages = new HashMap<>();
        participantImages.put(participant, participantImageDTO);

        final byte[] validBytes = validImageBytes();
        final TournamentAccreditationCards.ImageAssets imageAssets = new TournamentAccreditationCards.ImageAssets(validBytes, validBytes, validBytes);

        final TournamentAccreditationCards cards = new TournamentAccreditationCards(mockMessageSource(), Locale.getDefault(), tournament,
                competitorsRoles, participantImages, imageAssets);

        cards.generate();
    }

    @Test
    public void noBackgroundImage_expectDefaultResourceBranch() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participant = new ParticipantDTO("1", "Name", "Lastname", club);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        final Map<ParticipantDTO, RoleDTO> competitorsRoles = new HashMap<>();
        competitorsRoles.put(participant, role);

        final TournamentAccreditationCards.ImageAssets imageAssets = new TournamentAccreditationCards.ImageAssets(null, null, null);

        final TournamentAccreditationCards cards = new TournamentAccreditationCards(mockMessageSource(), Locale.getDefault(), tournament,
                competitorsRoles, new HashMap<>(), imageAssets);

        cards.generate();
    }
}



