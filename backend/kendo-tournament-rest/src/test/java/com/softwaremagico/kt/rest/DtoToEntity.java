package com.softwaremagico.kt.rest;

import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.rest.model.ClubDto;
import org.modelmapper.ModelMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "dtoToEntity")
public class DtoToEntity {
    private static final Integer CLUB_ID = 1;
    private static final String CLUB_NAME = "Club1";
    private static final String CLUB_EMAIL = "Club1@email.com";
    private static final String CLUB_WEB = "http://web.com";
    private static final String CLUB_CITY = "Valencia";
    private static final String CLUB_COUNTRY = "Spain";
    private static final String CLUB_PHONE = "+3433366699";
    private static final String CLUB_ADDRESS = "C/baja 1";

    private ModelMapper modelMapper = new ModelMapper();

    @Test
    public void whenConvertClubEntityToClubDto_thenCorrect() {
        Club club = new Club();
        club.setId(CLUB_ID);
        club.setName(CLUB_NAME);
        club.setAddress(CLUB_ADDRESS);
        club.setPhone(CLUB_PHONE);
        club.setCountry(CLUB_COUNTRY);
        club.setCity(CLUB_CITY);
        club.setEmail(CLUB_EMAIL);
        club.setWeb(CLUB_WEB);

        ClubDto clubDto = modelMapper.map(club, ClubDto.class);
        Assert.assertEquals(club.getId(), clubDto.getId());
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
        ClubDto clubDto = new ClubDto();
        clubDto.setId(CLUB_ID);
        clubDto.setName(CLUB_NAME);
        clubDto.setAddress(CLUB_ADDRESS);
        clubDto.setPhone(CLUB_PHONE);
        clubDto.setCountry(CLUB_COUNTRY);
        clubDto.setCity(CLUB_CITY);
        clubDto.setEmail(CLUB_EMAIL);
        clubDto.setWeb(CLUB_WEB);

        Club club = modelMapper.map(clubDto, Club.class);
        Assert.assertEquals(clubDto.getId(), club.getId());
        Assert.assertEquals(clubDto.getName(), club.getName());
        Assert.assertEquals(clubDto.getAddress(), club.getAddress());
        Assert.assertEquals(clubDto.getPhone(), club.getPhone());
        Assert.assertEquals(clubDto.getCountry(), club.getCountry());
        Assert.assertEquals(clubDto.getCity(), club.getCity());
        Assert.assertEquals(clubDto.getEmail(), club.getEmail());
        Assert.assertEquals(clubDto.getWeb(), club.getWeb());
    }
}
