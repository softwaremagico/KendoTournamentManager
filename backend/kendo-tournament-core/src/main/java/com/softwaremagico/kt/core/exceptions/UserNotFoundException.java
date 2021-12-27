package com.softwaremagico.kt.core.exceptions;

import com.softwaremagico.kt.logger.ExceptionType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends NotFoundException {
    private static final long serialVersionUID = 3091553855925575861L;

    public UserNotFoundException(Class<?> clazz, String message, ExceptionType type) {
        super(clazz, message, type);
    }

    public UserNotFoundException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.WARNING);
    }

    public UserNotFoundException(Class<?> clazz) {
        this(clazz, "User not found");
    }

    public UserNotFoundException(Class<?> clazz, Throwable e) {
        super(clazz, e);
    }
}
