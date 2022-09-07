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

import com.softwaremagico.kt.core.controller.models.ElementDTO;
import com.softwaremagico.kt.core.converters.ElementConverter;
import com.softwaremagico.kt.core.converters.models.ConverterRequest;
import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.CrudProvider;
import com.softwaremagico.kt.logger.ExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BasicInsertableController<ENTITY, DTO extends ElementDTO, REPOSITORY extends JpaRepository<ENTITY, Integer>,
        PROVIDER extends CrudProvider<ENTITY, Integer, REPOSITORY>, CONVERTER_REQUEST extends ConverterRequest<ENTITY>,
        CONVERTER extends ElementConverter<ENTITY, DTO, CONVERTER_REQUEST>>
        extends StandardController<ENTITY, DTO, REPOSITORY, PROVIDER> {

    protected CONVERTER converter;

    protected BasicInsertableController(PROVIDER provider, CONVERTER converter) {
        super(provider);
        this.converter = converter;
    }

    public DTO get(Integer id) {
        final ENTITY entity = provider.get(id).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + id + "' not found.",
                ExceptionType.INFO));
        return converter.convert(createConverterRequest(entity));
    }

    @Override
    public List<DTO> get() {
        return provider.getAll().parallelStream().map(this::createConverterRequest).map(converter::convert).collect(Collectors.toList());
    }

    @Transactional
    public DTO update(DTO dto, String username) {
        dto.setUpdatedBy(username);
        return create(dto, null);
    }

    @Transactional
    public List<DTO> updateAll(List<DTO> dtos, String username) {
        List<DTO> refreshedData = new ArrayList<>();
        dtos.forEach(dto -> {
            dto.setUpdatedBy(username);
            refreshedData.add(create(dto, null));
        });
        return refreshedData;
    }

    @Transactional
    public DTO create(DTO dto, String username) {
        if (dto.getCreatedBy() == null && username != null) {
            dto.setCreatedBy(username);
        }
        validate(dto);
        return converter.convert(createConverterRequest(super.provider.save(converter.
                reverse(dto))));
    }

    @Transactional
    public List<DTO> create(Collection<DTO> dtos, String username) {
        dtos.forEach(dto -> {
            if (dto.getCreatedBy() == null && username != null) {
                dto.setCreatedBy(username);
            }
        });
        validate(dtos);
        return converter.convertAll(createConverterRequest(super.provider.save(converter.
                reverseAll(dtos))));
    }


    public void delete(DTO entity) {
        provider.delete(converter.reverse(entity));
    }

    public void delete(Collection<DTO> entities) {
        provider.delete(converter.reverseAll(entities));
    }

    protected abstract CONVERTER_REQUEST createConverterRequest(ENTITY entity);

    protected List<CONVERTER_REQUEST> createConverterRequest(Collection<ENTITY> entities) {
        final List<CONVERTER_REQUEST> requests = new ArrayList<>();
        entities.forEach(entity -> requests.add(createConverterRequest(entity)));
        return requests;
    }

    @Override
    public void validate(DTO dto) throws ValidateBadRequestException {

    }

    @Override
    public void validate(Collection<DTO> dtos) throws ValidateBadRequestException {
        dtos.forEach(this::validate);
    }
}
