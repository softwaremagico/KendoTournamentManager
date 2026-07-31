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

import com.lowagie.text.Image;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.context.MessageSource;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"listsUnitTests"})
public class ParticipantAccreditationCardTest {

    private MessageSource mockMessageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    private Image logoImage() throws Exception {
        return Image.getInstance(getClass().getResourceAsStream("/kendo-tournament-manager-logo.png").readAllBytes());
    }

    private ParticipantDTO participant(ClubDTO club) {
        return new ParticipantDTO("1", "Name", "Lastname", club);
    }

    @Test
    public void fullGeneration_withValidData_expectNoException() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participant = participant(club);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        final ParticipantAccreditationCard card = new ParticipantAccreditationCard(mockMessageSource(), Locale.getDefault(), tournament,
                participant, role, logoImage(), logoImage());

        card.generate();
    }

    @Test
    public void participantWithoutClubAndId_expectDefaultValues() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = participant(null);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.REFEREE);

        final ParticipantAccreditationCard card = new ParticipantAccreditationCard(mockMessageSource(), Locale.getDefault(), tournament,
                participant, role, null, null);

        card.pageTable(500, 700);
    }

    @Test
    public void volunteerPressOrganizerRoles_expectColorBranches() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");

        for (final RoleType roleType : new RoleType[]{RoleType.VOLUNTEER, RoleType.PRESS, RoleType.ORGANIZER}) {
            final ParticipantDTO participant = participant(club);
            final RoleDTO role = new RoleDTO(tournament, participant, roleType);
            final ParticipantAccreditationCard card = new ParticipantAccreditationCard(mockMessageSource(), Locale.getDefault(), tournament,
                    participant, role, null, null);
            card.pageTable(500, 700);
        }
    }

    @Test
    public void nullTournament_expectSignatureFallbackBranch() throws Exception {
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participant = participant(club);
        final RoleDTO role = new RoleDTO(null, participant, RoleType.COMPETITOR);

        final ParticipantAccreditationCard card = new ParticipantAccreditationCard(mockMessageSource(), Locale.getDefault(), null,
                participant, role, null, null);

        card.pageTable(500, 700);
    }
}


