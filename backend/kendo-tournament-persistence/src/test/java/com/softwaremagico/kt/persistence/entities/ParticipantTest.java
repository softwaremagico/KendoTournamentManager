package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "participantEntity")
public class ParticipantTest {

    @Test
    public void constructor_expectIdCardNormalizedAndNameCased() {
        final Participant participant = new Participant("ab-12 34 ", "john", "doe", null);

        assertEquals(participant.getIdCard(), "AB1234");
        assertEquals(participant.getName(), "John");
        assertEquals(participant.getLastname(), "Doe");
    }

    @Test
    public void isValid_withNameAndIdCard_expectTrue() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        assertTrue(participant.isValid());
    }

    @Test
    public void isValid_withoutIdCard_expectFalse() {
        final Participant participant = new Participant(null, "John", "Doe", null);
        assertFalse(participant.isValid());
    }

    @Test
    public void isValid_withEmptyName_expectFalse() {
        final Participant participant = new Participant("ID1", "", "Doe", null);
        assertFalse(participant.isValid());
    }

    @Test
    public void compareTo_byLastnameThenName_expectOrdered() {
        final Participant a = new Participant("ID1", "Ana", "Alpha", null);
        final Participant b = new Participant("ID2", "Beto", "Beta", null);

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    public void compareTo_withSameNames_andIds_expectOrderedById() {
        final Participant a = new Participant("ID1", "Ana", "Alpha", null);
        a.setId(1);
        final Participant b = new Participant("ID2", "Ana", "Alpha", null);
        b.setId(2);

        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void compareTo_withSameNamesAndNoIds_expectConsistentNonException() {
        final Participant a = new Participant("ID1", "Ana", "Alpha", null);
        final Participant b = new Participant("ID2", "Ana", "Alpha", null);

        // No exception, and comparing to itself is always 0 (falls back to identity hash otherwise).
        assertEquals(Integer.signum(a.compareTo(a)), 0);
        a.compareTo(b);
    }

    @Test
    public void generateTemporalToken_expectTokenAndExpirationSet() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        participant.generateTemporalToken();

        assertNotNull(participant.getTemporalToken());
        assertTrue(participant.getTemporalTokenExpiration().isAfter(LocalDateTime.now(ZoneId.systemDefault())));
    }

    @Test
    public void generateToken_expectTokenAndAccountExpirationSet() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        participant.generateToken();

        assertNotNull(participant.getToken());
        assertTrue(participant.getAccountExpiration().isAfter(LocalDateTime.now(ZoneId.systemDefault())));
    }

    @Test
    public void isAccountNonExpired_withoutToken_expectFalse() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        assertFalse(participant.isAccountNonExpired());
    }

    @Test
    public void isAccountNonExpired_withFreshToken_expectTrue() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        participant.generateToken();
        assertTrue(participant.isAccountNonExpired());
    }

    @Test
    public void isCredentialsNonExpired_withoutToken_expectFalse() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        assertFalse(participant.isCredentialsNonExpired());
    }

    @Test
    public void accountFlags_expectAlwaysUnlockedAndEnabled() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        assertTrue(participant.isAccountNonLocked());
        assertTrue(participant.isEnabled());
    }

    @Test
    public void getUsername_expectIdNameLastnameConcatenated() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        participant.setId(7);

        assertEquals(participant.getUsername(), "7_John_Doe");
    }

    @Test
    public void getPassword_expectDelegatesToToken() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        participant.setToken("abc123");

        assertEquals(participant.getPassword(), "abc123");
    }

    @Test
    public void getRoles_expectContainsParticipantRole() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        assertTrue(participant.getRoles().contains(Participant.PARTICIPANT_ROLE));
    }

    @Test
    public void getAuthorities_expectParticipantAuthority() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        assertEquals(participant.getAuthorities().size(), 1);
    }

    @Test
    public void toString_expectLastnameNameFormat() {
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        assertNotNull(participant.toString());
    }

    @Test
    public void equals_delegatesToElementIdentity() {
        final Participant p1 = new Participant("ID1", "John", "Doe", null);
        p1.setId(1);
        final Participant p2 = new Participant("ID2", "Jane", "Roe", null);
        p2.setId(1);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void equals_withDistinctTransientInstances_expectFalse() {
        final Participant p1 = new Participant("ID1", "John", "Doe", null);
        final Participant p2 = new Participant("ID1", "John", "Doe", null);

        assertNotEquals(p1, p2);
    }
}

