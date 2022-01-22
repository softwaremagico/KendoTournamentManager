package com.softwaremagico.kt.persistence.entities;


import com.softwaremagico.kt.persistence.encryption.RoleTypeCryptoConverter;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tournament")
    private Tournament tournament;

    @OneToMany
    private Participant competitor;

    @Column(name = "role_type")
    @Enumerated(EnumType.STRING)
    @Convert(converter = RoleTypeCryptoConverter.class)
    private RoleType type;


    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Participant getCompetitor() {
        return competitor;
    }

    public void setCompetitor(Participant competitor) {
        this.competitor = competitor;
    }

    public RoleType getType() {
        return type;
    }

    public void setType(RoleType type) {
        this.type = type;
    }
}
