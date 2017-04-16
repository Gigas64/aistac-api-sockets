/*
 * @(#)SocketClientHandler.java
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
import io.aistac.common.api.sockets.transport.TransportService;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import io.aistac.common.canonical.log.LoggerQueueService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The {@code SocketClientHandler} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 10-Jan-2016
 */
public class SocketClientAsynchronousHandler implements TaskHandlerInterface {

    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String CLIENT = "CLIENT";

    private AsynchronousSocketChannel socketChannel;
    private final ConnectionBean connection;
    private volatile boolean isRunning;

    /**
     * Constructor to start an asynchronous socket client, passing the {@code ConnectionBean}
     *
     * @param connection the {@code ConnectionBean}
     */
    public SocketClientAsynchronousHandler(ConnectionBean connection) {
        this.connection = connection;
        this.isRunning = false;
    }

    /**
     * @return true if the client is still running
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * stops the client at the next cycle.
     */
    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        LOGGER.info(CLIENT, "Starting Client [" + connection.getId() + "] on Socket [" + connection.getHost() + ":" + connection.getPort() + "]");
        isRunning = true;
        try {
            // CONNECT
            this.connect();
            while(isRunning) {
                // WAIT REQUEST
                LOGGER.trace(CLIENT, "Poll request queue [" + connection.getQueueOut() + "]");
                TransportBean response = this.sendRequest(TransportQueueService.queue(connection.getQueueOut()).poll(1, TimeUnit.SECONDS));
                // ADD RESPONSE
                if(response != null) {
                    LOGGER.trace(CLIENT, "Response TransportBean: " + response.toXML(PRINTED, TRIMMED));
                    LOGGER.debug(CLIENT, "TransportBean response with command " + CommandBits.getStringFromBits(response.getCommand()) + " added to queue [" + connection.getQueueIn() + "]");
                    TransportQueueService.queue(connection.getQueueIn()).add(response);
                    // now remove it from the TransportService
                    LOGGER.trace(CLIENT, "Delivered Response so removing TransportBean [" + response.getId() + "] from the TransportService");
                    TransportService.getInstance().removeTransport(connection.getId(), response.getId());
                    // check for exit
                    if(CommandBits.contain(response.getCommand(), CommandBits.CMD_CLOSE)) {
                        // exit out of the loop
                        LOGGER.debug(CLIENT, "Closing connection [" + connection.getId() + "] after request to close");
                        isRunning = false;
                    }
                } else {
                    LOGGER.debug(CLIENT, "Response returned a null");
                    // if we got null the just exit
                    isRunning = false;
                }
            }
            if(socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch(InterruptedException | IOException | NullPointerException | NotYetConnectedException ex) {
            LOGGER.fatal(CLIENT, "Interrupted Exception when getting request from queue: " + ex.getMessage());
        }
        LOGGER.info(CLIENT, "Exiting Client [" + connection.getId() + "] on Socket [" + connection.getHost() + ":" + connection.getPort() + "]");
        isRunning = false;
    }

    private void connect() throws IOException {
        LOGGER.trace(CLIENT, "Opening Asynchronous Socket Channel for client [" + connection.getId() + "]");
        socketChannel = AsynchronousSocketChannel.open();
        //continue if it was successfully created
        if(socketChannel.isOpen()) {
            //set some options
            socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
            socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            LOGGER.trace(CLIENT, "Conecting client [" + connection.getId() + "] to [" + connection.getHost() + ":" + connection.getPort() + "]");
            Future<Void> connect = socketChannel.connect(new InetSocketAddress(connection.getHost(), connection.getPort()));
            if(connect == null) {
                LOGGER.error(CLIENT, "Unable connect to server socket [" + connection.getHost() + ":" + connection.getPort() + "]");
                throw new NullPointerException("Client '" + connection.getOwner() + "' error: Unable connect to server socket for [" + connection.getHost() + ":" + connection.getPort() + "]");
            }
        } else {
            LOGGER.error(CLIENT, "Unable to open server socket [" + connection.getHost() + ":" + connection.getPort() + "]");
            throw new NotYetConnectedException();
        }
    }

    private TransportBean sendRequest(TransportBean request) {
        if(!socketChannel.isOpen()) {
            LOGGER.error(CLIENT, "Client [" + connection.getId() + "] error: Unable to send request, no Server connection");
            return null;
        }
        try {
            LOGGER.trace(CLIENT, "Request TransportBean: " + request.toXML(PRINTED, TRIMMED));
            LOGGER.debug(CLIENT, "TransportBean request with command " + CommandBits.getStringFromBits(request.getCommand()) + " written to Socket");
            ByteBuffer data = ByteBuffer.allocateDirect(8 * 1024);
            // WRITE SOCKET
            try {
                LOGGER.trace(CLIENT, "Write to Socket Channel");
                socketChannel.write(StringEncoder.encode(request.toXML())).get(1, TimeUnit.SECONDS);
            } catch(TimeoutException timeoutException) {
                LOGGER.error(CLIENT, "Client [" + connection.getId() + "] timed out when writing to Socket Channel");
                // as we timed out when we did the write, put the request back on the queue
                TransportQueueService.queue(connection.getQueueOut()).put(request);
                return null;
            }
            // READ SOCKET
            TransportBean response = null;
            try {
                LOGGER.trace(CLIENT, "Reading from Socket Channel");
                socketChannel.read(data).get(1, TimeUnit.SECONDS);
                // decode the data
                data.flip();
                if(data.hasRemaining()) {
                    response = TransportBean.buildObjectBean(StringEncoder.decode(data));
                }
            } catch(TimeoutException timeoutException) {
                LOGGER.error(CLIENT, "Client [" + connection.getId() + "] timed out when reading from Socket Channel");
                // Ignore as this allowed us to skip over the code on read timeout
            }
            return response;
        } catch(ObjectBeanException ex) {
            LOGGER.error(CLIENT, "Client [" + connection.getId() + "] error: Unable build the Transport Bean " + ex.toString());
        } catch(InterruptedException | ExecutionException | NotYetConnectedException ex) {
            LOGGER.error(CLIENT, "Client [" + connection.getId() + "] error: Unable to read from server because of an " + ex.toString());
        }
        return null;
    }

    @Override
    public String getName() {
        return "SocketClientConnection:[" + connection.getId() + "]";
    }
}
