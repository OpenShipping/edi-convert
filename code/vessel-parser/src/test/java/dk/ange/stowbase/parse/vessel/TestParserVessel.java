package dk.ange.stowbase.parse.vessel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Test;
import org.junit.Ignore;

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
    @Ignore("This is not really a unit test, it is a program for converting vessels")
    @Test
    public void convert() throws Exception {
        //doConvert("vessel-9071533.xls"); // Wuxi Dragon
        //doConvert("vessel-9071533-wud.xls");
        //doConvert("vessel-8913679CAD.xls");
        //doConvert("vessel-1000001.xls");
        //doConvert("vessel-9124366.xls");
        //doConvert("vessel-9137894.xls");
        //doConvert ("vessel-9071533_test.xls");
        //doConvert("vessel-9071533_test.xls");
        //doConvert("vessel-9367803-Taicang-Dragon_test.xls");
    	doConvert("Emma Maersk.xls");
    }

    private void doConvert(final String resourceName) throws Exception {
        final InputStream inputStream = TestParserVessel.class.getResourceAsStream(resourceName);
        final Result result = ParseVessel.parse(inputStream);
        if (result.messages.getException() != null) {
            throw new RuntimeException(result.messages.getException());
        }
        String x = result.json;
      FileOutputStream f = new FileOutputStream( resourceName+ ".json");
//      FileOutputStream f = new FileOutputStream("vessel-9071533-wud.json");
      Writer w = new OutputStreamWriter(f);
      w.write(x);
      w.close();
      f.close();


        assertNotNull("Be able to display stack trace", result.messages.getDeveloperStatus());
        assertNull("Should not get a stack trace:\n" + result.messages.getDeveloperStatus(), result.messages
                .getException());
        assertNotNull(result.json);
        assertNotNull(result.messages.getStatus());
  //      System.out.println(result.messages.getStatus());
        // org.junit.Assert.assertEquals("For developing", result.messages.getStatus());
    }

}
