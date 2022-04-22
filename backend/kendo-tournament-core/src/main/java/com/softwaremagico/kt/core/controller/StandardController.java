package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.Validates;
import com.softwaremagico.kt.core.providers.CrudProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public abstract class StandardController<ENTITY, DTO, REPOSITORY extends JpaRepository<ENTITY, Integer>,
        PROVIDER extends CrudProvider<ENTITY, Integer, REPOSITORY>> implements Validates<DTO> {
    protected final PROVIDER provider;

    protected StandardController(PROVIDER provider) {
        this.provider = provider;
    }

    public abstract Collection<DTO> get();

    public abstract DTO get(Integer id);

    public void deleteById(Integer id) {
        provider.deleteById(id);
    }

    public long count() {
        return provider.count();
    }
}
