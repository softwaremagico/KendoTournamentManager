package com.softwaremagico.kt.rest;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantReducedDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationResponseDTO;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.testng.Assert;
import org.testng.annotations.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Guards the JSON contract of the DTO used in responses available to GUEST and
 * anonymous clients (rankings, teams, roles and participant login).
 */
@Test(groups = "participantAccess")
public class ParticipantReducedDtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void reducedParticipantDoesNotSerializeIdCard() throws Exception {
        final ClubDTO club = new ClubDTO();
        club.setName("Test club");
        club.setCity("Valencia");
        club.setCountry("Spain");
        final ParticipantDTO participant = new ParticipantDTO("SEC-IDCARD-951", "Name", "Lastname", club);

        Assert.assertTrue(objectMapper.writeValueAsString(participant).contains("\"idCard\""),
                "The full DTO must keep idCard for authorized management endpoints");

        final String reducedJson = objectMapper.writeValueAsString(new ParticipantReducedDTO(participant));
        Assert.assertFalse(reducedJson.contains("\"idCard\""),
                "Reduced DTO must not expose idCard, even as a null property");
        Assert.assertTrue(reducedJson.contains("\"name\""),
                "Reduced DTO must still expose public participant data");
    }

    @Test
    public void registrationResponseDoesNotSerializeIdCardAndUsesPublicRoles() throws Exception {
        final RegistrationResponseDTO response = new RegistrationResponseDTO();
        response.setId(9);
        response.setName("Ada");
        response.setLastname("Yukimura");
        response.setRoles(java.util.List.of(RoleType.COMPETITOR, RoleType.REFEREE));
        response.setTeamName("Team A");
        response.setPendingReview(true);

        final JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        Assert.assertFalse(json.has("idCard"), "Registration response must not expose idCard");
        Assert.assertTrue(json.path("roles").isArray(), "Registration roles must be serialized as an array");
        Assert.assertEquals(json.path("roles").get(0).asText(), "COMPETITOR");
        Assert.assertEquals(json.path("roles").get(1).asText(), "REFEREE");
        Assert.assertEquals(json.path("teamName").asText(), "Team A");
        Assert.assertTrue(json.path("pendingReview").asBoolean());
    }
}
