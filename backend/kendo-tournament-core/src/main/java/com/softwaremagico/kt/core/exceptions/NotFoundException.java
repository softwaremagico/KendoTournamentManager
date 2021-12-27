package com.softwaremagico.kt.core.exceptions;

import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends LoggedException {
    private static final long serialVersionUID = 7032994111678894370L;

    public NotFoundException(Class<?> clazz, String message, ExceptionType type) {
        super(clazz, message, type, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.SEVERE, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(Class<?> clazz) {
        this(clazz, "Object not found");
    }

    public NotFoundException(Class<?> clazz, Throwable e) {
        super(clazz, e);
    }
}
