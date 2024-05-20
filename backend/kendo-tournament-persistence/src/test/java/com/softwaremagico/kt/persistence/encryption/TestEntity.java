package com.softwaremagico.kt.persistence.encryption;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

@Entity
public class TestEntity {
    private static final String STRING_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int STRING_LENGTH = 1000;
    private static final int BYTES_LENGTH = 1000;
    private static final int COLUMN_LENGTH = 1000;
    private static final SecureRandom random = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = StringCryptoConverter.class)
    @Column(length = 4 * COLUMN_LENGTH, nullable = false)
    private String stringColumn;

    @Column(length = 4 * COLUMN_LENGTH, nullable = false)
    @Convert(converter = IntegerCryptoConverter.class)
    private Integer intColumn;

    @Column(length = 4 * COLUMN_LENGTH, nullable = false)
    @Convert(converter = LongCryptoConverter.class)
    private Long longColumn;

    @Column(length = 4 * COLUMN_LENGTH, nullable = false)
    @Convert(converter = DoubleCryptoConverter.class)
    private Double doubleColumn;

    @Column(length = 4 * COLUMN_LENGTH, nullable = false)
    @Convert(converter = ByteArrayCryptoConverter.class)
    private byte[] bytesColumn;

    @Column(length = 4 * COLUMN_LENGTH, nullable = false)
    @Convert(converter = LocalDateCryptoConverter.class)
    private LocalDate localDateColumn;

    @Column(length = 4 * COLUMN_LENGTH, nullable = false)
    @Convert(converter = LocalDateTimeCryptoConverter.class)
    private LocalDateTime localDateTimeColumn;

    @Column(length = 4 * COLUMN_LENGTH, nullable = false)
    @Convert(converter = TimestampCryptoConverter.class)
    private Timestamp timestampColumn;

    public TestEntity() {

    }

    public static TestEntity newEntity() {
        TestEntity randomEntity = new TestEntity();
        randomEntity.setData();
        return randomEntity;
    }

    public void setData() {
        setStringColumn(generateRandomString());
        setIntColumn(generateRandomInteger());
        setLongColumn(generateRandomLong());
        setDoubleColumn(generateRandomDouble());
        setBytesColumn(generateRandomBytes());
        setLocalDateColumn(LocalDate.now());
        setLocalDateTimeColumn(LocalDateTime.now());
        setTimestampColumn(new Timestamp(System.currentTimeMillis()));
    }

    public String getStringColumn() {
        return stringColumn;
    }

    public void setStringColumn(String stringColumn) {
        this.stringColumn = stringColumn;
    }

    public Integer getIntColumn() {
        return intColumn;
    }

    public void setIntColumn(Integer intColumn) {
        this.intColumn = intColumn;
    }

    public Long getLongColumn() {
        return longColumn;
    }

    public void setLongColumn(Long longColumn) {
        this.longColumn = longColumn;
    }

    public Double getDoubleColumn() {
        return doubleColumn;
    }

    public void setDoubleColumn(Double doubleColumn) {
        this.doubleColumn = doubleColumn;
    }

    public byte[] getBytesColumn() {
        return bytesColumn;
    }

    public void setBytesColumn(byte[] bytesColumn) {
        this.bytesColumn = bytesColumn;
    }

    public LocalDate getLocalDateColumn() {
        return localDateColumn;
    }

    public void setLocalDateColumn(LocalDate localDateColumn) {
        this.localDateColumn = localDateColumn;
    }

    public LocalDateTime getLocalDateTimeColumn() {
        return localDateTimeColumn;
    }

    public void setLocalDateTimeColumn(LocalDateTime localDateTimeColumn) {
        this.localDateTimeColumn = localDateTimeColumn;
    }

    public Timestamp getTimestampColumn() {
        return timestampColumn;
    }

    public void setTimestampColumn(Timestamp timestampColumn) {
        this.timestampColumn = timestampColumn;
    }

    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(STRING_LENGTH);
        for (int i = 0; i < STRING_LENGTH; i++) {
            sb.append(STRING_CHARACTERS.charAt(random.nextInt(STRING_CHARACTERS.length())));
        }
        return sb.toString();
    }

    private byte[] generateRandomBytes() {
        byte[] b = new byte[BYTES_LENGTH];
        random.nextBytes(b);
        return b;
    }

    private long generateRandomLong() {
        return new Random().nextLong();
    }

    public Integer generateRandomInteger() {
        return new Random().nextInt();
    }

    public Double generateRandomDouble() {
        return new Random().nextDouble();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestEntity that = (TestEntity) o;
        return Objects.equals(stringColumn, that.stringColumn) && Objects.equals(intColumn, that.intColumn) &&
                Objects.equals(longColumn, that.longColumn) && Objects.equals(doubleColumn, that.doubleColumn) &&
                Arrays.equals(bytesColumn, that.bytesColumn) && Objects.equals(localDateColumn, that.localDateColumn) &&
                Objects.equals(localDateTimeColumn, that.localDateTimeColumn) && Objects.equals(timestampColumn, that.timestampColumn);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(stringColumn, intColumn, longColumn, doubleColumn, localDateColumn, localDateTimeColumn, timestampColumn);
        result = 31 * result + Arrays.hashCode(bytesColumn);
        return result;
    }

    @Override
    public String toString() {
        return getIntColumn().toString();
    }
}
