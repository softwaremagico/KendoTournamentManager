package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "restServicesUnit")
public class NetworkControllerTests {

    private NetworkController networkController;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        networkController = new NetworkController();
    }

    @Test
    public void shouldReturnNonNullMacValue() {
        final String mac = networkController.getHostMac();

        assertNotNull(mac);
    }

    @Test
    public void shouldReturnStableMacAcrossCalls() {
        final String first = networkController.getHostMac();
        final String second = networkController.getHostMac();

        assertEquals(first, second);
    }

    @Test
    public void shouldReturnEmptyOrValidHexMac() {
        final String mac = networkController.getHostMac();

        if (mac.isEmpty()) {
            assertEquals(mac, "");
        } else {
            assertTrue(mac.matches("([0-9A-F]{2}-)*[0-9A-F]{2}"));
            assertFalse(mac.contains("--"));
            assertEquals(mac, mac.toUpperCase());
        }
    }

    @Test
    public void shouldReturnEmptyWhenNetworkInterfaceIsNull() {
        final InetAddress inetAddress = mock(InetAddress.class);
        final NetworkController controller = new NetworkController() {
            @Override
            protected InetAddress getLocalHost() {
                return inetAddress;
            }

            @Override
            protected NetworkInterface getByInetAddress(InetAddress address) {
                return null;
            }
        };

        assertEquals(controller.getHostMac(), "");
    }

    @Test
    public void shouldReturnEmptyWhenHardwareAddressIsNull() throws Exception {
        final InetAddress inetAddress = mock(InetAddress.class);
        final NetworkInterface networkInterface = mock(NetworkInterface.class);
        when(networkInterface.getHardwareAddress()).thenReturn(null);
        final NetworkController controller = new NetworkController() {
            @Override
            protected InetAddress getLocalHost() {
                return inetAddress;
            }

            @Override
            protected NetworkInterface getByInetAddress(InetAddress address) {
                return networkInterface;
            }
        };

        assertEquals(controller.getHostMac(), "");
    }

    @Test
    public void shouldReturnEmptyWhenHardwareAddressIsEmpty() throws Exception {
        final InetAddress inetAddress = mock(InetAddress.class);
        final NetworkInterface networkInterface = mock(NetworkInterface.class);
        when(networkInterface.getHardwareAddress()).thenReturn(new byte[]{});
        final NetworkController controller = new NetworkController() {
            @Override
            protected InetAddress getLocalHost() {
                return inetAddress;
            }

            @Override
            protected NetworkInterface getByInetAddress(InetAddress address) {
                return networkInterface;
            }
        };

        assertEquals(controller.getHostMac(), "");
    }

    @Test
    public void shouldReturnEmptyWhenHostLookupFails() {
        final NetworkController controller = new NetworkController() {
            @Override
            protected InetAddress getLocalHost() {
                throw new RuntimeException("forced");
            }
        };

        assertEquals(controller.getHostMac(), "");
    }
}

