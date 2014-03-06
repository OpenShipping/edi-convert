package dk.ange.stowbase.edifact.baplie;

import java.io.IOException;
import java.io.InputStream;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.format.FormatReader;
import dk.ange.stowbase.edifact.lexer.EdifactLexer;
import dk.ange.stowbase.edifact.parser.ContentHandler;
import dk.ange.stowbase.edifact.parser.EdifactReader;

/**
 *
 */
public class RunBaplie {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final EdifactReader reader = new EdifactReader();
        reader.setContentHandler(new CH());
        reader.setSegmentTable(FormatReader.readFormat(RunBaplie.class.getResourceAsStream("BAPLIE_D.95B")));
        try (final InputStream inputStream = RunBaplie.class.getResourceAsStream("GBSOU_arrival.BAPLIE.edi.txt")) {
            reader.parse(new EdifactLexer(inputStream));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CH implements ContentHandler {
        @Override
        public void startGroup(final String position) {
            System.out.println("startGroup: " + position);
            if (position.equals("TDT/TDT")) {
                // blah
            } else if (position.equals("DTM")) {
                // bleh
            }
        }

        @Override
        public void endGroup(final String position) {
            System.out.println("endGroup: " + position);
        }

        @Override
        public void segment(final String position, final Segment segment) {
            System.out.println("Segment at: " + position);
        }
    }

}
