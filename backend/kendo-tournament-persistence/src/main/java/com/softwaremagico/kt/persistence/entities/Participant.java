package com.softwaremagico.kt.persistence.entities;

/*
 * #%L
 * KendoTournamentManager
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.persistence.encryption.BooleanCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.LocalDateTimeCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.security.AvailableRole;
import com.softwaremagico.kt.utils.IParticipantName;
import com.softwaremagico.kt.utils.NameUtils;
import com.softwaremagico.kt.utils.StringUtils;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A registered person is any person that is in a tournament. Can be a
 * competitor, referee, public, etc.
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "participants",
        indexes = {
                @Index(name = "ind_club", columnList = "club"),
                @Index(name = "ind_token", columnList = "temporal_token"),
        })
public class Participant extends Element implements Comparable<Participant>, IParticipantName, UserDetails, IAuthenticatedUser {

    public static final String PARTICIPANT_ROLE = "participant";

    //Token expiration in seconds.
    private static final int TEMPORARY_TOKEN_DURATION = 5 * 60;

    //Account expiration in days.
    private static final int TEMPORARY_PARTICIPANT_ACCOUNT_DURATION = 365;
    private static final int TOKEN_LENGTH = 15;

    @Column(name = "id_card", unique = true)
    @Convert(converter = StringCryptoConverter.class)
    private String idCard;

    @Column(name = "name", nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String name = "";

    @Column(name = "lastname", nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String lastname = "";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "club", nullable = false)
    private Club club;

    @Column(name = "has_avatar")
    @Convert(converter = BooleanCryptoConverter.class)
    private Boolean hasAvatar = false;

    @Column(name = "temporal_token", length = TOKEN_LENGTH, unique = true)
    private String temporalToken;

    @Column(name = "temporal_token_expiration")
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime temporalTokenExpiration;

    @Column(name = "token", length = TOKEN_LENGTH)
    private String token;

    @Column(name = "account_expiration")
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime accountExpiration;

    public Participant() {
        super();
    }


    public Participant(String idCard, String name, String lastname, Club club) {
        setName(name);
        setLastname(lastname);
        setIdCard(idCard);
        setClub(club);
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String value) {
        idCard = value.replace("-", "").replace(" ", "").trim().toUpperCase();
    }

    public boolean isValid() {
        return getName().length() > 0 && getIdCard() != null && getIdCard().length() > 0;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = StringUtils.setCase(value);
    }

    @Override
    public String getLastname() {
        return lastname;
    }

    public void setLastname(String value) {
        lastname = StringUtils.setCase(value);
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public Boolean getHasAvatar() {
        return hasAvatar;
    }

    public void setHasAvatar(Boolean hasAvatar) {
        this.hasAvatar = hasAvatar;
    }

    /**
     * Compare participants avoiding accent problems.
     *
     * @param otherParticipant
     * @return
     */
    @Override
    public int compareTo(Participant otherParticipant) {
        final String string1 = this.lastname + " " + this.name;
        final String string2 = otherParticipant.lastname + " " + otherParticipant.name;

        // Ignore accents
        final Collator collator = Collator.getInstance(new Locale("es"));
        collator.setStrength(Collator.SECONDARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);

        return collator.compare(string1, string2);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTemporalToken() {
        return temporalToken;
    }

    public void setTemporalToken(String token) {
        this.temporalToken = token;
    }

    public LocalDateTime getTemporalTokenExpiration() {
        return temporalTokenExpiration;
    }

    public void setTemporalTokenExpiration(LocalDateTime tokenExpiration) {
        this.temporalTokenExpiration = tokenExpiration;
    }

    public LocalDateTime getAccountExpiration() {
        return accountExpiration;
    }

    public void setAccountExpiration(LocalDateTime accountExpiration) {
        this.accountExpiration = accountExpiration;
    }

    public void generateTemporalToken() {
        setTemporalToken(StringUtils.generateRandomToken(TOKEN_LENGTH));
        setTemporalTokenExpiration(LocalDateTime.now().plusSeconds(TEMPORARY_TOKEN_DURATION));
    }

    public void generateToken() {
        setToken(StringUtils.generateRandomToken(TOKEN_LENGTH));
        setAccountExpiration(LocalDateTime.now().plusDays(TEMPORARY_PARTICIPANT_ACCOUNT_DURATION));
    }

    @Override
    public String toString() {
        return NameUtils.getLastnameName(getLastname(), getName());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(AvailableRole.ROLE_PARTICIPANT.name()));
    }

    @Override
    public String getPassword() {
        return getToken();
    }

    @Override
    public String getUsername() {
        return getName() + "_" + getLastname() + "_" + getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountExpiration != null && LocalDateTime.now().isBefore(accountExpiration);
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return accountExpiration != null && LocalDateTime.now().isBefore(accountExpiration);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Set<String> getRoles() {
        return new HashSet<>(List.of(PARTICIPANT_ROLE));
    }
}
