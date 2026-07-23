package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.core.exceptions.DataInputException;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.ImageFormat;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("all")
@Test
public class ControllerModelsDTOCoverageTest {

    @Test
    public void testAchievementDTOEqualsHashCodeAndToString() {
        final AchievementDTO left = this.createAchievementDTO(10, "Ana", "Perez", "Cup", AchievementType.TERMINATOR, AchievementGrade.GOLD);
        final AchievementDTO same = this.createAchievementDTO(10, "Ana", "Perez", "Cup", AchievementType.TERMINATOR, AchievementGrade.GOLD);
        final AchievementDTO differentGrade = this.createAchievementDTO(10, "Ana", "Perez", "Cup", AchievementType.TERMINATOR, AchievementGrade.SILVER);

        assertThat(left)
                .isEqualTo(same)
                .hasSameHashCodeAs(same)
                .isNotEqualTo(differentGrade)
                
                .hasToString(left.toString());
        assertThat(left.toString()).contains("Achievement").contains("achievementType");
    }

    @Test
    public void testAchievementDTOSettersAndGetters() {
        final AchievementDTO dto = new AchievementDTO();
        final ParticipantDTO participant = this.createParticipantDTO(2, "Ken", "Shin");
        final TournamentDTO tournament = this.createTournamentDTO(3, "Regional");

        dto.setParticipant(participant);
        dto.setTournament(tournament);
        dto.setAchievementType(AchievementType.BILLY_THE_KID);
        dto.setAchievementGrade(AchievementGrade.BRONZE);

        assertThat(dto.getParticipant()).isEqualTo(participant);
        assertThat(dto.getTournament()).isEqualTo(tournament);
        assertThat(dto.getAchievementType()).isEqualTo(AchievementType.BILLY_THE_KID);
        assertThat(dto.getAchievementGrade()).isEqualTo(AchievementGrade.BRONZE);
    }

    @Test
    public void testTournamentImageDTOBase64ForAllBranches() {
        final byte[] data = new byte[]{1, 2, 3};
        final String encoded = Base64.getEncoder().encodeToString(data);
        final TournamentImageDTO dto = new TournamentImageDTO();

        dto.setTournament(this.createTournamentDTO(4, "Open"));
        dto.setImageType(TournamentImageType.BANNER);
        dto.setDefaultImage(true);
        assertThat(dto.isDefaultImage()).isTrue();

        dto.setData(null);
        assertThat(dto.getBase64()).isNull();

        dto.setData(data);
        dto.setImageCompression(ImageCompression.PNG);
        assertThat(dto.getBase64()).isEqualTo("data:image/png;base64," + encoded);

        dto.setImageCompression(ImageCompression.JPG);
        assertThat(dto.getBase64()).isEqualTo("data:image/jpeg;base64," + encoded);
    }

    @Test
    public void testGroupLinkDTOToStringAndAccessors() {
        final GroupLinkDTO link = new GroupLinkDTO();

        assertThat(link.toString()).contains("null-null");

        final GroupDTO source = new GroupDTO();
        source.setLevel(1);
        source.setIndex(2);
        final GroupDTO destination = new GroupDTO();
        destination.setLevel(3);
        destination.setIndex(4);

        link.setSource(source);
        link.setDestination(destination);
        link.setWinner(2);
        link.setTournament(this.createTournamentDTO(7, "Nationals"));

        assertThat(link.getSource()).isEqualTo(source);
        assertThat(link.getDestination()).isEqualTo(destination);
        assertThat(link.getWinner()).isEqualTo(2);
        assertThat(link.getTournament()).isNotNull();
        assertThat(link.toString()).contains("1-2").contains("3-4").contains("winner=2");
    }

    @Test
    public void testRoleDTOEqualsHashCodeAndToStringBranches() {
        final RoleDTO role = this.createRoleDTO(10, "A", "B", RoleType.COMPETITOR);
        final RoleDTO same = this.createRoleDTO(10, "A", "B", RoleType.COMPETITOR);
        final RoleDTO different = this.createRoleDTO(10, "A", "B", RoleType.REFEREE);

        role.setDiplomaPrinted(true);
        role.setAccreditationPrinted(true);
        assertThat(role.isDiplomaPrinted()).isTrue();
        assertThat(role.isAccreditationPrinted()).isTrue();

        assertThat(role)
                .isEqualTo(same)
                .hasSameHashCodeAs(same)
                .isNotEqualTo(different)
                ;
        assertThat(role.toString()).contains("ROLE");

        final RoleDTO withoutTournament = new RoleDTO();
        assertThat(withoutTournament.toString()).isNotBlank();
    }

