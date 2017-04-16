/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.handler.connections;

import io.aistac.common.api.sockets.handler.connections.ConnectionTypeEnum;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class ConnectionTypeEnumTest {

    @Test
    public void testIsValid() throws Exception {
        assertThat(ConnectionTypeEnum.isValid(ConnectionTypeEnum.CLIENT),is(true));
        assertThat(ConnectionTypeEnum.isValid(ConnectionTypeEnum.SERVER),is(true));
        assertThat(ConnectionTypeEnum.isValid(ConnectionTypeEnum.OBSERVER),is(true));
        assertThat(ConnectionTypeEnum.isValid(ConnectionTypeEnum.NO_VALUE),is(false));
        assertThat(ConnectionTypeEnum.isValid(ConnectionTypeEnum.UNDEFINED),is(false));
    }

}