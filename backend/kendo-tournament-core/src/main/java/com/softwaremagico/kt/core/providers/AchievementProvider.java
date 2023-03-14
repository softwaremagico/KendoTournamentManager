package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.values.AchievementType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AchievementProvider extends CrudProvider<Achievement, Integer, AchievementRepository> {

    @Autowired
    public AchievementProvider(AchievementRepository achievementRepository) {
        super(achievementRepository);
    }

    public Achievement add(Participant participant, Tournament tournament, AchievementType achievementType) {
        return repository.save(new Achievement(participant, tournament, achievementType));
    }

    public List<Achievement> get(Participant participant) {
        return repository.findByParticipant(participant);
    }

    public List<Achievement> get(Tournament tournament, AchievementType achievementType) {
        return repository.findByTournamentAndAchievementType(tournament, achievementType);
    }

    public List<Achievement> get(AchievementType achievementType) {
        return repository.findByAchievementType(achievementType);
    }

    public List<Achievement> get(Tournament tournament) {
        return repository.findByTournament(tournament);
    }

    public int delete(Tournament tournament) {
        return repository.deleteByTournament(tournament);
    }
}