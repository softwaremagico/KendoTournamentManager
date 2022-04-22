package com.softwaremagico.kt.core.providers;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class CrudProvider<ENTITY, KEY, REPOSITORY extends JpaRepository<ENTITY, KEY>> {

    protected final REPOSITORY repository;

    public CrudProvider(REPOSITORY repository) {
        this.repository = repository;
    }

    public ENTITY save(ENTITY entity) {
        return repository.save(entity);
    }

    public Collection<ENTITY> saveAll(Collection<ENTITY> entity) {
        return repository.saveAll(entity);
    }

    public Optional<ENTITY> get(KEY id) {
        return repository.findById(id);
    }

    public List<ENTITY> getAll() {
        return repository.findAll();
    }

    public ENTITY update(ENTITY entity) {
        return repository.save(entity);
    }

    public void delete(ENTITY entity) {
        repository.delete(entity);
    }

    public void deleteById(KEY id) {
        repository.deleteById(id);
    }

    public void delete() {
        repository.deleteAll();
    }

    public long count() {
        return repository.count();
    }
}
