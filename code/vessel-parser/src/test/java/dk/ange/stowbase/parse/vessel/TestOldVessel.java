package dk.ange.stowbase.parse.vessel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;
import org.stowbase.client.import_.Bundle;
import org.stowbase.client.import_.BundleStowbaseObject;
import org.stowbase.client.import_.StowbaseReader;

import dk.ange.stowbase.parse.vessel.ParseVessel.Result;

/**
 * Test {@link ParseVessel}
 */
public class TestOldVessel {

    private static final double DELTA = 1e-9;

    /**
     * Test convert()
     *
     * @throws Exception
     */
    @Test
    public void convertLegoWithStability() throws Exception {
        final Result result;
        try (final InputStream inputStream = new GZIPInputStream(
                TestOldVessel.class.getResourceAsStream("lego-maersk-old.xls.gz"))) {
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
                + "DG: Using old format in DG sheet, it should be changed to the new Cargo Spaces based format\n" //
                + "tanks: The volume capacity written in tanks!B4 is 490.00 while the one derived from "
                + "mass capacity and density is 487.80\n", //
                result.messages.getStatus());
        assertNotNull(result.messages.getStatus());

        final Bundle bundle = StowbaseReader.readStowbaseData(result.json);
        assertNotNull(bundle);
        Assert.assertEquals(203, bundle.size());

        final BundleStowbaseObject vesselProfile = bundle.single("vesselProfile");
        assertNotNull(vesselProfile);

        validateStacks(vesselProfile.get("vesselStacks").getAsObjects());

        validateTanks(vesselProfile.get("tanks").getAsObjects());

        final BundleStowbaseObject stability = vesselProfile.get("stability").getAsSingleObject();
        assertNotNull(stability.get("classificationSociety"));
        assertEquals("DNV", stability.get("classificationSociety").getAsString());

        validateHolds(vesselProfile.get("holds").getAsObjects());

        validateBonjean(vesselProfile.get("bonjeanCurve").getAsSingleObject());

        validateHullWeight(vesselProfile.get("hullweightBlocks").getAsObjects());

        validateHydrostatics(vesselProfile.get("draftFunction").getAsSingleObject());

        validateMetacenter(vesselProfile.get("metacentreCurve").getAsSingleObject());
    }

    private void validateMetacenter(BundleStowbaseObject metacenter) {
        assertNotNull(metacenter);
        assertEquals("nearest", metacenter.get("extrapolation").getAsString());
        assertEquals("interpolation2d", metacenter.get("functionType").getAsString());
        assertEquals("trimInM", metacenter.get("input1").getAsString());
        assertEquals("draftInM", metacenter.get("input2").getAsString());
        assertEquals("linear", metacenter.get("interpolation").getAsString());
        assertEquals("metaCentreInMAboveKeel", metacenter.get("output").getAsString());
        /* TODO: correct sorting delayed to next commit #180
        assertEquals("9.8;9.3;10.1;9.6;10.2;9.7;10.5;10.0;9.9;9.4;10.4;9.9;10.3;9.8;10.0;9.5", metacenter.get("sampleData").getAsString());
        assertEquals("0.0;-1.0", metacenter.get("samplePoints1").getAsString());
        assertEquals("3.0;6.0;7.0;10.0;4.0;9.0;8.0;5.0", metacenter.get("samplePoints2").getAsString());
        */
    }

    private void validateHydrostatics(BundleStowbaseObject draftFunction) {
        assertNotNull(draftFunction);
        assertEquals("nearest", draftFunction.get("extrapolation").getAsString());
        assertEquals("interpolation2d", draftFunction.get("functionType").getAsString());
        assertEquals("displacement", draftFunction.get("input1").getAsString());
        assertEquals("lcg", draftFunction.get("input2").getAsString());
        assertEquals("linear", draftFunction.get("interpolation").getAsString());
        assertEquals("draft", draftFunction.get("output").getAsString());
        /* TODO: correct sorting delayed to next commit #180
        assertEquals("10.0;9.0;8.0;7.0;6.0;5.0;4.0;3.0;10.0;9.0;8.0;7.0;6.0;5.0;4.0;3.0", draftFunction.get("sampleData").getAsString());
        assertEquals("1.62E7;1.458E7;1.296E7;1.134E7;9720000.0;8100000.0;6480000.0;4860000.0", draftFunction.get("samplePoints1").getAsString());
        assertEquals("-1000.0;1000.0", draftFunction.get("samplePoints2").getAsString());
        */
    }

