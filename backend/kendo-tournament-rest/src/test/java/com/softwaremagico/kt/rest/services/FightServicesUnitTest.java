package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.pdf.lists.FightSummary;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

@Test(groups = "restServicesUnit")
public class FightServicesUnitTest {

    @Mock
    private FightController fightController;

    @Mock
    private KendoSecurityService kendoSecurityService;

    @Mock
    private PdfController pdfController;

    @Mock
    private TournamentController tournamentController;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletResponse response;

    private FightServices fightServices;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authentication.getName()).thenReturn("tester");
        fightServices = new FightServices(fightController, kendoSecurityService, pdfController, tournamentController);
    }

    @Test
    public void shouldDelegateGetAllFromTournamentId() {
        when(fightController.getByTournamentId(9)).thenReturn(List.of(new FightDTO()));

        final List<FightDTO> fights = fightServices.getAll(9, null);

        assertEquals(fights.size(), 1);
        verify(fightController).getByTournamentId(9);
    }

    @Test
    public void shouldDelegateGetAllFromTournamentDto() {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        when(fightController.get(tournamentDTO)).thenReturn(List.of(new FightDTO()));

        final List<FightDTO> fights = fightServices.getAll(tournamentDTO, null);

        assertEquals(fights.size(), 1);
        verify(fightController).get(tournamentDTO);
    }

    @Test
    public void shouldGenerateTournamentPdfAndSetHeader() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setName("Cup");
        final FightSummary fightSummary = org.mockito.Mockito.mock(FightSummary.class);
        when(tournamentController.get(10)).thenReturn(tournamentDTO);
        when(pdfController.generateFightsSummaryList(eq(Locale.ENGLISH), eq(tournamentDTO))).thenReturn(fightSummary);
        when(fightSummary.generate()).thenReturn(new byte[]{1, 2, 3});

        final byte[] bytes = fightServices.getTeamsScoreRankingFromTournamentAsPdf(10, Locale.ENGLISH, response, null);

        assertEquals(bytes, new byte[]{1, 2, 3});
        verify(response).setHeader(eq("Content-Disposition"), any(String.class));
    }

    @Test
    public void shouldWrapPdfErrorAsBadRequest() throws InvalidXmlElementException, EmptyPdfBodyException {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final FightSummary fightSummary = org.mockito.Mockito.mock(FightSummary.class);
        when(tournamentController.get(10)).thenReturn(tournamentDTO);
        when(pdfController.generateFightsSummaryList(eq(Locale.ENGLISH), eq(tournamentDTO))).thenReturn(fightSummary);
        when(fightSummary.generate()).thenThrow(new InvalidXmlElementException("invalid"));

        final BadRequestException exception = expectThrows(BadRequestException.class,
                () -> fightServices.getTeamsScoreRankingFromTournamentAsPdf(10, Locale.ENGLISH, response, null));

        assertEquals(exception.getMessage(), "invalid");
    }

    @Test
    public void shouldDelegateCurrentAndDeleteOperations() {
        final FightDTO fightDTO = new FightDTO();
        when(fightController.getCurrent(11)).thenReturn(fightDTO);
        doNothing().when(fightController).deleteById(7, "tester", "s1");

        final FightDTO current = fightServices.getCurrent(11, null);
        fightServices.delete(7, authentication, "s1", null);

        assertNotNull(current);
        verify(fightController).getCurrent(11);
        verify(fightController).deleteById(7, "tester", "s1");
    }

    @Test
    public void shouldDelegateTournamentDeleteAndCreateEndpoints() {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final FightDTO fightDTO = new FightDTO();
        fightDTO.setDuels(new ArrayList<>());

        when(fightController.update(fightDTO, "tester", "s2")).thenReturn(fightDTO);
        when(fightController.generateDuels(fightDTO, "tester")).thenReturn(fightDTO);
        when(fightController.createFights(4, com.softwaremagico.kt.core.managers.TeamsOrder.NONE, 2, "tester", "s2"))
                .thenReturn(List.of(new FightDTO()));
        when(fightController.createNextFights(4, "tester", "s2")).thenReturn(List.of(new FightDTO()));
        when(fightController.getBy(15)).thenReturn(List.of(new FightDTO()));
        when(fightController.scoresGoesFromCompetitorsNameToCenter(4)).thenReturn(true);

        fightServices.delete(tournamentDTO, null);
        final FightDTO generated = fightServices.generateDuels(fightDTO, authentication, "s2", null);
        final List<FightDTO> created = fightServices.create(4, 2, authentication, "s2", null);
        final List<FightDTO> next = fightServices.createNext(4, authentication, "s2", null);
        final List<FightDTO> byCompetitor = fightServices.getByCompetitor(15, null);
        final boolean scoresFromNameToCenter = fightServices.scoresGoesFromCompetitorsNameToCenter(4, null);

        assertNotNull(generated);
        assertEquals(created.size(), 1);
        assertEquals(next.size(), 1);
        assertEquals(byCompetitor.size(), 1);
        assertEquals(scoresFromNameToCenter, true);
        verify(fightController).delete(tournamentDTO);
    }

    @Test
    public void shouldThrowBadRequestWhenFightDataIsMissing() {
        expectThrows(BadRequestException.class,
                () -> fightServices.generateDuels(null, authentication, null, null));
    }

    @Test
    public void shouldReturnUpdatedFightWhenDuelsAlreadyGenerated() {
        final FightDTO fightDTO = new FightDTO();
        fightDTO.setDuels(List.of(new com.softwaremagico.kt.core.controller.models.DuelDTO()));
        when(fightController.update(fightDTO, "tester", null)).thenReturn(fightDTO);

        final FightDTO result = fightServices.generateDuels(fightDTO, authentication, null, null);

        assertEquals(result.getDuels().size(), 1);
    }
}

