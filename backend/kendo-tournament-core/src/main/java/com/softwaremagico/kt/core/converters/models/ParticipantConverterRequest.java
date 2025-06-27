package com.softwaremagico.kt.core.converters.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;

public class ParticipantConverterRequest extends ConverterRequest<Participant> {

    private final Club club;
    private final ClubDTO clubDTO;

    public ParticipantConverterRequest(Participant entity) {
        super(entity);
        this.club = null;
        this.clubDTO = null;
    }

    public ParticipantConverterRequest(Participant entity, Club club) {
        super(entity);
        this.club = club;
        this.clubDTO = null;
    }

    public ParticipantConverterRequest(Participant entity, ClubDTO clubDTO) {
        super(entity);
        this.club = null;
        this.clubDTO = clubDTO;
    }

    @Override
    public Participant getEntity() {
        return super.getEntityWithoutChecks();
    }

    public Club getClub() {
        return club;
    }

    public ClubDTO getClubDTO() {
        return clubDTO;
    }
}
