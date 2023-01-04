package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.softwaremagico.kt.persistence.entities.ImageFormat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ParticipantImageDTO extends ElementDTO {
    private static final String BASE64_PREFIX = "data:image/png;base64,";
    private ParticipantDTO participant;
    private byte[] data;
    private ImageFormat imageFormat;

    public ParticipantDTO getParticipant() {
        return participant;
    }

    public void setParticipant(ParticipantDTO participant) {
        this.participant = participant;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @JsonGetter
    public String getBase64() {
        if (data == null) {
            return null;
        }
        return BASE64_PREFIX + Base64.getEncoder().encodeToString(data);
    }

    @JsonSetter
    public void setBase64(String base64) {
        if (base64 != null) {
            this.data = Base64.getDecoder().decode(base64.replaceFirst(BASE64_PREFIX, "").getBytes(StandardCharsets.UTF_8));
        }
    }

    public ImageFormat getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(ImageFormat imageFormat) {
        this.imageFormat = imageFormat;
    }
}
