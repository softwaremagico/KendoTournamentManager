package com.softwaremagico.kt.logger;

import org.slf4j.Logger;

public abstract class BasicLogger {

    protected BasicLogger() {
        super();
    }

    /**
     * Shows not critical errors. I.e. Email address not found, permissions not
     * allowed for this user, ...
     *
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void warning(Logger logger, String messageTemplate, Object... arguments) {
        logger.warn(messageTemplate, arguments);
    }

    /**
     * Shows not critical errors. I.e. Email address not found, permissions not
     * allowed for this user, ...
     *
     * @param logger          the Logger.
     * @param className       the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void warning(Logger logger, String className, String messageTemplate, Object... arguments) {
        logger.warn(className + ": " + messageTemplate, arguments);
    }

    /**
     * Events that have business meaning (i.e. creating category, deleting form,
     * ...). To follow user actions.
     *
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void info(Logger logger, String messageTemplate, Object... arguments) {
        logger.info(messageTemplate, arguments);
    }

    /**
     * Events that have business meaning (i.e. creating category, deleting form,
     * ...). To follow user actions.
     * <p>
     *
     * @param logger          the Logger.
     * @param className       the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void info(Logger logger, String className, String messageTemplate, Object... arguments) {
        info(logger, className + ": " + messageTemplate, arguments);
    }

    /**
     * For following the trace of the execution. I.e. Knowing if the application
     * access to a method, opening database connection, etc.
     *
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void debug(Logger logger, String messageTemplate, Object... arguments) {
        if (logger.isDebugEnabled()) {
            logger.debug(messageTemplate, arguments);
        }
    }

    /**
     * For following the trace of the execution. I.e. Knowing if the application
     * access to a method, opening database connection, etc.
     *
     * @param logger          the Logger.
     * @param className       the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void debug(Logger logger, String className, String messageTemplate, Object... arguments) {
        if (logger.isDebugEnabled()) {
            logger.debug(className + ": " + messageTemplate, arguments);
        }
    }

    /**
     * To log any not expected error that can cause application malfunctions.
     * I.e. couldn't open database connection, etc..
     *
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    protected static void severe(Logger logger, String messageTemplate, Object... arguments) {
        logger.error(messageTemplate, arguments);
    }

    /**
     * To log any not expected error that can cause application malfunctions.
     *
     * @param logger          the Logger.
     * @param className       the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void severe(Logger logger, String className, String messageTemplate, Object... arguments) {
        severe(logger, className + ": " + messageTemplate, arguments);
    }

    /**
     * Logs an error and send an email to the email configured in settings.conf
     * file.
     *
     * @param logger          the Logger.
     * @param className       the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void errorMessageNotification(Logger logger, String className, String messageTemplate, Object... arguments) {
        severe(logger, className, messageTemplate, arguments);
    }

    public static void errorMessageNotification(Logger logger, String className, Throwable throwable) {
        logger.error("Exception on class {}:\n", className, throwable);
    }

}
