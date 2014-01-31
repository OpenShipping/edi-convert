package dk.ange.stowbase.edifact.parser;

import java.io.InputStream;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.format.FormatReader;
import dk.ange.stowbase.edifact.lexer.EdifactLexer;

/**
 * Test a very simple example
 */
public class RunAsjFormat {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final EdifactReader reader = new EdifactReader();
        reader.setContentHandler(new CH());
        reader.setSegmentTable(FormatReader.readFormat(BaplieTest.class.getResourceAsStream("ASJ_D.95B")));
        final InputStream inputStream = BaplieTest.class.getResourceAsStream("ASJ.edi.txt");
        reader.parse(new EdifactLexer(inputStream));
    }

    private static class CH implements ContentHandler {
        public void startGroup(final String position) {
            System.out.println("startGroup: " + position);
        }

        public void endGroup(final String position) {
            System.out.println("endGroup: " + position);
        }

        public void segment(final String position, final Segment segment) {
            System.out.println("Segment at: " + position);
        }
    }

}
