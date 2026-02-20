package com.softwaremagico.kt.core.controller.models;

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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.util.Base64;

public class TournamentImageDTO extends ElementDTO {

    @Serial
    private static final long serialVersionUID = 1880511590408058653L;

    private static final String IMAGE_PNG_BASE_64 = "data:image/png;base64,";
    private static final String IMAGE_JPG_BASE_64 = "data:image/jpeg;base64,";


    @NotNull
    private TournamentDTO tournament;

    @NotNull
    private byte[] data;

    @NotNull
    private TournamentImageType imageType;

    @NotNull
    private ImageCompression imageCompression;

    private boolean defaultImage = false;

    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournament) {
        this.tournament = tournament;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public TournamentImageType getImageType() {
        return imageType;
    }

    public void setImageType(TournamentImageType imageType) {
        this.imageType = imageType;
    }

    public ImageCompression getImageCompression() {
        return imageCompression;
    }

    public void setImageCompression(ImageCompression imageCompression) {
        this.imageCompression = imageCompression;
    }

    public boolean isDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(boolean defaultImage) {
        this.defaultImage = defaultImage;
    }

    @JsonGetter
    public String getBase64() {
        if (data == null) {
            return null;
        }
        return switch (imageCompression) {
            case JPG -> IMAGE_JPG_BASE_64 + Base64.getEncoder().encodeToString(data);
            case PNG -> IMAGE_PNG_BASE_64 + Base64.getEncoder().encodeToString(data);
        };
    }
}
