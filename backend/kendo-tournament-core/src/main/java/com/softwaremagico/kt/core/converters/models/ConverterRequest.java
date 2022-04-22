package com.softwaremagico.kt.core.converters.models;

import com.softwaremagico.kt.core.exceptions.UnexpectedValueException;
import com.softwaremagico.kt.logger.ExceptionType;

public class ConverterRequest<T> {
    public ConverterRequest(T entity) {
        this.entity = entity;
    }

    private T entity;

    public T getEntity() {
        if (entity == null) {
            throw new UnexpectedValueException(this.getClass(), "Entity could not be converted into a proper object.\n ", ExceptionType.WARNING);
        }
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
