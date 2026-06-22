package com.softwaremagico.kt.logger.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Logger)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.logger.Auditable;
import com.softwaremagico.kt.logger.FindBugsSuppressWarnings;
import com.softwaremagico.kt.logger.SuppressFBWarnings;
import org.testng.annotations.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@Test(groups = "loggerAdaptersTests")
public class LoggerAnnotationsCoverageTest {

    @Test
    public void shouldExposeRetentionAndTargetMetadata() throws NoSuchMethodException {
        final Retention auditableRetention = Auditable.class.getAnnotation(Retention.class);
        final Target auditableTarget = Auditable.class.getAnnotation(Target.class);

        assertNotNull(auditableRetention);
        assertNotNull(auditableTarget);
        assertEquals(auditableRetention.value(), RetentionPolicy.RUNTIME);

        final Retention suppressRetention = SuppressFBWarnings.class.getAnnotation(Retention.class);
        assertNotNull(suppressRetention);
        assertEquals(suppressRetention.value(), RetentionPolicy.CLASS);

        final Retention findBugsRetention = FindBugsSuppressWarnings.class.getAnnotation(Retention.class);
        assertNotNull(findBugsRetention);
        assertEquals(findBugsRetention.value(), RetentionPolicy.CLASS);
    }

    @Test
    public void shouldReadAnnotationValuesOnMethods() throws NoSuchMethodException {
        final Method method = AnnotatedMethods.class.getDeclaredMethod("sampleMethod");

        // CLASS retention annotations are not readable via reflection at runtime.
        final SuppressFBWarnings suppressFBWarnings = method.getAnnotation(SuppressFBWarnings.class);
        assertNull(suppressFBWarnings);

        final Method valueMethod = SuppressFBWarnings.class.getDeclaredMethod("value");
        final Method justificationMethod = SuppressFBWarnings.class.getDeclaredMethod("justification");
        assertNotNull(valueMethod.getDefaultValue());
        assertEquals(justificationMethod.getDefaultValue(), "");
    }

    private static class AnnotatedMethods {

        @Auditable
        @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH"}, justification = "test-justification")
        @FindBugsSuppressWarnings(value = {"SE_TRANSIENT_FIELD_NOT_RESTORED"})
        public void sampleMethod() {
            // no-op
        }
    }
}


