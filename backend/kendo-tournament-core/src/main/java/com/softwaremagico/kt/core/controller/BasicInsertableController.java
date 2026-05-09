package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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
import java.util.stream.Collectors;

/**
 * Abstract generic controller that adds full CRUD lifecycle management to
 * {@link StandardController}, including DTO↔entity conversion and event propagation.
 * <p>
 * Concrete subclasses bind the six generic type parameters to a specific domain
 * aggregate (e.g. Tournament, Fight, Participant) and inherit create, update, and
 * delete operations that:
 * <ol>
 *   <li>Convert incoming DTOs to entities via the bound {@link CONVERTER}.</li>
 *   <li>Delegate persistence to the {@link PROVIDER}.</li>
 *   <li>Notify registered {@link ElementCreatedListener}, {@link ElementUpdatedListener}
 *       or {@link ElementDeletedListener} observers (e.g. WebSocket broadcast).</li>
 * </ol>
 * </p>
 * <p>
 * All write operations are transactional by default.
 * </p>
 *
 * @param <ENTITY>           the JPA entity type
 * @param <DTO>              the data-transfer object type exposed by the REST layer
 * @param <REPOSITORY>       the JPA repository for the entity
 * @param <PROVIDER>         the CRUD provider delegating to the repository
 * @param <CONVERTER_REQUEST> the converter request wrapper
 * @param <CONVERTER>        the converter that maps between entity and DTO
 */
