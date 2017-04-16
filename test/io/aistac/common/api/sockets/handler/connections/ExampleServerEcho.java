/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * @(#)ExampleServerEcho.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.handler.connections;

import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.canonical.log.LoggerQueueService;
import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.api.sockets.transport.TransportQueueInterface;
import io.aistac.common.api.sockets.transport.TransportService;
import io.aistac.common.api.sockets.valueholder.CommandBits;

/**
 * The {@code ExampleServerEcho} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 24-Apr-2016
 */
public class ExampleServerEcho implements TransportQueueInterface {

    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String TEST = "TEST.ECHO";
    private final ConnectionBean connection;

    public ExampleServerEcho(ConnectionBean connection) {
        this.connection = connection;
    }


    @Override
    public ConnectionBean getConnection() {
        return connection;
    }

    @Override
    public void deliver(TransportBean ob) {
        LOGGER.debug(TEST, "Revieved object and echoing");
        final int command = CommandBits.CMD_RESPONSE | CommandBits.REQ_NOT_USED | CommandBits.DATA_TEXT;
        sendData(command, "Return Text");
    }

}
