package dk.ange.tcc.convert;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import dk.ange.tcc.convert.CoprarParser.Result;

/**
 * Test {@link StowageParser}
 */
public class TestCoprarParser {

    /**
     * Test convert()
     *
     * @throws Exception
     */
    @Test
    public void testParseTemplate() throws Exception {
        doParse("coprar-template.xls");
    }

    /**
     * Test convert()
     *
     * @throws Exception
     */
    @Test
    public void testParseLongLoadList() throws Exception {
        doParse("coprar-loadlist-long.xls");
    }

    private void doParse(final String resourceName) throws IOException {
        final String vesselImo = "9301471";
        final String vesselName = "CSAV Valencia";
        final Result result;
        try (final InputStream inputStream = TestCoprarParser.class.getResourceAsStream(resourceName)) {
            result = CoprarParser.parse(inputStream, vesselImo, vesselName);
        }
        assertNotNull("Be able to display stack trace", result.messages.getDeveloperStatus());
        if (result.messages.getException() != null) {
            result.messages.getException().printStackTrace();
        }
        assertNull("Should not get a stack trace:\n" + result.messages.getDeveloperStatus(),
                result.messages.getException());
        assertNotNull(result.coprarData);
        // System.err.println(new String(result.coprarData));
        assertNotNull(result.messages.getStatus());
        // org.junit.Assert.assertEquals("For developing", result.messages.getStatus());
        // final OutputStream f = new FileOutputStream("coprar.edi");
        // f.write(result.coprarData);
        // f.close();
    }

}
