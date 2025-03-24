package com.softwaremagico.kt.rest.security;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service("securityService")
public class KendoSecurityService {

    public String getGuestPrivilege() {
        return "GUEST";
    }

    public String getViewerPrivilege() {
        return "VIEWER";
    }

    public String getAdminPrivilege() {
        return "ADMIN";
    }

    public String getEditorPrivilege() {
        return "EDITOR";
    }

    public String getParticipantPrivilege() {
        return "PARTICIPANT";
    }
}
