package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import org.testng.annotations.Test;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

/**
 * Covers {@link VersionController.NamespaceContextImpl}, the only part of {@link VersionController}
 * that can be exercised without performing a real network call to GitHub.
 */
@Test(groups = "versionController")
public class VersionControllerTest {

    private static final String DEFAULT_NS = "http://maven.apache.org/POM/4.0.0";

    private NamespaceContext newContext() {
        final Map<String, String> namespaces = new HashMap<>();
        namespaces.put("pom", DEFAULT_NS);
        return new VersionController.NamespaceContextImpl(DEFAULT_NS, namespaces);
    }

    @Test
    public void getNamespaceURI_withXmlnsPrefix_expectXmlnsNamespaceUri() {
        final NamespaceContext context = newContext();
        assertEquals(context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE), XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }

    @Test
    public void getNamespaceURI_withXmlPrefix_expectXmlNamespaceUri() {
        final NamespaceContext context = newContext();
        assertEquals(context.getNamespaceURI(XMLConstants.XML_NS_PREFIX), XMLConstants.XML_NS_URI);
    }

    @Test
    public void getNamespaceURI_withDefaultPrefix_expectDefaultNamespace() {
        final NamespaceContext context = newContext();
        assertEquals(context.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX), DEFAULT_NS);
    }

    @Test
    public void getNamespaceURI_withKnownPrefix_expectMappedNamespace() {
        final NamespaceContext context = newContext();
        assertEquals(context.getNamespaceURI("pom"), DEFAULT_NS);
    }

    @Test
    public void getNamespaceURI_withUnknownPrefix_expectNullNamespaceUri() {
        final NamespaceContext context = newContext();
        assertEquals(context.getNamespaceURI("unknown"), XMLConstants.NULL_NS_URI);
    }

    @Test
    public void getNamespaceURI_withNullPrefix_expectIllegalArgumentException() {
        final NamespaceContext context = newContext();
        assertThrows(IllegalArgumentException.class, () -> context.getNamespaceURI(null));
    }

    @Test
    public void getPrefix_expectIllegalStateException() {
        final NamespaceContext context = newContext();
        assertThrows(IllegalStateException.class, () -> context.getPrefix(DEFAULT_NS));
    }

    @Test
    public void getPrefixes_expectIllegalStateException() {
        final NamespaceContext context = newContext();
        assertThrows(IllegalStateException.class, () -> context.getPrefixes(DEFAULT_NS));
    }
}

