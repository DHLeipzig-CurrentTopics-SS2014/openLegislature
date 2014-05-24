package org.openlegislature.process;

import org.junit.Test;
import org.openlegislature.util.OpenLegislatureConstants;

import static org.junit.Assert.assertEquals;

public class TestTxtToXmlConverter {

    TxtToXmlConverter testable = new TxtToXmlConverter(new OpenLegislatureConstants());

    @Test
    public void testCreateTagFromFromName() throws Exception {
        assertEquals("<test>",testable.createTagFrom("test"));
    }

    @Test
    public void testCreateClosingTagFromName() throws Exception {
        assertEquals("</test>",testable.createClosingTagFrom("test"));
    }
}
