package com.softwaremagico.kt.rest.security;

import com.softwaremagico.kt.logger.RestServerLogger;
import org.springframework.stereotype.Controller;

import java.net.InetAddress;
import java.net.NetworkInterface;


@Controller
public class NetworkController {

    public String getHostMac() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            final NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
            final byte[] hardwareAddress = ni.getHardwareAddress();
            final String[] hexadecimal = new String[hardwareAddress.length];
            for (int i = 0; i < hardwareAddress.length; i++) {
                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
            }
            return String.join("-", hexadecimal);
        } catch (Exception e) {
            RestServerLogger.warning(this.getClass().getName(), "No mac server found!");
            e.printStackTrace();
        }
        return "";
    }
}
