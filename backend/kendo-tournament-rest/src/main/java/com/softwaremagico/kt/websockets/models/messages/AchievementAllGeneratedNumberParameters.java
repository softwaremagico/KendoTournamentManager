package com.softwaremagico.kt.websockets.models.messages;

public class AchievementAllGeneratedNumberParameters {
    private int tournamentNumber;
    private long achievementsNumber;

    public AchievementAllGeneratedNumberParameters(int tournamentNumber, long achievementsNumber) {
        this.tournamentNumber = tournamentNumber;
        this.achievementsNumber = achievementsNumber;
    }

    public int getTournamentNumber() {
        return tournamentNumber;
    }

    public void setTournamentNumber(int tournamentNumber) {
        this.tournamentNumber = tournamentNumber;
    }

    public long getAchievementsNumber() {
        return achievementsNumber;
    }

    public void setAchievementsNumber(long achievementsNumber) {
        this.achievementsNumber = achievementsNumber;
    }
}
