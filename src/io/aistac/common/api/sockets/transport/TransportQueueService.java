/*
 * @(#)TransportQueueService.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.transport;

import io.aistac.common.canonical.queue.AbstractQueueManager;
import io.aistac.common.canonical.queue.ObjectBeanQueue;
import io.aistac.common.canonical.log.LoggerQueueService;

/**
 * The {@code TransportQueueService} Class extends the methods of the parent class.
 *
 * @author Darryl Oatridge
 * @version 1.00 07-Apr-2016
 */
public class TransportQueueService extends AbstractQueueManager<TransportBean> {

    private final static LoggerQueueService LOGGER = LoggerQueueService.getInstance();
    private final static String QUEUE = "TRANS.QUEUE";

    // Singleton Instance
    private volatile static TransportQueueService INSTANCE;

    //<editor-fold defaultstate="expanded" desc="Singletone Methods">
    // private Method to avoid instantiation externally
    private TransportQueueService() {
        // this should be empty
    }

    /**
     * Singleton pattern to get the instance of the {@code TransportQueueService} class
     * @return instance of the {@code TransportQueueService}
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public static TransportQueueService getInstance() {
        if(INSTANCE == null) {
            synchronized (TransportQueueService.class) {
                // Check again just incase before we synchronised an instance was created
                if(INSTANCE == null) {
                    INSTANCE = new TransportQueueService();
                }
            }
        }
        return INSTANCE;
    }

    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Public Queue Methods">
    /* ***************************************************
     * P U B L I C   Q U E U E   M E T H O D S
     * ***************************************************/

    /**
     * returns an {@code ObjectBeanQueue<TransportBean>} queue. If the identity does not exist a queue is
     * created therfore never returns null and not requiring a set up before using
     *
     * @param queueName the name of the queue
     * @return the requested {@code ObjectBeanQueue<TransportBean>} queue
     */
    public ObjectBeanQueue<TransportBean> getQueue(String queueName) {
        LOGGER.debug(QUEUE, "get queue [" + queueName + "]");
        return this.createQueue(queueName, MAX_CAPACITY);
    }

    /**
     * Abbreviated static version of {@code getQueue(queueName)}. Returns an {@code ObjectBeanQueue<TransportBean>} queue.
     * If the name does not exist a queue is created therfore never returns null and not requiring a set up before using
     *
     * @param queueName the name of the queue
     * @return the requested {@code ObjectBeanQueue<TransportBean>} queue
     */
    public static ObjectBeanQueue<TransportBean> queue(String queueName) {
        return TransportQueueService.getInstance().getQueue(queueName);
    }

    //</editor-fold>

 }
