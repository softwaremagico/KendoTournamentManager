package com.softwaremagico.kt.persistence.entities;

import com.softwaremagico.kt.persistence.encryption.LocalDateCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "users")
public class User {
    public static final int MAX_UNIQUE_COLUMN_LENGTH = 190;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_card_number", length = MAX_UNIQUE_COLUMN_LENGTH)
    @Convert(converter = StringCryptoConverter.class)
    private String idCardNumber;

    @Column(length = MAX_UNIQUE_COLUMN_LENGTH, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String firstname;

    @Column(length = MAX_UNIQUE_COLUMN_LENGTH, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String lastname;

    @Column(length = MAX_UNIQUE_COLUMN_LENGTH, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String email;

    @Column(nullable = false)
    @Convert(converter = LocalDateCryptoConverter.class)
    private LocalDate birthdate;

    @Column(name = "phone_number", length = MAX_UNIQUE_COLUMN_LENGTH, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String phoneNumber;

    @Column(length = MAX_UNIQUE_COLUMN_LENGTH, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String address;

    @Column(name = "postal_code", length = MAX_UNIQUE_COLUMN_LENGTH, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String postalCode;

    @Column(length = MAX_UNIQUE_COLUMN_LENGTH, nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String city;

    public Integer getId() {
        return id;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "User{" + "email='" + email + "'}";
    }
}
