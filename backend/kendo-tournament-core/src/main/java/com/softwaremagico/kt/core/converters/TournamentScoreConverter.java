package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.TournamentScoreDTO;
import com.softwaremagico.kt.core.converters.models.TournamentScoreConverterRequest;
import com.softwaremagico.kt.persistence.entities.TournamentScore;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TournamentScoreConverter extends ElementConverter<TournamentScore, TournamentScoreDTO, TournamentScoreConverterRequest> {

    @Override
    public TournamentScoreDTO convert(TournamentScoreConverterRequest from) {
        final TournamentScoreDTO tournamentScoreDTO = new TournamentScoreDTO();
        BeanUtils.copyProperties(from.getEntity(), tournamentScoreDTO);
        return tournamentScoreDTO;
    }

    @Override
    public TournamentScore reverse(TournamentScoreDTO to) {
        if (to == null) {
            return null;
        }
        final TournamentScore tournamentScore = new TournamentScore();
        BeanUtils.copyProperties(tournamentScore, tournamentScore);
        return tournamentScore;
    }
}
