package com.softwaremagico.kt.websockets.models.messages;

public class AchievementGeneratedNumberParameters {
    private String tournamentName;
    private long achievementsNumber;

    public AchievementGeneratedNumberParameters(String tournamentName, long achievementsNumber) {
        this.tournamentName = tournamentName;
        this.achievementsNumber = achievementsNumber;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public long getAchievementsNumber() {
        return achievementsNumber;
    }

    public void setAchievementsNumber(long achievementsNumber) {
        this.achievementsNumber = achievementsNumber;
    }
}
