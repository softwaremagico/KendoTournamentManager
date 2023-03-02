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

import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DuelProvider extends CrudProvider<Duel, Integer, DuelRepository> {

    @Autowired
    public DuelProvider(DuelRepository duelRepository) {
        super(duelRepository);
    }

    public long delete(Tournament tournament) {
        return repository.deleteByTournament(tournament);
    }

    public long count(Tournament tournament) {
        return repository.countByTournament(tournament);
    }

    public List<Duel> getUnties(List<Participant> participants) {
        return repository.findUntiesByParticipantIn(participants);
    }

    @Cacheable("duelsDurationAverage")
    public Long getDurationAverage() {
        return repository.getDurationAverage();
    }

    public Set<Duel> findByOnlyScore(Tournament tournament, Score score) {
        final List<Score> forbiddenScores = new ArrayList<>(Arrays.asList(Score.values()));
        forbiddenScores.remove(score);
        forbiddenScores.remove(Score.EMPTY);
        return repository.findByOnlyScore(tournament, Collections.singleton(score), forbiddenScores);
    }

    public Set<Duel> findByScorePerformedInLessThan(Tournament tournament, int maxSeconds) {
        return repository.findByScoreOnTimeLess(tournament, maxSeconds);
    }

    @CacheEvict(allEntries = true, value = {"duelsDurationAverage"})
    @Scheduled(fixedDelay = 60 * 10 * 1000)
    public void reportCacheEvict() {
        //Only for handling Spring cache.
    }

}
