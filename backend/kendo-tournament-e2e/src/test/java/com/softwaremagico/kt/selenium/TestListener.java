package com.softwaremagico.kt.selenium;

/*-
 * #%L
 * Kendo Tournament Manager (End-to-End Tests)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.testng.ITestContext;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.logging.Logger;

/**
 * Reports lifecycle and duration information for each browser workflow.
 */
public class TestListener implements ITestListener, IConfigurationListener {
    private static final Logger LOGGER = Logger.getLogger(TestListener.class.getName());

    @Override
    public void onTestStart(ITestResult result) {
        LOGGER.info(() -> "### Test started '" + testName(result) + "'.");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOGGER.info(() -> "### Test finished '" + testName(result) + "' (" + duration(result) + "ms).");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.severe(() -> "### Test failed '" + testName(result) + "' (" + duration(result) + "ms)."
                + " Cause: " + (result.getThrowable() == null ? "unknown" : result.getThrowable().getMessage()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOGGER.warning(() -> "### Test skipped '" + testName(result) + "'. Cause: "
                + (result.getThrowable() == null ? "configuration failure" : result.getThrowable().getMessage()));
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        LOGGER.severe(() -> "### Configuration failed '" + testName(result) + "'. Cause: "
                + (result.getThrowable() == null ? "unknown" : result.getThrowable().getMessage()));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Not used by the E2E suite.
    }

    @Override
    public void onStart(ITestContext context) {
        LOGGER.info(() -> "##### Starting E2E test context '" + context.getName() + "'.");
    }

    @Override
    public void onFinish(ITestContext context) {
        LOGGER.info(() -> "##### Finished E2E test context '" + context.getName() + "'.");
    }

    private String testName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }

    private long duration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
