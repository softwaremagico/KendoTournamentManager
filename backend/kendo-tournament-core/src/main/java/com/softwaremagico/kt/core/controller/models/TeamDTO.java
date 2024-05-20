package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.utils.IName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeamDTO extends ElementDTO implements IName {

    private String name;

    private TournamentDTO tournament;

    private List<ParticipantDTO> members;

    private Integer group;

    public TeamDTO() {
        super();
        members = new ArrayList<>();
    }

    public TeamDTO(String name, TournamentDTO tournament) {
        this();
        setName(name);
        setTournament(tournament);
    }

    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournament) {
        this.tournament = tournament;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParticipantDTO> getMembers() {
        return members;
    }

    public void setMembers(List<ParticipantDTO> members) {
        this.members = members;
    }

    public void addMember(ParticipantDTO member) {
        this.members.add(member);
    }

    public boolean isMember(ParticipantDTO member) {
        return members.contains(member);
    }

    @Override
    public String toString() {
        if (getName() != null) {
            return getName();
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TeamDTO teamDTO)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return getName().equals(teamDTO.getName()) && Objects.equals(getTournament(), teamDTO.getTournament())
                && Objects.equals(getMembers(), teamDTO.getMembers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName(), getTournament(), getMembers());
    }
}
