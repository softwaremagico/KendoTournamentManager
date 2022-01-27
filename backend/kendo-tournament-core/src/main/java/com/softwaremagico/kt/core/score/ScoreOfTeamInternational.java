package com.softwaremagico.kt.core.score;

import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;

import java.util.List;

public class ScoreOfTeamInternational extends ScoreOfTeam {

	public ScoreOfTeamInternational(Team team, List<Fight> fights) {
		super(team, fights);
	}

	@Override
	public int compareTo(ScoreOfTeam o) {
		if (getWonFights() > o.getWonFights()) {
			return -1;
		}

		if (getWonFights() < o.getWonFights()) {
			return 1;
		}

		if (getDrawFights() > o.getDrawFights()) {
			return -1;
		}

		if (getDrawFights() < o.getDrawFights()) {
			return 1;
		}

		if (getWonDuels() > o.getWonDuels()) {
			return -1;
		}

		if (getWonDuels() < o.getWonDuels()) {
			return 1;
		}

		if (getHits() > o.getHits()) {
			return -1;
		}

		if (getHits() < o.getHits()) {
			return 1;
		}

		return o.getGoldenPoints().compareTo(getGoldenPoints());

	}
}
