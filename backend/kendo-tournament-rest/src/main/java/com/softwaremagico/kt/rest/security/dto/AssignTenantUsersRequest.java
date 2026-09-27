package com.softwaremagico.kt.rest.security.dto;

import java.util.List;

public class AssignTenantUsersRequest {
    private List<String> usernames;

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
}
