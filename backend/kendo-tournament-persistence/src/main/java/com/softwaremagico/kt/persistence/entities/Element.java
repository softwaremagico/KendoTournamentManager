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

import com.softwaremagico.kt.persistence.encryption.SHA512HashGenerator;
import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Version;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class for all JPA-managed domain entities.
 * <p>
 * Provides common auditing fields (createdAt, createdBy, updatedAt, updatedBy), an
 * auto-generated primary key, an optimistic-locking version counter, and a SHA-512
 * hash of the {@code createdBy} / {@code updatedBy} values for tamper detection.
 * </p>
 * <p>
 * Field-level encryption of sensitive audit strings is delegated to
 * {@link com.softwaremagico.kt.persistence.encryption.StringCryptoConverter}.
 * The hashes are stored in plain text so they can be verified without decrypting
 * the originals.
 * </p>
 */
@MappedSuperclass
public abstract class Element implements Serializable {
    protected static final int MAX_UNIQUE_COLUMN_LENGTH = 190;

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Timestamp populated automatically by Hibernate when the row is first inserted. */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Username (encrypted at rest) of the user who created this record. */
    @Access(AccessType.PROPERTY)
    @Column(name = "created_by")
    @Convert(converter = StringCryptoConverter.class)
    private String createdBy;

    /** SHA-512 hash of {@code createdBy}, stored in plain text for integrity verification. */
    @Column(name = "created_by_hash", length = SHA512HashGenerator.ALGORITHM_LENGTH)
    @Convert(converter = SHA512HashGenerator.class)
    private String createdByHash;

    /** Timestamp updated automatically by Hibernate on every flush. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Username (encrypted at rest) of the user who last modified this record. */
    @Access(AccessType.PROPERTY)
    @Column(name = "updated_by")
    @Convert(converter = StringCryptoConverter.class)
    private String updatedBy;

    /** SHA-512 hash of {@code updatedBy}, stored in plain text for integrity verification. */
    @Column(name = "updated_by_hash", length = SHA512HashGenerator.ALGORITHM_LENGTH)
    @Convert(converter = SHA512HashGenerator.class)
    private String updatedByHash;

    /** Optimistic-locking version counter incremented by Hibernate on every update. */
    @Version
    private Integer version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the username of the creator and simultaneously updates the SHA-512 hash
     * used for tamper detection.
     *
     * @param createdBy the username to store
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        this.createdByHash = createdBy;
    }

    public String getCreatedByHash() {
        return createdByHash;
    }

    public void setCreatedByHash(String createdByHash) {
        this.createdByHash = createdByHash;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets the username of the last editor and simultaneously updates the SHA-512 hash
     * used for tamper detection.
     *
     * @param updatedBy the username to store
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        this.updatedByHash = updatedBy;
    }

    public String getUpdatedByHash() {
        return updatedByHash;
    }

    public void setUpdatedByHash(String updatedByHash) {
        this.updatedByHash = updatedByHash;
    }

    /**
     * Two entities are considered equal when they share the same database primary key.
     * A {@code null} ID (transient entity) is never equal to another entity.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Element element = (Element) o;
        return Objects.equals(id, element.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
