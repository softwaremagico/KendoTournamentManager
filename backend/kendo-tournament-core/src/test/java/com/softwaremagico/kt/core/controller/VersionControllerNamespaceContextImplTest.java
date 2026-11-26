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
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

@Test(groups = "namespaceContextImplTests")
public class VersionControllerNamespaceContextImplTest {

    @Test
    public void shouldReturnXmlnsUriForXmlnsPrefix() {
        final VersionController.NamespaceContextImpl context =
                new VersionController.NamespaceContextImpl("http://default", Map.of());

        assertEquals(context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE), XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }

    @Test
    public void shouldReturnXmlUriForXmlPrefix() {
        final VersionController.NamespaceContextImpl context =
                new VersionController.NamespaceContextImpl("http://default", Map.of());

        assertEquals(context.getNamespaceURI(XMLConstants.XML_NS_PREFIX), XMLConstants.XML_NS_URI);
    }

    @Test
    public void shouldReturnDefaultNamespaceForDefaultPrefix() {
        final VersionController.NamespaceContextImpl context =
                new VersionController.NamespaceContextImpl("http://default", Map.of());

        assertEquals(context.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX), "http://default");
    }

    @Test
    public void shouldReturnMappedNamespaceForKnownPrefix() {
        final VersionController.NamespaceContextImpl context =
                new VersionController.NamespaceContextImpl("http://default", Map.of("pom", "http://maven.apache.org/POM/4.0.0"));

        assertEquals(context.getNamespaceURI("pom"), "http://maven.apache.org/POM/4.0.0");
    }

    @Test
    public void shouldReturnNullNsUriForUnknownPrefix() {
        final VersionController.NamespaceContextImpl context =
                new VersionController.NamespaceContextImpl("http://default", Map.of());

        assertEquals(context.getNamespaceURI("unknown"), XMLConstants.NULL_NS_URI);
    }

    @Test
    public void shouldThrowExceptionWhenPrefixIsNull() {
        final VersionController.NamespaceContextImpl context =
                new VersionController.NamespaceContextImpl("http://default", Map.of());

        assertThrows(IllegalArgumentException.class, () -> context.getNamespaceURI(null));
    }

    @Test
    public void shouldThrowExceptionWhenGetPrefixCalled() {
        final VersionController.NamespaceContextImpl context =
                new VersionController.NamespaceContextImpl("http://default", Map.of());

        assertThrows(IllegalStateException.class, () -> context.getPrefix("any"));
    }

    @Test
    public void shouldThrowExceptionWhenGetPrefixesCalled() {
        final VersionController.NamespaceContextImpl context =
                new VersionController.NamespaceContextImpl("http://default", Map.of());

        assertThrows(IllegalStateException.class, () -> context.getPrefixes("any"));
    }
}

