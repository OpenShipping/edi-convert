package dk.ange.tcc.convert;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import dk.ange.tcc.convert.StowageParser.Result;

/**
 * Test {@link StowageParser}
 */
public class TestStowageParser {

    /**
     * Test convert()
     * 
     * @throws Exception
     */
    @Test
    public void testParse() throws Exception {
        doParse("stowage-template.xls");
        doParse("stowage-long.xls");
    }

    private void doParse(final String resourceName) throws IOException {
        final InputStream inputStream = TestStowageParser.class.getResourceAsStream(resourceName);
        final Result result = StowageParser.parse(inputStream);
        inputStream.close();
        assertNotNull("Be able to display stack trace", result.messages.getDeveloperStatus());
        assertNull("Should not get a stack trace:\n" + result.messages.getDeveloperStatus(), result.messages
                .getException());
        assertNotNull(result.jsonData);
        assertNotNull(result.messages.getStatus());
        // org.junit.Assert.assertEquals("For developing", result.messages.getStatus());
    }

}
