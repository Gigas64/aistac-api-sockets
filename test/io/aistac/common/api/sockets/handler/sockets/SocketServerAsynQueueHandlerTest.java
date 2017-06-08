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
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class SocketServerAsynQueueHandlerTest {

    @Test()
    public void testServer() throws Exception {
        // server
        ConnectionBean serverConnection = new ConnectionBean(1, ConnectionTypeEnum.SERVER, "localhost", 10201, -1, "MyServer");
        SocketServerAsyncQueueHandler server = new SocketServerAsyncQueueHandler(serverConnection);
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

        // the test
        TransportQueueService.queue(clientConnection.getQueueOut()).add(transport);
        TransportBean take = TransportQueueService.queue(serverConnection.getQueueIn()).poll(10, TimeUnit.SECONDS);
        TransportQueueService.queue(serverConnection.getQueueOut()).add(transport);
        TransportQueueService.queue(clientConnection.getQueueIn()).poll(10, TimeUnit.SECONDS);
        //
        client.stop();
        clientWorker.get(10, TimeUnit.SECONDS);
        server.stop();
        serverWorker.get(10, TimeUnit.SECONDS);
    }

    /**
     * *************************************
     * logging to output screen
     **************************************
     */
    private static ExecutorService executor;

    @BeforeClass
    public static void setUpClass() {
        executor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        //setup logging
        Callable<String> worker = () -> {
            return writeLogs();
        };
        executor.submit(worker);
    }

    @AfterClass
    public static void tearDownClass() {
        // shut down the executor to print the logs
        executor.shutdownNow();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch(InterruptedException ex) {
            // do nothing
        }
    }

    private static String writeLogs() {
        LoggerQueueService queueService = LoggerQueueService.getInstance();
        queueService.setLogLevel(LoggerLevel.TRACE);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");

        LinkedHashMap<String, StringBuffer> logMap = new LinkedHashMap<>();
        logMap.put("CLIENT", new StringBuffer("\nCLIENT:\n"));
        logMap.put("SERVER", new StringBuffer("\nSERVER:\n"));

        // identify class
        System.out.println("*** START TEST LOG ***");

        try {
            while(true) {
                LoggerBean log = queueService.take();
                logMap.keySet().stream().filter((logName) -> (log.getTag().contains(logName) || logName.equals("ALL"))).forEachOrdered((logName) -> {
                    logMap.get(logName)
                        .append(formatter.format(new Date())).append(" ")
                        .append(LoggerLevel.level(log.getId())).append(" ")
                        .append(log.getTag()).append(" ")
                        .append((log.getMessage()).trim()).append("\n");
                });
            }
        } catch(InterruptedException ex) {
            // fall through
        }
        logMap.keySet().stream().forEachOrdered((logName) -> {
            System.out.println(logMap.get(logName).toString());
        });
        return "Done";
    }
}