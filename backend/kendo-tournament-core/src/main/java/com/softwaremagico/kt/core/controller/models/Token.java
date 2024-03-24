package com.softwaremagico.kt.core.controller.models;

import com.softwaremagico.kt.persistence.entities.Participant;

public class Token {

    private String token;


    public Token() {
        super();
    }

    public Token(Participant participant) {
        this();
        setToken(participant.getToken());
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
