package com.softwaremagico.kt.logger;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "encryptorLogger")
public class EncryptorLoggerTests {

    @Test
    public void testInfoWithClassName() {
        // Should not throw any exception
        EncryptorLogger.info(EncryptorLoggerTests.class, "Test message");
        Assert.assertTrue(true);
    }

    @Test
    public void testInfoWithArguments() {
        // Should not throw any exception
        EncryptorLogger.info(EncryptorLoggerTests.class, "Test message with {}", "arg1");
        Assert.assertTrue(true);
    }

    @Test
    public void testWarningWithClassName() {
        // Should not throw any exception
        EncryptorLogger.warning(EncryptorLoggerTests.class, "Warning message");
        Assert.assertTrue(true);
    }

    @Test
    public void testWarningWithArguments() {
        // Should not throw any exception
        EncryptorLogger.warning(EncryptorLoggerTests.class, "Warning message with {}", "arg1");
        Assert.assertTrue(true);
    }

    @Test
    public void testDebugWithClassName() {
        // Should not throw any exception
        EncryptorLogger.debug(EncryptorLoggerTests.class, "Debug message");
        Assert.assertTrue(true);
    }

    @Test
    public void testDebugWithArguments() {
        // Should not throw any exception
        EncryptorLogger.debug(EncryptorLoggerTests.class, "Debug message with {}", "arg1");
        Assert.assertTrue(true);
    }

    @Test
    public void testSevereWithClassName() {
        // Should not throw any exception
        EncryptorLogger.severe(EncryptorLoggerTests.class, "Severe message");
        Assert.assertTrue(true);
    }

    @Test
    public void testSevereWithArguments() {
        // Should not throw any exception
        EncryptorLogger.severe(EncryptorLoggerTests.class, "Severe message with {}", "arg1");
        Assert.assertTrue(true);
    }

    @Test
    public void testErrorMessageWithClassName() {
        // Should not throw any exception
        EncryptorLogger.errorMessage(EncryptorLoggerTests.class, "Error message");
        Assert.assertTrue(true);
    }

    @Test
    public void testErrorMessageWithClass() {
        // Should not throw any exception
        Exception exception = new Exception("Test exception");
        EncryptorLogger.errorMessage(EncryptorLoggerTests.class, exception);
        Assert.assertTrue(true);
    }

    @Test
    public void testErrorMessageWithObject() {
        // Should not throw any exception
        Exception exception = new Exception("Test exception");
        EncryptorLogger.errorMessage(new Object(), exception);
        Assert.assertTrue(true);
    }

    @Test
    public void testIsDebugEnabled() {
        // Should return a boolean value without throwing exception
        boolean debugEnabled = EncryptorLogger.isDebugEnabled();
        Assert.assertTrue(debugEnabled == true || debugEnabled == false);
    }

    @Test
    public void testMultipleArguments() {
        // Should not throw any exception with multiple arguments
        EncryptorLogger.info(EncryptorLoggerTests.class, "Message with {} and {}", "arg1", "arg2");
        Assert.assertTrue(true);
    }

    @Test
    public void testErrorMessageWithMultipleArguments() {
        // Should not throw any exception with multiple arguments
        EncryptorLogger.errorMessage(EncryptorLoggerTests.class, "Error with {}", "detailedInfo");
        Assert.assertTrue(true);
    }
}

