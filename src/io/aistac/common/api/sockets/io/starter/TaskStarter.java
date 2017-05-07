/*
 * @(#)TaskStarter.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.io.starter;

import io.aistac.common.canonical.log.LoggerQueueService;
import io.aistac.common.canonical.properties.TaskPropertiesService;
import io.aistac.common.canonical.valueholder.ValueHolder;
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.handler.connections.ConnectionService;
import io.aistac.common.api.sockets.handler.connections.ConnectionTypeEnum;
import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.api.sockets.transport.TransportQueueService;
import io.aistac.common.api.sockets.transport.TransportService;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The {@code TaskStarter} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 05-Apr-2016
 */
public class TaskStarter {

    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String STARTER = "STARTER";

    /**
     * @param args the command line arguments
     * @return false if the arguments are incorrect
     * @throws InterruptedException server was interrupted
     * @throws ExecutionException server had an execution exception
     */
    public static boolean starter(String[] args) throws InterruptedException, ExecutionException {
        // create the command line parser
        final CommandLineParser parser = new DefaultParser();

        final Option oTaskName = Option.builder("n").argName("oTaskName").hasArg().longOpt("taskName").desc("The name of the task component").build();
        final Option oTaskPort = Option.builder("p").argName("oTaskPort").hasArg().longOpt("taskPort").desc("The task server port number").build();
        final Option oPropertiesPort = Option.builder("pp").argName("oPropertiesPort").hasArg().longOpt("propertiesPort").desc("the port number of the properties server").build();
        final Option oPropertiesHost = Option.builder("ph").argName("oPropertiesHost").hasArg().longOpt("propertiesHost").desc("the host IP or name of the properties server").build();
        final Option oXmlRoot = Option.builder("x").argName("oXmlRoot").hasArg().longOpt("xmlRoot").desc("the root name of xml data").build();
        final Option oLogLevel = Option.builder("l").argName("oLogLevel").hasArg().longOpt("logLevel").desc("the logger lever (FATAL,ERROR,WARN,INFO,DEBUG,TRACE)").build();
        // create the Options
        final Options options = new Options();
        options.addOption("h", "help", false, "Prints this usage message");
        options.addOption(oTaskName);
        options.addOption(oTaskPort);
        options.addOption(oPropertiesPort);
        options.addOption(oPropertiesHost);
        options.addOption(oXmlRoot);
        options.addOption(oLogLevel);

        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
            // parse the command line arguments
        } catch(ParseException exp) {
            System.err.println("Unexpected exception when reading command line options: " + exp.getMessage());
            usage(options);
            return false;
        }
        if(cmdLine == null || cmdLine.hasOption('h') || cmdLine.hasOption("help")) {
            usage(options);
            return false;
        }
        // get the command line values
        final String taskName = cmdLine.getOptionValue("taskName", "SingleTaskLogger");
        final int taskPort = Integer.parseInt(cmdLine.getOptionValue("taskPort", "20667"));
        final String propertiesHost = cmdLine.getOptionValue("propertiesHost", "localhost");
        final int propertiesPort = Integer.parseInt(cmdLine.getOptionValue("propertiesPort", "-1"));
        final String xmlRoot = cmdLine.getOptionValue("xmlRoot", "Oathouse");
        final String logLevel = cmdLine.getOptionValue("logLevel", "ERROR");
        LoggerQueueService.getInstance().setLogLevel(logLevel);
        // start up the logger
        LOGGER.debug(STARTER, "Command Line Options:");
        LOGGER.debug(STARTER, "   taskName       = " + taskName);
        LOGGER.debug(STARTER, "   taskPort       = " + taskPort);
        LOGGER.debug(STARTER, "   propertiesHost = " + propertiesHost);
        LOGGER.debug(STARTER, "   propertiesPort = " + propertiesPort);
        LOGGER.debug(STARTER, "   xmlRoot        = " + xmlRoot);
        LOGGER.debug(STARTER, "   logLevel       = " + logLevel);

        LOGGER.info(STARTER, "Server Started [" + taskPort + "]...");
        // set the main properties
        TaskPropertiesService.getInstance().add("aistac.task.profile.owner", taskName);
        TaskPropertiesService.getInstance().add("aistac.task.profile.instance", ValueHolder.uniqueName(taskName, "instance"));
        TaskPropertiesService.getInstance().add("aistac.api.sockets.server.port", taskPort);
        TaskPropertiesService.getInstance().add("aistac.canonical.objectbean.xml.root", xmlRoot);

        // initialise the Connection Service
        ConnectionService.getInstance();

        if(propertiesPort > 1024) {
            // register the config task connection with the ConnectionService
            final ConnectionBean propertiesConnection = ConnectionService.registerConnection(ConnectionTypeEnum.CLIENT, propertiesHost, propertiesPort);
            // add to properties
            TaskPropertiesService.getInstance().add("aistac.connection.properties.id", propertiesConnection.getId());
            // send a request to get all properties

            final int command = CommandBits.CMD_PROPERTY | CommandBits.REQ_OBSERVE | CommandBits.DATA_COMMSXML | CommandBits.OPT_RETRY;
            TransportService.registerTransport(propertiesConnection.getId(), command, propertiesConnection.toXML());
            try {
                TransportBean response = TransportQueueService.queue(propertiesConnection.getQueueIn()).poll(5, TimeUnit.SECONDS);
                if(response == null || CommandBits.contain(response.getCommand(), CommandBits.CMD_FAILURE)) {
                    LOGGER.error(STARTER, "Response to observe Properties from [" + propertiesHost + ":" + propertiesPort + "] returned failure");
                }
            } catch(InterruptedException ex) {
                LOGGER.error(STARTER, "Interupt exception when wating for a response to observe Properties request from [" + propertiesHost + ":" + propertiesPort + "] : " + ex);
            }
        }
        return true;

    }

    private static void usage(Options options) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java {TaskStarter} ", options);

    }

    // this is an entry point classand thus should not be instanciated
    private TaskStarter() {
    }
}
