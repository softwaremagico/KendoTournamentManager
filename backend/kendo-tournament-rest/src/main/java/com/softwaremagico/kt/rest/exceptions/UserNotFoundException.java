package com.softwaremagico.kt.rest.exceptions;

import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedHttpException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserNotFoundException extends LoggedHttpException {
    private static final long serialVersionUID = 7032994901678894370L;

    public UserNotFoundException(Class<?> clazz, String message, ExceptionType type) {
        super(clazz, message, type, HttpStatus.BAD_REQUEST);
    }

    public UserNotFoundException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.WARNING, HttpStatus.BAD_REQUEST);
    }

    public UserNotFoundException(Class<?> clazz) {
        this(clazz, "Invalid parameters");
    }

    public UserNotFoundException(Class<?> clazz, Throwable e) {
        super(clazz, e);
    }
}
