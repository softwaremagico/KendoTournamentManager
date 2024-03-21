package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.ElementDTO;
import com.softwaremagico.kt.core.converters.ElementConverter;
import com.softwaremagico.kt.core.converters.models.ConverterRequest;
import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.CrudProvider;
import com.softwaremagico.kt.logger.ExceptionType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BasicInsertableController<ENTITY, DTO extends ElementDTO, REPOSITORY extends JpaRepository<ENTITY, Integer>,
        PROVIDER extends CrudProvider<ENTITY, Integer, REPOSITORY>, CONVERTER_REQUEST extends ConverterRequest<ENTITY>,
        CONVERTER extends ElementConverter<ENTITY, DTO, CONVERTER_REQUEST>>
        extends StandardController<ENTITY, DTO, REPOSITORY, PROVIDER> {

    private final Set<ElementCreatedListener> elementCreatedListeners = new HashSet<>();
    private final Set<ElementUpdatedListener> elementUpdatedListeners = new HashSet<>();
    private final Set<ElementDeletedListener> elementDeletedListeners = new HashSet<>();

    private final CONVERTER converter;

    public interface ElementCreatedListener {
        void created(ElementDTO element, String actor);
    }

    public interface ElementUpdatedListener {
        void updated(ElementDTO element, String actor);
    }

    public interface ElementDeletedListener {
        void deleted(ElementDTO element, String actor);
    }

    protected BasicInsertableController(PROVIDER provider, CONVERTER converter) {
        super(provider);
        this.converter = converter;
    }

    public CONVERTER getConverter() {
        return converter;
    }

    public void addElementCreatedListeners(ElementCreatedListener listener) {
        elementCreatedListeners.add(listener);
    }

    public void addElementUpdatedListeners(ElementUpdatedListener listener) {
        elementUpdatedListeners.add(listener);
    }

    public void addElementDeletedListeners(ElementDeletedListener listener) {
        elementDeletedListeners.add(listener);
    }


    public DTO get(Integer id) {
        final ENTITY entity = getProvider().get(id).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + id + "' not found.",
                ExceptionType.INFO));
        return convert(entity);
    }

    @Override
    public List<DTO> get() {
        return convertAll(getProvider().getAll());
    }

    @Transactional
    public DTO update(DTO dto, String username) {
        dto.setUpdatedBy(username);
        validate(dto);
        final DTO updatedDTO = convert(super.getProvider().save(reverse(dto)));

        try {
            return updatedDTO;
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    elementUpdatedListeners.forEach(elementUpdatedListener -> elementUpdatedListener.updated(updatedDTO, username))).start();
        }
    }

    @Transactional
    public List<DTO> updateAll(List<DTO> dtos, String username) {
        final List<DTO> refreshedData = new ArrayList<>();
        dtos.forEach(dto -> {
            dto.setUpdatedBy(username);
            refreshedData.add(convert(super.getProvider().save(reverse(dto))));
        });
        try {
            return refreshedData;
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    refreshedData.forEach(updatedDTO ->
                            elementUpdatedListeners.forEach(elementUpdatedListener -> elementUpdatedListener.updated(updatedDTO, username)))).start();
        }
    }

    @Transactional
    public DTO create(DTO dto, String username) {
        if (dto.getCreatedBy() == null && username != null) {
            dto.setCreatedBy(username);
        }
        validate(dto);
        final DTO savedDTO = convert(super.getProvider().save(reverse(dto)));

        try {
            return savedDTO;
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    elementCreatedListeners.forEach(elementCreatedListener -> elementCreatedListener.created(savedDTO, username))).start();
        }
    }

    @Transactional
    public List<DTO> create(Collection<DTO> dtos, String username) {
        dtos.forEach(dto -> {
            if (dto.getCreatedBy() == null && username != null) {
                dto.setCreatedBy(username);
            }
        });
        validate(dtos);
        final List<DTO> savedDTOs = convertAll(super.getProvider().save(reverseAll(dtos)));
        try {
            return savedDTOs;
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    savedDTOs.forEach(savedDTO ->
                            elementCreatedListeners.forEach(elementCreatedListener -> elementCreatedListener.created(savedDTO, username)))).start();
        }
    }


    public void delete(DTO entity, String username) {
        try {
            getProvider().delete(reverse(entity));
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    elementDeletedListeners.forEach(elementDeletedListener -> elementDeletedListener.deleted(entity, username))).start();
        }
    }

    public void delete(Collection<DTO> entities, String username) {
        try {
            getProvider().delete(reverseAll(entities));
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    entities.forEach(deletedDTO ->
                            elementDeletedListeners.forEach(elementDeletedListener -> elementDeletedListener.deleted(deletedDTO, username)))).start();
        }
    }

    public void deleteAll() {
        getProvider().deleteAll();
    }

    protected abstract CONVERTER_REQUEST createConverterRequest(ENTITY entity);

    protected List<CONVERTER_REQUEST> createConverterRequest(Collection<ENTITY> entities) {
        final List<CONVERTER_REQUEST> requests = new ArrayList<>();
        entities.forEach(entity -> requests.add(createConverterRequest(entity)));
        return requests;
    }

    protected DTO convert(ENTITY entity) {
        return converter.convert(createConverterRequest(entity));
    }

    protected ENTITY reverse(DTO dto) {
        return converter.reverse(dto);
    }

    protected List<DTO> convertAll(Collection<ENTITY> entities) {
        return converter.convertAll(entities.stream().map(this::createConverterRequest).toList());
    }

    protected List<ENTITY> reverseAll(Collection<DTO> dtos) {
        return converter.reverseAll(dtos);
    }

    @Override
    public void validate(DTO dto) throws ValidateBadRequestException {

    }

    @Override
    public void validate(Collection<DTO> dtos) throws ValidateBadRequestException {
        dtos.forEach(this::validate);
    }
}
