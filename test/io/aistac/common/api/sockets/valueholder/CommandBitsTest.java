/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.aistac.common.api.sockets.valueholder;

import io.aistac.common.canonical.valueholder.AbstractBitsTester;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import io.aistac.common.canonical.exceptions.AiStacSchemaException;
import static io.aistac.common.canonical.valueholder.AbstractBits.getStringBinaryForBits;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Darryl Oatridge
 */
public class CommandBitsTest {

    @Test
    public void testStatusBit() throws AiStacSchemaException {
        String clsName = CommandBits.class.getName();
        List<String> exemptNames = Stream.of
                ("X_CMD_MASK"
                ,"X_DATA_MASK"
                ,"X_TD_MASK"
                ,"X_OPT_MASK"
                ).collect(Collectors.toList());
        AbstractBitsTester.testBits(clsName, exemptNames);
    }

    /*
     *
     */
    @Test
    public void testValidateBits() throws Exception {
        int valid;
        int invalid;
        valid = 0x01040802;
        assertThat(CommandBits.validateCommand(valid),is(true));
        invalid = 0x00000802;
        assertThat(CommandBits.validateCommand(invalid),is(false));
        invalid = 0x00000800;
        assertThat(CommandBits.validateCommand(invalid),is(false));
        invalid = 0x00000002;
        assertThat(CommandBits.validateCommand(invalid),is(false));
        invalid = 0x0104080a;
        assertThat(CommandBits.validateCommand(invalid),is(false));
        invalid = 0x01040a02;
        assertThat(CommandBits.validateCommand(invalid),is(false));
        invalid = 0x00000000;
        assertThat(CommandBits.validateCommand(invalid),is(false));
        // boundary testing
        int lowerBoundary =  CommandBits.CMD_REQUEST | CommandBits.REQ_SET | CommandBits.DATA_NOT_USED;
        assertThat(CommandBits.validateCommand(lowerBoundary),is(true));
        int upperBoundary =  CommandBits.CMD_CLOSE | CommandBits.REQ_NOT_USED | CommandBits.DATA_UNDEFINED;
        assertThat(CommandBits.validateCommand(upperBoundary),is(true));
        int withOptions =  CommandBits.CMD_CLOSE | CommandBits.REQ_NOT_USED | CommandBits.DATA_UNDEFINED | CommandBits.OPT_ENCRYPT;
        assertThat(CommandBits.validateCommand(withOptions),is(true));
    }

    @Test
    public void testMasks() throws Exception {
        int command = CommandBits.CMD_REQUEST | CommandBits.REQ_KEY | CommandBits.DATA_LONG;
        assertThat(command, is(CommandBits.CMD_REQUEST | CommandBits.REQ_KEY | CommandBits.DATA_LONG));
        command = CommandBits.turnOff(command, CommandBits.X_TD_MASK);
        assertThat(command, is(CommandBits.CMD_REQUEST | CommandBits.REQ_KEY ));
        command = CommandBits.turnOff(command, CommandBits.X_DATA_MASK);
        assertThat(command, is(CommandBits.CMD_REQUEST ));
        command = CommandBits.turnOff(command, CommandBits.X_CMD_MASK);
        assertThat(command, is(0));
    }

}