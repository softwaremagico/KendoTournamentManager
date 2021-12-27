package com.softwaremagico.kt.persistence.entities;


import com.softwaremagico.kt.persistence.encryption.ByteArrayCryptoConverter;

import javax.persistence.*;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Image {
    // 2mb
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Lob
    @Column(length = MAX_FILE_SIZE, nullable = false)
    @Convert(converter = ByteArrayCryptoConverter.class)
    private byte[] data;


    public Integer getId() {
        return id;
    }


    public byte[] getData() {
        return (data == null) ? null : data.clone();
    }

    public void setData(byte[] data) {
        this.data = (data == null) ? null : data.clone();
    }

}
