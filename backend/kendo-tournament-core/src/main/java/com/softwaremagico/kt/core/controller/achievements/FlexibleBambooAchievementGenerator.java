package com.softwaremagico.kt.core.controller.achievements;

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

import com.softwaremagico.kt.core.providers.AchievementProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FlexibleBambooAchievementGenerator extends AchievementGenerationSupport {

    private static final int MINIMUM_ROLES_BAMBOO_NORMAL = 2;
    private static final int MINIMUM_ROLES_BAMBOO_BRONZE = 3;
    private static final int MINIMUM_ROLES_BAMBOO_SILVER = 4;
    private static final int MINIMUM_ROLES_BAMBOO_GOLD = 5;

    private final ParticipantProvider participantProvider;
    private final TournamentProvider tournamentProvider;

    public FlexibleBambooAchievementGenerator(AchievementProvider achievementProvider, ParticipantProvider participantProvider,
                                              TournamentProvider tournamentProvider) {
        super(achievementProvider);
        this.participantProvider = participantProvider;
        this.tournamentProvider = tournamentProvider;
    }

    public List<Achievement> generateFlexibleAsBambooAchievement(Tournament tournament) {
        final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament);
        previousTournaments.add(tournament);
        final List<Participant> participants = participantProvider.get(previousTournaments, MINIMUM_ROLES_BAMBOO_NORMAL);
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO, participants));
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.NORMAL, participants, tournament);
    }

    public List<Achievement> generateFlexibleAsBambooAchievementBronze(Tournament tournament) {
        final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament);
        previousTournaments.add(tournament);
        final List<Participant> participants = participantProvider.get(previousTournaments, MINIMUM_ROLES_BAMBOO_BRONZE);
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO,
                AchievementGrade.BRONZE, participants));
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.BRONZE, participants, tournament);
    }

    public List<Achievement> generateFlexibleAsBambooAchievementSilver(Tournament tournament) {
        final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament);
        previousTournaments.add(tournament);
        final List<Participant> participants = participantProvider.get(previousTournaments, MINIMUM_ROLES_BAMBOO_SILVER);
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO,
                AchievementGrade.SILVER, participants));
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.SILVER, participants, tournament);
    }

    public List<Achievement> generateFlexibleAsBambooAchievementGold(Tournament tournament) {
        final List<Tournament> previousTournaments = tournamentProvider.getPreviousTo(tournament);
        previousTournaments.add(tournament);
        final List<Participant> participants = participantProvider.get(previousTournaments, MINIMUM_ROLES_BAMBOO_GOLD);
        participants.removeAll(participantProvider.getParticipantsWithAchievementFromList(AchievementType.FLEXIBLE_AS_BAMBOO,
                AchievementGrade.GOLD, participants));
        return generateAchievement(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.GOLD, participants, tournament);
    }
}


