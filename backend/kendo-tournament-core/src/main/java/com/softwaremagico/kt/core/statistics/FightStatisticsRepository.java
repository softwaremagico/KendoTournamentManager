package com.softwaremagico.kt.core.statistics;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


/**
 * Statistics are not stored into database, but to keep the data structure, this dummy class is needed for now.
 */
@Repository
public class FightStatisticsRepository implements JpaRepository<FightStatistics, Integer> {
    @Override
    public List<FightStatistics> findAll() {
        return null;
    }

    @Override
    public List<FightStatistics> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<FightStatistics> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<FightStatistics> findAllById(Iterable<Integer> integers) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void delete(FightStatistics entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {

    }

    @Override
    public void deleteAll(Iterable<? extends FightStatistics> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends FightStatistics> S save(S entity) {
        return null;
    }

    @Override
    public <S extends FightStatistics> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<FightStatistics> findById(Integer integer) {
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
    public <S extends FightStatistics> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends FightStatistics> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<FightStatistics> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public FightStatistics getOne(Integer integer) {
        return null;
    }

    @Override
    public FightStatistics getById(Integer integer) {
        return null;
    }

    @Override
    public <S extends FightStatistics> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends FightStatistics> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends FightStatistics> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends FightStatistics> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends FightStatistics> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends FightStatistics> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends FightStatistics, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
