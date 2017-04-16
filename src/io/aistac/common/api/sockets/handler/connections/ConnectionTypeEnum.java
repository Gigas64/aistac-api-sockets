/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * @(#)ConnectionTypeEnum.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.handler.connections;

/**
 * The {@code ConnectionTypeEnum} Enumeration
 *
 * @author Darryl Oatridge
 * @version 1.00 10-Apr-2016
 */
public enum ConnectionTypeEnum {
    UNDEFINED, NO_VALUE, CLIENT, OBSERVER, SERVER;

    protected static boolean isValid(ConnectionTypeEnum type) {
        return !(type.equals(UNDEFINED) || type.equals(NO_VALUE));
    }
}
