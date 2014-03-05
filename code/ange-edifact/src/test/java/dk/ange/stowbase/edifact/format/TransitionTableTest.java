package dk.ange.stowbase.edifact.format;

import java.io.InputStream;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.Tag;
import dk.ange.stowbase.edifact.baplie.BaplieTest;
import dk.ange.stowbase.edifact.lexer.EdifactLexer;

/**
 * Read a Baplie file.
 * <p>
 * Write parse Events in a SAX-calls like format. (not the final product, a step on the way)
 */
public final class TransitionTableTest {

    private static final TransitionTable TRANSITON_TABLE = new TransitionTable(FormatReader.readFormat(BaplieTest.class
            .getResourceAsStream("BAPLIE_D.95B")));

    /**
     * @param inputStream
     */
    public void parse(final InputStream inputStream) {
        final EdifactLexer parser;
        parser = new EdifactLexer(inputStream);
        SegmentFormat state = null;
        while (parser.hasNext()) {
            final Segment segment = parser.next();
            if (segment.getTag() == Tag.getInstance("UNB") || segment.getTag() == Tag.getInstance("UNZ")) {
                continue; // Skip header and footer
            }

            final SegmentFormat newState = TRANSITON_TABLE.getTransition(state, segment.getTag());
            if (newState == null) {
                throw new RuntimeException("Bad format, did not expect " + segment.getTag() + " when in state " + state);
            }
            state = newState;

            if (segment.getTag() == Tag.getInstance("LOC")) {
                System.out.println();
            }
            System.out.print(segment.getTag());
            System.out.print(" ");
        }
        System.out.println();
    }

    /**
     * Test
     *
     * @param args
     */
    public static void main(final String[] args) {
        TRANSITON_TABLE.write(System.out);
        System.out.println("-----");
        new TransitionTableTest().parse(BaplieTest.class.getResourceAsStream("GBSOU_arrival.BAPLIE.edi.txt"));
    }

}
