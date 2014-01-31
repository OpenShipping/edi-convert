package dk.ange.stowbase.edifact.parser;

import java.io.InputStream;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.format.FormatReader;
import dk.ange.stowbase.edifact.lexer.EdifactLexer;

/**
 * 
 */
public class BaplieTest {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final EdifactReader reader = new EdifactReader();
        reader.setContentHandler(new CH());
        reader.setSegmentTable(FormatReader.readFormat(BaplieTest.class.getResourceAsStream("BAPLIE_D.95B")));
        final InputStream inputStream = BaplieTest.class.getResourceAsStream("GBSOU_arrival.BAPLIE.edi.txt");
        reader.parse(new EdifactLexer(inputStream));
    }

    private static class CH implements ContentHandler {
        public void startGroup(final String position) {
            System.out.println("startGroup: " + position);
            if (position.equals("TDT/TDT")) {
                // blah
            } else if (position.equals("DTM")) {
                // bleh
            }
        }

        public void endGroup(final String position) {
            System.out.println("endGroup: " + position);
        }

        public void segment(final String position, final Segment segment) {
            System.out.println("Segment at: " + position);
        }
    }

}
