package com.softwaremagico.kt.rest.exceptions;


import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.logger.LoggedHttpException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such element")
public class InvalidRequestException extends LoggedHttpException {
    private static final long serialVersionUID = 7032994901678894370L;

    public InvalidRequestException(Class<?> clazz, String message, ExceptionType type) {
        super(clazz, message, type, HttpStatus.NOT_FOUND);
    }

    public InvalidRequestException(Class<?> clazz, String message) {
        super(clazz, message, ExceptionType.WARNING, HttpStatus.NOT_FOUND);
    }

    public InvalidRequestException(Class<?> clazz) {
        this(clazz, "Status not found");
    }

    public InvalidRequestException(Class<?> clazz, Throwable e) {
        super(clazz, e);
    }
}
