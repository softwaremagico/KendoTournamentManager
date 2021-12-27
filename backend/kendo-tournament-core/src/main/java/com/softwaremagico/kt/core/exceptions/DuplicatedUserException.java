package com.softwaremagico.kt.core.exceptions;

import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DuplicatedUserException extends LoggedException {
    private static final long serialVersionUID = 3091553855925575861L;

    public DuplicatedUserException(Class<?> clazz, String message, ExceptionType type) {
        super(clazz, message, type, HttpStatus.NOT_FOUND);
    }

    public DuplicatedUserException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.WARNING, HttpStatus.NOT_FOUND);
    }

    public DuplicatedUserException(Class<?> clazz) {
        this(clazz, "User not found");
    }

    public DuplicatedUserException(Class<?> clazz, Throwable e) {
        super(clazz, e);
    }
}
