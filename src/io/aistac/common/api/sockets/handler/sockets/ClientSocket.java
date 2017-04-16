/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * @(#)ClientSocket.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.handler.sockets;

import io.aistac.common.canonical.exceptions.ObjectBeanException;
import io.aistac.common.api.sockets.handler.codec.StringEncoder;
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.transport.TransportBean;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;

/**
 * The {@code ClientSocket} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 10-Jan-2016
 */
public class ClientSocket implements ClientHandlerInterface {
    private final ConnectionBean connection;
    private volatile boolean connected;
    private volatile SocketChannel socketChannel;


    public ClientSocket(ConnectionBean connection) {
        this.connection = connection;
        this.connected = false;
        socketChannel = null;
    }

    @Override
    public boolean connect() throws IOException  {
        try {
            //create a new socket channel
            socketChannel = SocketChannel.open();
            //continue if it was successfully created
            if(socketChannel.isOpen()) {
                //set the blocking mode
                socketChannel.configureBlocking(true);
                //set some options
                socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
                socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
                socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);
                //connect this channel's socket
                socketChannel.connect(new InetSocketAddress(connection.getHost(), connection.getPort()));
                //check if the connection was successfully accomplished
                if(socketChannel.isConnected()) {
                    connected = true;
                    return true;
                }
            }
        } catch(IOException ex) {
            throw new IOException("Client [" + connection.getId() + "] connect error: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public void disconnect() throws IOException {
        try {
            socketChannel.close();
        } catch(IOException ex) {
            throw new IOException("Client [" + connection.getId() + "] dissconnect error: " + ex.getMessage());
        }
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }


    @Override
    public TransportBean request(TransportBean request) throws NotYetConnectedException, IOException {
        if(request == null) {
            throw new NullPointerException("Client [" + connection.getId() + "] error: The request TransportBean object is null");
        }
        socketChannel.write(StringEncoder.encode(request.toXML()));
        ByteBuffer data = ByteBuffer.allocateDirect(8 * 1024);
        socketChannel.read(data);
        // decode the data
        data.flip();
        if(data.hasRemaining()) {
            try {
                return TransportBean.buildObjectBean(StringEncoder.decode(data));
            } catch(ObjectBeanException ex) {
                throw new IOException("Client [" + connection.getId() + "] error: Unable build the Transport Bean " + ex.toString());
            }
        }
        return null;
     }

    @Override
    public ConnectionBean getConnectionBean() {
        return connection;
    }
}
