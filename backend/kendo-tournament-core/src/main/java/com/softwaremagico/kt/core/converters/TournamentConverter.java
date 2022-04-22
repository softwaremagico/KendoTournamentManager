package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentScoreConverterRequest;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class TournamentConverter extends ElementConverter<Tournament, TournamentDTO, TournamentConverterRequest> {
    private final TournamentScoreConverter tournamentScoreConverter;

    public TournamentConverter(TournamentScoreConverter tournamentScoreConverter) {
        this.tournamentScoreConverter = tournamentScoreConverter;
    }


    @Override
    public TournamentDTO convert(TournamentConverterRequest from) {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        BeanUtils.copyProperties(from.getEntity(), tournamentDTO);
        tournamentDTO.setTournamentScoreDTO(tournamentScoreConverter.convert(
                new TournamentScoreConverterRequest(from.getEntity().getTournamentScore())));
        return tournamentDTO;
    }

    @Override
    public Tournament reverse(TournamentDTO to) {
        if (to == null) {
            return null;
        }
        final Tournament tournament = new Tournament();
        tournament.setTournamentScore(tournamentScoreConverter.reverse(to.getTournamentScoreDTO()));
        BeanUtils.copyProperties(tournament, tournament);
        return tournament;
    }
}
