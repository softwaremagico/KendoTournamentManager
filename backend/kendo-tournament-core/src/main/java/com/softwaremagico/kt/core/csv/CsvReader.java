package com.softwaremagico.kt.core.csv;

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

import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.persistence.entities.Element;

import java.util.Arrays;
import java.util.List;

public abstract class CsvReader<E extends Element> {
    private static final String CSV_SEPARATOR = ";";
    private static final String LINE_SEPARATOR = "\\r?\\n";

    public abstract List<E> readCSV(String csvContent);

    protected void checkHeaders(String[] fileHeaders, String... elementHeaders) throws InvalidCsvFieldException {
        for (String header : fileHeaders) {
            if (getHeaderIndex(elementHeaders, header) < 0) {
                throw new InvalidCsvFieldException(this.getClass(), "Invalid header '" + header + "'.", header);
            }
        }
    }

    public String[] getHeaders(String content) {
        final String[] lines = content.replace("#", "").split(LINE_SEPARATOR);
        if (lines.length > 1) {
            return lines[0].split(CSV_SEPARATOR);
        }
        return new String[0];
    }


    public String[] getContent(String content) {
        final String[] lines = content.split("\\r?\\n");
        if (lines.length > 1) {
            return Arrays.copyOfRange(lines, 1, lines.length);
        }
        return new String[0];
    }

    public int getHeaderIndex(String[] headers, String header) {
        for (int i = 0; i < headers.length; i++) {
            if (header != null && headers[i] != null && headers[i].trim().equalsIgnoreCase(header.trim())) {
                return i;
            }
        }
        return -1;
    }

    public String getField(String line, int index) {
        if (index < 0) {
            return null;
        }
        final String[] columns = line.split(CSV_SEPARATOR);
        if (index < columns.length) {
            return columns[index].trim();
        }
        return null;
    }
}
