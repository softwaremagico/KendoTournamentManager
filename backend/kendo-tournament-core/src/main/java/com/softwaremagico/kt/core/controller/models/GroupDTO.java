package com.softwaremagico.kt.core.controller.models;

import java.util.List;

public class GroupDTO extends ElementDTO {

    private TournamentDTO tournament;

    private List<TeamDTO> teams;

    private Integer shiaijo;

    private Integer level;

    private List<FightDTO> fights;

    private Integer numberOfWinners;

    private List<DuelDTO> unties;

    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournamentDTO) {
        this.tournament = tournamentDTO;
    }

    public List<TeamDTO> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamDTO> teams) {
        this.teams = teams;
    }

    public Integer getShiaijo() {
        return shiaijo;
    }

    public void setShiaijo(Integer shiaijo) {
        this.shiaijo = shiaijo;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<FightDTO> getFights() {
        return fights;
    }

    public void setFights(List<FightDTO> fights) {
        this.fights = fights;
    }

    public Integer getNumberOfWinners() {
        return numberOfWinners;
    }

    public void setNumberOfWinners(Integer numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }

    public List<DuelDTO> getUnties() {
        return unties;
    }

    public void setUnties(List<DuelDTO> unties) {
        this.unties = unties;
    }
}
