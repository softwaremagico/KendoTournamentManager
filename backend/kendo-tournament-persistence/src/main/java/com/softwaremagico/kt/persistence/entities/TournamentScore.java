package com.softwaremagico.kt.persistence.entities;

import com.softwaremagico.kt.persistence.encryption.TournamentTypeCryptoConverter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tournament_scores")
public class TournamentScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tournament_type")
    @Enumerated(EnumType.STRING)
    @Convert(converter = TournamentTypeCryptoConverter.class)
    private ScoreType scoreType;

    @Column(name = "points_by_victory")
    private int pointsByVictory = 1;

    @Column(name = "points_by_draw")
    private int pointsByDraw = 0;


    public ScoreType getScoreType() {
        return scoreType;
    }

    public void setScoreType(ScoreType scoreType) {
        this.scoreType = scoreType;
    }

    public int getPointsByVictory() {
        return pointsByVictory;
    }

    public void setPointsByVictory(int pointsByVictory) {
        this.pointsByVictory = pointsByVictory;
    }

    public int getPointsByDraw() {
        return pointsByDraw;
    }

    public void setPointsByDraw(int pointsByDraw) {
        this.pointsByDraw = pointsByDraw;
    }
}