    private void validateHullWeight(List<BundleStowbaseObject> hullWeightBlocks) {
        assertEquals(3, hullWeightBlocks.size());
        assertEquals(0.0, hullWeightBlocks.get(0).get("aftDensityInKgPrM").getAsDouble(), DELTA);
        assertEquals(0.0, hullWeightBlocks.get(0).get("aftLcgInM").getAsDouble(), DELTA);
        assertEquals(100000.0, hullWeightBlocks.get(0).get("foreDensityInKgPrM").getAsDouble(), DELTA);
        assertEquals(20.0, hullWeightBlocks.get(0).get("foreLcgInM").getAsDouble(), DELTA);
        assertEquals(0.0, hullWeightBlocks.get(0).get("tcgInM" ).getAsDouble(), DELTA);
    }

    private void validateBonjean(BundleStowbaseObject bonjean) {
        assertNotNull(bonjean);
        /* TODO: correct sorting delayed to next commit #180
        assertEquals("0.0;110.0;90.0;20.0", bonjean.get("samplePoints1").getAsString());
        assertEquals("3.0;6.0;7.0;10.0;4.0;9.0;8.0;5.0", bonjean.get("samplePoints2").getAsString());
        */
    }

    private void validateStacks(final List<BundleStowbaseObject> vesselStacks) {
        {
            final BundleStowbaseObject stack0200A = vesselStacks.get(0);
            assertEquals("0", stack0200A.get("rowName").getAsString());
            assertEquals("2", stack0200A.get("overlappingFeuBay").getAsString());

            final BundleStowbaseObject stackSupport0200A = stack0200A.get("vesselStackSupports").getAsObjects().get(2);
            assertEquals("2", stackSupport0200A.get("bayName").getAsString());
            assertEquals(Arrays.asList("80", "82"), stackSupport0200A.get("dcTiersFromBelow").getAsStringList());
            assertNull(stackSupport0200A.get("imoForbidden"));

            final BundleStowbaseObject stackSupport0100A = stack0200A.get("vesselStackSupports").getAsObjects().get(0);
            assertEquals("1", stackSupport0100A.get("bayName").getAsString());
            assertEquals(Arrays.asList("80", "82"), stackSupport0100A.get("dcTiersFromBelow").getAsStringList());
            assertNull(stackSupport0100A.get("imoForbidden"));
        }
        {
            final BundleStowbaseObject stack0203A = vesselStacks.get(6);
            assertEquals("2", stack0203A.get("overlappingFeuBay").getAsString());
            assertEquals("3", stack0203A.get("rowName").getAsString());

            final BundleStowbaseObject stackSupport0203A = stack0203A.get("vesselStackSupports").getAsObjects().get(2);
            assertEquals("2", stackSupport0203A.get("bayName").getAsString());
            assertEquals(Arrays.asList("80", "82"), stackSupport0203A.get("dcTiersFromBelow").getAsStringList());
            assertEquals(true, stackSupport0203A.get("imoForbidden").getAsBoolean());

            final BundleStowbaseObject stackSupport0103A = stack0203A.get("vesselStackSupports").getAsObjects().get(0);
            assertEquals("1", stackSupport0103A.get("bayName").getAsString());
            assertEquals(Arrays.asList("80", "82"), stackSupport0103A.get("dcTiersFromBelow").getAsStringList());
            assertEquals(true, stackSupport0103A.get("imoForbidden").getAsBoolean());
        }
    }

    private void validateTanks(final List<BundleStowbaseObject> tanks) {
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
    }

    private void validateHolds(final List<BundleStowbaseObject> holds) {
        assertEquals(3, holds.size());
        final BundleStowbaseObject hold2 = holds.get(1);
        assertEquals(Arrays.asList("14", "10"), hold2.get("feubays").getAsStringList());
        final List<String> acceptsImo = hold2.get("acceptsImo").getAsStringList();
        assertTrue(acceptsImo.contains("2.2"));
        assertTrue(!acceptsImo.contains("2.3"));
    }

}
