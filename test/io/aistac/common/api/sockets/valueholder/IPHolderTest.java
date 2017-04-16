/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.valueholder;

import io.aistac.common.api.sockets.valueholder.IPHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class IPHolderTest {

    public IPHolderTest() {
    }

    @Test
    public void testIpToLong() {
        String ips = "192.168.1.1";
        long ipd = IPHolder.ipToLong(ips);
        assertThat(IPHolder.longToIp(ipd), is(equalTo(ips)));

    }

}