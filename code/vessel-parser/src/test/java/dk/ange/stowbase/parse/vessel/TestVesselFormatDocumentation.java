package dk.ange.stowbase.parse.vessel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link VesselFormatDocumentation}
 */
public class TestVesselFormatDocumentation {

    /**
     * Test asMarkdown()
     */
    @Test
    public void testAsMarkdown() {
        Assert.assertNotNull(VesselFormatDocumentation.asMarkdown());
    }

    /**
     * Test asHtml()
     */
    @Test
    public void testAsHtml() {
        final String html = VesselFormatDocumentation.asHtml();
        assertNotNull(html);
        assertTrue(html.contains("<h1>"));
        assertTrue(html.contains("<dl>"));
        assertTrue(html.contains("<dd>"));
        assertTrue(html.contains("<dt>"));
    }

}