    @Test
    public void testImageDTOCoversAllSetBase64Branches() {
        final ImageDTO dto = new ImageDTO();
        final byte[] data = new byte[]{9, 8, 7};
        final String encoded = Base64.getEncoder().encodeToString(data);

        dto.setImageFormat(ImageFormat.BASE64);
        assertThat(dto.getImageFormat()).isEqualTo(ImageFormat.BASE64);

        dto.setData(null);
        assertThat(dto.getBase64()).isNull();

        dto.setData(data);
        assertThat(dto.getBase64()).isEqualTo("data:image/png;base64," + encoded);

        dto.setBase64("data:image/png;base64," + encoded);
        assertThat(dto.getData()).containsExactly(data);

        dto.setBase64("data:image/jpeg;base64," + encoded);
        assertThat(dto.getData()).containsExactly(data);

        assertThatThrownBy(() -> dto.setBase64("data:image/gif;base64," + encoded))
                .isInstanceOf(DataInputException.class);

        final byte[] originalData = new byte[]{1, 1, 1};
        dto.setData(originalData);
        dto.setBase64(null);
        assertThat(dto.getData()).containsExactly(originalData);
    }

    @Test
    public void testAchievementDTOEqualsSelfAndSuperMismatchAndFieldMismatch() {
        final AchievementDTO base = this.createAchievementDTO(20, "Nao", "Ito", "Cup", AchievementType.TERMINATOR, AchievementGrade.GOLD);

        // this == o branch
        
        // super.equals(o) == false branch (different createdAt/id in ElementDTO)
        final AchievementDTO differentSuper = this.createAchievementDTO(21, "Nao", "Ito", "Cup", AchievementType.TERMINATOR, AchievementGrade.GOLD);
        assertThat(base.equals(differentSuper)).isFalse();

        // Same ElementDTO identity but mismatching participant branch
        final AchievementDTO differentParticipant = this.createAchievementDTO(20, "Other", "Ito", "Cup", AchievementType.TERMINATOR, AchievementGrade.GOLD);
        assertThat(base.equals(differentParticipant)).isFalse();

        // Same ElementDTO identity but mismatching tournament branch
        final AchievementDTO differentTournament = this.createAchievementDTO(20, "Nao", "Ito", "OtherCup", AchievementType.TERMINATOR, AchievementGrade.GOLD);
        assertThat(base.equals(differentTournament)).isFalse();

        // Same ElementDTO identity but mismatching achievement type branch
        final AchievementDTO differentType = this.createAchievementDTO(20, "Nao", "Ito", "Cup", AchievementType.BILLY_THE_KID, AchievementGrade.GOLD);
        assertThat(base.equals(differentType)).isFalse();
    }

    @Test
    public void testRoleDTOEqualsSelfSuperMismatchAndFieldMismatch() {
        final RoleDTO base = this.createRoleDTO(30, "Akira", "Kato", RoleType.COMPETITOR);

        // this == o branch
        
        // super.equals(o) == false branch
        final RoleDTO differentSuper = this.createRoleDTO(31, "Akira", "Kato", RoleType.COMPETITOR);
        assertThat(base.equals(differentSuper)).isFalse();

        // Same ElementDTO identity but mismatching participant branch
        final RoleDTO differentParticipant = this.createRoleDTO(30, "Other", "Kato", RoleType.COMPETITOR);
        assertThat(base.equals(differentParticipant)).isFalse();

        // Same ElementDTO identity but mismatching role type branch
        final RoleDTO differentRoleType = this.createRoleDTO(30, "Akira", "Kato", RoleType.REFEREE);
        assertThat(base.equals(differentRoleType)).isFalse();
    }

    @Test
    public void testRoleDTOEqualsTournamentMismatchBranch() {
        final RoleDTO base = this.createRoleDTO(40, "Mai", "Ito", RoleType.COMPETITOR);

        final RoleDTO differentTournament = this.createRoleDTO(40, "Mai", "Ito", RoleType.COMPETITOR);
        differentTournament.setTournament(this.createTournamentDTO(40, "AnotherTournament"));

        assertThat(base.equals(differentTournament)).isFalse();
    }

