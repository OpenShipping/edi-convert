package convert;

import java.io.InputStream;
import java.io.Writer;

import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.export.WriterExporter;

import dk.ange.stowbase.edifact.baplie.BaplieContentHandler;
import dk.ange.stowbase.edifact.format.FormatReader;
import dk.ange.stowbase.edifact.lexer.EdifactLexer;
import dk.ange.stowbase.edifact.parser.EdifactReader;

public class Baplie {

    public static void convertBaplie(final InputStream baplieStream, final String imoNumber, final Writer jsonWriter) {
        final WriterExporter writerExporter = new WriterExporter(jsonWriter);
        final StowbaseObjectFactory objectFactory = writerExporter.stowbaseObjectFactory();
        final EdifactReader reader = new EdifactReader();
        final BaplieContentHandler contentHandler = new BaplieContentHandler(objectFactory, imoNumber);
        reader.setContentHandler(contentHandler);
        reader.setSegmentTable(FormatReader.readFormat(BaplieContentHandler.class.getResourceAsStream("BAPLIE_D.95B")));
        reader.parse(new EdifactLexer(baplieStream));
        writerExporter.flush("containers.json");
    }

}
