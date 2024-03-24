package com.softwaremagico.kt.core.controller.models;

import com.softwaremagico.kt.persistence.entities.Participant;

import java.time.LocalDateTime;

public class Token {

    private String token;
    private LocalDateTime expiration;

    private ParticipantDTO participant;


    public Token() {
        super();
    }

    public Token(Participant participant) {
        this();
        setToken(participant.getToken());
        setExpiration(participant.getAccountExpiration());
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public ParticipantDTO getParticipant() {
        return participant;
    }

    public void setParticipant(ParticipantDTO participant) {
        this.participant = participant;
    }
}
