package com.softwaremagico.kt.core.controller.models;

import com.softwaremagico.kt.persistence.entities.Participant;

import java.time.LocalDateTime;

public class TemporalToken {

    private String temporalToken;

    private LocalDateTime temporalTokenExpirationTime;

    public TemporalToken() {
        super();
    }

    public TemporalToken(Participant participant) {
        this();
        setTemporalToken(participant.getTemporalToken());
        setTemporalTokenExpirationTime(participant.getTemporalTokenExpiration());
    }


    public String getTemporalToken() {
        return temporalToken;
    }

    public void setTemporalToken(String temporalToken) {
        this.temporalToken = temporalToken;
    }

    public LocalDateTime getTemporalTokenExpirationTime() {
        return temporalTokenExpirationTime;
    }

    public void setTemporalTokenExpirationTime(LocalDateTime temporalTokenExpirationTime) {
        this.temporalTokenExpirationTime = temporalTokenExpirationTime;
    }
}
