package dk.ange.stowbase.parse.vessel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.junit.Test;

import dk.ange.stowbase.parse.vessel.ParseVessel.Result;

/**
 * Test {@link ParseVessel}
 */
public class TestParserVessel {

    /**
     * Test convert()
     *
     * @throws Exception
     */
    @Test
    public void convertLegoWithStability() throws Exception {
        final Result result;
        try (final InputStream inputStream = TestParserVessel.class.getResourceAsStream("lego-maersk-w-stability.xls")) {
            result = ParseVessel.parse(inputStream);
        }

        assertNotNull("Be able to display stack trace", result.messages.getDeveloperStatus());
        assertNull("Should not get a stack trace:\n" + result.messages.getDeveloperStatus(),
                result.messages.getException());
        assertNotNull(result.json);
        assertNotNull(result.messages.getStatus());
    }

}
