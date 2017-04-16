/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * @(#)TransportQueueInterfaceImp.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.transport;

import io.aistac.common.api.sockets.transport.TransportQueueInterface;
import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import io.aistac.common.canonical.log.LoggerQueueService;

/**
 * The {@code TransportQueueInterfaceImp} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 08-Apr-2016
 */
public class TransportQueueInterfaceImp implements TransportQueueInterface {

    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String TEST = "TEST.TQIMP";

    private TransportBean transport = null;
    private final ConnectionBean connection;

    public TransportQueueInterfaceImp(ConnectionBean connection) {
        this.connection = connection;
    }

    public TransportBean getTransport() {
        return transport;
    }

    @Override
    public void deliver(TransportBean ob) {
        LOGGER.debug(TEST, "Recieved Transport command " + CommandBits.getStringFromBits(ob.getCommand()));
        transport = ob;
    }

    @Override
    public ConnectionBean getConnection() {
        return connection;
    }

}
