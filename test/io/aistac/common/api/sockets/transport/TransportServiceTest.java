/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.transport;

import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.api.sockets.transport.TransportService;
import io.aistac.common.canonical.data.example.BeanBuilder;
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.handler.connections.ConnectionService;
import io.aistac.common.api.sockets.handler.connections.ConnectionTypeEnum;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TransportServiceTest {


    @Test
    public void testRegiaterTransport() throws Exception {
        final int command = CommandBits.CMD_REQUEST | CommandBits.REQ_REMOVE_ID | CommandBits.DATA_LONG;
        ConnectionBean connection = ConnectionService.registerConnection(ConnectionTypeEnum.CLIENT, "localhost", 12345);
        TransportBean transport = TransportService.registerTransport(connection.getId(), command, "SomeData");
        assertThat(transport.getData(),is("SomeData"));
        assertThat(transport.getConnectionId(),is(connection.getId()));
        assertThat(transport.getCommand(),is(command));
    }


    @Test
    public void testRegiaterTransportExceptions() throws Exception {

        try {
            TransportService.registerTransport(0, 0);
            fail();
        } catch(IllegalArgumentException ex) {
            // success
        }
        final int command = CommandBits.CMD_REQUEST | CommandBits.REQ_REMOVE_ID | CommandBits.DATA_LONG;
        try {
            TransportService.registerTransport(0, command);
            fail();
        } catch(IllegalArgumentException ex) {
            // success
        }
    }

}