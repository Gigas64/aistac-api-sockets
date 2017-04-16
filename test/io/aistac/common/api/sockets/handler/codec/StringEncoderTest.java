/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.aistac.common.api.sockets.handler.codec;

import io.aistac.common.api.sockets.handler.codec.StringEncoder;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Darryl Oatridge
 */
public class StringEncoderTest {

    @Before
    public void setUp() {
    }

    /**
     * Test of encode method, of class StringEncoder.
     */
    @Test
    public void testEncode() {
        StringBuilder sb = new StringBuilder();
        sb.append("var dds = Y.DD.DDM._drags;").append("\n").append("var list = {};").append("\n").append("var lis = Y.all('#' + k + ' li.item');").append("\n").append("var xml = \"<modules page='${page}'>\";").append("\n").append("Y.each(dds, function(v, k) {").append("\n").append("  var par = v.get('node').get('parentNode');").append("\n").append("  if(par.test('ul.list')) {").append("\n").append("    if(!list[par.get('id')]) {").append("\n").append("      list[par.get('id')] = [];").append("\n").append("      var minned = (mod.hasClass('minned')) ? 0:1;").append("\n").append("    }").append("\n").append("  }").append("\n").append("  !\"£$%^&*()+}{[]\\~#@':;?/><|").append("\n").append("  xml += \"<module id='\" + moduleId + \"' col='\" + k.substring(4) + \"'/>\";").append("\n").append("});").append("\n");

        assertEquals(sb.toString(),StringEncoder.decode(StringEncoder.encode(sb.toString())));
    }
}
