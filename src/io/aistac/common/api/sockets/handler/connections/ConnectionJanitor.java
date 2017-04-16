/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * @(#)ConnectionJanitor.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.handler.connections;

import io.aistac.common.canonical.handler.TaskHandlerInterface;
import io.aistac.common.api.sockets.transport.TransportQueueService;
import io.aistac.common.canonical.log.LoggerQueueService;
import java.util.concurrent.TimeUnit;


/**
 * The {@code ConnectionJanitor} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 08-Apr-2016
 */
public class ConnectionJanitor implements TaskHandlerInterface {
    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String JANITOR = "CONNECT.JANITOR";

    private volatile boolean isRunning;

    @Override
    public void run() {
        LOGGER.debug(JANITOR, "Connection Janitor starting...");
        isRunning = true;
        ConnectionService connectionService = ConnectionService.getInstance();
        while(isRunning) {
            try {
                // now pause for a bit
                TimeUnit.SECONDS.sleep(10);
            } catch(InterruptedException ex) {
                // if interrupted then exit
                isRunning = false;
            }
            // check the server first
            connectionService.start(connectionService.SERVER_ID());
            // then look at all the queues
            connectionService.getAllConnections().stream().filter((connection) -> (connection.getTaskId() > 0)).forEach((connection) -> {
                connectionService.start(connection.getId());
            });
        }
        isRunning = false;
        LOGGER.debug(JANITOR, "Connection Janitor exiting");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void stop() {
        LOGGER.debug(JANITOR, "Request to stop Connection Janitor");

        isRunning = false;
    }

    @Override
    public String getName() {
        return "ConnectionJanitorTask";
    }

}
