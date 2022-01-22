package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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


import com.softwaremagico.kt.persistence.encryption.ByteArrayCryptoConverter;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;

import javax.persistence.*;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tournament_image")
public class TournamentImage {
    // 2mb
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Lob
    @Column(length = MAX_FILE_SIZE, nullable = false)
    @Convert(converter = ByteArrayCryptoConverter.class)
    private byte[] data;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    private TournamentImageType imageType;


    public Integer getId() {
        return id;
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

    @Override
    public String toString() {
        return "TournamentImage{tournament='" + tournament + "', size='" + getData().length + "'}";
    }

}
