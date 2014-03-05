package dk.ange.stowbase.edifact.lexer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

import dk.ange.stowbase.edifact.Segment;

/**
 * Outputs the parsed EDIFACT message as a JSON document.
 */
public abstract class Jsonify {

    /**
     * Outputs the parsed EDIFACT message as a JSON document.
     *
     * The JSON document will be a list segments. Each segment is a JSON object with two entries: - "tag", mapped to a
     * three-letter string, e.g. "LOC" - "elements", mapped to a list of lists. Each element of "elements" is either a
     * list of strings (which may contain only one item) or the list [null].
     *
     * @param segments
     *            Segments to JSONify
     * @param destination
     *            The JSON document is output on this stream. This method DOES NOT close the stream after use.
     * @param encoding
     *            The JSON document is encoded using this encoding.
     */
    public static void jsonify(final List<Segment> segments, final OutputStream destination, final JsonEncoding encoding) {
        try (final JsonGenerator jgen = new JsonFactory().createJsonGenerator(destination, encoding)) {
            jgen.useDefaultPrettyPrinter();
            jgen.writeStartArray();// BEGIN list of segments
            for (final Segment seg : segments) {
                jgen.writeStartObject(); // BEGIN segment object
                jgen.writeStringField("tag", seg.getTag().toString());
                jgen.writeFieldName("elements");
                jgen.writeStartArray(); // BEGIN list of elements
                for (int i = 0; i < seg.size(); ++i) {
                    jgen.writeStartArray(); // BEGIN list of "data elements"
                    for (int j = 0; j < seg.size(i); ++j) {
                        jgen.writeString(seg.get(i, j, null));
                    }
                    jgen.writeEndArray(); // END list of "data elements"
                }
                jgen.writeEndArray(); // END list of elements
                jgen.writeEndObject(); // END segment object
            }
            jgen.writeEndArray(); // END list of segments
        } catch (final JsonGenerationException e) {
            throw new RuntimeException("There was en error in the JSON-generating code", e);
        } catch (final IOException e) {
            throw new RuntimeException("IO error while writing to the given OutputStream", e);
        }
    }

}
