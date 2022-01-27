package com.softwaremagico.kt.core.score;


import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;

import java.util.List;

public abstract class ScoreOfTeam implements Comparable<ScoreOfTeam> {

	private Team team;
	private List<Fight> fights;
	private Integer wonFights = null;
	private Integer drawFights = null;
	private Integer wonDuels = null;
	private Integer drawDuels = null;
	private Integer goldenPoint = null;
	private Integer hits = null;

	public ScoreOfTeam(Team team, List<Fight> fights) {
		this.team = team;
		this.fights = fights;
	}

	public Team getTeam() {
		return team;
	}

	public Tournament getTournament() {
		if (team != null) {
			return team.getTournament();
		}
		return null;
	}

	public Integer getWonFights() {
		if (wonFights == null) {
			wonFights = 0;
			for (Fight fight : fights) {
				if (fight.isOver()) {
					Team winner = fight.getWinner();
					if (winner != null && winner.equals(team)) {
						wonFights++;
					}
				}
			}
		}
		return wonFights;
	}

	public Integer getDrawFights() {
		if (drawFights == null) {
			drawFights = 0;
			for (Fight fight : fights) {
				if ((fight.getTeam1().equals(team) || fight.getTeam2().equals(team))) {
					if (fight.isOver() && fight.isDrawFight()) {
						drawFights++;
					}
				}
			}
		}
		return drawFights;
	}

	public Integer getWonDuels() {
		if (wonDuels == null) {
			wonDuels = 0;
			for (Fight fight : fights) {
				wonDuels += fight.getWonDuels(team);
			}
		}
		return wonDuels;
	}

	public Integer getDrawDuels() {
		if (drawDuels == null) {
			drawDuels = 0;
			for (Fight fight : fights) {
				if (fight.isOver()) {
					drawDuels += fight.getDrawDuels(team);
				}
			}
		}
		return drawDuels;
	}

	public Integer getHits() {
		if (hits == null) {
			hits = 0;
			for (Fight fight : fights) {
				hits += fight.getScore(team);
			}
		}
		return hits;
	}

//	public Integer getGoldenPoints() {
//		if (goldenPoint == null) {
//			try {
//				goldenPoint = UndrawPool.getInstance().getUndrawsWon(fights.get(0).getTournament(),
//						fights.get(0).getLevel(), fights.get(0).getGroup(), team);
//			} catch (SQLException ex) {
//				KendoTournamentLogger.errorMessage(this.getClass(), ex);
//			}
//		}
//		return goldenPoint;
//	}

	@Override
	public abstract int compareTo(ScoreOfTeam o);

	@Override
	public String toString() {
		String text = team.getName() + ": Fights:" + getWonFights() + "/" + getDrawFights() + ", Duels: "
				+ getWonDuels() + "/" + getDrawDuels() + ", hits:" + getHits();
//		for (int i = 0; i < getGoldenPoints(); i++) {
//			text += "*";
//		}
		return text + "\n";
	}
}
