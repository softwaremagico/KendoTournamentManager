package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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


import com.softwaremagico.kt.persistence.encryption.ByteArrayCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.ImageFormatCryptoConverter;
import com.softwaremagico.kt.persistence.values.ImageFormat;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "participant_image")
public class ParticipantImage extends Element {
    // 2mb
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;


    @Lob
    @Column(name = "data", length = MAX_FILE_SIZE, nullable = false)
    @Convert(converter = ByteArrayCryptoConverter.class)
    private byte[] data;

    @Column(name = "format", nullable = false)
    @Enumerated(EnumType.STRING)
    @Convert(converter = ImageFormatCryptoConverter.class)
    private ImageFormat imageFormat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant", nullable = false)
    private Participant participant;

    public ParticipantImage() {
        super();
        imageFormat = ImageFormat.RAW;
    }

    public byte[] getData() {
        return (data == null) ? null : data.clone();
    }

    public void setData(byte[] data) {
        this.data = (data == null) ? null : data.clone();
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public ImageFormat getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(ImageFormat imageFormat) {
        this.imageFormat = imageFormat;
    }

    @Override
    public String toString() {
        return "ParticipantImage{participant='" + participant + "', size='" + getData().length + "'}";
    }

}
