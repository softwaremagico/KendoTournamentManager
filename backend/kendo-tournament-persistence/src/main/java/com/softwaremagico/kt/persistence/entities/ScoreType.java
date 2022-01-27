package com.softwaremagico.kt.persistence.entities;

public enum ScoreType {

    CLASSIC("classic"), WIN_OVER_DRAWS("winOverDraws"), EUROPEAN("european"), CUSTOM("custom"), INTERNATIONAL(
            "international");
    public static final ScoreType DEFAULT = ScoreType.INTERNATIONAL;
    private String tag;

    ScoreType(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static ScoreType getScoreType(String tag) {
        for (final ScoreType scoreType : ScoreType.values()) {
            if (scoreType.getTag().equals(tag.toLowerCase())) {
                return scoreType;
            }
        }
        return DEFAULT;
    }
}
