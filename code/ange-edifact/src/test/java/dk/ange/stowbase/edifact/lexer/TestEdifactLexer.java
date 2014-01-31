package dk.ange.stowbase.edifact.lexer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.parser.BaplieTest;

/**
 * Test the lexer
 */
public class TestEdifactLexer {

    /**
     * Test a bug I have fixed
     */
    @Test
    public void testEmptyComposite() {
        final EdifactLexer lexer = new EdifactLexer(BaplieTest.class
                .getResourceAsStream("GBSOU_arrival.BAPLIE.edi.txt"));
        final Segment segment = lexer.next();
        assertEquals("Segment[UNB+UNOA:2+WOC+ALL+090805:0958+2059++PSTOW 3.1.13.1+++111']", segment.toString());
        assertEquals(10, segment.size());
        assertEquals(0, segment.size(5));
        assertEquals("missing", segment.get(5, 0, "missing"));
    }

}
