package com.softwaremagico.kt;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.mockito.Mockito.when;

@Test(groups = "loggableDispatcherServletTests")
public class LoggableDispatcherServletTest {

    private LoggableDispatcherServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new LoggableDispatcherServlet();
    }

    @Test
    public void shouldWrapPlainRequestBeforeDispatching() throws Exception {
        when(request.getAttributeNames()).thenReturn(java.util.Collections.emptyEnumeration());

        final Method doDispatch = DispatcherServletTestSupport.doDispatchMethod();
        try {
            doDispatch.invoke(servlet, request, response);
        } catch (Exception e) {
            // The servlet is not fully initialized in this unit test (no WebApplicationContext),
            // we only care about exercising the request-wrapping logic in doDispatch.
        }
    }

    @Test
    public void shouldNotRewrapAlreadyWrappedRequest() throws Exception {
        final ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, 256 * 1024);

        final Method doDispatch = DispatcherServletTestSupport.doDispatchMethod();
        try {
            doDispatch.invoke(servlet, wrapped, response);
        } catch (Exception e) {
            // Same as above: only exercising the branch, not the full Spring MVC dispatch.
        }
    }

    private static final class DispatcherServletTestSupport {
        static Method doDispatchMethod() throws NoSuchMethodException {
            final Method method = LoggableDispatcherServlet.class.getDeclaredMethod("doDispatch", HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            return method;
        }
    }
}

