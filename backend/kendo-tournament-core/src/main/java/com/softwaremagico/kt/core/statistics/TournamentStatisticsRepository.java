package com.softwaremagico.kt.core.statistics;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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
public class TournamentStatisticsRepository implements JpaRepository<TournamentStatistics, Integer> {
    @Override
    public List<TournamentStatistics> findAll() {
        return new ArrayList<>();
    }

    @Override
    public List<TournamentStatistics> findAll(Sort sort) {
        return new ArrayList<>();
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public Page<TournamentStatistics> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<TournamentStatistics> findAllById(Iterable<Integer> integers) {
        return new ArrayList<>();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void delete(TournamentStatistics entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {

    }

    @Override
    public void deleteAll(Iterable<? extends TournamentStatistics> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends TournamentStatistics> S save(S entity) {
        return null;
    }

    @Override
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public <S extends TournamentStatistics> List<S> saveAll(Iterable<S> entities) {
        return new ArrayList<>();
    }

    @Override
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public Optional<TournamentStatistics> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends TournamentStatistics> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends TournamentStatistics> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<TournamentStatistics> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public TournamentStatistics getOne(Integer integer) {
        return null;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public TournamentStatistics getById(Integer integer) {
        return null;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public TournamentStatistics getReferenceById(Integer integer) {
        return null;
    }

    @Override
    public <S extends TournamentStatistics> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends TournamentStatistics> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends TournamentStatistics> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends TournamentStatistics> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends TournamentStatistics> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends TournamentStatistics> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    public <S extends TournamentStatistics, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
