package com.softwaremagico.kt.persistence.dialect;

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

import org.hibernate.dialect.PostgreSQLDialect;

/**
 * PostgreSQL dialect used by the Kendo Tournament Manager.
 * <p>
 * See {@link KendoMySQLDialect} for the reason why the automatic generation of column
 * check constraints is disabled.
 * </p>
 */
public class KendoPostgreSQLDialect extends PostgreSQLDialect {

    @Override
    public boolean supportsColumnCheck() {
        return false;
    }
}