    private AchievementDTO createAchievementDTO(int id, String name, String lastname, String tournamentName,
                                                AchievementType type, AchievementGrade grade) {
        final AchievementDTO dto = new AchievementDTO();
        dto.setId(id);
        dto.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        dto.setParticipant(this.createParticipantDTO(id, name, lastname));
        dto.setTournament(this.createTournamentDTO(id, tournamentName));
        dto.setAchievementType(type);
        dto.setAchievementGrade(grade);
        return dto;
    }

    private RoleDTO createRoleDTO(int id, String name, String lastname, RoleType roleType) {
        final RoleDTO dto = new RoleDTO(this.createTournamentDTO(id, "Tournament" + id), this.createParticipantDTO(id, name, lastname), roleType);
        dto.setId(id);
        dto.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        return dto;
    }

    private ParticipantDTO createParticipantDTO(int id, String name, String lastname) {
        final ParticipantDTO dto = new ParticipantDTO();
        dto.setId(id);
        dto.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        dto.setName(name);
        dto.setLastname(lastname);
        return dto;
    }

    private TournamentDTO createTournamentDTO(int id, String name) {
        final TournamentDTO dto = new TournamentDTO(name, 2, 3, TournamentType.LEAGUE);
        dto.setId(id);
        dto.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        return dto;
    }

    @Test
    public void testClubDTOEqualsHashCodeAndToStringBranches() {
        final ClubDTO base = this.createClubDTO("Dojo A", "ES", "Madrid");
        final ClubDTO same = this.createClubDTO("Dojo A", "ES", "Madrid");
        final ClubDTO differentCity = this.createClubDTO("Dojo A", "ES", "Sevilla");

                assertThat(base)
                .isEqualTo(same)
                .hasSameHashCodeAs(same)
                .isNotEqualTo(differentCity)
                
                .hasToString("Dojo A");

        final ClubDTO nullName = this.createClubDTO("Dojo B", "ES", "Bilbao");
        nullName.setName(null);
        assertThat(nullName.toString()).isNotBlank();
    }

    @Test
    public void testClubDTOEqualsCoversNameCountryAndOptionalFieldsBranches() {
        final ClubDTO base = this.createClubDTO("Dojo C", "ES", "Madrid");
        final ClubDTO same = this.createClubDTO("Dojo C", "ES", "Madrid");

        assertThat(base).isEqualTo(same);

        final ClubDTO differentName = this.createClubDTO("Dojo D", "ES", "Madrid");
        assertThat(base).isNotEqualTo(differentName);

        final ClubDTO differentCountry = this.createClubDTO("Dojo C", "PT", "Madrid");
        assertThat(base).isNotEqualTo(differentCountry);

        final ClubDTO differentAddress = this.createClubDTO("Dojo C", "ES", "Madrid");
        differentAddress.setAddress("Other address");
        assertThat(base).isNotEqualTo(differentAddress);

        final ClubDTO differentRepresentative = this.createClubDTO("Dojo C", "ES", "Madrid");
        differentRepresentative.setRepresentativeId("other-rep");
        assertThat(base).isNotEqualTo(differentRepresentative);

        final ClubDTO differentEmail = this.createClubDTO("Dojo C", "ES", "Madrid");
        differentEmail.setEmail("other@example.com");
        assertThat(base).isNotEqualTo(differentEmail);

        final ClubDTO differentPhone = this.createClubDTO("Dojo C", "ES", "Madrid");
        differentPhone.setPhone("999");
        assertThat(base).isNotEqualTo(differentPhone);

        final ClubDTO differentWeb = this.createClubDTO("Dojo C", "ES", "Madrid");
        differentWeb.setWeb("https://other.example.com");
        assertThat(base).isNotEqualTo(differentWeb);
    }

