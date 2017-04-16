 /*
 * @(#)DataQueueInterface.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.transport;

import io.aistac.common.canonical.exceptions.ObjectBeanException;
import io.aistac.common.canonical.properties.SimpleStringCipher;
import io.aistac.common.canonical.queue.QueueDeliveryInterface;
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.api.sockets.handler.connections.ConnectionService;
import io.aistac.common.api.sockets.valueholder.CommandBits;

/**
 * The {@code DataQueueInterface} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 25-Mar-2016
 */
public interface TransportQueueInterface extends QueueDeliveryInterface<TransportBean> {

    /**
     * @return The {@code ConnectionBean} associated with this Interface
     */
    public abstract ConnectionBean getConnection();

    /**
     * send a {@code TransportBean} to the {@code ConnectionBean} queueOut. This also checks the queue monitor
     * and starts it is it isn't already. The {@code TransportBean} is created with the given data and the command
     * instructions. The data is the data to be carried in the {@code TransportBean}. Within the command the inclusion of
     * {@code CommandBits.OPT_ENCRYPT} will encrypt the data.
     *
     * @param command a valid set of {@code CommandBits}
     * @param data the data to be transmitted as defined by the command
     * @return true if the client was started and the data sent
     */
    default TransportBean sendData(int command, String data) {
        if(getConnection() == null) {
            throw new NullPointerException("The connection parameter can't be null");
        }
        // check the connection has been registered
        if(!ConnectionService.getInstance().isConnection(getConnection().getId())) {
            throw new IllegalArgumentException("The connection parameter has not been registered with the ConnectionService.");
        }
        // check the monitor is all running OK
        startQueueMonitor(getConnection().getQueueIn(), TransportQueueService.queue(getConnection().getQueueIn()));
        // send the transport bean
        String _data = data == null ? "" : data;
        if(CommandBits.contain(command, CommandBits.OPT_ENCRYPT)) {
            try {
                _data = SimpleStringCipher.encrypt(_data);
            } catch(ObjectBeanException ex) {
                throw new SecurityException(ex.getMessage());
            }
        }
        // register the TransportBean with the service
        TransportBean transport;
        try {
            transport = TransportService.registerTransport(getConnection().getId(), command, _data);
        } catch(IllegalArgumentException ex) {
            return null;
        }
        // add to the queue
        TransportQueueService.queue(getConnection().getQueueOut()).add(transport);
        // poke the connectiuon to make sure it is runnning
        ConnectionService.poke(getConnection().getId());
        return transport;
    }

}
