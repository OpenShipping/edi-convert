package dk.ange.tcc.convert;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import dk.ange.tcc.convert.ExcelBaplieParser;
import dk.ange.tcc.convert.ExcelBaplieParser.Result;

/**
 * Test {@link StowageParser}
 */
public class TestExcelBaplieParser {

    /**
     * Test testParseTemplate()
     * 
     * @throws Exception
     */
    @Test
    public void testParseTemplate() throws Exception {
        doParse("baplie-template.xls");
    }

    private void doParse(final String resourceName) throws IOException {
        final InputStream inputStream = TestExcelBaplieParser.class.getResourceAsStream(resourceName);
        final String vesselImo = "9301471";
        final String vesselName = "CSAV Valencia";
        final Result result = ExcelBaplieParser.parse(inputStream, vesselImo, vesselName);
        inputStream.close();
        assertNotNull("Be able to display stack trace", result.messages.getDeveloperStatus());
        if (result.messages.getException() != null) {
            result.messages.getException().printStackTrace();
        }
        assertNull("Should not get a stack trace:\n" + result.messages.getDeveloperStatus(),
                result.messages.getException());
        assertNotNull(result.baplieData);
        assertNotNull(result.messages.getStatus());
    }

}
