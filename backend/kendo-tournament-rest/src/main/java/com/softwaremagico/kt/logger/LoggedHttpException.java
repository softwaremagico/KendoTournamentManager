package com.softwaremagico.kt.logger;

import org.springframework.http.HttpStatus;

public abstract class LoggedHttpException extends RuntimeException {
    private static final long serialVersionUID = -2118048384077287599L;
    private HttpStatus status;

    protected LoggedHttpException(Class<?> clazz, String message, ExceptionType type, HttpStatus status) {
        super(message);
        this.status = status;
        final String className = clazz.getName();
        switch (type) {
            case INFO:
                RestServerLogger.info(className, message);
                break;
            case WARNING:
                RestServerLogger.warning(className, message);
                break;
            case SEVERE:
                RestServerLogger.severe(className, message);
                break;
            default:
                RestServerLogger.debug(className, message);
                break;
        }
    }

    protected LoggedHttpException(Class<?> clazz, Throwable e, HttpStatus status) {
        this(clazz, e);
        this.status = status;
    }

    protected LoggedHttpException(Class<?> clazz, String message, Throwable e, HttpStatus status) {
        this(clazz, message, e);
        this.status = status;
    }

    public LoggedHttpException(Class<?> clazz, Throwable e) {
        super(e);
        RestServerLogger.errorMessage(clazz, e);
    }

    protected LoggedHttpException(Class<?> clazz, String message, Throwable e) {
        super(message, e);
        RestServerLogger.errorMessage(clazz, e);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
