package com.softwaremagico.kt.rest.security;

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

import tools.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.core.controller.QrController;
import com.softwaremagico.kt.core.controller.models.QrCodeDTO;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Tenant;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.security.dto.AuthGuestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * End-to-end coverage of the guest QR flow with JWT/IP binding enabled.
 *
 * <p>
 * The QR code only carries a signed ticket plus the tournament id. The backend
 * exchanges that ticket for a guest JWT whose IP claim must come from the
 * socket peer, never from a client-controlled {@code X-Forwarded-For} header.
 * This test verifies that the issued token is accepted by a subsequent signed
 * REST call even when the caller keeps sending a forged forwarded header.
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Test(groups = {"guestQrAccess"})
public class GuestQrAccessIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String SOCKET_IP = "192.0.2.44";
    private static final String SPOOFED_FORWARDED_IP = "203.0.113.7";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private QrController qrController;

    @Autowired
    private TenantRepository tenantRepository;

    private Integer tournamentId;
    private Integer tenantId;
    private String guestTicket;

    @BeforeClass(alwaysRun = true)
    public void createQrTournamentAndTicket() {
        final Tenant tenant = tenantRepository.findByNameAndActiveTrue("Default")
                .orElseGet(() -> tenantRepository.save(new Tenant("Default")));
        tenantId = tenant.getId();

        TenantContext.setTenantId(tenantId);
        try {
            tournamentId = tournamentProvider.save("QR Guest Tournament", 1, 3, TournamentType.LEAGUE, "qr-test").getId();
            final QrCodeDTO qrCode = qrController.generateGuestQrCodeForTournamentFights(tournamentId, null, false);
            final String link = qrCode.getContent();
            Assert.assertNotNull(link);
            // Angular hash routing puts the parameters after the fragment marker.
            final String rawQuery = link.substring(link.indexOf('?') + 1);
            Assert.assertEquals(queryParameter(rawQuery, "tournamentId"), String.valueOf(tournamentId));
            Assert.assertEquals(queryParameter(rawQuery, "user"), "guest");
            guestTicket = queryParameter(rawQuery, "ticket");
            Assert.assertNotNull(guestTicket, "Guest QR link must include a signed ticket");
        } finally {
            TenantContext.clear();
        }
    }

    private String queryParameter(String rawQuery, String name) {
        for (String pair : rawQuery.split("&")) {
            final String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[0].equals(name)) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private RequestPostProcessor fromSocket(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    @Test
    public void guestQrLoginIgnoresForwardedHeaderAndTokenWorksForSignedRequests() throws Exception {
        final AuthGuestRequest request = new AuthGuestRequest();
        request.setTournamentId(tournamentId);
        request.setTicket(guestTicket);

        final MvcResult loginResult = mockMvc.perform(post("/auth/public/login/guest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Forwarded-For", SPOOFED_FORWARDED_IP)
                        .with(fromSocket(SOCKET_IP))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.AUTHORIZATION))
                .andReturn();

        final String jwt = loginResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
        Assert.assertNotNull(jwt);
        Assert.assertEquals(jwtTokenUtil.getUserIp(jwt), SOCKET_IP,
                "Guest token IP must come from the socket peer, not from X-Forwarded-For");

        mockMvc.perform(get("/auth/roles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .header("X-Forwarded-For", SPOOFED_FORWARDED_IP)
                        .with(fromSocket(SOCKET_IP))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andExpect(MockMvcResultMatchers.content().string(containsString("guest")));
    }
}
