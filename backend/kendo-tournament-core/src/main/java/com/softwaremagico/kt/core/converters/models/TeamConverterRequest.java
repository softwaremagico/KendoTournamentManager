package com.softwaremagico.kt.core.converters.models;

import com.softwaremagico.kt.persistence.entities.Team;

public class TeamConverterRequest extends ConverterRequest<Team> {
    public TeamConverterRequest(Team entity) {
        super(entity);
    }
}
