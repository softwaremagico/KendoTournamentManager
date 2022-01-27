package com.softwaremagico.kt.persistence.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.List;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tournament")
    private Tournament tournament;

    @OneToMany
    @JoinTable(name = "teams_by_group", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "team_id"))
    @OrderColumn(name = "index")
    private List<Team> teams;

    @Column(name = "winners")
    private Integer winners = 1;

    @Column(name = "shiaijo")
    private Integer shiaijo = 0;

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public Integer getWinners() {
        return winners;
    }

    public void setWinners(Integer winners) {
        this.winners = winners;
    }

    public Integer getShiaijo() {
        return shiaijo;
    }

    public void setShiaijo(Integer shiaijo) {
        this.shiaijo = shiaijo;
    }
}

