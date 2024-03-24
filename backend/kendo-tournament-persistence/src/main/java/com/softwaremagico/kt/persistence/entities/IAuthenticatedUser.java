package com.softwaremagico.kt.persistence.entities;

public interface IAuthenticatedUser {

    Integer getId();

    String getUsername();

    void setPassword(String password);
}
