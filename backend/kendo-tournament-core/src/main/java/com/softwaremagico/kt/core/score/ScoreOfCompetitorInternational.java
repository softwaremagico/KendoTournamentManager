package com.softwaremagico.kt.core.score;


import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;

import java.util.List;

/**
 * Same as european
 */
public class ScoreOfCompetitorInternational extends ScoreOfCompetitorEuropean {

    public ScoreOfCompetitorInternational(Participant competitor, List<Fight> fights) {
        super(competitor, fights);
    }

}
