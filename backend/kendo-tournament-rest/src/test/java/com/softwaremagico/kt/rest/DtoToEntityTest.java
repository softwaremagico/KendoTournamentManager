package com.softwaremagico.kt.rest;

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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.persistence.entities.Club;
import org.modelmapper.ModelMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "dtoToEntity")
public class DtoToEntityTest {
    private static final String CLUB_NAME = "Club1";
    private static final String CLUB_EMAIL = "Club1@email.com";
    private static final String CLUB_WEB = "http://web.com";
    private static final String CLUB_CITY = "Valencia";
    private static final String CLUB_COUNTRY = "Spain";
    private static final String CLUB_PHONE = "+3433366699";
    private static final String CLUB_ADDRESS = "C/baja 1";

    private final ModelMapper modelMapper = new ModelMapper();

    @Test
    public void whenConvertClubEntityToClubDto_thenCorrect() {
        Club club = new Club();
        club.setName(CLUB_NAME);
        club.setAddress(CLUB_ADDRESS);
        club.setPhone(CLUB_PHONE);
        club.setCountry(CLUB_COUNTRY);
        club.setCity(CLUB_CITY);
        club.setEmail(CLUB_EMAIL);
        club.setWeb(CLUB_WEB);

        ClubDTO clubDto = modelMapper.map(club, ClubDTO.class);
        Assert.assertEquals(club.getName(), clubDto.getName());
        Assert.assertEquals(club.getAddress(), clubDto.getAddress());
        Assert.assertEquals(club.getPhone(), clubDto.getPhone());
        Assert.assertEquals(club.getCountry(), clubDto.getCountry());
        Assert.assertEquals(club.getCity(), clubDto.getCity());
        Assert.assertEquals(club.getEmail(), clubDto.getEmail());
        Assert.assertEquals(club.getWeb(), clubDto.getWeb());
    }

    @Test
    public void whenConvertClubDtoToClubEntity_thenCorrect() {
        ClubDTO clubDto = new ClubDTO();
        clubDto.setName(CLUB_NAME);
        clubDto.setAddress(CLUB_ADDRESS);
        clubDto.setPhone(CLUB_PHONE);
        clubDto.setCountry(CLUB_COUNTRY);
        clubDto.setCity(CLUB_CITY);
        clubDto.setEmail(CLUB_EMAIL);
        clubDto.setWeb(CLUB_WEB);

        Club club = modelMapper.map(clubDto, Club.class);
        Assert.assertEquals(clubDto.getName(), club.getName());
        Assert.assertEquals(clubDto.getAddress(), club.getAddress());
        Assert.assertEquals(clubDto.getPhone(), club.getPhone());
        Assert.assertEquals(clubDto.getCountry(), club.getCountry());
        Assert.assertEquals(clubDto.getCity(), club.getCity());
        Assert.assertEquals(clubDto.getEmail(), club.getEmail());
        Assert.assertEquals(clubDto.getWeb(), club.getWeb());
    }
}
