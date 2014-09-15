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
public class TestUascVessel {

    private static final double DELTA = 1e-9;

    /**
     * Test convert()
     *
     * @throws Exception
     */
    @Test
    public void convertUascDohaWithEmptyRows() throws Exception {
        final Result result;
        try (final InputStream inputStream = new GZIPInputStream(
                TestUascVessel.class.getResourceAsStream("UASC_DOHA_9397585.xlsx.gz"))) {
            result = ParseVessel.parse(inputStream);
        }

        assertNotNull("Be able to display stack trace", result.messages.getDeveloperStatus());
        assertNull("Should not get a stack trace:\n" + result.messages.getDeveloperStatus(),
                result.messages.getException());
        assertNotNull(result.json);
        assertTrue(result.messages.getStatus().startsWith("OK"));
        assertEquals("" //
                + "OK\n"
                + "Parsed the following sheets: [Vessel, Bays, Tier20, Tier40, Slots45, Reef20, Reef40, Wgt20, Wgt40, Height20, Height40, Pos20, Pos40, DG, Lids, Tanks, VarTanks, ConstWgts, Stability, Hydrostatics, MetaCenter, HullWgtDistr, Bonjean]\n"
                + "Unused sheets: [Help, xDg20, xDg40, xStressLimits, xBonjean-graph]\n", //
                result.messages.getStatus());
        assertNotNull(result.messages.getStatus());

        
        final Bundle bundle = StowbaseReader.readStowbaseData(result.json);
        assertNotNull(bundle);
        Assert.assertEquals(1679, bundle.size());

        
        final BundleStowbaseObject vesselProfile = bundle.single("vesselProfile");
        assertNotNull(vesselProfile);

        validateStacks(vesselProfile.get("vesselStacks").getAsObjects());
        
        validateTanks(vesselProfile.get("tanks").getAsObjects());
        
        final BundleStowbaseObject stability = vesselProfile.get("stability").getAsSingleObject();
        assertNotNull(stability);

        validateHolds(vesselProfile.get("holds").getAsObjects());

        validateBonjean(vesselProfile.get("bonjeanCurve").getAsSingleObject());

        validateHullWeight(vesselProfile.get("hullweightBlocks").getAsObjects());

        validateHydrostaticsDraft(vesselProfile.get("draftFunction").getAsSingleObject());

        validateHydrostaticsTrim(vesselProfile.get("trimFunction").getAsSingleObject());

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

        assertEquals(728, metacenter.get("sampleData").getAsString().length());
        assertEquals(3, metacenter.get("samplePoints1").getAsString().length());
        assertEquals(459, metacenter.get("samplePoints2").getAsString().length());
    }

    private void validateHydrostaticsDraft(BundleStowbaseObject draftFunction) {
        assertNotNull(draftFunction);
        assertEquals("nearest", draftFunction.get("extrapolation").getAsString());
        assertEquals("interpolation2d", draftFunction.get("functionType").getAsString());
        assertEquals("displacement", draftFunction.get("input1").getAsString());
        assertEquals("lcg", draftFunction.get("input2").getAsString());
        assertEquals("linear", draftFunction.get("interpolation").getAsString());
        assertEquals("draft", draftFunction.get("output").getAsString());

        assertEquals(919, draftFunction.get("sampleData").getAsString().length());
        assertEquals(943, draftFunction.get("samplePoints1").getAsString().length());
        assertEquals(14, draftFunction.get("samplePoints2").getAsString().length());
        assertEquals("-1000.0;1000.0", draftFunction.get("samplePoints2").getAsString());
    }

    private void validateHydrostaticsTrim(BundleStowbaseObject trimFunction) {
        assertNotNull(trimFunction);
        assertEquals("nearest", trimFunction.get("extrapolation").getAsString());
        assertEquals("interpolation2d", trimFunction.get("functionType").getAsString());
        assertEquals("displacement", trimFunction.get("input1").getAsString());
        assertEquals("lcg", trimFunction.get("input2").getAsString());
        assertEquals("linear", trimFunction.get("interpolation").getAsString());
        assertEquals("trim", trimFunction.get("output").getAsString());

        assertEquals(8094, trimFunction.get("sampleData").getAsString().length());
        assertEquals(943, trimFunction.get("samplePoints1").getAsString().length());
        assertEquals(42, trimFunction.get("samplePoints2").getAsString().length());
    }

    private void validateHullWeight(List<BundleStowbaseObject> hullWeightBlocks) {
        assertEquals(38, hullWeightBlocks.size());
        assertEquals(1500.0, hullWeightBlocks.get(0).get("aftDensityInKgPrM").getAsDouble(), DELTA);
        assertEquals(115.7, hullWeightBlocks.get(0).get("aftLcgInM").getAsDouble(), DELTA);
        assertEquals(1469.0, hullWeightBlocks.get(0).get("foreDensityInKgPrM").getAsDouble(), DELTA);
        assertEquals(118.9, hullWeightBlocks.get(0).get("foreLcgInM").getAsDouble(), DELTA);
        assertEquals(0.0, hullWeightBlocks.get(0).get("tcgInM" ).getAsDouble(), DELTA);
    }

