/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.handler.connections;

import io.aistac.common.api.sockets.transport.TransportQueueInterfaceImp;
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
import java.util.concurrent.TimeUnit;
import static org.hamcrest.CoreMatchers.*;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Darryl Oatridge
 */
public class ConnectionServiceTest {

    @Test
    public void testRegisterConnection() throws Exception {
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

        assertThat(clientDelivery.getTransport().getCommand(),is(CommandBits.CMD_RESPONSE | CommandBits.REQ_NOT_USED | CommandBits.DATA_TEXT));

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

        // Put your log tags here, ALL produces all tags
        LinkedHashMap<String, StringBuffer> logMap = new LinkedHashMap<>();
        logMap.put("CLIENT", new StringBuffer("\nCLIENT:\n"));
        logMap.put("SERVER", new StringBuffer("\nSERVER:\n"));
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