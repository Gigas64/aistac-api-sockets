/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)ConnectionService.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.handler.connections;

import io.aistac.common.canonical.data.AbstractMemoryBeanCache;
import io.aistac.common.canonical.data.ObjectEnum;
import io.aistac.common.canonical.handler.TaskHandlerService;
import io.aistac.common.canonical.log.LoggerQueueService;
import io.aistac.common.canonical.properties.TaskPropertiesService;
import io.aistac.common.canonical.valueholder.ValueHolder;
import static io.aistac.common.api.sockets.handler.connections.ConnectionTypeEnum.SERVER;
import io.aistac.common.api.sockets.handler.sockets.SocketClientAsynchronousHandler;
import io.aistac.common.api.sockets.handler.sockets.SocketServerAsynchronousHandler;
import java.util.List;
import java.util.Set;

/**
 * The {@code ConnectionService} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 07-Apr-2016
 */
@SuppressWarnings("FinalClass")
public final class ConnectionService extends AbstractMemoryBeanCache<ConnectionBean> {

    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String CONNECT = "CONNECT.SERVICE";
    // Singleton Instance
    private volatile static ConnectionService INSTANCE;
    // the instance name of the owner
    private static final String WHOAMI = ValueHolder.uniqueName("ConnectionService", "owner");
    private final int serverConnectionId;

    // Janitor instance
    private final int janitorTaskId;

    //<editor-fold defaultstate="collapsed" desc="Singleton Methods">
    // private Method to avoid instantiation externally
    private ConnectionService() {
        // start the Janitor
        janitorTaskId = TaskHandlerService.registerTaskHandler(new ConnectionJanitor());
        // create the server connection
        int port = TaskPropertiesService.getIntProp("aistac.api.sockets.server.port", 10201);
        serverConnectionId = setConnection(SERVER, "localhost", port).getId();
    }

    /**
     * Singleton pattern to get the instance of the {@code ConnectionService} class
     * @return instance of the {@code ConnectionService}
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public static ConnectionService getInstance() {
        if(INSTANCE == null) {
            synchronized (ConnectionService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new ConnectionService().init();
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
     * @return true if the connection janitor is running
     */
    public boolean isJanitorRunning() {
        return TaskHandlerService.getInstance().isTaskRunning(janitorTaskId);
    }

    /**
     * starts a connection handler and allocates the taskId to the returned {@code ConnectionBean}
     *
     * @param connectionId the identifier of the connection
     * @return a modified {@code ConnectionBean}
     */
    public synchronized ConnectionBean start(int connectionId) {
        if(!isConnection(connectionId)) {
            throw new IllegalArgumentException("The connection id [" + connectionId + "] is not registered");
        }
        ConnectionBean connection = getConnection(connectionId);
        if(connection.getTaskId() == -1 || !TaskHandlerService.getInstance().isTask(connection.getTaskId())) {
            LOGGER.debug(CONNECT, "Starting connection handler [" + connectionId + "] -> [" + connection.getHost() + ":" + connection.getPort() + "]");
            int taskId;
            if(connection.getType().equals(ConnectionTypeEnum.CLIENT) || connection.getType().equals(ConnectionTypeEnum.OBSERVER)) {
                // set the taskId with the new task
                taskId = TaskHandlerService.registerTaskHandler(new SocketClientAsynchronousHandler(connection));
            } else {
                taskId = TaskHandlerService.registerTaskHandler(new SocketServerAsynchronousHandler(connection));
            }
            connection.setTaskId(taskId, WHOAMI);
            LOGGER.trace(CONNECT, "Set connection taskId [" + connection.getTaskId() + "]");
            connection = super.setObjectInKey(ObjectEnum.DEFAULT_KEY.value(), connection);
        }
        return connection;
    }

    /**
     * stops a connection handler returning a modified {@code ConnectionBean} with the taskId reset to -1
     *
     * @param connectionId the connection Id of the task handler to be stopped
     * @return a modified {@code ConnectionBean}
     */
    public synchronized ConnectionBean stop(int connectionId) {
        LOGGER.debug(CONNECT, "Stop connection handler [" + connectionId + "]");
        if(!isConnection(connectionId)) {
            throw new IllegalArgumentException("The connection id [" + connectionId + "] is not registered");
        }
        ConnectionBean connection = getConnection(connectionId);
        LOGGER.trace(CONNECT, "              connection [" + connection.getHost() + ":" + connection.getPort() + "]");
        LOGGER.trace(CONNECT, "                  taskId [" + connection.getTaskId() + "]");
        TaskHandlerService.getInstance().stopTaskHandler(connection.getTaskId());
        connection.setTaskId(-1, WHOAMI);
        return super.setObjectInKey(ObjectEnum.DEFAULT_KEY.value(), connection);
    }

