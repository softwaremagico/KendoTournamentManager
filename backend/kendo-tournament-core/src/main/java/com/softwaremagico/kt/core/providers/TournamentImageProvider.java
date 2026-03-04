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

import com.softwaremagico.kt.core.controller.TournamentImageController;
import com.softwaremagico.kt.core.exceptions.DataInputException;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import com.softwaremagico.kt.persistence.repositories.TournamentImageRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Component
public class TournamentImageProvider extends CrudProvider<TournamentImage, Integer, TournamentImageRepository> {

    private static final String DEFAULT_BANNER_IMAGE = "/images/default-banner.png";
    private static final String DEFAULT_DIPLOMA_IMAGE = "/images/default-diploma.png";
    private static final String DEFAULT_PHOTO_IMAGE = "/images/default-photo.png";
    private static final String DEFAULT_ACCREDITATION_IMAGE = "/images/accreditation-background.png";

    private static byte[] defaultAccreditation;
    private static byte[] defaultBanner;
    private static byte[] defaultDiploma;
    private static byte[] defaultPhoto;

    private final TournamentRepository tournamentRepository;

    @Autowired
    public TournamentImageProvider(TournamentImageRepository repository, TournamentRepository tournamentRepository) {
        super(repository);
        this.tournamentRepository = tournamentRepository;
    }

    public Optional<TournamentImage> get(Tournament tournament, TournamentImageType imageType) {
        return getRepository().findByTournamentAndImageType(tournament, imageType);
    }

    public int delete(Tournament tournament, TournamentImageType imageType) {
        return getRepository().deleteByTournamentAndImageType(tournament, imageType);
    }

    public List<TournamentImage> getAll(Tournament tournament) {
        return getRepository().findByTournament(tournament);
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

    public TournamentImage getDefaultImage(Tournament tournament, TournamentImageType type) {
        final TournamentImage tournamentImage = new TournamentImage();
        tournamentImage.setTournament(tournament);
        tournamentImage.setImageType(type);
        tournamentImage.setImageCompression(ImageCompression.PNG);
        switch (type) {
            case ACCREDITATION -> tournamentImage.setData(getDefaultAccreditation());
            case BANNER -> tournamentImage.setData(getDefaultBanner());
            case DIPLOMA -> tournamentImage.setData(getDefaultDiploma());
            case PHOTO -> tournamentImage.setData(getDefaultPhoto());
            default -> {
                //Not needed
            }
        }
        return tournamentImage;
    }

    public TournamentImage add(MultipartFile file, Tournament tournament, TournamentImageType type, ImageCompression imageCompression,
                               String username) throws DataInputException {
        try {
            delete(tournament, type);
            final TournamentImage tournamentImage = new TournamentImage();
            tournamentImage.setTournament(tournament);
            tournamentImage.setData(file.getBytes());
            tournamentImage.setCreatedBy(username);
            tournamentImage.setImageType(type);
            tournamentImage.setImageCompression(imageCompression);
            tournamentRepository.save(tournament);
            return save(tournamentImage);
        } catch (IOException e) {
            throw new DataInputException(this.getClass(), "File creation failed.");
        }
    }

    public TournamentImage add(TournamentImage tournamentImage, String username) throws DataInputException {
        delete(tournamentImage.getTournament(), tournamentImage.getImageType());
        tournamentImage.setCreatedBy(username);
        tournamentRepository.save(tournamentImage.getTournament());
        return save(tournamentImage);
    }
}
