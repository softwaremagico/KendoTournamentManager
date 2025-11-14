package com.softwaremagico.kt.core.statistics;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.logger.SuppressFBWarnings;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Statistics are not stored into database, but to keep the data structure, this dummy class is needed for now.
 */
@Repository
public class ParticipantStatisticsRepository implements JpaRepository<ParticipantStatistics, Integer> {
    @Override
    public List<ParticipantStatistics> findAll() {
        return new ArrayList<>();
    }

    @Override
    public List<ParticipantStatistics> findAll(Sort sort) {
        return new ArrayList<>();
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public Page<ParticipantStatistics> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<ParticipantStatistics> findAllById(Iterable<Integer> integers) {
        return new ArrayList<>();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {
        //Not needed
    }

    @Override
    public void delete(ParticipantStatistics entity) {
        //Not needed
    }

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {
        //Not needed
    }

    @Override
    public void deleteAll(Iterable<? extends ParticipantStatistics> entities) {
        //Not needed
    }

    @Override
    public void deleteAll() {
        //Not needed
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends ParticipantStatistics> S save(S entity) {
        return null;
    }

    @Override
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public <S extends ParticipantStatistics> List<S> saveAll(Iterable<S> entities) {
        return new ArrayList<>();
    }

    @Override
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public Optional<ParticipantStatistics> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public void flush() {
        //Not needed
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends ParticipantStatistics> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends ParticipantStatistics> List<S> saveAllAndFlush(Iterable<S> entities) {
        return new ArrayList<>();
    }

    @Override
    public void deleteAllInBatch(Iterable<ParticipantStatistics> entities) {
        //Not needed
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {
        //Not needed
    }

    @Override
    public void deleteAllInBatch() {
        //Not needed
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public ParticipantStatistics getOne(Integer integer) {
        return null;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public ParticipantStatistics getById(Integer integer) {
        return null;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public ParticipantStatistics getReferenceById(Integer integer) {
        return null;
    }

    @Override
    public <S extends ParticipantStatistics> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends ParticipantStatistics> List<S> findAll(Example<S> example) {
        return new ArrayList<>();
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends ParticipantStatistics> List<S> findAll(Example<S> example, Sort sort) {
        return new ArrayList<>();
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends ParticipantStatistics> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ParticipantStatistics> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends ParticipantStatistics> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends ParticipantStatistics, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
