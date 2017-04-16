/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)TransportService.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.transport;

import io.aistac.common.canonical.data.AbstractMemoryBeanCache;
import io.aistac.common.canonical.valueholder.ValueHolder;
import io.aistac.common.api.sockets.handler.connections.ConnectionService;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import io.aistac.common.canonical.log.LoggerQueueService;
import java.util.List;
import java.util.Set;

/**
 * The {@code TransportService} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 07-Apr-2016
 */
public class TransportService extends AbstractMemoryBeanCache<TransportBean> {

    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String SERVICE = "TRANS.SERVICE";
    // Singleton Instance
    private volatile static TransportService INSTANCE;
    // the instance name of the owner
    private static final String WHOAMI = ValueHolder.uniqueName("TransportService", "owner");

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private TransportService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code TransportService} class
     * @return instance of the {@code TransportService}
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public static TransportService getInstance() {
        if(INSTANCE == null) {
            synchronized (TransportService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new TransportService();
                }
            }
        }
        return INSTANCE;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Public Class Methods">
    /* ***************************************************
     * P U B L I C   C L A S S   M E T H O D S
     * ***************************************************/

    /**
     * returns the {@code TransportBean} with the specified identifier and connectionId.
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     * @param identifier the identifier of the {@code TransportBean}
     * @return T the object bean
     */
    public TransportBean getTransport(int connectionId, int identifier) {
        LOGGER.debug(SERVICE, "Get TransportBean connection [" + connectionId + "] id [" + identifier + "]");
        return getObjectInKey(connectionId, identifier);
    }

    /**
     * returns all {@code TransportBean} objects for a specified connectionId
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     * @return LinkedList of {@code TransportBean}
     */
    public List<TransportBean> getAllTransports(int connectionId) {
        return (super.getAllObjectsInKey(connectionId));
    }

    /**
     * Returns all the connectionIds that are currently being held
     *
     * @return set of integers
     */
    public Set<Integer> getAllConnections() {
        return (super.getAllKeysInMap());
    }

    /**
     * gets all the identifers for a particular connectionId
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     * @return a set of integer id values
     */
    public Set<Integer> getAllIdentifier(int connectionId) {
        return (super.getAllIdentifiersInKey(connectionId));
    }

    /**
     * returns true if the identifier exists within a certain connectionId.
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     * @param identifier the identifier of the {@code TransportBean}
     * @return true if found and false if not
     */
    public boolean isIdentifier(int connectionId, int identifier) {
        if(getAllIdentifier(connectionId).contains(identifier)) {
            return (true);
        }
        return (false);
    }

    /**
     * saves {@code TransportBean}. If the {@code TransportBean} identifier exists, the {@code TransportBean} replaces the existing one and modifies the
     * {@code TransportBean} modified parameter.
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     * @param command the {@code CommandBits} instructions
     * @param data the String data to transport
     * @return the {@code TransportBean} that was set
    */
    public synchronized TransportBean setTransport(int connectionId, int command, String data) {
        LOGGER.debug(SERVICE, "Set TransportBean connection [" + connectionId + "]");
        LOGGER.debug(SERVICE, "                  command    " + CommandBits.getStringFromBits(command) );
        LOGGER.trace(SERVICE, "                  data       '" + data + "'");
        if(!CommandBits.validateCommand(command)) {
            throw new IllegalArgumentException("Unable to set TransportBean as the command parameter is not valid");
        }
        if(!ConnectionService.getInstance().isConnection(connectionId)) {
            throw new IllegalArgumentException("The connection identifier [" + connectionId + "] is not registered with the ConnectionServer");
        }
        String _data = data == null ? "" : data;
        TransportBean t = new TransportBean(generateIdentifier(connectionId), connectionId, command, _data, WHOAMI);
        // save it and return
        return (super.setObjectInKey(connectionId, t));
    }

    /**
     * Removes {@code TransportBean} with the provided connectionId and identifier.
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     * @param identifier the identifier of the {@code TransportBean}
     * @return the {@code TransportBean} that was deleted
     */
    public TransportBean removeTransport(int connectionId, int identifier) {
        LOGGER.debug(SERVICE, "Remove TransportBean connection [" + connectionId + "] id [" + identifier + "]");
        return super.removeObjectInKey(connectionId, identifier);
    }

    /**
     * Removes a whole connectionId and all the {@code TransportBean} objects within that connectionId.
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     */
    public void removeConnection(int connectionId) {
        LOGGER.debug(SERVICE, "Remove All TransportBean connection [" + connectionId + "]");
        super.removeAllObjectsInKey(connectionId);
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Public Static Methods">
    /* ***************************************************
     * P U B L I C   S T A T I C   M E T H O D S
     * ***************************************************/

    /**
     * Utility static to register a transport bean.
     * This also included a unique id with the connectionId being the ConnectionBean id. The transport bean will take the queueIn from the
     * identity .
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     * @param command the valid command bits
     * @param data the transport data
     * @return a {@code TransportBean}
     */
    public static TransportBean registerTransport(int connectionId, int command, String data) {
        return TransportService.getInstance().setTransport(connectionId, command, data);
    }

    /**
     * Utility static to quickly generate a {@code TransportBean} with the instance ideas of the identity.
     * The transport bean will take the queueIn from the identity with the queueOut being the
     * destination of the transportBean.
     *
     * @param connectionId the connectionId associated with this {@code TransportBean}.
     * @param command the valid command bits
     * @return a {@code TransportBean}
     */
    public static TransportBean registerTransport(int connectionId, int command) {
        return registerTransport(connectionId, command, "");
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Private Class Methods">
    /* ***************************************************
     * P R I V A T E   C L A S S   M E T H O D S
     * ***************************************************/


    //</editor-fold>

 }
