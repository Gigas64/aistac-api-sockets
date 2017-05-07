/*
 * @(#)SocketServerHandler.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.handler.sockets;

import static io.aistac.common.canonical.data.ObjectBean.XmlFormat.PRINTED;
import static io.aistac.common.canonical.data.ObjectBean.XmlFormat.TRIMMED;
import io.aistac.common.canonical.exceptions.ObjectBeanException;
import io.aistac.common.canonical.handler.TaskHandlerInterface;
import io.aistac.common.api.sockets.handler.codec.StringEncoder;
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.api.sockets.transport.TransportQueueService;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import io.aistac.common.canonical.log.LoggerQueueService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@code SocketServerHandler} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 09-Jan-2016
 */
public class SocketServerAsyncQueueHandler implements TaskHandlerInterface {

    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String SERVER = "SERVER";
    private volatile boolean isRunning;
    private final ConnectionBean connection;
    private volatile AsynchronousServerSocketChannel serverSocketChannel;

    public SocketServerAsyncQueueHandler(ConnectionBean connection) {
        this.connection = connection;
    }

    /**
     * @return true if the socket server is running
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * stops the socket server.
     */
    @Override
    public void stop() {
        isRunning = false;
        try {
            serverSocketChannel.close();
        } catch(IOException ex) {
            // ignore
        }
        LOGGER.info(SERVER, "Server Stop request has been activated");
    }

