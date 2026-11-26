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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
public class VersionController {

    private static final String VERSION_URL = "https://raw.githubusercontent.com/softwaremagico/KendoTournamentManager/refs/heads/main/backend/pom.xml";
    private static final String POM_CONTEXT = "http://maven.apache.org/POM/4.0.0";
    private static final String POM = "pom";
    private static final String POM_VERSION_SEARCH = "/pom:project/pom:version";


    public String getLatestVersionFromGithub() throws XPathExpressionException, IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            final HttpGet request = new HttpGet(VERSION_URL);
            request.addHeader("content-type", "application/json");
            final HttpResponse result = httpClient.execute(request);
            final String pomXMl = EntityUtils.toString(result.getEntity(), StandardCharsets.UTF_8);

            final InputSource source = new InputSource(new StringReader(pomXMl));

            final XPathFactory xpathFactory = XPathFactory.newInstance();
            final XPath xpath = xpathFactory.newXPath();
            final Map<String, String> namespaces = new HashMap<>();
            namespaces.put(POM, POM_CONTEXT);
            xpath.setNamespaceContext(new NamespaceContextImpl(POM_CONTEXT, namespaces));
            final Document doc = (Document) xpath.evaluate("/", source, XPathConstants.NODE);
            return xpath.evaluate(POM_VERSION_SEARCH, doc);
        }
    }


    static class NamespaceContextImpl implements NamespaceContext {
        private final Map<String, String> namespaces;
        private final String defaultNamespaceURI;

        NamespaceContextImpl(String defaultNamespaceURI,
                             Map<String, String> namespaces) {
            this.defaultNamespaceURI = defaultNamespaceURI;
            this.namespaces = namespaces;
        }

        public Iterator getPrefixes(String namespaceURI) {
            throw new IllegalStateException("Not Implemented.");
        }

        public String getPrefix(String namespaceURI) {
            throw new IllegalStateException("Not Implemented.");
        }

        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException();
            }
            if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
            if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
                return XMLConstants.XML_NS_URI;
            }
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                return defaultNamespaceURI;
            }
            String result = namespaces.get(prefix);
            if (result == null) {
                result = XMLConstants.NULL_NS_URI;
            }
            return result;
        }
    }
}
