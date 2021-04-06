package uk.co.compendiumdev.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Checks if Port is in USE
 * Provided code from EvilTester Repository
 * All credits go to author of that repository
 */
public class Port {

    public static boolean inUse(String host, String port) {
        return inUse(host, Integer.valueOf(port));
    }

    public static boolean inUse(String host, int port) {
        Socket s = null;

        try {

            // prevent this taking ages when no proxy setup by timing out after 5 seconds
            SocketAddress address = new InetSocketAddress(host, port);
            s = new Socket();
            s.connect(address, 5000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if( s != null){
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
    }
}
