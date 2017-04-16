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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

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

    @Test
    public void testTestSend() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        //setup logging
        Callable<String> worker = () -> {
                    return writeLogs();
        };
        executor.submit(worker);

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

    private String writeLogs() {
        LoggerQueueService queueService = LoggerQueueService.getInstance();
        queueService.setLogLevel(LoggerLevel.TRACE);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        try {
            while(true) {
                LoggerBean log = queueService.take();
                String output = (formatter.format(new Date()) + " " + LoggerLevel.level(log.getId()) + " " + log.getTag() + " " + log.getMessage()).trim();
                System.out.println(output);
            }
        } catch(InterruptedException ex) {
            // fall through
        }
        return "Done";
    }
}