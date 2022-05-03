package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "teams", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "tournament"}))
public class Team extends Element implements Comparable<Team> {

    @Column(name = "name")
    private String name;

    @OneToMany
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "members_of_team", joinColumns = @JoinColumn(name = "team_id"), inverseJoinColumns = @JoinColumn(name = "member_id"))
    @OrderColumn(name = "member_index")
    private List<Participant> members;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament")
    private Tournament tournament;

    @Column(name = "group_index")
    private Integer group = 0; // for the championship

    public Team() {
        super();
        members = new ArrayList<>();
    }

    public Team(String name, Tournament tournament) {
        this();
        setName(name);
        setTournament(tournament);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Participant> getMembers() {
        return members;
    }

    public void setMembers(List<Participant> members) {
        this.members = members;
    }

    public void addMember(Participant member) {
        this.members.add(member);
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public boolean isMember(Participant member) {
        return members.contains(member);
    }

    @Override
    public int compareTo(Team team) {
        // Ignore accents
        final Collator collator = Collator.getInstance(new Locale("es"));
        collator.setStrength(Collator.SECONDARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);

        return collator.compare(getName(), team.getName());
    }
}
