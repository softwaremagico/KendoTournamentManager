package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class DuelProvider extends CrudProvider<Duel, Integer, DuelRepository> {
    private static final int CACHE_EXPIRATION_TIME = 10 * 60 * 1000;

    private final GroupRepository groupRepository;
    private final TournamentRepository tournamentRepository;

    @Autowired
    public DuelProvider(DuelRepository duelRepository, GroupRepository groupRepository,
            TournamentRepository tournamentRepository) {
        super(duelRepository);
        this.groupRepository = groupRepository;
        this.tournamentRepository = tournamentRepository;
    }

    public long delete(Tournament tournament) {
        return this.getRepository().deleteByTournament(tournament);
    }

    public long count(Tournament tournament) {
        return this.getRepository().countByTournament(tournament);
    }

    public List<Duel> get(Participant participant) {
        return this.getRepository().findByParticipant(participant);
    }

    public List<Duel> getWhenBothAreInvolved(Participant participant1, Participant participant2) {
        return this.getRepository().findByParticipants(participant1, participant2);
    }

    public List<Duel> get(Tournament tournament) {
        return this.getRepository().findByTournament(tournament);
    }

    public List<Duel> getUnties(Collection<Participant> participants) {
        return this.getRepository().findUntiesByParticipantIn(participants);
    }

    public List<Duel> getUnties() {
        return this.getRepository().findAllUnties();
    }

    @Cacheable(value = "duels-duration-average", key = "'average'")
    public Long getDurationAverage() {
        final Long duration = this.getRepository().getDurationAverage();
        return duration != null ? duration : -1;
    }

    public Long getDurationAverage(Participant participant) {
        final Long duration = this.getRepository().getDurationAverage(participant);
        return duration != null ? duration : -1;
    }

    public Long getDurationAverage(Tournament tournament) {
        return this.getRepository().getDurationAverage(tournament);
    }

    public Duel getFirstDuel(Tournament tournament) {
        return this.getRepository().findFirstByTournamentOrderByStartedAtAsc(tournament);
    }

    public Duel getLastDuel(Tournament tournament) {
        return this.getRepository().findFirstByTournamentOrderByFinishedAtDesc(tournament);
    }

    public Long countScore(Tournament tournament, Score score) {
        return this.getRepository().countScore(tournament, Collections.singletonList(score));
    }

    public Set<Duel> findByOnlyScore(Tournament tournament, Score score) {
        final List<Score> forbiddenScores = new ArrayList<>(Arrays.asList(Score.values()));
        forbiddenScores.remove(score);
        forbiddenScores.remove(Score.EMPTY);
        return this.getRepository().findByOnlyScore(tournament, forbiddenScores);
    }

    public Set<Duel> findByScorePerformedInLessThan(Tournament tournament, int maxSeconds) {
        return this.getRepository().findByScoreOnTimeLess(tournament, maxSeconds);
    }

    public List<Duel> findByScoreDuration(Tournament tournament, int scoreMaxDuration) {
        return this.getRepository()
                .findByTournamentAndCompetitor1ScoreTimeLessThanEqualOrCompetitor2ScoreTimeLessThanEqual(tournament,
                        scoreMaxDuration, scoreMaxDuration);
    }

    public long countFaults(Tournament tournament) {
        final Long faults = this.getRepository().countFaultsByTournament(tournament, true);
        final Long hansokus = this.getRepository().countScore(tournament, Collections.singletonList(Score.HANSOKU));
        return (faults != null ? faults : 0) + (hansokus != null ? hansokus : 0) * 2;
    }

    public long countScoreFromCompetitor(Participant participant, Collection<Tournament> tournaments) {
        if (participant == null || tournaments == null || tournaments.isEmpty()) {
            return 0L;
        }
        try {
            final Long leftScore = this.getRepository().countLeftScoreFromCompetitor(participant, tournaments);
            final Long rightScore = this.getRepository().countRightScoreFromCompetitor(participant, tournaments);
            return (leftScore != null ? leftScore : 0L) + (rightScore != null ? rightScore : 0L);
        } catch (NullPointerException _) {
            return 0L;
        }
    }

    public long countScoreAgainstCompetitor(Participant participant, Collection<Tournament> tournaments) {
        if (participant == null || tournaments == null || tournaments.isEmpty()) {
            return 0L;
        }
        try {
            final Long leftScore = this.getRepository().countLeftScoreAgainstCompetitor(participant, tournaments);
            final Long rightScore = this.getRepository().countRightScoreAgainstCompetitor(participant, tournaments);
            return (leftScore != null ? leftScore : 0L) + (rightScore != null ? rightScore : 0L);
        } catch (NullPointerException _) {
            return 0L;
        }
    }

    @CacheEvict(allEntries = true, value = {"duels-duration-average"})
    @Scheduled(fixedDelay = CACHE_EXPIRATION_TIME)
    public void reportCacheEvict() {
        // Only for handling Spring cache.
    }

    public List<Duel> getUntiesFromTournament(Integer tournamentId) {
        final List<Group> groups = this.groupRepository
                .findByTournamentOrderByLevelAscIndexAsc(this.tournamentRepository.findById(tournamentId)
                        .orElseThrow(() -> new TournamentNotFoundException(this.getClass(),
                                "No tournament found with id '" + tournamentId + "',", ExceptionType.INFO)));
        return groups.stream().flatMap(group -> group.getUnties().stream()).toList();
    }

    public List<Duel> getUntiesFromGroup(Integer groupId) {
        final Group group = this.groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return new ArrayList<>();
        }
        return group.getUnties();
    }

    public List<Duel> getUntiesFromParticipant(Participant participant) {
        return this.getRepository().findUntiesByParticipantIn(Collections.singletonList(participant));
    }

}
