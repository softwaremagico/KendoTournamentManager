package com.softwaremagico.kt.core.providers;

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

import com.softwaremagico.kt.core.exceptions.DataInputException;
import com.softwaremagico.kt.core.images.ImageUtils;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.ParticipantImage;
import com.softwaremagico.kt.persistence.repositories.ParticipantImageRepository;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import com.softwaremagico.kt.persistence.values.ImageFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class ParticipantImageProvider extends CrudProvider<ParticipantImage, Integer, ParticipantImageRepository> {

    private final ParticipantRepository participantRepository;

    @Autowired
    public ParticipantImageProvider(ParticipantImageRepository repository, ParticipantRepository participantRepository) {
        super(repository);
        this.participantRepository = participantRepository;
    }

    public Optional<ParticipantImage> get(Participant participant) {
        return getRepository().findByParticipant(participant);
    }

    public List<ParticipantImage> getBy(Collection<Participant> participants) {
        return getRepository().findByParticipantIn(participants);
    }

    public int delete(Participant participant) {
        participant.setHasAvatar(false);
        participantRepository.save(participant);
        return getRepository().deleteByParticipant(participant);
    }

    public ParticipantImage add(MultipartFile file, Participant participant, String username) throws DataInputException {
        try {
            delete(participant);
            final ParticipantImage participantImage = new ParticipantImage();
            participantImage.setParticipant(participant);
            participantImage.setData(ImageUtils.getBytes(ImageUtils.cropImage(
                    ImageUtils.resizeImage(ImageUtils.getImage(file.getBytes())))));
            participantImage.setImageFormat(ImageFormat.BASE64);
            participantImage.setCreatedBy(username);
            participant.setHasAvatar(true);
            participantRepository.save(participant);
            return save(participantImage);
        } catch (IOException e) {
            throw new DataInputException(this.getClass(), "File creation failed.");
        }
    }


    public ParticipantImage add(ParticipantImage participantImage, String username) throws DataInputException {
        delete(participantImage.getParticipant());
        participantImage.setCreatedBy(username);
        try {
            participantImage.setData(ImageUtils.getBytes(ImageUtils.cropImage(
                    ImageUtils.resizeImage(ImageUtils.getImage(participantImage.getData())))));
        } catch (IOException e) {
            KendoTournamentLogger.warning(this.getClass(), "Image cannot be cropped");
        }
        final Participant participant = participantImage.getParticipant();
        participant.setHasAvatar(participantImage.getData() != null);
        participantRepository.save(participant);
        return save(participantImage);
    }
}
