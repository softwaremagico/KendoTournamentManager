package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.converters.ElementConverter;
import com.softwaremagico.kt.core.converters.models.ConverterRequest;
import com.softwaremagico.kt.core.exceptions.ClubNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.CrudProvider;
import com.softwaremagico.kt.logger.ExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BasicInsertableController<ENTITY, DTO, REPOSITORY extends JpaRepository<ENTITY, Integer>,
        PROVIDER extends CrudProvider<ENTITY, Integer, REPOSITORY>, CONVERTER_REQUEST extends ConverterRequest<ENTITY>,
        CONVERTER extends ElementConverter<ENTITY, DTO, CONVERTER_REQUEST>>
        extends StandardController<ENTITY, DTO, REPOSITORY, PROVIDER> {

    protected CONVERTER converter;

    protected BasicInsertableController(PROVIDER provider, CONVERTER converter) {
        super(provider);
        this.converter = converter;
    }

    public DTO get(Integer id) {
        final ENTITY entity = provider.get(id).orElseThrow(() -> new ClubNotFoundException(getClass(), "Entity with id '" + id + "' not found.",
                ExceptionType.INFO));
        return converter.convert(createConverterRequest(entity));
    }

    @Override
    public List<DTO> get() {
        return provider.getAll().parallelStream().map(this::createConverterRequest).map(converter::convert).collect(Collectors.toList());
    }

    @Transactional
    public DTO update(DTO dto) {
        validate(dto);
        return converter.convert(createConverterRequest(super.provider.save(converter.
                reverse(dto))));
    }

    @Transactional
    public DTO create(DTO dto) {
        validate(dto);
        return converter.convert(createConverterRequest(super.provider.save(converter.
                reverse(dto))));
    }


    public void delete(DTO entity) {
        provider.delete(converter.reverse(entity));
    }

    protected abstract CONVERTER_REQUEST createConverterRequest(ENTITY entity);

    @Override
    public void validate(DTO dto) throws ValidateBadRequestException {

    }
}
