package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.core.controller.models.GroupLinkDTO;
import com.softwaremagico.kt.core.converters.GroupLinkConverter;
import com.softwaremagico.kt.core.converters.models.GroupLinkConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.GroupLinkProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.GroupLinkRepository;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class GroupLinkController extends BasicInsertableController<GroupLink, GroupLinkDTO, GroupLinkRepository,
        GroupLinkProvider, GroupLinkConverterRequest, GroupLinkConverter> {

    private final TournamentProvider tournamentProvider;

    public GroupLinkController(GroupLinkProvider provider, GroupLinkConverter converter, TournamentProvider tournamentProvider) {
        super(provider, converter);
        this.tournamentProvider = tournamentProvider;
    }


    protected GroupLinkConverterRequest createConverterRequest(GroupLink groupLink) {
        return new GroupLinkConverterRequest(groupLink);
    }

    public List<GroupLinkDTO> getLinks(Integer tournamentId) {
        return getLinks(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)));
    }

    public List<GroupLinkDTO> getLinks(Tournament tournament) {
        return convertAll(getProvider().generateLinks(tournament));
    }


}
