package com.softwaremagico.kt.core.controller.models;

public class QrCodeDTO extends ImageDTO {

    private TournamentDTO tournament;



    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournament) {
        this.tournament = tournament;
    }
}
