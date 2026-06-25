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

import org.springframework.stereotype.Controller;

import java.net.SocketException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;


@Controller
public class NetworkController {

    protected InetAddress getLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    protected NetworkInterface getByInetAddress(InetAddress address) throws SocketException {
        return NetworkInterface.getByInetAddress(address);
    }

    public String getHostMac() {
        try {
            final InetAddress localHost = this.getLocalHost();
            final NetworkInterface ni = this.getByInetAddress(localHost);
            if (ni == null) {
                return "";
            }
            final byte[] hardwareAddress = ni.getHardwareAddress();
            if (hardwareAddress == null || hardwareAddress.length == 0) {
                return "";
            }
            final String[] hexadecimal = new String[hardwareAddress.length];
            for (int i = 0; i < hardwareAddress.length; i++) {
                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
            }
            return String.join("-", hexadecimal);
        } catch (Exception _) {
            //Ignored.
        }
        return "";
    }
}
