package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.models.GroupConverterRequest;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.logger.RestServerLogger;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/groups")
public class GroupServices extends BasicServices<Group, GroupDTO, GroupRepository, GroupProvider, GroupConverterRequest, GroupConverter, GroupController> {
    private final PdfController pdfController;
    private final TournamentController tournamentController;

    public GroupServices(GroupController groupController, KendoSecurityService kendoSecurityService, PdfController pdfController,
                         TournamentController tournamentController) {
        super(groupController, kendoSecurityService);
        this.pdfController = pdfController;
        this.tournamentController = tournamentController;
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege, "
            + "@securityService.guestPrivilege)")
    @Operation(summary = "Gets all groups.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupDTO> getAll(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                                 HttpServletRequest request) {
        return getController().getFromTournament(tournamentId);
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets all groups.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/level/{level}/index/{index}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GroupDTO get(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId") Integer tournamentId,
                        @Parameter(description = "Level of the group", required = true) @PathVariable("level") Integer level,
                        @Parameter(description = "Index of the group", required = true) @PathVariable("index") Integer index,
                        HttpServletRequest request) {
        if (level == null || index == null) {
            throw new BadRequestException(this.getClass(), "Level or Index not set!");
        }
        return getController().getFromTournament(tournamentId, level, index);
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Set teams on a group.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/{groupId}/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public GroupDTO updateTeam(@Parameter(description = "Id of the group to update", required = true) @PathVariable("groupId") Integer groupId,
                               @RequestBody List<TeamDTO> teamsDto,
                               Authentication authentication,
                               HttpServletRequest request) {
        return getController().setTeams(groupId, teamsDto, authentication.getName());
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Set teams on a group.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(value = "/{groupId}/teams/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public GroupDTO addTeam(@Parameter(description = "Id of the group to update", required = true) @PathVariable("groupId") Integer groupId,
                            @RequestBody List<TeamDTO> teamsDto,
                            Authentication authentication,
                            HttpServletRequest request) {
        return getController().addTeams(groupId, teamsDto, authentication.getName());
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Set teams on a group.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(value = "/{groupId}/teams/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public GroupDTO deleteTeamFromGroup(@Parameter(description = "Id of the group to update", required = true) @PathVariable("groupId") Integer groupId,
                                        @RequestBody List<TeamDTO> teamsDto,
                                        Authentication authentication,
                                        HttpServletRequest request) {
        return getController().deleteTeams(groupId, teamsDto, authentication.getName());
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Removes teams from any group.", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping(value = "/tournaments/{tournamentId}/teams/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupDTO> deleteTeam(@Parameter(description = "Id of an existing tournament", required = true)
                                     @PathVariable("tournamentId") Integer tournamentId,
                                     @RequestBody List<TeamDTO> teamsDto,
                                     Authentication authentication,
                                     HttpServletRequest request) {
        return getController().deleteTeamsFromTournament(tournamentId, teamsDto, authentication.getName());
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Removes all teams from all groups", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(value = "/tournaments/{tournamentId}/teams/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GroupDTO> deleteAllTeam(@Parameter(description = "Id of an existing tournament", required = true)
                                        @PathVariable("tournamentId") Integer tournamentId,
                                        Authentication authentication,
                                        HttpServletRequest request) {
        return getController().deleteTeamsFromTournament(tournamentId, authentication.getName());
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Set teams on the first group.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/teams", produces = MediaType.APPLICATION_JSON_VALUE)
    public GroupDTO updateTeam(@RequestBody List<TeamDTO> teamsDto,
                               Authentication authentication,
                               HttpServletRequest request) {
        return getController().setTeams(teamsDto, authentication.getName());
    }

    @PreAuthorize("hasAnyAuthority(@securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Adds untie duels.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/{groupId}/unties", produces = MediaType.APPLICATION_JSON_VALUE)
    public GroupDTO addUnties(@Parameter(description = "Id of the group to update", required = true) @PathVariable("groupId") Integer groupId,
                              @RequestBody List<DuelDTO> duelDTOs,
                              Authentication authentication,
                              HttpServletRequest request) {
        return getController().addUnties(groupId, duelDTOs, authentication.getName());
    }

    @PreAuthorize("hasAnyAuthority(@securityService.viewerPrivilege, @securityService.editorPrivilege, @securityService.adminPrivilege)")
    @Operation(summary = "Gets all groups from a tournament.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournaments/{tournamentId}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public byte[] getAllFromTournamentAsPdf(@Parameter(description = "Id of an existing tournament", required = true) @PathVariable("tournamentId")
                                            Integer tournamentId,
                                            Locale locale, HttpServletResponse response, HttpServletRequest request) {
        final TournamentDTO tournament = tournamentController.get(tournamentId);
        try {
            final byte[] bytes = pdfController.generateGroupList(locale, tournament).generate();
            final ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(tournament.getName() + " - group list.pdf").build();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            return bytes;
        } catch (InvalidXmlElementException | EmptyPdfBodyException e) {
            RestServerLogger.errorMessage(this.getClass(), e);
            throw new BadRequestException(this.getClass(), e.getMessage());
        }
    }
}
