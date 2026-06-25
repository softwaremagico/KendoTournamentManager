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

    @FunctionalInterface
    interface ResourceLoader {
        InputStream getResourceAsStream(String resourcePath);
    }

    private static final String DEFAULT_BANNER_IMAGE = "/images/default-banner.png";
    private static final String DEFAULT_DIPLOMA_IMAGE = "/images/default-diploma.png";
    private static final String DEFAULT_PHOTO_IMAGE = "/images/default-photo.png";
    private static final String DEFAULT_ACCREDITATION_IMAGE = "/images/accreditation-background.png";

    private byte[] defaultAccreditation;
    private byte[] defaultBanner;
    private byte[] defaultDiploma;
    private byte[] defaultPhoto;

    private final TournamentRepository tournamentRepository;
    private final ResourceLoader resourceLoader;

    @Autowired
    public TournamentImageProvider(TournamentImageRepository repository, TournamentRepository tournamentRepository) {
        this(repository, tournamentRepository, TournamentImageController.class::getResourceAsStream);
    }

    TournamentImageProvider(TournamentImageRepository repository, TournamentRepository tournamentRepository,
                            ResourceLoader resourceLoader) {
        super(repository);
        this.tournamentRepository = tournamentRepository;
        this.resourceLoader = resourceLoader;
    }

    public Optional<TournamentImage> get(Tournament tournament, TournamentImageType imageType) {
        return this.getRepository().findByTournamentAndImageType(tournament, imageType);
    }

    public int delete(Tournament tournament, TournamentImageType imageType) {
        return this.getRepository().deleteByTournamentAndImageType(tournament, imageType);
    }

    public List<TournamentImage> getAll(Tournament tournament) {
        return this.getRepository().findByTournament(tournament);
    }

    private byte[] getDefaultBanner() {
        if (this.defaultBanner == null) {
            try (final InputStream inputStream = this.resourceLoader.getResourceAsStream(DEFAULT_BANNER_IMAGE)) {
                if (inputStream != null) {
                    this.defaultBanner = inputStream.readAllBytes();
                }
            } catch (NullPointerException | IOException _) {
                KendoTournamentLogger.severe(TournamentImageProvider.class.getName(), "No default banner found!");
            }
        }
        return this.defaultBanner;
    }

    private byte[] getDefaultAccreditation() {
        if (this.defaultAccreditation == null) {
            try (final InputStream inputStream = this.resourceLoader.getResourceAsStream(DEFAULT_ACCREDITATION_IMAGE)) {
                if (inputStream != null) {
                    this.defaultAccreditation = inputStream.readAllBytes();
                }
            } catch (NullPointerException | IOException _) {
                KendoTournamentLogger.severe(TournamentImageController.class.getName(),
                        "No default accreditation found!");
            }
        }
        return this.defaultAccreditation;
    }

    private byte[] getDefaultDiploma() {
        if (this.defaultDiploma == null) {
            try (final InputStream inputStream = this.resourceLoader.getResourceAsStream(DEFAULT_DIPLOMA_IMAGE)) {
                if (inputStream != null) {
                    this.defaultDiploma = inputStream.readAllBytes();
                }
            } catch (NullPointerException | IOException _) {
                KendoTournamentLogger.severe(TournamentImageController.class.getName(), "No default diploma found!");
            }
        }
        return this.defaultDiploma;
    }

    private byte[] getDefaultPhoto() {
        if (this.defaultPhoto == null) {
            try (final InputStream inputStream = this.resourceLoader.getResourceAsStream(DEFAULT_PHOTO_IMAGE)) {
                if (inputStream != null) {
                    this.defaultPhoto = inputStream.readAllBytes();
                }
            } catch (NullPointerException | IOException _) {
                KendoTournamentLogger.severe(TournamentImageController.class.getName(), "No default diploma found!");
            }
        }
        return this.defaultPhoto;
    }

    public TournamentImage getDefaultImage(Tournament tournament, TournamentImageType type) {
        final TournamentImage tournamentImage = new TournamentImage();
        tournamentImage.setTournament(tournament);
        tournamentImage.setImageCompression(ImageCompression.PNG);
        if (type == null) {
            throw new NullPointerException("Image type cannot be null");
        }
        tournamentImage.setImageType(type);
        if (TournamentImageType.ACCREDITATION.equals(type)) {
            tournamentImage.setData(this.getDefaultAccreditation());
        } else if (TournamentImageType.BANNER.equals(type)) {
            tournamentImage.setData(this.getDefaultBanner());
        } else if (TournamentImageType.DIPLOMA.equals(type)) {
            tournamentImage.setData(this.getDefaultDiploma());
        } else {
            tournamentImage.setData(this.getDefaultPhoto());
        }
        return tournamentImage;
    }

    public TournamentImage add(MultipartFile file, Tournament tournament, TournamentImageType type,
                               ImageCompression imageCompression, String username) throws DataInputException {
        try {
            this.delete(tournament, type);
            final TournamentImage tournamentImage = new TournamentImage();
            tournamentImage.setTournament(tournament);
            tournamentImage.setData(file.getBytes());
            tournamentImage.setCreatedBy(username);
            tournamentImage.setImageType(type);
            tournamentImage.setImageCompression(imageCompression);
            this.tournamentRepository.save(tournament);
            return this.save(tournamentImage);
        } catch (IOException _) {
            throw new DataInputException(this.getClass(), "File creation failed.");
        }
    }

    public TournamentImage add(TournamentImage tournamentImage, String username) throws DataInputException {
        this.delete(tournamentImage.getTournament(), tournamentImage.getImageType());
        tournamentImage.setCreatedBy(username);
        this.tournamentRepository.save(tournamentImage.getTournament());
        return this.save(tournamentImage);
    }
}
