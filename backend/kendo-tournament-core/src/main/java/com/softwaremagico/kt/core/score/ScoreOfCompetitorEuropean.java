package com.softwaremagico.kt.core.score;


import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.utils.NameUtils;

import java.util.List;

public class ScoreOfCompetitorEuropean extends ScoreOfCompetitor {

	public ScoreOfCompetitorEuropean(Participant competitor, List<Fight> fights) {
		super(competitor, fights);
	}

	@Override
	public int compareTo(ScoreOfCompetitor o) {
		if (getDuelsWon() > o.getDuelsWon()) {
			return -1;
		}
		if (getDuelsWon() < o.getDuelsWon()) {
			return 1;
		}

		if (getDuelsDraw() > o.getDuelsDraw()) {
			return -1;
		}
		if (getDuelsDraw() < o.getDuelsDraw()) {
			return 1;
		}

		if (getHits() > o.getHits()) {
			return -1;
		}
		if (getHits() < o.getHits()) {
			return 1;
		}

		// More duels done with same score is negative.
		if (getDuelsDone() > o.getDuelsDone()) {
			return 1;
		}

		if (getDuelsDone() < o.getDuelsDone()) {
			return -1;
		}

		// Draw score, order by name;
		return NameUtils.getLastnameName(getCompetitor()).compareTo(NameUtils.getLastnameName(o.getCompetitor()));
	}

}
