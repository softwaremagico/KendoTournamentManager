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

import org.hibernate.dialect.MySQLDialect;

/**
 * MySQL dialect used by the Kendo Tournament Manager.
 * <p>
 * Hibernate 7 generates a {@code CHECK} constraint for every enum mapped column. For the
 * columns whose value is protected by an {@code AttributeConverter} the constraint is
 * rendered by running the converter over every enum constant, which produces the
 * encrypted representation of the value. As the encryption is not deterministic (a random
 * initialization vector is used), the generated constraint would only accept the very
 * first ciphertext of every enum constant and would reject any other value.
 * </p>
 * <p>
 * This dialect therefore disables the automatic generation of column check constraints.
 * The constraints of the tournament types and of the tournament extra properties are
 * managed by hand with the migration scripts shipped in {@code documents/migrations}.
 * </p>
 */
public class KendoMySQLDialect extends MySQLDialect {

    @Override
    public boolean supportsColumnCheck() {
        return false;
    }
}
