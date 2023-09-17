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
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import com.softwaremagico.kt.persistence.repositories.TournamentImageRepository;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class TournamentImageController extends BasicInsertableController<TournamentImage, TournamentImageDTO, TournamentImageRepository,
        TournamentImageProvider, TournamentImageConverterRequest, TournamentImageConverter> {
    private static final String DEFAULT_BANNER_IMAGE = "/images/default-banner.png";
    private static final String DEFAULT_DIPLOMA_IMAGE = "/images/default-diploma.png";
    private static final String DEFAULT_PHOTO_IMAGE = "/images/default-photo.png";
    private static final String DEFAULT_ACCREDITATION_IMAGE = "/images/accreditation-background.png";
    private static byte[] defaultAccreditation;
    private static byte[] defaultBanner;
    private static byte[] defaultDiploma;
    private static byte[] defaultPhoto;
    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;


    @Autowired
    public TournamentImageController(TournamentImageProvider provider, TournamentImageConverter converter,
                                     TournamentConverter tournamentConverter, TournamentProvider tournamentProvider) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
    }

    private static byte[] getDefaultBanner() {
        if (defaultBanner == null) {
            try (InputStream inputStream = TournamentImageController.class.getResourceAsStream(DEFAULT_BANNER_IMAGE)) {
                if (inputStream != null) {
                    defaultBanner = inputStream.readAllBytes();
                }
            } catch (NullPointerException | IOException ex) {
                KendoTournamentLogger.severe(TournamentImageController.class.getName(), "No default banner found!");
            }
        }
        return defaultBanner;
    }

    private static byte[] getDefaultAccreditation() {
        if (defaultAccreditation == null) {
            try (InputStream inputStream = TournamentImageController.class.getResourceAsStream(DEFAULT_ACCREDITATION_IMAGE)) {
                if (inputStream != null) {
                    defaultAccreditation = inputStream.readAllBytes();
                }
            } catch (NullPointerException | IOException ex) {
                KendoTournamentLogger.severe(TournamentImageController.class.getName(), "No default accreditation found!");
            }
        }
        return defaultAccreditation;
    }

    private static byte[] getDefaultDiploma() {
        if (defaultDiploma == null) {
            try (InputStream inputStream = TournamentImageController.class.getResourceAsStream(DEFAULT_DIPLOMA_IMAGE)) {
                if (inputStream != null) {
                    defaultDiploma = inputStream.readAllBytes();
                }
            } catch (NullPointerException | IOException ex) {
                KendoTournamentLogger.severe(TournamentImageController.class.getName(), "No default diploma found!");
            }
        }
        return defaultDiploma;
    }

    private static byte[] getDefaultPhoto() {
        if (defaultPhoto == null) {
            try (InputStream inputStream = TournamentImageController.class.getResourceAsStream(DEFAULT_PHOTO_IMAGE)) {
                if (inputStream != null) {
                    defaultPhoto = inputStream.readAllBytes();
                }
            } catch (NullPointerException | IOException ex) {
                KendoTournamentLogger.severe(TournamentImageController.class.getName(), "No default diploma found!");
            }
        }
        return defaultPhoto;
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

    private TournamentImageDTO getDefaultImage(TournamentDTO tournamentDTO, TournamentImageType type) {
        final TournamentImageDTO tournamentImageDTO = new TournamentImageDTO();
        tournamentImageDTO.setTournament(tournamentDTO);
        tournamentImageDTO.setImageType(type);
        tournamentImageDTO.setImageCompression(ImageCompression.PNG);
        switch (type) {
            case ACCREDITATION -> tournamentImageDTO.setData(getDefaultAccreditation());
            case BANNER -> tournamentImageDTO.setData(getDefaultBanner());
            case DIPLOMA -> tournamentImageDTO.setData(getDefaultDiploma());
            case PHOTO -> tournamentImageDTO.setData(getDefaultPhoto());
            default -> {
            }
        }
        return tournamentImageDTO;
    }

    public TournamentImageDTO get(TournamentDTO tournamentDTO, TournamentImageType type) {
        final Tournament tournament = tournamentConverter.reverse(tournamentDTO);
        final TournamentImageDTO result = convert(getProvider().get(tournament, type).orElse(null));
        if (result != null) {
            return result;
        }
        return getDefaultImage(tournamentConverter.convert(new TournamentConverterRequest(tournament)), type);
    }

    public TournamentImageDTO add(MultipartFile file, Integer tournamentId, TournamentImageType type, ImageCompression imageCompression,
                                  String username) {
        final TournamentDTO tournamentDTO = tournamentConverter.convert(new TournamentConverterRequest(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."))));
        return add(file, tournamentDTO, type, imageCompression, username);
    }

    public TournamentImageDTO add(MultipartFile file, TournamentDTO tournamentDTO, TournamentImageType type, ImageCompression imageCompression,
                                  String username) throws DataInputException {
        try {
            delete(tournamentDTO, type);
            final TournamentImage tournamentImage = new TournamentImage();
            tournamentImage.setTournament(tournamentConverter.reverse(tournamentDTO));
            tournamentImage.setData(file.getBytes());
            tournamentImage.setCreatedBy(username);
            tournamentImage.setImageType(type);
            tournamentImage.setImageCompression(imageCompression);
            tournamentProvider.save(tournamentConverter.reverse(tournamentDTO));
            return convert(getProvider().save(tournamentImage));
        } catch (IOException e) {
            throw new DataInputException(this.getClass(), "File creation failed.");
        }
    }

    public TournamentImageDTO add(TournamentImageDTO tournamentImageDTO, String username) throws DataInputException {
        delete(tournamentImageDTO.getTournament(), tournamentImageDTO.getImageType());
        tournamentImageDTO.setCreatedBy(username);
        final Tournament tournament = tournamentConverter.reverse(tournamentImageDTO.getTournament());
        tournamentProvider.save(tournament);
        return convert(getProvider().save(reverse(tournamentImageDTO)));
    }

    public int delete(TournamentDTO tournamentDTO, TournamentImageType type) {
        final Tournament tournament = tournamentConverter.reverse(tournamentDTO);
        tournamentProvider.save(tournament);
        return getProvider().delete(tournament, type);
    }
}
