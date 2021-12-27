package com.softwaremagico.kt.logger;

import org.springframework.http.HttpStatus;

public abstract class LoggedException extends RuntimeException {
	private static final long serialVersionUID = -2118048384077287599L;
	private HttpStatus status;

	protected LoggedException(Class<?> clazz, String message, ExceptionType type, HttpStatus status) {
		super(message);
		this.status = status;
		final String className = clazz.getName();
		switch (type) {
			case INFO:
				KendoTournamentLogger.info(className, message);
				break;
			case WARNING:
				KendoTournamentLogger.warning(className, message);
				break;
			case SEVERE:
				KendoTournamentLogger.severe(className, message);
				break;
			default:
				KendoTournamentLogger.debug(className, message);
				break;
		}
	}

	protected LoggedException(Class<?> clazz, Throwable e, HttpStatus status) {
		this(clazz, e);
		this.status = status;
	}

	public LoggedException(Class<?> clazz, Throwable e) {
		super(e);
		KendoTournamentLogger.errorMessage(clazz, e);
	}

	public HttpStatus getStatus() {
		return status;
	}
}
