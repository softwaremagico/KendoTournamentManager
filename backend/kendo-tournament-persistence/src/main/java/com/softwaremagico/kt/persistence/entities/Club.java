package com.softwaremagico.kt.persistence.entities;

import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.utils.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.text.Collator;
import java.util.Locale;

/**
 * Defines a club. A club is an organization where competitors came from. This
 * discrimination is not important now, but in future can be used to generate
 * statistics
 */
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "club")
public class Club implements Comparable<Club> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @Convert(converter = StringCryptoConverter.class)
    private String name = "";

    @Column(name = "country")
    @Convert(converter = StringCryptoConverter.class)
    private String country = "";

    @Column(name = "city")
    @Convert(converter = StringCryptoConverter.class)
    private String city = "";

    @Column(name = "address")
    @Convert(converter = StringCryptoConverter.class)
    private String address = "";

    @Column(name = "representative")
    private String representativeId = "";

    @Column(name = "email")
    @Convert(converter = StringCryptoConverter.class)
    private String email = "";

    @Column(name = "phone")
    @Convert(converter = StringCryptoConverter.class)
    private String phone = null;

    @Column(name = "web")
    @Convert(converter = StringCryptoConverter.class)
    private String web = "";

    public Club() {
    }

    public Club(String name, String country, String city) {
        setName(name);
        setCountry(country);
        setCity(city);
    }

    public Integer getId() {
        return id;
    }

    /**
     * Representative is a registered person of the club that will be use to
     * establish contact in future tournaments.
     *
     * @param representativeId Identification number of the registered person.
     * @param email
     * @param phone
     */
    public void setRepresentative(String representativeId, String email, String phone) {
        this.representativeId = representativeId;
        this.email = email;
        this.phone = phone;
    }

    private void setName(String value) {
        this.name = StringUtils.setCase(value);
    }

    public String getName() {
        return name;
    }

    /**
     * City of the club. Only is an information value.
     *
     * @param value
     */
    private void setCountry(String value) {
        country = StringUtils.setCase(value);
    }

    /**
     * City of the club. Only is an information value.
     *
     * @param value
     */
    private void setCity(String value) {
        city = StringUtils.setCase(value);
    }

    /**
     * Address of the club. Only is an information value.
     *
     * @param value
     */
    public void setAddress(String value) {
        address = StringUtils.setCase(value);
    }

    /**
     * Sets the email of the representative. For future contacts reference.
     *
     * @param email
     */
    public void setMail(String email) {
        this.email = email;
    }

    /**
     * Sets the phone of the representative. For future contacts reference.
     *
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRepresentative(String representative) {
        this.representativeId = representative;
    }

    public void storeWeb(String value) {
        web = value.trim();
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getWeb() {
        return web;
    }

    public String getMail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRepresentativeId() {
        return representativeId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Club)) {
            return false;
        }
        final Club otherClub = (Club) object;
        return this.name.equals(otherClub.name) && this.city.equals(otherClub.city);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.city != null ? this.city.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * A club is compared only using the name and the city.
     */
    @Override
    public int compareTo(Club c) {
        // Ignore accents
        final Collator collator = Collator.getInstance(new Locale("es"));
        collator.setStrength(Collator.SECONDARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);

        return collator.compare(getName() + getCity(), c.getName() + c.getCity());
    }


}
