package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.ParticipantImage;
import com.softwaremagico.kt.persistence.repositories.ParticipantImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class ParticipantImageProvider extends CrudProvider<ParticipantImage, Integer, ParticipantImageRepository> {

    @Autowired
    public ParticipantImageProvider(ParticipantImageRepository repository) {
        super(repository);
    }

    public Optional<ParticipantImage> get(Participant participant) {
        return getRepository().findByParticipant(participant);
    }

    public List<ParticipantImage> get(Collection<Participant> participants) {
        return getRepository().findByParticipantIn(participants);
    }

    public int delete(Participant participant) {
        return getRepository().deleteByParticipant(participant);
    }
}
