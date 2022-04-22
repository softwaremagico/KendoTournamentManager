package com.softwaremagico.kt.core.converters.models;

import com.softwaremagico.kt.persistence.entities.Tournament;

public class TournamentConverterRequest extends ConverterRequest<Tournament> {
    public TournamentConverterRequest(Tournament entity) {
        super(entity);
    }
}
