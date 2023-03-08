package com.softwaremagico.kt.core.statistics;

public class FightStatistics {

    private Integer fightsNumber;
    private Integer fightsByTeam;
    private Integer duelsNumber;
    //In seconds.
    private Long time;

    public Integer getFightsNumber() {
        return fightsNumber;
    }

    public void setFightsNumber(Integer fightsNumber) {
        if (fightsNumber != null && fightsNumber >= 0) {
            this.fightsNumber = fightsNumber;
        } else {
            this.fightsNumber = null;
        }
    }

    public Integer getFightsByTeam() {
        return fightsByTeam;
    }

    public void setFightsByTeam(Integer fightsByTeam) {
        this.fightsByTeam = fightsByTeam;
    }

    public Integer getDuelsNumber() {
        return duelsNumber;
    }

    public void setDuelsNumber(Integer duelsNumber) {
        if (duelsNumber != null && duelsNumber >= 0) {
            this.duelsNumber = duelsNumber;
        } else {
            this.duelsNumber = null;
        }
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
