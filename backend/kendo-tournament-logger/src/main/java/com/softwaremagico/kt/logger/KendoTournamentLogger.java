package com.softwaremagico.kt.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines basic log behavior. Uses log4j.properties.
 */
public class KendoTournamentLogger extends BasicLogger {

    private static final Logger logger = LoggerFactory.getLogger(KendoTournamentLogger.class);

    /**
     * Events that have business meaning (i.e. creating category, deleting form,
     * ...). To follow user actions.
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void info(String className, String messageTemplate, Object... arguments) {
        info(logger, className, messageTemplate, arguments);
    }

    /**
     * Shows not critical errors. I.e. Email address not found, permissions not
     * allowed for this user, ...
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void warning(String className, String messageTemplate, Object... arguments) {
        warning(logger, className, messageTemplate, arguments);
    }

    /**
     * For following the trace of the execution. I.e. Knowing if the application
     * access to a method, opening database connection, etc.
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void debug(String className, String messageTemplate, Object... arguments) {
        debug(logger, className, messageTemplate, arguments);
    }

    /**
     * To log any not expected error that can cause application malfunction.
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void severe(String className, String messageTemplate, Object... arguments) {
        severe(logger, className, messageTemplate, arguments);
    }

    public static void errorMessage(Class<?> clazz, Throwable throwable) {
        errorMessageNotification(logger, clazz.getName(), throwable);
    }

    /**
     * To log java exceptions and log also the stack trace. If enabled, also can
     * send an email to the administrator to alert of the error.
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void errorMessage(String className, String messageTemplate, Object... arguments) {
        errorMessageNotification(logger, className, messageTemplate, arguments);
    }

    public static void errorMessage(Object object, Throwable throwable) {
        errorMessageNotification(logger, object.getClass().getName(), throwable);
    }

    public static boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
}
