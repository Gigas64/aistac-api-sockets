/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.aistac.common.api.sockets.transport;

import io.aistac.common.api.sockets.transport.TransportBean;
import io.aistac.common.canonical.data.BeanTester;
import io.aistac.common.canonical.data.ObjectBean;
import io.aistac.common.canonical.data.example.BeanBuilder;
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class TransportBeanTest {

    /**
     * Tests the underlying bean.
     */
    @Test
    public void test_TransportBean() throws Exception {
        boolean isPrintXML = false;
        boolean isGroupKey = true;
        BeanTester.testObjectBean(TransportBean.class.getName(), isPrintXML, isGroupKey);
    }

    @Test
    public void testConnectionId() throws Exception {
        TransportBean bean = (TransportBean) BeanBuilder.addBeanValues(new TransportBean());
        assertThat(bean.getKey(), is(bean.getConnectionId()));
    }

}
