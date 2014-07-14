package dk.ange.stowbase.parse.vessel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.stowbase.client.import_.Bundle;
import org.stowbase.client.import_.BundleStowbaseObject;
import org.stowbase.client.import_.StowbaseReader;

import dk.ange.stowbase.parse.vessel.ParseVessel.Result;

/**
 * Test correct mapping of transverse coordinates based on TransversePositiveDirection.
 * <p>
 * See ticket #157.
 */
public class TestTransversePositiveDirection {

    /**
     * Test the legacy mapping. Mostly port, tanks to starboard.
     *
     * @throws IOException
     */
    @Test
    public void testStarboard() throws IOException {
        // If it causes problem that we use the shared "lego-maersk-w-stability.xls" file, copy it to
        // "lego-maersk-transverse-starboard.xls" and use that for the test
        validateVessel("lego-maersk-w-stability.xls");
    }

    /**
     * Test the legacy mapping. Mostly port, tanks to starboard.
     *
     * @throws IOException
     */
    @Test
    public void testPort() throws IOException {
        validateVessel("lego-maersk-transverse-port.xls");
    }

    /**
     * Test the legacy mapping. Mostly port, tanks to starboard.
     *
     * @throws IOException
     */
    @Test
    public void testLegacy() throws IOException {
        validateVessel("lego-maersk-transverse-legacy.xls");
    }

    private void validateVessel(final String fileName) throws IOException {
        final Result result;
        try (final InputStream inputStream = TestTransversePositiveDirection.class.getResourceAsStream(fileName)) {
            result = ParseVessel.parse(inputStream);
        }
        assertTrue(result.messages.getStatus().startsWith("OK"));
        final Bundle bundle = StowbaseReader.readStowbaseData(result.json);
        assertNotNull(bundle);
        final BundleStowbaseObject vesselProfile = bundle.single("vesselProfile");

        validateStacks(vesselProfile.get("vesselStacks").getAsObjects());
        validateTanks(vesselProfile.get("tanks").getAsObjects());
        validateConstWgts(vesselProfile.get("constantWeightBlocks").getAsObjects());
    }

    private void validateStacks(final List<BundleStowbaseObject> vesselStacks) {
        // index 5, 0202B
        final BundleStowbaseObject stack = vesselStacks.get(5);
        assertEquals("2", stack.get("rowName").getAsString());
        assertEquals("2", stack.get("overlappingFeuBay").getAsString());
        final BundleStowbaseObject stackSupport = stack.get("vesselStackSupports").getAsObjects().get(2);
        assertEquals("2", stackSupport.get("bayName").getAsString());
        assertEquals(2.5, stackSupport.get("centerToThePortInM").getAsDouble(), 0.0001);
    }

    private void validateTanks(final List<BundleStowbaseObject> tanks) {
        assertNotNull(tanks);
        { // Second tank should be "Tank 1 P"
            final BundleStowbaseObject tank2 = tanks.get(1);
            assertEquals("Tank 1 P", tank2.get("description").getAsString());
            final BundleStowbaseObject tcgFunction = tank2.get("tcgFunction").getAsSingleObject();
            assertNotNull(tcgFunction);
            assertEquals("function2d", tcgFunction.getGroup());
            final String sampleData = tcgFunction.get("sampleData").getAsString();
            assertNotNull(sampleData);
            final String[] split = sampleData.split(";");
            assertEquals(8, split.length);
            assertEquals("-2.698", split[0]);
        }
        { // Fourth tank should be "Tank 2 P"
            final BundleStowbaseObject tank4 = tanks.get(3);
            assertEquals("Tank 2 P", tank4.get("description").getAsString());
            final BundleStowbaseObject tcgFunction = tank4.get("tcgFunction").getAsSingleObject();
            assertNotNull(tcgFunction);
            assertEquals("function2d", tcgFunction.getGroup());
            final String sampleData = tcgFunction.get("sampleData").getAsString();
            assertNotNull(sampleData);
            final String[] split = sampleData.split(";");
            assertEquals(2, split.length);
            assertEquals("-5.0", split[0]);
        }
    }

    private void validateConstWgts(final List<BundleStowbaseObject> constantWeightBlocks) {
        assertNotNull(constantWeightBlocks);
        assertEquals(3, constantWeightBlocks.size());
        final BundleStowbaseObject block = constantWeightBlocks.get(2);
        assertEquals("Extra, Port", block.get("description").getAsString());
        assertEquals(6.0, block.get("tcgInM").getAsDouble(), 0.0001);
    }

}