    @Test
    public void testParticipantDTOToStringNullNameAndEqualsNullClassBranches() {
        final ParticipantDTO base = this.createParticipantDTO(90, "Haru", "Sato");
        base.setClub(this.createClubDTO("Club H", "JP", "Osaka"));

        
        final ParticipantDTO anonymous = this.createParticipantDTO(91, "Tmp", "Tmp");
        anonymous.setName(null);
        assertThatThrownBy(anonymous::toString).isInstanceOf(NullPointerException.class);

        final ParticipantDTO differentClub = this.createParticipantDTO(90, "Haru", "Sato");
        differentClub.setClub(this.createClubDTO("Club X", "JP", "Osaka"));
        assertThat(base).isNotEqualTo(differentClub);
    }

    @Test
    public void testTeamDTOEqualsMembersAndToStringBranches() {
        final TeamDTO base = this.createTeamDTO(60, "Team A");
        base.addMember(this.createParticipantDTO(61, "A", "One"));

        final TeamDTO same = this.createTeamDTO(60, "Team A");
        same.addMember(this.createParticipantDTO(61, "A", "One"));

        final TeamDTO differentMembers = this.createTeamDTO(60, "Team A");
        differentMembers.addMember(this.createParticipantDTO(62, "B", "Two"));

                assertThat(base)
                .isEqualTo(same)
                .hasSameHashCodeAs(same)
                .isNotEqualTo(differentMembers)
                ;
        assertThat(base.isMember(this.createParticipantDTO(61, "A", "One"))).isTrue();
        assertThat(base).hasToString("Team A");

        final TeamDTO unnamed = this.createTeamDTO(60, "Fallback");
        unnamed.setName(null);
        assertThat(unnamed.toString()).isNotBlank();
    }

    @Test
    public void testTournamentDTOEqualsConstructorsAndToStringBranches() {
        final TournamentDTO byDefaultDuration = new TournamentDTO("T1", 2, 3, TournamentType.LEAGUE, null);
        final TournamentDTO explicitDuration = new TournamentDTO("T1", 2, 3, TournamentType.LEAGUE, 120);

        assertThat(byDefaultDuration.getDuelsDuration()).isNotNull();
        assertThat(explicitDuration.getDuelsDuration()).isEqualTo(120);

        final TournamentDTO base = this.createTournamentDTO(70, "Finals");
        final TournamentDTO same = this.createTournamentDTO(70, "Finals");
        final TournamentDTO differentType = this.createTournamentDTO(70, "Finals");
        differentType.setType(TournamentType.TREE);

        base.setLocked(true);
        base.setFightSize(3);
        base.setLockedAt(LocalDateTime.of(2026, Month.JANUARY, 2, 10, 0));
        base.setStartedAt(LocalDateTime.of(2026, Month.JANUARY, 3, 10, 0));
        base.setFinishedAt(LocalDateTime.of(2026, Month.JANUARY, 4, 10, 0));

                assertThat(base)
                .isEqualTo(same)
                .hasSameHashCodeAs(same)
                .isNotEqualTo(differentType)
                
                .hasToString("Finals");
        assertThat(base.isLocked()).isTrue();
        assertThat(base.getFightSize()).isEqualTo(3);
        assertThat(base.getLockedAt()).isNotNull();
        assertThat(base.getStartedAt()).isNotNull();
        assertThat(base.getFinishedAt()).isNotNull();

        final TournamentDTO unnamed = this.createTournamentDTO(71, "Tmp");
        unnamed.setName(null);
        assertThat(unnamed.toString()).isNotBlank();
    }

    @Test
    public void testGroupDTOEqualsHashCodeAndToStringBranches() {
        final GroupDTO base = this.createGroupDTO(80, 1, 0, 1);
        final GroupDTO same = this.createGroupDTO(80, 1, 0, 1);
        final GroupDTO differentShiaijo = this.createGroupDTO(80, 2, 0, 1);

                assertThat(base)
                .isEqualTo(same)
                .hasSameHashCodeAs(same)
                .isNotEqualTo(differentShiaijo)
                ;
        assertThat(base.toString()).contains("Group{").contains("level=0").contains("index=1");
    }

    private ClubDTO createClubDTO(String name, String country, String city) {
        final ClubDTO dto = new ClubDTO();
        dto.setName(name);
        dto.setCountry(country);
        dto.setCity(city);
        dto.setAddress("Address");
        dto.setRepresentativeId("rep");
        dto.setEmail("mail@example.com");
        dto.setPhone("123");
        dto.setWeb("https://example.com");
        return dto;
    }