    @Override
    public void run() {
        if(connection == null) {
            LOGGER.fatal(SERVER, "The ConnectionBean is null. Exiting Socket Server Asynchronous Handler");
            return;
        }
        LOGGER.info(SERVER, "Starting Server [" + connection.getId() + "] on Socket [" + connection.getHost() + ":" + connection.getPort() + "]");

        try {
            LOGGER.trace(SERVER, "Opening Asynchronous Server Socket Channel");
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            if(serverSocketChannel.isOpen()) {
                //set some options
                serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                //bind the server socket channel to local address
                LOGGER.trace(SERVER, "Binding Server Socket Channel [" + connection.getId() + "] on Socket [" + connection.getHost() + ":" + connection.getPort() + "]");
                serverSocketChannel.bind(new InetSocketAddress(connection.getHost(), connection.getPort()));
                connect();
            }
            LOGGER.trace(SERVER, "Shutting down Server socket");
            if(serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
        } catch(IOException ex) {
            LOGGER.error(SERVER, "IOException from the Server Socket Channel with message: " + ex.getMessage() + "");
        }
        LOGGER.info(SERVER, "Exiting Server [" + connection.getId() + "] on Socket [" + connection.getHost() + ":" + connection.getPort() + "]");
    }

    private void connect() {
        ExecutorService taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());

        isRunning = true;
        if(!serverSocketChannel.isOpen()) {
            LOGGER.fatal(SERVER, "Server Socket not open when preparing to accept connections");
            isRunning = false;
            return;
        }
        while(isRunning) {
            LOGGER.trace(SERVER, "Accepting Asynchronous Socket Channel Future");
            Future<AsynchronousSocketChannel> asynchronousSocketChannelFuture = serverSocketChannel.accept();
            //wait for incoming connections
            LOGGER.debug(SERVER, "Waiting to accept incoming connection...");
            try (AsynchronousSocketChannel asynchronousSocketChannel = asynchronousSocketChannelFuture.get()) {
                LOGGER.debug(SERVER, "Socket accepted from " + asynchronousSocketChannelFuture.get().getRemoteAddress().toString());
                // clear the queues as we now have a new connection
                TransportQueueService.queue(connection.getQueueIn()).clear();
                TransportQueueService.queue(connection.getQueueOut()).clear();
                LOGGER.trace(SERVER, "Submitting the send data task to the executor");
                // create the receive execute
                Callable<Integer> receiveWorker = () -> {
                    return receiveData(asynchronousSocketChannel);
                };
                LOGGER.trace(SERVER, "Submitting the receive data task to the executor");
                // create the send execute
                Callable<Integer> sendWorker = () -> {
                    return sendData(asynchronousSocketChannel);
                };
                // start up the send
                taskExecutor.submit(sendWorker);
                // Wait for the receive worker to exit
                final int receiveCmd = taskExecutor.submit(sendWorker).get();
                LOGGER.trace(SERVER, "Response received from executor: " + CommandBits.getStringFromBits(receiveCmd) + "");
                // close the Socket read for the next accept
                asynchronousSocketChannelFuture.get().shutdownInput();
                asynchronousSocketChannelFuture.get().close();
                if(CommandBits.contain(receiveCmd, CommandBits.CMD_EXIT)) {
                    LOGGER.debug(SERVER, "Command to Exit has been received");
                    isRunning = false;
                }
            } catch(InterruptedException | ExecutionException | IOException ex) {
                LOGGER.fatal(SERVER, "Server Socket Channel Future was interupted with: " + ex.toString());
                isRunning = false;
            }
        }
        LOGGER.trace(SERVER, "Shutting down Task Executor");
        taskExecutor.shutdownNow();
        while(!taskExecutor.isTerminated()) {
            //wait until all threads are finished
        }
        LOGGER.debug(SERVER, "No longer waiting to accept incoming connection");
        isRunning = false;
    }
    private int receiveData(final AsynchronousSocketChannel asynchronousSocketChannel) throws InterruptedException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        //transmitting data
        try {
            TransportBean request = null;
            // allo for 5 timeouts before closing the socket
            AtomicInteger timeoutRemaining = new AtomicInteger(5);
            while(asynchronousSocketChannel.isOpen()) {
                if(!isRunning) {
                    return CommandBits.CMD_EXIT;
                }
                try {
                    buffer.clear();
                    // READ SOCKET
                    LOGGER.trace(SERVER, "Socket Channel Read");
                    asynchronousSocketChannel.read(buffer).get(5, TimeUnit.SECONDS);
                    buffer.flip();
                    if(buffer.hasRemaining()) {
                        // reset the timeout counter as we did read something
                        timeoutRemaining.set(5);
                        try {
                            request = TransportBean.buildObjectBean(StringEncoder.decode(buffer));
                            LOGGER.trace(SERVER, "Request TransportBean: " + request.toXML(PRINTED, TRIMMED));
                            // ADD REQUEST QUEUE
                            LOGGER.debug(SERVER, "TransportBean request with command " + CommandBits.getStringFromBits(request.getCommand()) + " added to queue [" + connection.getQueueIn() + "]");
                            // check if the request was an exit
                            if(CommandBits.contain(request.getCommand(), CommandBits.CMD_EXIT)) {
                                // send to the recieve queue so it can exit cleanly;
                                TransportQueueService.queue(connection.getQueueOut()).put(request);
                                return CommandBits.CMD_EXIT;
                            }
                            TransportQueueService.queue(connection.getQueueIn()).add(request);
                        } catch(ObjectBeanException ex) {
                            LOGGER.error(SERVER, "Error when building the Object Bean: " + ex.getMessage());
                        }
                    } else {
                        if(timeoutRemaining.decrementAndGet() < 0) {
                            return CommandBits.CMD_CLOSE;
                        }
                        LOGGER.debug(SERVER, "Nothing to read from the client. Retries left [" + timeoutRemaining + "]");
                    }
                } catch(TimeoutException ex) {
                    LOGGER.debug(SERVER, "Socket Channel read timed out before completing. Attempting another read");
                }
            }
        } catch(InterruptedException | ExecutionException | NotYetConnectedException ex) {
            LOGGER.error(SERVER, "Exception thrown when recieving data: " + ex.toString());
            // just let it fall out to the end
        }
        // return the closed command
        return CommandBits.CMD_CLOSE;
    }

    private int sendData(final AsynchronousSocketChannel asynchronousSocketChannel) throws InterruptedException {
        try {
            while(asynchronousSocketChannel.isOpen()) {
                if(!isRunning) {
                    return CommandBits.CMD_EXIT;
                }
                TransportBean response;
                // WAIT RESPONSE QUEUE **
                LOGGER.debug(SERVER, "Take from response queue [" + connection.getQueueOut() + "]");
                response = TransportQueueService.queue(connection.getQueueOut()).take();
                // check if null as timed out
                if(response == null) {
                    LOGGER.debug(SERVER, "Response queue was empty");
                    continue;
                }
                try {
                    // WRITE SOCKET
                    LOGGER.trace(SERVER, "Response TransportBean: " + response.toXML(PRINTED, TRIMMED));
                    LOGGER.debug(SERVER, "TransportBean response with command " + CommandBits.getStringFromBits(response.getCommand()) + " written to Socket");
                    asynchronousSocketChannel.write(StringEncoder.encode(response.toXML())).get(5, TimeUnit.SECONDS);
                    if(CommandBits.contain(response.getCommand(), CommandBits.CMD_CLOSE)) {
                        return CommandBits.CMD_CLOSE;
                    }
                } catch(TimeoutException ex) {
                    LOGGER.error(SERVER, "Timeout on socket write when sending a response. Request put back on queue queue [" + connection.getQueueOut() + "]");
                    // add the request back to the queue as write timed out
                    TransportQueueService.queue(connection.getQueueOut()).put(response);
                }
            }
        } catch(InterruptedException | ExecutionException | NotYetConnectedException ex) {
            LOGGER.error(SERVER, "Exception thrown when recieving data: " + ex.toString());
            // just let it fall out to the end
        }
        // return the closed command
        return CommandBits.CMD_CLOSE;
    }

    @Override
    public String getName() {
        return "SocketServerConnection:[" + connection.getId() + "]";
    }

}
