package com.softwaremagico.kt.core.converters;

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

import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.converters.models.AchievementConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.stereotype.Component;

@Component
public class AchievementConverter extends ElementConverter<Achievement, AchievementDTO, AchievementConverterRequest> {
    private final TournamentConverter tournamentConverter;
    private final TournamentRepository tournamentRepository;
    private final ParticipantReducedConverter participantReducedConverter;
    private final ParticipantConverter participantConverter;
    private final ParticipantProvider participantProvider;

    public AchievementConverter(TournamentConverter tournamentConverter, TournamentRepository tournamentRepository,
                                ParticipantReducedConverter participantReducedConverter, ParticipantConverter participantConverter,
                                ParticipantProvider participantProvider) {
        this.tournamentConverter = tournamentConverter;
        this.tournamentRepository = tournamentRepository;
        this.participantReducedConverter = participantReducedConverter;
        this.participantConverter = participantConverter;
        this.participantProvider = participantProvider;
    }


    @Override
    protected AchievementDTO convertElement(AchievementConverterRequest from) {
        final AchievementDTO achievementDTO = new AchievementDTO();
        BeanUtils.copyProperties(from.getEntity(), achievementDTO);
        try {
            achievementDTO.setTournament(tournamentConverter.convert(
                    new TournamentConverterRequest(from.getEntity().getTournament())));
        } catch (LazyInitializationException | FatalBeanException e) {
            achievementDTO.setTournament(tournamentConverter.convert(
                    new TournamentConverterRequest(tournamentRepository.findById(from.getEntity().getTournament().getId()).orElse(null))));
        }
        try {
            achievementDTO.setParticipant(participantReducedConverter.convert(
                    new ParticipantConverterRequest(from.getEntity().getParticipant())));
        } catch (LazyInitializationException | FatalBeanException e) {
            achievementDTO.setParticipant(participantReducedConverter.convert(
                    new ParticipantConverterRequest(participantProvider.get(from.getEntity().getParticipant().getId()).orElse(null))));
        }
        return achievementDTO;
    }

    @Override
    public Achievement reverse(AchievementDTO to) {
        if (to == null) {
            return null;
        }
        final Achievement achievement = new Achievement();
        BeanUtils.copyProperties(to, achievement);
        achievement.setParticipant(participantConverter.reverse(to.getParticipant()));
        achievement.setTournament(tournamentConverter.reverse(to.getTournament()));
        return achievement;
    }
}