    private void validateBonjean(BundleStowbaseObject bonjean) {
        assertNotNull(bonjean);
        assertEquals("nearest", bonjean.get("extrapolation").getAsString());
        assertEquals("interpolation2d", bonjean.get("functionType").getAsString());
        assertEquals("lcg", bonjean.get("input1").getAsString());
        assertEquals("draft", bonjean.get("input2").getAsString());
        assertEquals("linear", bonjean.get("interpolation").getAsString());
        assertEquals("bonjeanArea", bonjean.get("output").getAsString());

        assertEquals(4751, bonjean.get("sampleData").getAsString().length());
        assertEquals(133, bonjean.get("samplePoints1").getAsString().length());
        assertEquals(152, bonjean.get("samplePoints2").getAsString().length());
    }

    private void validateStacks(final List<BundleStowbaseObject> vesselStacks) {
        {
            final BundleStowbaseObject stack0200A = vesselStacks.get(0);
            assertEquals("0", stack0200A.get("rowName").getAsString());
            assertEquals("2", stack0200A.get("overlappingFeuBay").getAsString());

            final BundleStowbaseObject stackSupport0200A = stack0200A.get("vesselStackSupports").getAsObjects().get(2);
            assertEquals("2", stackSupport0200A.get("bayName").getAsString());
            assertEquals(Arrays.asList("84", "86", "88", "90", "92"), stackSupport0200A.get("dcTiersFromBelow").getAsStringList());
            assertNull(stackSupport0200A.get("imoForbidden"));

            final BundleStowbaseObject stackSupport0100A = stack0200A.get("vesselStackSupports").getAsObjects().get(0);
            assertEquals("1", stackSupport0100A.get("bayName").getAsString());
            assertEquals(Arrays.asList("84", "86", "88", "90"), stackSupport0100A.get("dcTiersFromBelow").getAsStringList());
            assertNull(stackSupport0100A.get("imoForbidden"));
        }
        {
            final BundleStowbaseObject stack0203A = vesselStacks.get(6);
            assertEquals("2", stack0203A.get("overlappingFeuBay").getAsString());
            assertEquals("2", stack0203A.get("rowName").getAsString());

            final BundleStowbaseObject stackSupport0203A = stack0203A.get("vesselStackSupports").getAsObjects().get(2);
            assertEquals("2", stackSupport0203A.get("bayName").getAsString());
            assertEquals(Arrays.asList("12", "14", "16"), stackSupport0203A.get("dcTiersFromBelow").getAsStringList());
            //assertEquals(true, stackSupport0203A.get("imoForbidden").getAsBoolean());

            final BundleStowbaseObject stackSupport0103A = stack0203A.get("vesselStackSupports").getAsObjects().get(0);
            assertEquals("1", stackSupport0103A.get("bayName").getAsString());
            assertEquals(Arrays.asList("14"), stackSupport0103A.get("dcTiersFromBelow").getAsStringList());
            //assertEquals(true, stackSupport0103A.get("imoForbidden").getAsBoolean());
        }
    }

    private void validateTanks(final List<BundleStowbaseObject> tanks) {
        assertNotNull(tanks);
        assertEquals(50, tanks.size());

        {
            final BundleStowbaseObject tank1 = tanks.get(0);
            assertEquals("NO.1 F B TK C", tank1.get("description").getAsString());
            final BundleStowbaseObject tcgFunction = tank1.get("tcgFunction").getAsSingleObject();
            assertNotNull(tcgFunction);
            assertEquals("function2d", tcgFunction.getGroup());
            final String samplePoints1 = tcgFunction.get("samplePoints1").getAsString();
            assertNotNull(samplePoints1);
            assertEquals(4, samplePoints1.split(";").length);
        }

        {
            final BundleStowbaseObject tank2 = tanks.get(1);
            assertEquals("NO.1 A W TK P", tank2.get("description").getAsString());
            final BundleStowbaseObject tcgFunction = tank2.get("tcgFunction").getAsSingleObject();
            assertNotNull(tcgFunction);
            assertEquals("function2d", tcgFunction.getGroup());
            final String samplePoints1 = tcgFunction.get("samplePoints1").getAsString();
            assertNotNull(samplePoints1);
            assertEquals(4, samplePoints1.split(";").length);
        }
    }

    private void validateHolds(final List<BundleStowbaseObject> holds) {
        assertEquals(8, holds.size());
        final BundleStowbaseObject hold2 = holds.get(1);
        assertEquals(Arrays.asList("10", "14"), hold2.get("feubays").getAsStringList());
        final List<String> acceptsImo = hold2.get("acceptsImo").getAsStringList();
        assertTrue(acceptsImo.contains("2.2"));
        assertTrue(!acceptsImo.contains("2.3"));
    }

}
