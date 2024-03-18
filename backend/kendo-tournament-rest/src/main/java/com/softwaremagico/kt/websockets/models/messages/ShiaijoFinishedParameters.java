package com.softwaremagico.kt.websockets.models.messages;

public class ShiaijoFinishedParameters {
    private String tournamentName;
    private String shiaijoName;

    public ShiaijoFinishedParameters(String tournamentName, String shiaijoName) {
        this.tournamentName = tournamentName;
        this.shiaijoName = shiaijoName;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public String getShiaijoName() {
        return shiaijoName;
    }

    public void setShiaijoName(String shiaijoName) {
        this.shiaijoName = shiaijoName;
    }
}
