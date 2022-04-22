package com.softwaremagico.kt.core.converters;

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.persistence.entities.Club;
import org.springframework.beans.BeanUtils;

public class ClubConverter extends ElementConverter<Club, ClubDTO, ClubConverterRequest> {

    @Override
    public ClubDTO convert(ClubConverterRequest from) {
        final ClubDTO clubDTO = new ClubDTO();
        BeanUtils.copyProperties(from.getEntity(), clubDTO);
        return clubDTO;
    }

    @Override
    public Club reverse(ClubDTO to) {
        if (to == null) {
            return null;
        }
        final Club club = new Club();
        BeanUtils.copyProperties(to, club);
        return club;
    }
}