    /**
     * returns {@code ConnectionBean} with the specified identifier.
     *
     * @param connectionId the identifier of the {@code ConnectionBean}
     * @return {@code ConnectionBean} or null if not found
     */
    public ConnectionBean getConnection(int connectionId) {
        return super.getObjectInKey(ObjectEnum.SINGLE_KEY.value(), connectionId);
    }

    /**
     * returns all {@code ConnectionBean}'s.
     *
     * @return LinkedList of {@code ConnectionBean}
     */
    public List<ConnectionBean> getAllConnections() {
        return (super.getAllObjectsInKey(ObjectEnum.SINGLE_KEY.value()));
    }

    /**
     * returns all the identifiers currently being held.
     *
     * @return a set of Integer Id's
     */
    public Set<Integer> getAllConnectionIds() {
        return (super.getAllIdentifiersInKey(ObjectEnum.SINGLE_KEY.value()));
    }

    /**
     * checks to see if a given {@code ConnectionBean} identifier is present
     *
     * @param connectionId the identifier of the {@code ConnectionBean}
     * @return true if found, false if not found
     */
    public boolean isConnection(int connectionId) {
        if(getAllConnectionIds().contains(connectionId)) {
            return (true);
        }
        return (false);
    }

    /**
     * saves {@code ConnectionBean} to the store. If the object
     * identifier exists, {@code ConnectionBean} replaces the existing one and modifies the
     * {@code ConnectionBean} modified parameter.
     *
     * @param type the connection type
     * @param host the host name
     * @param port the port number
     * @return The {@code ConnectionBean} saved
     */
    public synchronized ConnectionBean setConnection(ConnectionTypeEnum type, String host, int port) {
        LOGGER.debug(CONNECT, "Set connection type [" + type.name() + "] connection [" + host + ":" + port + "]");
        if(!ConnectionTypeEnum.isValid(type)) {
            throw new IllegalArgumentException("The connection type [" + type.name() + "] is not valid");
        }
        if(type.equals(SERVER) && isConnection(serverConnectionId)) {
            LOGGER.trace(CONNECT, "Return Server id [" + serverConnectionId + "]");
            return getConnection(serverConnectionId);
        }
        // check there is not already that port and host
        for(ConnectionBean cb : getAllObjectsInKey(ObjectEnum.SINGLE_KEY.value())) {
            if(cb.getHost().equals(host) && cb.getPort() == port && !cb.getType().equals(SERVER)) {
                LOGGER.trace(CONNECT, "Return existing connection id [" + cb.getId() + "]");
                return cb;
            }
        }
        ConnectionBean connection = new ConnectionBean(generateIdentifier(ObjectEnum.SINGLE_KEY.value()), type, host, port, -1,WHOAMI);
        LOGGER.trace(CONNECT, "Return connection id [" + connection.getId() + "]");
        return (super.setObjectInKey(ObjectEnum.SINGLE_KEY.value(), connection));
    }

    /**
     * Removes {@code ConnectionBean} with the provided identifier .
     *
     * @param connectionId the id of the object to be removed
     * @return the object that was deleted
     */
    public ConnectionBean removeConnection(int connectionId) {
        LOGGER.debug(CONNECT, "Remove connection id [" + connectionId + "]");
        return super.removeObjectInKey(ObjectEnum.SINGLE_KEY.value(), connectionId);
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Public Static Methods">
    /* ***************************************************
     * P U B L I C   S T A T I C   M E T H O D S
     * ***************************************************/

    /**
     * A utility static to register a connection. The returned connection is a clone of the connection
     * with a new unique id and should replace the sent connection. This ensure the registered connection
     * has a unique reference in this instance.
     *
     * @param type the connection type
     * @param host the host name
     * @param port the port number
     * @return a clone of the {@code ConnectionBean} with a registered unique id.
     */
    public static ConnectionBean registerConnection(ConnectionTypeEnum type, String host, int port) {
        return ConnectionService.getInstance().setConnection(type, host, port);
    }

    /**
     * Pokes a connection to make sure it is running
     * @param connectionId the connection id to poke
     */
    public static void poke(int connectionId) {
        ConnectionService.getInstance().start(connectionId);
    }

    public static int SERVER_ID() {
        return ConnectionService.getInstance().getServerConnectionId();
    }

    public static int JANITOR_TASK_ID() {
        return ConnectionService.getInstance().getJanitorTaskId();
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Private Class Methods">
    /* ***************************************************
     * P R I V A T E   C L A S S   M E T H O D S
     * ***************************************************/

    private int getServerConnectionId() {
        return serverConnectionId;
    }

    private int getJanitorTaskId() {
        return janitorTaskId;
    }

    private ConnectionService init() {
        start(serverConnectionId);
        return this;
    }
 }
