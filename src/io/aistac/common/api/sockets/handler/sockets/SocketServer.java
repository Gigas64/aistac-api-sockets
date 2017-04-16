/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * @(#)SocketServer.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:	Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.handler.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * The {@code SocketServer} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 09-Jan-2016
 */
public class SocketServer {

    public static void main(String[] args) {

        final int DEFAULT_PORT = 5555;
        final String IP = "127.0.0.1";
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        //create a new server socket channel
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            //continue if it was successfully created
            if(serverSocketChannel.isOpen()) {
                //set the blocking mode
                serverSocketChannel.configureBlocking(true);
                //set some options
                serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                //bind the server socket channel to local address
                serverSocketChannel.bind(new InetSocketAddress(IP, DEFAULT_PORT));
                //display a waiting message while ... waiting clients
                System.out.println("Waiting for connections ...");
                //wait for incoming connections
                while(true) {
                    try (SocketChannel socketChannel = serverSocketChannel.accept()) {
                        System.out.println("Incoming connection from: " + socketChannel.getRemoteAddress());
                        //transmitting data
                        while(socketChannel.read(buffer) != -1) {
                            buffer.flip();
                            socketChannel.write(buffer);
                            if(buffer.hasRemaining()) {
                                buffer.compact();
                            } else {
                                buffer.clear();
                            }
                        }
                    }
                }
            }
        } catch(IOException ex) {
            System.out.println("Unable to open server socket channel: " + ex.getMessage());
        }

    }

}
