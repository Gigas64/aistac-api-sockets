/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.transport;

import io.aistac.common.api.sockets.transport.TransportQueueService;
import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.canonical.data.example.BeanBuilder;
import io.aistac.common.canonical.queue.ObjectBeanQueue;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TransportQueueServiceTest {

    @Before
    public void setUp() {
    }

    @Test(timeout = 500)
    public void testQueueTransport() throws Exception {
        TransportBean bean = (TransportBean) BeanBuilder.addBeanValues(new TransportBean());
        ObjectBeanQueue<TransportBean> queue = TransportQueueService.queue("QueueIn");
        queue.add(bean);
        TransportBean take = queue.take();
        assertThat(bean,is(take));

        String xmlBean = bean.toXML();
        queue.add(xmlBean);
        take = queue.take();
        assertThat(bean,is(take));

    }

}