public abstract class BasicInsertableController<ENTITY, DTO extends ElementDTO, REPOSITORY extends JpaRepository<ENTITY, Integer>,
        PROVIDER extends CrudProvider<ENTITY, Integer, REPOSITORY>, CONVERTER_REQUEST extends ConverterRequest<ENTITY>,
        CONVERTER extends ElementConverter<ENTITY, DTO, CONVERTER_REQUEST>>
        extends StandardController<ENTITY, DTO, REPOSITORY, PROVIDER> {

    private final Set<ElementCreatedListener> elementCreatedListeners = new HashSet<>();
    private final Set<ElementUpdatedListener> elementUpdatedListeners = new HashSet<>();
    private final Set<ElementDeletedListener> elementDeletedListeners = new HashSet<>();

    private final CONVERTER converter;

    public interface ElementCreatedListener {
        void created(ElementDTO element, String actor, String session);
    }

    public interface ElementUpdatedListener {
        void updated(ElementDTO element, String actor, String session);
    }

    public interface ElementDeletedListener {
        void deleted(ElementDTO element, String actor, String session);
    }

    protected BasicInsertableController(PROVIDER provider, CONVERTER converter) {
        super(provider);
        this.converter = converter;
    }

    public CONVERTER getConverter() {
        return converter;
    }

    /**
     * Registers a listener that is notified asynchronously when an entity is created.
     *
     * @param listener the listener to register
     */
    public void addElementCreatedListeners(ElementCreatedListener listener) {
        elementCreatedListeners.add(listener);
    }

    /**
     * Registers a listener that is notified asynchronously when an entity is updated.
     *
     * @param listener the listener to register
     */
    public void addElementUpdatedListeners(ElementUpdatedListener listener) {
        elementUpdatedListeners.add(listener);
    }

    /**
     * Registers a listener that is notified asynchronously when an entity is deleted.
     *
     * @param listener the listener to register
     */
    public void addElementDeletedListeners(ElementDeletedListener listener) {
        elementDeletedListeners.add(listener);
    }


    /**
     * Retrieves the entity with the given ID and converts it to a DTO.
     *
     * @param id the primary key of the entity
     * @return the entity as a DTO
     * @throws com.softwaremagico.kt.core.exceptions.NotFoundException if no entity with the given ID exists
     */
    public DTO get(Integer id) {
        final ENTITY entity = getProvider().get(id).orElseThrow(() -> new NotFoundException(getClass(), "Entity with id '" + id + "' not found.",
                ExceptionType.INFO));
        return convert(entity);
    }

    @Override
    public List<DTO> get() {
        return convertAll(getProvider().getAll());
    }

    @Override
    public List<DTO> get(Collection<Integer> ids) {
        return convertAll(getProvider().get(ids));
    }

    /**
     * Persists the given DTO, notifying all registered {@link ElementUpdatedListener}s
     * asynchronously after the transaction commits.
     *
     * @param dto      the entity data to persist
     * @param username the authenticated user performing the update
     * @param session  the client session identifier for WebSocket notifications
     * @return the updated entity as a DTO
     */
    @Transactional
    public DTO update(DTO dto, String username, String session) {
        dto.setUpdatedBy(username);
        validate(dto);
        final DTO updatedDTO = convert(super.getProvider().save(reverse(dto)));

        try {
            return updatedDTO;
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    elementUpdatedListeners.forEach(elementUpdatedListener ->
                            elementUpdatedListener.updated(updatedDTO, username, session))).start();
        }
    }

    /**
     * Persists all DTOs in the list, notifying registered {@link ElementUpdatedListener}s
     * asynchronously for each updated entity.
     *
     * @param dtos     the list of entity data to persist
     * @param username the authenticated user performing the update
     * @param session  the client session identifier for WebSocket notifications
     * @return the updated entities as DTOs, in the same order as the input list
     */
    @Transactional
    public List<DTO> updateAll(List<DTO> dtos, String username, String session) {
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
                            elementUpdatedListeners.forEach(elementUpdatedListener ->
                                    elementUpdatedListener.updated(updatedDTO, username, session)))).start();
        }
    }

    /**
     * Validates, persists and converts a single DTO, then notifies all registered
     * {@link ElementCreatedListener}s asynchronously.
     *
     * @param dto      the entity data to create
     * @param username the authenticated user performing the creation
     * @param session  the client session identifier for WebSocket notifications
     * @return the persisted entity as a DTO
     */
    @Transactional
    public DTO create(DTO dto, String username, String session) {
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
                    elementCreatedListeners.forEach(elementCreatedListener ->
                            elementCreatedListener.created(savedDTO, username, session))).start();
        }
    }

    /**
     * Validates, persists and converts a collection of DTOs, notifying
     * {@link ElementCreatedListener}s for each created entity.
     *
     * @param dtos     the entities to create
     * @param username the authenticated user performing the creation
     * @param session  the client session identifier for WebSocket notifications
     * @return the persisted entities as a list of DTOs
     */
    @Transactional
    public List<DTO> create(Collection<DTO> dtos, String username, String session) {
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
                            elementCreatedListeners.forEach(elementCreatedListener ->
                                    elementCreatedListener.created(savedDTO, username, session)))).start();
        }
    }


    /**
     * Deletes the entity represented by the given DTO, then notifies all registered
     * {@link ElementDeletedListener}s asynchronously.
     *
     * @param entity   the DTO of the entity to delete
     * @param username the authenticated user performing the deletion
     * @param session  the client session identifier for WebSocket notifications
     */
    public void delete(DTO entity, String username, String session) {
        try {
            getProvider().delete(reverse(entity));
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    elementDeletedListeners.forEach(elementDeletedListener ->
                            elementDeletedListener.deleted(entity, username, session))).start();
        }
    }

    /**
     * Deletes all entities represented by the given DTOs, notifying
     * {@link ElementDeletedListener}s for each deleted entity.
     *
     * @param entities the DTOs of the entities to delete
     * @param username the authenticated user performing the deletion
     * @param session  the client session identifier for WebSocket notifications
     */
    public void delete(Collection<DTO> entities, String username, String session) {
        try {
            getProvider().delete(reverseAll(entities));
        } finally {
            //Advise the frontend!
            new Thread(() ->
                    entities.forEach(deletedDTO ->
                            elementDeletedListeners.forEach(elementDeletedListener ->
                                    elementDeletedListener.deleted(deletedDTO, username, session)))).start();
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

    /**
     * Converts a JPA entity to its corresponding DTO using the bound converter.
     *
     * @param entity the entity to convert
     * @return the converted DTO
     */
    protected DTO convert(ENTITY entity) {
        return converter.convert(createConverterRequest(entity));
    }

    /**
     * Converts a DTO back to its corresponding JPA entity using the bound converter.
     *
     * @param dto the DTO to reverse-convert
     * @return the corresponding entity
     */
    protected ENTITY reverse(DTO dto) {
        return converter.reverse(dto);
    }

    protected List<DTO> convertAll(Collection<ENTITY> entities) {
        return new ArrayList<>(converter.convertAll(entities.stream().map(this::createConverterRequest)
                .collect(Collectors.toCollection(ArrayList::new))));
    }

    protected List<DTO> convertAllNotSorted(Collection<ENTITY> entities) {
        return new ArrayList<>(converter.convertAllNotSorted(entities.stream().map(this::createConverterRequest)
                .collect(Collectors.toCollection(ArrayList::new))));
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