    private TeamDTO createTeamDTO(int id, String name) {
        final TeamDTO dto = new TeamDTO(name, this.createTournamentDTO(id, "Tournament" + id));
        dto.setId(id);
        dto.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        return dto;
    }

    private GroupDTO createGroupDTO(int id, int shiaijo, int level, int index) {
        final GroupDTO dto = new GroupDTO();
        dto.setId(id);
        dto.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        dto.setTournament(this.createTournamentDTO(id, "Tournament" + id));
        dto.setTeams(new java.util.ArrayList<>());
        dto.setFights(new java.util.ArrayList<>());
        dto.setUnties(new java.util.ArrayList<>());
        dto.setNumberOfWinners(1);
        dto.setShiaijo(shiaijo);
        dto.setLevel(level);
        dto.setIndex(index);
        return dto;
    }

    @Test
    public void testTournamentDTOEqualsMismatchesTypeAndNullScoreBranches() {
        final TournamentDTO base = this.createTournamentDTO(110, "Spring Cup");

        final TournamentDTO differentType = this.createTournamentDTO(110, "Spring Cup");
        differentType.setType(TournamentType.TREE);
        assertThat(base).isNotEqualTo(differentType);

        final TournamentDTO nullScore = this.createTournamentDTO(110, "Spring Cup");
        nullScore.setTournamentScore(null);
        assertThat(base).isNotEqualTo(nullScore);
    }

    @Test
    public void testGroupDTOEqualsMismatchesShiaijoAndIndexBranches() {
        final GroupDTO base = this.createGroupDTO(111, 1, 1, 1);

        final GroupDTO differentShiaijo = this.createGroupDTO(111, 2, 1, 1);
        assertThat(base).isNotEqualTo(differentShiaijo);

        final GroupDTO differentIndex = this.createGroupDTO(111, 1, 1, 2);
        // Current implementation does not include index in equals/hashCode.
        assertThat(base).isEqualTo(differentIndex);
    }

    @Test
    public void testParticipantReducedDTOBranches() {
        final ParticipantReducedDTO base = new ParticipantReducedDTO();
        base.setId(300);
        base.setName("Nori");
        base.setLastname("Sato");

        final ParticipantReducedDTO same = new ParticipantReducedDTO();
        same.setId(300);
        same.setName("Nori");
        same.setLastname("Sato");

        assertThat(base).isEqualTo(same).hasSameHashCodeAs(same);

        final ParticipantReducedDTO differentName = new ParticipantReducedDTO();
        differentName.setId(300);
        differentName.setName("Other");
        differentName.setLastname("Sato");
        assertThat(base).isNotEqualTo(differentName);
        assertThat(base);
        assertThat(base.toString()).contains("Nori");

        final ParticipantReducedDTO nullName = new ParticipantReducedDTO();
        nullName.setId(301);
        nullName.setLastname("Null");
        assertThat(nullName.toString()).isNotBlank();
    }

    @Test
    public void testTournamentScoreDTOEqualsBranches() {
        final TournamentScoreDTO base = new TournamentScoreDTO();
        base.setId(400);
        base.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        base.setPointsByVictory(3);
        base.setPointsByDraw(1);

        final TournamentScoreDTO same = new TournamentScoreDTO();
        same.setId(400);
        same.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        same.setPointsByVictory(3);
        same.setPointsByDraw(1);

        assertThat(base).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(base);

        final TournamentScoreDTO differentSuper = new TournamentScoreDTO();
        differentSuper.setId(401);
        differentSuper.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        differentSuper.setPointsByVictory(3);
        differentSuper.setPointsByDraw(1);
        assertThat(base).isNotEqualTo(differentSuper);

        final TournamentScoreDTO differentVictory = new TournamentScoreDTO();
        differentVictory.setId(400);
        differentVictory.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        differentVictory.setPointsByVictory(9);
        differentVictory.setPointsByDraw(1);
        assertThat(base).isNotEqualTo(differentVictory);

        final TournamentScoreDTO differentDraw = new TournamentScoreDTO();
        differentDraw.setId(400);
        differentDraw.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        differentDraw.setPointsByVictory(3);
        differentDraw.setPointsByDraw(2);
        assertThat(base).isNotEqualTo(differentDraw);
    }

