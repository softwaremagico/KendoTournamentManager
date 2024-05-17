package com.softwaremagico.kt.core.converters;

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
import com.softwaremagico.kt.core.converters.models.GroupConverterRequest;
import com.softwaremagico.kt.core.converters.models.GroupLinkConverterRequest;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class GroupLinkConverter extends ElementConverter<GroupLink, GroupLinkDTO, GroupLinkConverterRequest> {
    private final GroupConverter groupConverter;

    public GroupLinkConverter(GroupConverter groupConverter) {
        this.groupConverter = groupConverter;
    }

    @Override
    protected GroupLinkDTO convertElement(GroupLinkConverterRequest from) {
        final GroupLinkDTO groupLinkDTO = new GroupLinkDTO();
        BeanUtils.copyProperties(from.getEntity(), groupLinkDTO);
        groupLinkDTO.setSource(groupConverter.convert(new GroupConverterRequest(from.getEntity().getSource())));
        groupLinkDTO.setDestination(groupConverter.convert(new GroupConverterRequest(from.getEntity().getDestination())));
        return groupLinkDTO;
    }

    @Override
    public GroupLink reverse(GroupLinkDTO to) {
        final GroupLink groupLink = new GroupLink();
        BeanUtils.copyProperties(to, groupLink);
        groupLink.setSource(groupConverter.reverse(to.getSource()));
        groupLink.setDestination(groupConverter.reverse(to.getDestination()));
        return groupLink;
    }
}
