/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.handler.connections;

// common imports
import io.aistac.common.api.sockets.handler.connections.ConnectionBean;
import io.aistac.common.canonical.data.BeanTester;
import io.aistac.common.canonical.data.example.BeanBuilder;
import io.aistac.common.canonical.data.example.ExampleBean;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import static java.util.Arrays.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
// Test Imports
import mockit.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class ConnectionBeanTest {

    /**
     * Tests the underlying bean.
     */
    @Test
    public void test_ConnectionBean() throws Exception {
        boolean isPrintXML = false;
        boolean isGroupKey = false;
//            List<String> exemptMethods = Stream.of("getId", "getKey").collect(Collectors.toList());
        BeanTester.testObjectBean(ConnectionBean.class.getName(), isPrintXML, isGroupKey);
    }

}