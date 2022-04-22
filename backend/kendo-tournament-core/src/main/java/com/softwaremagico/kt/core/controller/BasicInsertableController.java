package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.converters.ElementConverter;
import com.softwaremagico.kt.core.converters.models.ConverterRequest;
import com.softwaremagico.kt.core.exceptions.ClubNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.CrudProvider;
import com.softwaremagico.kt.logger.ExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Collection;
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
    public Collection<DTO> get() {
        return provider.getAll().parallelStream().map(this::createConverterRequest).map(converter::convert).collect(Collectors.toList());
    }

    @Transactional
    protected DTO update(DTO dto) {
        validate(dto);
        return converter.convert(createConverterRequest(super.provider.save(converter.
                reverse(dto))));
    }

    @Transactional
    protected DTO create(DTO dto) {
        validate(dto);
        return converter.convert(createConverterRequest(super.provider.save(converter.
                reverse(dto))));
    }

    protected abstract CONVERTER_REQUEST createConverterRequest(ENTITY entity);

    @Override
    public void validate(DTO dto) throws ValidateBadRequestException {

    }
}
