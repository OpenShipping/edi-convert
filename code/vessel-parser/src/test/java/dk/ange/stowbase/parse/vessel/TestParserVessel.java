package dk.ange.stowbase.parse.vessel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.stowbase.client.import_.Bundle;
import org.stowbase.client.import_.BundleStowbaseObject;
import org.stowbase.client.import_.StowbaseReader;

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
        assertTrue(result.messages.getStatus().startsWith("OK"));
        assertEquals("" //
                + "OK\n" //
                + "Parsed the following sheets: [Vessel, Bays, Tier20, Tier40, Reef20, Reef40, Height20, "
                + "Height40, Pos20, Pos40, DG, tanks, VarTanks, ConstWgts, Stability, Hydrostatics, "
                + "MetaCenter, HullWgtDistr, Bonjean, StressLimits]\n" //
                + "Tanks: The volume capacity written in tanks!B4 is 490.00 while the one derived from "
                + "mass capacity and density is 487.80\n", //
                result.messages.getStatus());
        assertNotNull(result.messages.getStatus());

        final Bundle bundle = StowbaseReader.readStowbaseData(result.json);
        assertNotNull(bundle);
        Assert.assertEquals(203, bundle.size());

        final BundleStowbaseObject vesselProfile = bundle.single("vesselProfile");
        assertNotNull(vesselProfile);

        final List<BundleStowbaseObject> tanks = vesselProfile.get("tanks").getAsObjects();
        assertNotNull(tanks);
        assertEquals(4, tanks.size()); // Four tanks validated in sheet

        { // First tank should be "Tank 1 S"
            final BundleStowbaseObject tank1 = tanks.get(0);
            assertEquals("Tank 1 S", tank1.get("description").getAsString());
            final BundleStowbaseObject tcgFunction = tank1.get("tcgFunction").getAsSingleObject();
            assertNotNull(tcgFunction);
            assertEquals("function2d", tcgFunction.getGroup());
            final String samplePoints1 = tcgFunction.get("samplePoints1").getAsString();
            assertNotNull(samplePoints1);
            assertEquals(8, samplePoints1.split(";").length);
        }

        { // Second tank should be "Tank 1 P"
            final BundleStowbaseObject tank2 = tanks.get(1);
            assertEquals("Tank 1 P", tank2.get("description").getAsString());
            final BundleStowbaseObject tcgFunction = tank2.get("tcgFunction").getAsSingleObject();
            assertNotNull(tcgFunction);
            assertEquals("function2d", tcgFunction.getGroup());
            final String samplePoints1 = tcgFunction.get("samplePoints1").getAsString();
            assertNotNull(samplePoints1);
            // Test that the tanks reader chooses numbers from vartanks when data is written also in tanks
            assertEquals(8, samplePoints1.split(";").length);
        }

        BundleStowbaseObject stability = vesselProfile.get("stability").getAsSingleObject();
        assertNotNull(stability.get("classificationSociety"));
        assertEquals("DNV", stability.get("classificationSociety").getAsString());
    }

}
