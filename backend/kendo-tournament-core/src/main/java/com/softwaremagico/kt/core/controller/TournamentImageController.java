package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentImageDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.TournamentImageConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentImageConverterRequest;
import com.softwaremagico.kt.core.exceptions.DataInputException;
import com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.TournamentImageProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import com.softwaremagico.kt.persistence.repositories.TournamentImageRepository;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class TournamentImageController extends BasicInsertableController<TournamentImage, TournamentImageDTO, TournamentImageRepository,
        TournamentImageProvider, TournamentImageConverterRequest, TournamentImageConverter> {
    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;


    @Autowired
    public TournamentImageController(TournamentImageProvider provider, TournamentImageConverter converter,
                                     TournamentConverter tournamentConverter, TournamentProvider tournamentProvider) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
    }


    @Override
    protected TournamentImageConverterRequest createConverterRequest(TournamentImage participantImage) {
        return new TournamentImageConverterRequest(participantImage);
    }

    public int deleteByTournamentId(Integer tournamentId, TournamentImageType type) {
        final Tournament tournament = tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new ParticipantNotFoundException(getClass(), "No tournaments found with id '" + tournamentId + "'."));
        return getProvider().delete(tournament, type);
    }

    public TournamentImageDTO get(Integer tournamentId, TournamentImageType type) {
        final Tournament tournament = tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new ParticipantNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."));
        return get(tournamentConverter.convert(new TournamentConverterRequest(tournament)), type);
    }


    public TournamentImageDTO get(TournamentDTO tournamentDTO, TournamentImageType type) {
        final Tournament tournament = tournamentConverter.reverse(tournamentDTO);
        final TournamentImageDTO result = convert(getProvider().get(tournament, type).orElse(null));
        if (result != null) {
            return result;
        }
        return convert(getProvider().getDefaultImage(tournament, type));
    }

    public TournamentImageDTO add(MultipartFile file, Integer tournamentId, TournamentImageType type, ImageCompression imageCompression,
                                  String username) {
        final TournamentDTO tournamentDTO = tournamentConverter.convert(new TournamentConverterRequest(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."))));
        return add(file, tournamentDTO, type, imageCompression, username);
    }

    public TournamentImageDTO add(MultipartFile file, TournamentDTO tournamentDTO, TournamentImageType type, ImageCompression imageCompression,
                                  String createdBy) throws DataInputException {
        return convert(getProvider().add(file, tournamentConverter.reverse(tournamentDTO), type, imageCompression, createdBy));
    }

    public TournamentImageDTO add(TournamentImageDTO tournamentImageDTO, String username) throws DataInputException {
        return convert(getProvider().add(reverse(tournamentImageDTO), username));
    }

    public int delete(TournamentDTO tournamentDTO, TournamentImageType type) {
        final Tournament tournament = tournamentConverter.reverse(tournamentDTO);
        tournamentProvider.save(tournament);
        return getProvider().delete(tournament, type);
    }
}
