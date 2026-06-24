package com.softwaremagico.kt.logger;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class FrontendLoggerTest {

    @Test(groups = {"restErrorResponse"})
    public void shouldLogMessagesUsingAllFacadeMethods() {
        FrontendLogger.info(this.getClass(), "info {}", "value");
        FrontendLogger.info(this.getClass().getName(), "info {}", "value");

        FrontendLogger.warning(this.getClass(), "warning {}", "value");
        FrontendLogger.warning(this.getClass().getName(), "warning {}", "value");

        FrontendLogger.debug(this.getClass(), "debug {}", "value");
        FrontendLogger.debug(this.getClass().getName(), "debug {}", "value");

        FrontendLogger.severe(this.getClass(), "severe {}", "value");
        FrontendLogger.severe(this.getClass().getName(), "severe {}", "value");

        FrontendLogger.errorMessage(this.getClass(), new IllegalStateException("boom"));
        FrontendLogger.errorMessage(this, new IllegalArgumentException("boom"));
        FrontendLogger.errorMessage(this.getClass().getName(), "error {}", "value");

        assertNotNull(Boolean.valueOf(FrontendLogger.isDebugEnabled()));
    }
}

