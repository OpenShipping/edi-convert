package dk.ange.stowbase.edifact.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.junit.Test;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.Tag;
import dk.ange.stowbase.edifact.parser.BaplieTest;

/**
 * Small integration test for EdifactParser.
 */
public class TestEdifactParserIntegration {
    /**
     * Parse the Ange Aphrodite BAPLIE and verify some very overall requirements of the BAPLIE standard.
     */
    @Test
    public void parseAphroditeBaplie() {
        final EdifactLexer parsedBaplie = new EdifactLexer(BaplieTest.class
                .getResourceAsStream("GBSOU_arrival.BAPLIE.edi.txt"));
        final List<Segment> segList = IteratorToList.read(parsedBaplie);
        // Check very overall BAPLIE structure
        assertEquals(Tag.getInstance("UNB"), segList.get(0).getTag());
        assertEquals(Tag.getInstance("UNH"), segList.get(1).getTag());
        assertEquals(Tag.getInstance("BGM"), segList.get(2).getTag());
        assertEquals(Tag.getInstance("DTM"), segList.get(3).getTag());
        assertEquals(Tag.getInstance("UNT"), segList.get(segList.size() - 2).getTag());
        assertEquals(Tag.getInstance("UNZ"), segList.get(segList.size() - 1).getTag());
    }

    /**
     * Parse the Ange Aphrodite BAPLIE and verify that the format is very close to the D95B version of the BAPLIE
     * standard.
     */
    @Test
    public void groupsInAphroditeBaplie() {
        // Read and parse
        final EdifactLexer parsedBaplie = new EdifactLexer(BaplieTest.class
                .getResourceAsStream("GBSOU_arrival.BAPLIE.edi.txt"));
        // Convert parsed data to summary string
        final List<Segment> segList = IteratorToList.read(parsedBaplie);
        final StringBuilder tags = new StringBuilder();
        for (final Segment seg : segList) {
            tags.append(seg.getTag().toString() + ";");
        }
        final String summary = tags.toString();
        // Checking towards D95B version of BAPLIE, see http://www.stylusstudio.com/edifact/frames.htm
        final String ANY_TAGS = "([A-Z]{3};)*";
        final String GROUPLESS = "UNB;UNH;BGM;DTM;(RFF;)?(NAD;)*%sUNT;UNZ;";
        assertTrue(summary.matches(String.format(GROUPLESS, ANY_TAGS)));
        final String GROUP1S = "(TDT;(LOC;)+(DTM;)+(RFF;)?(FTX;)?)+";
        assertTrue(summary.matches(String.format(GROUPLESS, GROUP1S + ANY_TAGS)));
        /*
         * OK, so it's BAPLIE-like and has at least one instance of group 1. The rest is more complicated - to avoid
         * stack overflow in the regex utility, we'll replace each group 2/3/4 instance with a simple tag, step by step.
         */
        final String GROUP3 = "EQD;(EQA;)*(NAD;)?";
        final String GROUP4 = "DGS;(FTX;)?";// In fact, the current test BAPLIE has no GROUP4 instances
        String reducedSummary = summary.replaceAll(GROUP3, "G3;").replaceAll(GROUP4, "G4;");
        reducedSummary = reducedSummary.replaceAll("G3;G3;", "G3;"); // TODO: Not sure if this always works
        reducedSummary = reducedSummary.replaceAll("G4;G4;", "G4;"); // TODO: Not sure if this always works
        // Group 2 contains group 3 and 4:
        final String GROUP2 = "LOC;(GID;)?(GDS;)?(FTX;)*(MEA;)+(DIM;)*(TMP;)?(RNG;)?(LOC;)*RFF;(G3;)?(G4;)?";
        reducedSummary = reducedSummary.replaceAll(GROUP2, "G2;");
        // Now check the overall structure.
        assertTrue(reducedSummary.matches(String.format(GROUPLESS, GROUP1S + "(G2;)*")));
    }

    /**
     * Verify that jsonify, as applied to the Aphrodite BAPLIE, does not throw Exceptions, and provides non-trivial
     * output.
     */
    @Test
    public void testJsonify() {
        // Read and parse
        final EdifactLexer parsedBaplie = new EdifactLexer(BaplieTest.class
                .getResourceAsStream("GBSOU_arrival.BAPLIE.edi.txt"));
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(600000);
        Jsonify.jsonify(IteratorToList.read(parsedBaplie), baos, JsonEncoding.UTF8);
        assertTrue(String.format("Only %d bytes", baos.size()), 100000 < baos.size());
        assertTrue(baos.toString().contains("ANGE APHRODITE"));
    }
}
