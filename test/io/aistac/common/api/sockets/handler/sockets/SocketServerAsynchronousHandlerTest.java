/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.handler.sockets;

import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.handler.connections.ConnectionTypeEnum;
import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.api.sockets.transport.TransportQueueService;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import io.aistac.common.canonical.log.LoggerBean;
import io.aistac.common.canonical.log.LoggerLevel;
import io.aistac.common.canonical.log.LoggerQueueService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class SocketServerAsynchronousHandlerTest {

    @Test(timeout = 12000)
    public void testServer() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        //setup logging
        Callable<String> worker = () -> {
                    return writeLogs();
        };
        executor.submit(worker);

        // server
        ConnectionBean serverConnection = new ConnectionBean(1, ConnectionTypeEnum.SERVER, "localhost", 10201, -1, "MyServer");
        SocketServerAsynchronousHandler server = new SocketServerAsynchronousHandler(serverConnection);
        Future<?> serverWorker = executor.submit(server);
        while(!server.isRunning()) {}
        // transport
        final int command = CommandBits.CMD_REQUEST | CommandBits.REQ_KEY | CommandBits.DATA_INTEGER;
        TransportBean transport = new TransportBean(1, 1, command, "24", "MyTransport");

        // client
        ConnectionBean clientConnection = new ConnectionBean(2, ConnectionTypeEnum.CLIENT, "localhost", 10201, -1, "MyClient");
        SocketClientAsynchronousHandler client = new SocketClientAsynchronousHandler(clientConnection);
        Future<?> clientWorker = executor.submit(client);
        while(!client.isRunning()) {}
        TransportQueueService.queue(clientConnection.getQueueOut()).add(transport);
        TransportQueueService.queue(serverConnection.getQueueIn()).take();
        TransportQueueService.queue(serverConnection.getQueueOut()).add(transport);
        TransportQueueService.queue(clientConnection.getQueueIn()).take();
        client.stop();
        clientWorker.get(2, TimeUnit.SECONDS);
        server.stop();
        serverWorker.get(2, TimeUnit.SECONDS);
    }

    private String writeLogs() {
        LoggerQueueService queueService = LoggerQueueService.getInstance();
        queueService.setLogLevel(LoggerLevel.TRACE);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        try {
            while(true) {
                LoggerBean log = queueService.take();
                if(log.getTag().contains("SERVER") || log.getTag().contains("CLIENT")) {
                    String output = (formatter.format(new Date()) + " " + LoggerLevel.level(log.getId()) + " " + log.getTag() + " " + log.getMessage()).trim();
                    System.out.println(output);
                }
            }
        } catch(InterruptedException ex) {
            // fall through
        }
        return "Done";
    }
}