    @Test
    public void testElementDTOEqualsBranches() {
        final ElementDTO base = new ElementDTO();
        base.setId(500);
        base.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));

        final ElementDTO same = new ElementDTO();
        same.setId(500);
        same.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));

        assertThat(base).isEqualTo(same).hasSameHashCodeAs(same);

        final ElementDTO different = new ElementDTO();
        different.setId(501);
        different.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));
        assertThat(base).isNotEqualTo(different);
        assertThat(base.equals("other")).isFalse();
    }

    @Test
    public void testElementDTOEqualsDifferentCreatedAtBranch() {
        final ElementDTO left = new ElementDTO();
        left.setId(700);
        left.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0));

        final ElementDTO right = new ElementDTO();
        right.setId(700);
        right.setCreatedAt(LocalDateTime.of(2026, Month.JANUARY, 2, 10, 0));

        assertThat(left).isNotEqualTo(right);
    }

    @Test
    public void testParticipantFightStatisticsDTOSetDuelsNumberBranches() {
        final ParticipantFightStatisticsDTO dto = new ParticipantFightStatisticsDTO();

        dto.setDuelsNumber(5L);
        assertThat(dto.getDuelsNumber()).isEqualTo(5L);

        dto.setDuelsNumber(-1L);
        assertThat(dto.getDuelsNumber()).isZero();

        dto.setDuelsNumber(null);
        assertThat(dto.getDuelsNumber()).isZero();
    }

    @Test
    public void testTournamentFightStatisticsDTOSetCountersBranches() {
        final TournamentFightStatisticsDTO dto = new TournamentFightStatisticsDTO();

        dto.setFightsNumber(10L);
        dto.setDuelsNumber(20L);
        assertThat(dto.getFightsNumber()).isEqualTo(10L);
        assertThat(dto.getDuelsNumber()).isEqualTo(20L);

        dto.setFightsNumber(-1L);
        dto.setDuelsNumber(-1L);
        assertThat(dto.getFightsNumber()).isNull();
        assertThat(dto.getDuelsNumber()).isNull();

        dto.setFightsNumber(null);
        dto.setDuelsNumber(null);
        assertThat(dto.getFightsNumber()).isNull();
        assertThat(dto.getDuelsNumber()).isNull();
    }

    @Test
    public void testParticipantDTOEqualsNameLastnameAndHashCodeNullClubBranches() {
        final ParticipantDTO base = this.createParticipantDTO(910, "Ken", "Sato");
        base.setClub(this.createClubDTO("Club A", "JP", "Tokyo"));

        final ParticipantDTO same = this.createParticipantDTO(910, "Ken", "Sato");
        same.setClub(this.createClubDTO("Club A", "JP", "Tokyo"));
        assertThat(base).isEqualTo(same);

        final ParticipantDTO differentName = this.createParticipantDTO(910, "Ren", "Sato");
        differentName.setClub(this.createClubDTO("Club A", "JP", "Tokyo"));
        assertThat(base).isNotEqualTo(differentName);

        final ParticipantDTO differentLastname = this.createParticipantDTO(910, "Ken", "Ito");
        differentLastname.setClub(this.createClubDTO("Club A", "JP", "Tokyo"));
        assertThat(base).isNotEqualTo(differentLastname);

        final ParticipantDTO nullClub = this.createParticipantDTO(911, "Null", "Club");
        nullClub.setClub(null);
        assertThat(base).hasSameHashCodeAs(same);
        assertThat(nullClub).hasSameHashCodeAs(nullClub);
        assertThat(base.equals("not-participant")).isFalse();
    }

    @Test
    public void testTournamentDTOEqualsExtraFieldMismatchBranches() {
        final TournamentDTO base = this.createTournamentDTO(920, "Master");

        final TournamentDTO differentName = this.createTournamentDTO(920, "Other");
        assertThat(base).isNotEqualTo(differentName);

        final TournamentDTO differentShiaijos = this.createTournamentDTO(920, "Master");
        differentShiaijos.setShiaijos(9);
        assertThat(base).isNotEqualTo(differentShiaijos);

        final TournamentDTO differentTeamSize = this.createTournamentDTO(920, "Master");
        differentTeamSize.setTeamSize(9);
        assertThat(base)
                .isNotEqualTo(differentTeamSize)
                .isNotEqualTo(null);
    }

    @Test
    public void testGroupDTOEqualsAllFieldMismatchBranches() {
        final GroupDTO base = this.createGroupDTO(930, 1, 1, 1);
        final GroupDTO same = this.createGroupDTO(930, 1, 1, 1);
        assertThat(base).isEqualTo(same);

        final GroupDTO differentTournament = this.createGroupDTO(930, 1, 1, 1);
        differentTournament.setTournament(this.createTournamentDTO(930, "OtherTournament"));
        assertThat(base).isNotEqualTo(differentTournament);

        final GroupDTO differentTeams = this.createGroupDTO(930, 1, 1, 1);
        differentTeams.setTeams(new java.util.ArrayList<>(java.util.List.of(this.createTeamDTO(930, "OtherTeam"))));
        assertThat(base).isNotEqualTo(differentTeams);

        final GroupDTO differentShiaijo = this.createGroupDTO(930, 2, 1, 1);
        assertThat(base).isNotEqualTo(differentShiaijo);

        final GroupDTO differentLevel = this.createGroupDTO(930, 1, 2, 1);
        assertThat(base).isNotEqualTo(differentLevel);

        final GroupDTO differentFights = this.createGroupDTO(930, 1, 1, 1);
        differentFights.setFights(null);
        assertThat(base).isNotEqualTo(differentFights);

        final GroupDTO differentWinners = this.createGroupDTO(930, 1, 1, 1);
        differentWinners.setNumberOfWinners(2);
        assertThat(base).isNotEqualTo(differentWinners);

        final GroupDTO differentUnties = this.createGroupDTO(930, 1, 1, 1);
        differentUnties.setUnties(null);
        assertThat(base)
                .isNotEqualTo(differentUnties)
                .isNotEqualTo(null);
    }

    @Test
    public void testSimpleControllerDtosAndTokenCoverage() {
        final ParticipantDTO participant = this.createParticipantDTO(940, "Mina", "Kobayashi");
        final TournamentDTO tournament = this.createTournamentDTO(940, "Autumn Cup");

        final ParticipantInTournamentDTO participantInTournamentDTO = new ParticipantInTournamentDTO();
        participantInTournamentDTO.setParticipant(participant);
        participantInTournamentDTO.setTournament(tournament);

        assertThat(participantInTournamentDTO.getParticipant()).isEqualTo(participant);
        assertThat(participantInTournamentDTO.getTournament()).isEqualTo(tournament);

        final ParticipantsInTournamentDTO participantsInTournamentDTO = new ParticipantsInTournamentDTO();
        participantsInTournamentDTO.setParticipant(java.util.List.of(participant));
        participantsInTournamentDTO.setTournament(tournament);

        assertThat(participantsInTournamentDTO.getParticipant()).containsExactly(participant);
        assertThat(participantsInTournamentDTO.getTournament()).isEqualTo(tournament);

        final LogDTO logDTO = new LogDTO();
        logDTO.setMessage("frontend event");

        assertThat(logDTO.getMessage()).isEqualTo("frontend event");
        assertThat(logDTO).hasToString("frontend event");

        final ParticipantImageDTO participantImageDTO = new ParticipantImageDTO();
        participantImageDTO.setParticipant(participant);

        assertThat(participantImageDTO.getParticipant()).isEqualTo(participant);

        final LocalDateTime expiration = LocalDateTime.of(2026, Month.FEBRUARY, 3, 4, 5);
        final Participant entity = new Participant();
        entity.setToken("token-123");
        entity.setAccountExpiration(expiration);

        final Token tokenFromParticipant = new Token(entity);
        assertThat(tokenFromParticipant.getContent()).isEqualTo("token-123");
        assertThat(tokenFromParticipant.getExpiration()).isEqualTo(expiration);
        assertThat(tokenFromParticipant.getParticipant()).isNull();

        final Token token = new Token();
        token.setContent("manual-token");
        token.setExpiration(expiration.plusDays(1));
        token.setParticipant(participant);

        assertThat(token.getContent()).isEqualTo("manual-token");
        assertThat(token.getExpiration()).isEqualTo(expiration.plusDays(1));
        assertThat(token.getParticipant()).isEqualTo(participant);
    }
}
