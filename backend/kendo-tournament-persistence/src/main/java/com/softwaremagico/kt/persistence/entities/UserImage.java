package com.softwaremagico.kt.persistence.entities;


import com.softwaremagico.kt.persistence.encryption.ByteArrayCryptoConverter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "user_image")
public class UserImage {
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
    @JoinColumn(name = "user", nullable = false)
    private User user;


    public Integer getId() {
        return id;
    }


    public byte[] getData() {
        return (data == null) ? null : data.clone();
    }

    public void setData(byte[] data) {
        this.data = (data == null) ? null : data.clone();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserImage{user='" + user + "', size='" + getData().length + "'}";
    }

}
