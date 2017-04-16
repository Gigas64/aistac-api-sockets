 /*
 * @(#)IPHolder.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.valueholder;

/**
 * The {@code IPHolder} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 23-Mar-2016
 */
public class IPHolder {

    /**
     * Converts a string IP address to a long value
     *
     * @param ipAddress can be padded or not
     * @return a convertible long
     */
    public static long ipToLong(String ipAddress) {

        String[] addrArray = ipAddress.split("\\.");
        long num = 0;
        for(int i = 0; i < addrArray.length; i++) {
            int power = 3 - i;
            // 1. (192 % 256) * 256 pow 3
            // 2. (168 % 256) * 256 pow 2
            // 3. (2 % 256) * 256 pow 1
            // 4. (1 % 256) * 256 pow 0
            num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
        }
        return num;
    }

    /**
     * converts a long into an IP
     *
     * @param ipLong the long representing an an IP
     * @return the IP address as a String
     */
    public static String longToIp(long ipLong) {
        return ((ipLong >> 24) & 0xFF) + "." + ((ipLong >> 16) & 0xFF) + "." + ((ipLong >> 8) & 0xFF) + "." + (ipLong & 0xFF);
    }


    private IPHolder() {
    }

}

