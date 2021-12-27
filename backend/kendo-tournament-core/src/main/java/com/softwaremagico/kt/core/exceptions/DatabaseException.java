package com.softwaremagico.kt.core.exceptions;

import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DatabaseException extends LoggedException {
    private static final long serialVersionUID = -1399325226733756592L;

    public DatabaseException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.SEVERE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DatabaseException(Class<?> clazz, String message, ExceptionType exceptionType) {
        super(clazz, message, exceptionType, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
