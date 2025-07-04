package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.persistence.entities.Participant;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TemporalToken {

    @NotNull
    private String content;

    @NotNull
    private LocalDateTime temporalTokenExpirationTime;

    public TemporalToken() {
        super();
    }

    public TemporalToken(Participant participant) {
        this();
        setContent(participant.getTemporalToken());
        setTemporalTokenExpirationTime(participant.getTemporalTokenExpiration());
    }

    public TemporalToken(String content) {
        this();
        setContent(content);
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTemporalTokenExpirationTime() {
        return temporalTokenExpirationTime;
    }

    public void setTemporalTokenExpirationTime(LocalDateTime temporalTokenExpirationTime) {
        this.temporalTokenExpirationTime = temporalTokenExpirationTime;
    }

    @Override
    public String toString() {
        return "TemporalToken{"
                + content + '}';
    }
}
