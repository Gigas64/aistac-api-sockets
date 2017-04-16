/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.handler.connections;

import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.handler.connections.ConnectionService;
import io.aistac.common.api.sockets.handler.connections.ConnectionTypeEnum;
import io.aistac.common.canonical.queue.ObjectBeanQueue;
import io.aistac.common.api.sockets.transport.TransportQueueInterfaceImp;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import io.aistac.common.canonical.log.LoggerBean;
import io.aistac.common.canonical.log.LoggerLevel;
import io.aistac.common.canonical.log.LoggerQueueService;
import io.aistac.common.api.sockets.transport.TransportQueueService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class ConnectionServiceTest {

    @Test
    public void testRegisterConnection() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        //setup logging
        Callable<String> worker = () -> {
                    return writeLogs();
        };
        executor.submit(worker);
        ConnectionService service = ConnectionService.getInstance();
        ConnectionBean server = service.getConnection(service.SERVER_ID());
        // start the server echo
        ExampleServerEcho echoDelivery = new ExampleServerEcho(server);
        // as we are not sending start the echo server.
        echoDelivery.startQueueMonitor(server.getQueueIn(), TransportQueueService.queue(server.getQueueIn()));
        // set tyhe client to talk to the server
        ConnectionBean client = ConnectionService.registerConnection(ConnectionTypeEnum.CLIENT, server.getHost(), server.getPort());
        service.start(client.getId());
        TransportQueueInterfaceImp clientDelivery = new TransportQueueInterfaceImp(client);

        final int command = CommandBits.CMD_REQUEST | CommandBits.REQ_KEY | CommandBits.DATA_INTEGER;
        clientDelivery.sendData(command, "24");
        TimeUnit.SECONDS.sleep(1);
        assertThat(clientDelivery.getTransport().getCommand(),is("Return Text"));

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