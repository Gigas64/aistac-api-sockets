/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.transport;

import io.aistac.common.api.sockets.transport.TransportQueueService;
import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.api.sockets.transport.TransportService;
import io.aistac.common.canonical.data.example.BeanBuilder;
import io.aistac.common.canonical.queue.ObjectBeanQueue;
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.handler.connections.ConnectionService;
import io.aistac.common.api.sockets.handler.connections.ConnectionTypeEnum;
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
import java.util.concurrent.TimeUnit;
import javafx.beans.binding.Bindings;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Darryl Oatridge
 */
public class TransportQueueInterfaceTest {
    public TransportQueueInterfaceTest() {
    }

    @Before
    public void setUp() {
    }

    @Test(timeout = 12000)
    public void testTestSend() throws Exception {

        final int command = CommandBits.CMD_REQUEST | CommandBits.REQ_KEY | CommandBits.DATA_INTEGER;
        final String data = "12";
        ConnectionBean connection = ConnectionService.registerConnection(ConnectionTypeEnum.CLIENT, "localhost", 12345);
        TransportQueueInterfaceImp delivery = new TransportQueueInterfaceImp(connection);


        assertThat(delivery.sendData(command, data),is(notNullValue()));
        // check the bean
        TransportBean take = TransportQueueService.queue(connection.getQueueOut()).take();
        assertThat(take.getData(), is("12"));
        assertThat(take.getCommand(), is(command));
        assertThat(take.getConnectionId(), is(connection.getId()));
        // check the services
        assertThat(connection.getId(),is(take.getConnectionId()));
        assertThat(ConnectionService.getInstance().isConnection(connection.getId()),is(true));
        assertThat(TransportService.getInstance().isIdentifier(take.getConnectionId(), take.getId()),is(true));

        // put the transportbean back on queue and see if it turns up
        TransportQueueService.queue(connection.getQueueIn()).add(take);
        // wait for the delivery
        while(delivery.getTransport() == null) {}
        assertThat(delivery.getTransport(),is(take));
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
        logMap.put("SERVER", new StringBuffer("\nSERVER:\n"));
        logMap.put("CLIENT", new StringBuffer("\nCLIENT:\n"));
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