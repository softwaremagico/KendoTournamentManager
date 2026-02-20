package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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


import com.softwaremagico.kt.persistence.encryption.ByteArrayCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.ImageCompressionCryptoConverter;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tournament_image", indexes = {
        @Index(name = "ind_tournament", columnList = "tournament"),
})
public class TournamentImage extends Element {
    // 2mb
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Lob
    @Column(length = MAX_FILE_SIZE, nullable = false)
    @Convert(converter = ByteArrayCryptoConverter.class)
    private byte[] data;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @Column(name = "image_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TournamentImageType imageType;

    @Column(name = "image_format", nullable = false)
    @Enumerated(EnumType.STRING)
    @Convert(converter = ImageCompressionCryptoConverter.class)
    private ImageCompression imageCompression;

    public TournamentImage() {
        super();
    }

    public byte[] getData() {
        return (data == null) ? null : data.clone();
    }

    public void setData(byte[] data) {
        this.data = (data == null) ? null : data.clone();
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
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

    @Override
    public String toString() {
        return "TournamentImage{tournament='" + tournament + "', size='" + getData().length + "'}";
    }

}
