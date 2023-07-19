package com.softwaremagico.kt.persistence.entities;

public class GroupLink {

    private Group source;

    private Group destination;

    private int winner;

    public Group getSource() {
        return source;
    }

    public void setSource(Group source) {
        this.source = source;
    }

    public Group getDestination() {
        return destination;
    }

    public void setDestination(Group destination) {
        this.destination = destination;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }
}
