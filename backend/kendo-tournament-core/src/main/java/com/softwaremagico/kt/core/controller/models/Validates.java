package com.softwaremagico.kt.core.controller.models;


import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;

public interface Validates<T> {
    void validate(T dto) throws ValidateBadRequestException;
}
