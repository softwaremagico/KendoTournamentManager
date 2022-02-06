package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.exceptions.UserNotFoundException;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipantProvider {

    private final ParticipantRepository participantRepository;

    @Autowired
    public ParticipantProvider(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    public Participant get(int id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(getClass(), "User with id '" + id + "' not found"));
    }

    public List<Participant> get(List<Integer> ids) {
        return participantRepository.findByIdIn(ids);
    }


    public List<Participant> getAll() {
        return participantRepository.findByOrderByLastnameAsc();
    }

    public long count() {
        return participantRepository.count();
    }

    public Participant save(Participant participant) {
        return participantRepository.save(participant);
    }

    public Participant update(Participant participant) {
        if (participant.getId() == null) {
            throw new UserNotFoundException(getClass(), "User with null id does not exists.");
        }
        return participantRepository.save(participant);
    }

    public void delete(Participant participant) {
        participantRepository.delete(participant);
    }

    public void delete(Integer id) {
        if (participantRepository.existsById(id)) {
            participantRepository.deleteById(id);
        } else {
            throw new UserNotFoundException(getClass(), "User with id '" + id + "' not found");
        }
    }
}
