package com.softwaremagico.kt.utils;

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

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "nameUtils")
public class NameUtilsAdditionalTests {

    class ParticipantNameTest implements IParticipantName {
        private String lastname;
        private String name;

        public ParticipantNameTest(String lastname, String name) {
            this.lastname = lastname;
            this.name = name;
        }

        @Override
        public String getLastname() {
            return lastname;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    class NameTest implements IName {
        private String name;

        public NameTest(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    // Tests for getLastnameName
    @Test
    public void testGetLastnameNameWithNull() {
        Assert.assertEquals(NameUtils.getLastnameName(null), " --- --- ");
    }

    @Test
    public void testGetLastnameNameWithValidParticipant() {
        IParticipantName participant = new ParticipantNameTest("Doe", "John");
        Assert.assertEquals(NameUtils.getLastnameName(participant), "Doe, John");
    }

    @Test
    public void testGetLastnameNameWithEmptyStrings() {
        Assert.assertEquals(NameUtils.getLastnameName("", ""), " --- --- ");
    }

    @Test
    public void testGetLastnameNameWithOnlyLastname() {
        Assert.assertEquals(NameUtils.getLastnameName("Doe", ""), "Doe, ");
    }

    @Test
    public void testGetLastnameNameWithOnlyName() {
        Assert.assertEquals(NameUtils.getLastnameName("", "John"), ", John");
    }

    @Test
    public void testGetLastnameNameStringsVersion() {
        Assert.assertEquals(NameUtils.getLastnameName("Smith", "Jane"), "Smith, Jane");
    }

    // Tests for getLastnameNameIni
    @Test
    public void testGetLastnameNameIniWithNull() {
        Assert.assertEquals(NameUtils.getLastnameNameIni(null), " --- --- ");
    }

    @Test
    public void testGetLastnameNameIniWithValidParticipant() {
        IParticipantName participant = new ParticipantNameTest("Garcia", "Carlos");
        String result = NameUtils.getLastnameNameIni(participant);
        Assert.assertTrue(result.contains("GARCIA"));
        Assert.assertTrue(result.contains("C"));
    }

    @Test
    public void testGetLastnameNameIniWithEmptyStrings() {
        Assert.assertEquals(NameUtils.getLastnameNameIni("", ""), " --- --- ");
    }

    @Test
    public void testGetLastnameNameIniWithMaxLength() {
        String result = NameUtils.getLastnameNameIni("Hernandez", "Miguel", 8);
        Assert.assertTrue(result.length() <= 8 || result.contains("."));
    }

    @Test
    public void testGetLastnameNameIniStringsVersion() {
        String result = NameUtils.getLastnameNameIni("Lopez", "Pedro");
        Assert.assertTrue(result.contains("LOPEZ"));
    }

    // Tests for getShortLastname
    @Test
    public void testGetShortLastnameWithNull() {
        IParticipantName participant = null;
        Assert.assertEquals(NameUtils.getShortLastname(participant), " --- --- ");
    }

    @Test
    public void testGetShortLastnameWithValidParticipant() {
        IParticipantName participant = new ParticipantNameTest("Martinez", "Luis");
        Assert.assertEquals(NameUtils.getShortLastname(participant), "Martinez");
    }

    @Test
    public void testGetShortLastnameStringsVersionDefault() {
        String result = NameUtils.getShortLastname("Fernandez");
        Assert.assertEquals(result.length(), 8);
    }

    @Test
    public void testGetShortLastnameStringsVersionWithLength() {
        String result = NameUtils.getShortLastname("Fernandez", 4);
        Assert.assertEquals(result, "Fern");
    }

    @Test
    public void testGetShortLastnameWithCompoundLastname() {
        String result = NameUtils.getShortLastname("De La Rosa", 5);
        Assert.assertTrue(result.length() <= 5);
    }

    @Test
    public void testGetShortLastnameWithShortLastname() {
        String result = NameUtils.getShortLastname("De", 3);
        Assert.assertTrue(result.length() <= 3);
    }

    // Tests for getShortLastnameName
    @Test
    public void testGetShortLastnameNameWithNull() {
        Assert.assertEquals(NameUtils.getShortLastnameName(null, 12), " --- --- ");
    }

    @Test
    public void testGetShortLastnameNameWithValidParticipant() {
        IParticipantName participant = new ParticipantNameTest("Gonzalez", "Antonio");
        String result = NameUtils.getShortLastnameName(participant, 12);
        Assert.assertTrue(result.contains(","));
    }

    @Test
    public void testGetShortLastnameNameWithEmptyStrings() {
        Assert.assertEquals(NameUtils.getShortLastnameName("", "", 10), "");
    }

    @Test
    public void testGetShortLastnameNameStringsVersion() {
        String result = NameUtils.getShortLastnameName("Rodriguez", "Roberto", 12);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains(","));
    }

    // Tests for getShortName
    @Test
    public void testGetShortNameWithNullParticipant() {
        IParticipantName participant = null;
        Assert.assertEquals(NameUtils.getShortName(participant), " --- ");
    }

    @Test
    public void testGetShortNameWithValidParticipant() {
        IParticipantName participant = new ParticipantNameTest("Torres", "Manuel");
        Assert.assertEquals(NameUtils.getShortName(participant), "Manuel");
    }

    @Test
    public void testGetShortNameStringsVersionDefault() {
        String result = NameUtils.getShortName("Alexandra");
        Assert.assertEquals(result.length(), 8);
        Assert.assertEquals(result, "Alexandr");
    }

    @Test
    public void testGetShortNameStringsVersionWithLength() {
        String result = NameUtils.getShortName("Christopher", 5);
        Assert.assertEquals(result, "Chris");
    }

    @Test
    public void testGetShortNameWithShortName() {
        String result = NameUtils.getShortName("Tom", 5);
        Assert.assertEquals(result, "Tom");
    }

    // Tests for getAcronym
    @Test
    public void testGetAcronymWithNull() {
        Assert.assertEquals(NameUtils.getAcronym(null), "");
    }

    @Test
    public void testGetAcronymWithValidParticipant() {
        IParticipantName participant = new ParticipantNameTest("Garcia", "Maria");
        String acronym = NameUtils.getAcronym(participant);
        Assert.assertEquals(acronym.length(), 2);
        Assert.assertTrue(acronym.startsWith("M"));
    }

    @Test
    public void testGetAcronymStringsVersionSimple() {
        String acronym = NameUtils.getAcronym("Hernandez", "Jorge");
        Assert.assertEquals(acronym, "JH");
    }

    @Test
    public void testGetAcronymStringsVersionCompound() {
        String acronym = NameUtils.getAcronym("De La Rosa", "Pablo");
        Assert.assertNotNull(acronym);
        Assert.assertTrue(acronym.length() >= 2);
    }

    @Test
    public void testGetAcronymWithShortFirstWord() {
        String acronym = NameUtils.getAcronym("De Carlos", "Juan");
        Assert.assertNotNull(acronym);
        Assert.assertTrue(acronym.length() >= 2);
    }

    // Tests for getShortName with IName
    @Test
    public void testGetShortNameINameDefault() {
        IName nameItem = new NameTest("Tournament");
        Assert.assertEquals(NameUtils.getShortName(nameItem), "Tournament");
    }

    @Test
    public void testGetShortNameINameWithLongName() {
        IName nameItem = new NameTest("VeryLongTournamentName");
        String result = NameUtils.getShortName(nameItem);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("."));
    }

    @Test
    public void testGetShortNameINameWithCustomLength() {
        IName nameItem = new NameTest("Championship2024");
        String result = NameUtils.getShortName(nameItem, 10);
        Assert.assertNotNull(result);
        // Verify it's using the IName interface version that returns formatted names
        Assert.assertTrue(result.length() > 0);
    }

    @Test
    public void testGetShortNameINameWithShortName() {
        IName nameItem = new NameTest("Cup");
        Assert.assertEquals(NameUtils.getShortName(nameItem), "Cup");
    }
}





