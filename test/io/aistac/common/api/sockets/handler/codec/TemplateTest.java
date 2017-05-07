/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.handler.codec;

// common imports
import io.aistac.common.canonical.data.example.BeanBuilder;
import io.aistac.common.canonical.data.example.ExampleBean;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import static java.util.Arrays.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
// logging imports
import io.aistac.common.canonical.log.LoggerBean;
import io.aistac.common.canonical.log.LoggerLevel;
import io.aistac.common.canonical.log.LoggerQueueService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
// threading imports
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TemplateTest {


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
        logMap.put("ALL", new StringBuffer("\nALL:\n"));

        // identify class
        System.out.println("*** Start Test Log ***");
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