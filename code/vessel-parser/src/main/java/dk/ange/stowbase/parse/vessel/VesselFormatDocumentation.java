package dk.ange.stowbase.parse.vessel;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import com.google.common.io.Resources;

/**
 * Documentation of the vessel XLS format.
 */
public class VesselFormatDocumentation {

    /**
     * @return The documentation as Markdown
     */
    public static String asMarkdown() {
        final URL resource = VesselFormatDocumentation.class.getResource("vessel-format-documentation.md");
        try {
            return Resources.toString(resource, Charset.forName("UTF8"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The documentation as HTML
     */
    public static String asHtml() {
        final String markdown = asMarkdown();
        final PegDownProcessor pegDownProcessor = new PegDownProcessor(Extensions.DEFINITIONS);
        return pegDownProcessor.markdownToHtml(markdown);
    }

}
