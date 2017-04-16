/*
 * @(#)CommandBits.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.valueholder;

import io.aistac.common.canonical.valueholder.AbstractBits;
import java.util.List;

/**
 * The {@code CommandBits} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 26-Mar-2016
 */
public class CommandBits extends AbstractBits{
    /*
    Command is allocated as following rules:
    1. only one bit allowed per byte for the first three bytes eg 00010101 00000100 00001000 00000010
    2. the forth Byte is optional and can can have zero to many bits settings
    */
    /* CMD_( 0x00 -> 0x07 )
    /** A request command */
    public static final int CMD_REQUEST = (int) Math.pow(0x2, 0x00);
    /** A response command */
    public static final int CMD_RESPONSE = (int) Math.pow(0x2, 0x01);
    /** A special command */
    public static final int CMD_PROPERTY = (int) Math.pow(0x2, 0x02);
    /** A retry command */
    public static final int CMD_SPECIAL = (int) Math.pow(0x2, 0x03);
    /** A failure indicator */
    public static final int CMD_FAILURE = (int) Math.pow(0x2, 0x04);
    /** A Close request command */
    public static final int CMD_CLOSE = (int) Math.pow(0x2, 0x05);
    /** Exit request command */
    public static final int CMD_EXIT = (int) Math.pow(0x2, 0x06);

    /* REQUEST( 0x08 -> 0x0f )
    /** REQ_SET set the data */
    public static final int REQ_SET = (int) Math.pow(0x2, 0x08);
    /** REQ_ID request by ID */
    public static final int REQ_ID = (int) Math.pow(0x2, 0x09);
    /** REQ_KEY request by key */
    public static final int REQ_KEY = (int) Math.pow(0x2, 0x0a);
    /** REQ_ALL all the data */
    public static final int REQ_ALL = (int) Math.pow(0x2, 0x0b);
    /** REQ_REMOVE_ID remove an ID */
    public static final int REQ_REMOVE_ID = (int) Math.pow(0x2, 0x0c);
    /** REQ_REMOVE_KEY remove a key */
    public static final int REQ_REMOVE_KEY = (int) Math.pow(0x2, 0x0d);
    /** REQ_OBSERVE request to observe for publish subscribe */
    public static final int REQ_OBSERVE = (int) Math.pow(0x2, 0x0e);
    /** REQ_NOT_USED there is no request instruction */
    public static final int REQ_NOT_USED = (int) Math.pow(0x2, 0x0f);

    /* TD_ ( 0x0f -> 0x18 )
    /** the {@code TransportBean} data has is not used and is empty */
    public static final int DATA_NOT_USED = (int) Math.pow(0x2, 0x10);
    /** the {@code TransportBean} data is an XML canonical {@code ObjectBean} subclass */
    public static final int DATA_BEANXML = (int) Math.pow(0x2, 0x11);
    /** the {@code TransportBean} data is specifically a {@code PropertiesBean} */
    public static final int DATA_PROPSXML = (int) Math.pow(0x2, 0x12);
    /** the {@code TransportBean} data is specifically a {@code ConnectionBean} */
    public static final int DATA_COMMSXML = (int) Math.pow(0x2, 0x13);
    /** the {@code TransportBean} data is plain text */
    public static final int DATA_TEXT = (int) Math.pow(0x2, 0x14);
    /** the {@code TransportBean} data is an integer value reference */
    public static final int DATA_INTEGER = (int) Math.pow(0x2, 0x15);
    /** the {@code TransportBean} data is a long value reference */
    public static final int DATA_LONG = (int) Math.pow(0x2, 0x16);
    /** the {@code TransportBean} data is undefined and known to the recipient (Special case data) */
    public static final int DATA_UNDEFINED = (int) Math.pow(0x2, 0x17);

    /** that data is encrypted */
    public static final int OPT_ENCRYPT = (int) Math.pow(0x2, 0x18);
    /** There is more to come */
    public static final int OPT_MORE_TO_COME = (int) Math.pow(0x2, 0x19);
    /** There is more to come */
    public static final int OPT_END_OF_LIST = (int) Math.pow(0x2, 0x1a);
    /** There is more to come */
    public static final int OPT_RETRY = (int) Math.pow(0x2, 0x1b);

    // useful abreviations and Masks
    public static final int X_CMD_MASK  = 0x000000ff;
    public static final int X_DATA_MASK = 0x0000ff00;
    public static final int X_TD_MASK   = 0x00ff0000;
    public static final int X_OPT_MASK  = 0xff000000;

    /**
     * returns a list of Constant names found in the bits
     *
     * @param bits the bits to breakdown
     * @return a list of the String names
     */
    public static List<String> getStringFromBits(int bits) {
        Class<CommandBits> cls = CommandBits.class;
        return AbstractBits.getStringFromBits(bits, cls);
    }

    /**
     * returns a bit value for named constance found in the given String
     *
     * @param sBits the String to convert
     * @return the Bit value of Strings found
     */
    public static int getBitsFromString(String sBits) {
        Class<CommandBits> cls = CommandBits.class;
        return AbstractBits.getBitsFromString(sBits, cls);
    }

    /**
     * Used to validate the CommandBits to ensure it is properly formed.
     * A properly formed CommandBits value has only a single bit in each of the bytes
     * of the integer. Also the fist two bytes are mandatory so can't be zero.
     *
     * @param bits the bits value to validate
     * @return true if the value is valid
     */
    public static boolean validateCommand(int bits) {
        int mask = 0x000000ff;
        for(int i = 0; i < 3; i++) {
            int test = mask & bits;
            if((test == 0) || (test & -test) != test) {
                return false;
            }
            mask <<= 8;
        }
        return true;
    }

}
