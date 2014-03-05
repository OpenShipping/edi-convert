package dk.ange.stowbase.edifact.baplie;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.export.WriterExporter;

import dk.ange.stowbase.edifact.format.FormatReader;
import dk.ange.stowbase.edifact.lexer.EdifactLexer;
import dk.ange.stowbase.edifact.parser.EdifactReader;

/**
 * Test that BAPLIEs are read correct
 */
public class TestBaplie {

    /**
     * Test
     *
     * @throws IOException
     */
    @Test
    public void testBaplie() throws IOException {
        final StringWriter stringWriter = new StringWriter();
        final WriterExporter writerExporter = new WriterExporter(stringWriter);
        final StowbaseObjectFactory stowbase = writerExporter.stowbaseObjectFactory();
        final String vesselImo = "1234567";

        final EdifactReader reader = new EdifactReader();
        final BaplieContentHandler contentHandler = new BaplieContentHandler(stowbase, vesselImo);
        reader.setContentHandler(contentHandler);
        reader.setSegmentTable(FormatReader.readFormat(BaplieContentHandler.class.getResourceAsStream("BAPLIE_D.95B")));
        final String fileName = "job_43002_input_Release_data.txt";
        try (final InputStream inputStream = ImportBaplie.class.getResourceAsStream(fileName)) {
            reader.parse(new EdifactLexer(inputStream));
        }

        stowbase.flush();
        writerExporter.flush("baplie.json");

        final String string = stringWriter.toString();
        final Matcher matcher = Pattern.compile("\"dangerousGoods\".*").matcher(string);
        assertTrue("Match dangerousGoods", matcher.find());
        matcher.usePattern(Pattern.compile(".*"));
        assertTrue("Match blank", matcher.find());
        assertTrue("Match type...", matcher.find());
        assertTrue("Match blank", matcher.find());
        assertTrue("Match data", matcher.find());
        // DGS+IMD+6.1+1809++I'
        final String dataLine = matcher.group();
        assertTrue("Should contain the data for DGS+IMD+6.1+1809++I': " + dataLine,
                dataLine.contains("urn:stowbase.org:dg:unNumber=1809,imdgClass=6.1"));
    }

}
