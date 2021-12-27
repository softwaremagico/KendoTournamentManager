package com.softwaremagico.kt.core.exceptions;

import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotValidInputException extends LoggedException {
    private static final long serialVersionUID = -5399325226777756514L;

    public NotValidInputException(Class<?> clazz, String message) {

        super(clazz, message, ExceptionType.INFO, HttpStatus.BAD_REQUEST);
    }

    public NotValidInputException(Class<?> clazz, String message, ExceptionType exceptionType) {

        super(clazz, message, exceptionType, HttpStatus.BAD_REQUEST);
    }
}
