/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.aistac.common.api.sockets.handler.sockets;

import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.transport.TransportBean;
import java.io.IOException;
import java.nio.channels.NotYetConnectedException;

/**
 *
 * @author Darryl Oatridge
 */
public interface ClientHandlerInterface {

    /**
     * Creates a client connection ready for transmission.
     *
     * @return true if the connection was created successfully, false otherwise
     * @throws IOException if the connection process fails
     */
    public boolean connect() throws IOException;

    /**
     * closes a client connection and disconnects
     *
     * @throws IOException if the connection can't be closed
     */
    public void disconnect() throws IOException;

    /**
     * checks if there is a client connection open
     *
     * @return true if there is a connection, false otherwise
     */
    public boolean isConnected();

    /**
     * transmits a {@code TransportBean} object over the connection and returns the response.
     * The response can be null;
     *
     * @param request The request {@code TransportBean} object
     * @return the response {@code TransportBean} object
     * @throws IOException if there is an IO exception
     * @throws NotYetConnectedException if the connection is not open to send
     */
    public TransportBean request(TransportBean request) throws IOException, NotYetConnectedException;

    /**
     * The {@code ConnectionBean} object relating to this client connection
     *
     * @return the currently held {@code TransportBean} object
     */
    public ConnectionBean getConnectionBean();
}
