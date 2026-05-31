package com.softwaremagico.kt.pdf.tests;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
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

import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "pdfExceptions")
public class PdfExceptionsTests {

	@Test
	public void testEmptyPdfBodyExceptionCreationWithMessage() {
		final String message = "PDF Body is empty";
		final EmptyPdfBodyException exception = new EmptyPdfBodyException(message);
		Assert.assertNotNull(exception);
		Assert.assertEquals(exception.getMessage(), message);
	}

	@Test
	public void testEmptyPdfBodyExceptionWithNullMessage() {
		final EmptyPdfBodyException exception = new EmptyPdfBodyException(null);
		Assert.assertNotNull(exception);
		Assert.assertNull(exception.getMessage());
	}

	@Test
	public void testEmptyPdfBodyExceptionIsException() {
		final EmptyPdfBodyException exception = new EmptyPdfBodyException("test");
		Assert.assertTrue(exception instanceof Exception);
	}

	@Test
	public void testEmptyPdfBodyExceptionCanBeCaught() {
		try {
			throw new EmptyPdfBodyException("test exception");
		} catch (final EmptyPdfBodyException e) {
			Assert.assertEquals(e.getMessage(), "test exception");
		}
	}

	@Test
	public void testEmptyPdfBodyExceptionSerialization() {
		// Verify that exception can be serialized (if needed)
		final EmptyPdfBodyException exception = new EmptyPdfBodyException("serializable test");
		Assert.assertNotNull(exception);
	}

	@Test
	public void testInvalidXmlElementExceptionCreationWithMessage() {
		final String message = "Invalid XML element";
		final InvalidXmlElementException exception = new InvalidXmlElementException(message);
		Assert.assertNotNull(exception);
		Assert.assertEquals(exception.getMessage(), message);
	}

	@Test
	public void testInvalidXmlElementExceptionWithMessageAndCause() {
		final String message = "Invalid XML element";
		final Exception cause = new Exception("Root cause");
		final InvalidXmlElementException exception = new InvalidXmlElementException(message, cause);
		Assert.assertNotNull(exception);
		Assert.assertEquals(exception.getMessage(), message);
		Assert.assertEquals(exception.getCause(), cause);
	}

	@Test
	public void testInvalidXmlElementExceptionWithNullMessage() {
		final InvalidXmlElementException exception = new InvalidXmlElementException(null);
		Assert.assertNotNull(exception);
		Assert.assertNull(exception.getMessage());
	}

	@Test
	public void testInvalidXmlElementExceptionWithNullCause() {
		final InvalidXmlElementException exception = new InvalidXmlElementException("message", null);
		Assert.assertNotNull(exception);
		Assert.assertEquals(exception.getMessage(), "message");
		Assert.assertNull(exception.getCause());
	}

	@Test
	public void testInvalidXmlElementExceptionIsException() {
		final InvalidXmlElementException exception = new InvalidXmlElementException("test");
		Assert.assertTrue(exception instanceof Exception);
	}

	@Test
	public void testInvalidXmlElementExceptionCanBeCaughtAsSingleArg() {
		try {
			throw new InvalidXmlElementException("test exception");
		} catch (final InvalidXmlElementException e) {
			Assert.assertEquals(e.getMessage(), "test exception");
			Assert.assertNull(e.getCause());
		}
	}

	@Test
	public void testInvalidXmlElementExceptionCanBeCaughtWithCause() {
		try {
			throw new InvalidXmlElementException("test exception", new RuntimeException("cause"));
		} catch (final InvalidXmlElementException e) {
			Assert.assertEquals(e.getMessage(), "test exception");
			Assert.assertNotNull(e.getCause());
			Assert.assertTrue(e.getCause() instanceof RuntimeException);
		}
	}

	@Test
	public void testInvalidXmlElementExceptionSerialization() {
		// Verify that exception can be serialized (if needed)
		final InvalidXmlElementException exception = new InvalidXmlElementException("serializable test");
		Assert.assertNotNull(exception);
	}

	@Test
	public void testEmptyPdfBodyExceptionDifferentMessages() {
		final EmptyPdfBodyException exception1 = new EmptyPdfBodyException("message 1");
		final EmptyPdfBodyException exception2 = new EmptyPdfBodyException("message 2");
		Assert.assertNotEquals(exception1.getMessage(), exception2.getMessage());
	}

	@Test
	public void testInvalidXmlElementExceptionCauseChain() {
		final RuntimeException rootCause = new RuntimeException("root");
		final InvalidXmlElementException exception = new InvalidXmlElementException("wrapped", rootCause);
		Assert.assertEquals(exception.getCause(), rootCause);
		Assert.assertEquals(exception.getCause().getMessage(), "root");
	}

	@Test
	public void testExceptionMessagePreservation() {
		final String complexMessage = "XML Element <tag> with attribute='value' is invalid";
		final InvalidXmlElementException exception = new InvalidXmlElementException(complexMessage);
		Assert.assertEquals(exception.getMessage(), complexMessage);
	}

	@Test
	public void testExceptionStackTracePreservation() {
		try {
			throw new EmptyPdfBodyException("Error occurred in PDF generation");
		} catch (final EmptyPdfBodyException e) {
			Assert.assertNotNull(e.getStackTrace());
			Assert.assertTrue(e.getStackTrace().length > 0);
		}
	}
}



