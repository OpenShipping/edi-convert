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
        assertEquals("15.016;15.247;17.037;21.76;15.034;18.718;15.412;15.024;16.811;15.075;15.88;15.239;18.347;15.154;15.055;17.7;"
                + "22.857;19.753;15.021;15.121;15.201;15.108;16.327;15.594;15.016;17.555;15.049;18.528;15.336;15.267;15.019;23.697;"
                + "15.141;17.158;15.946;15.186;15.018;20.534;15.06;16.705;19.57;21.117;15.087;15.173;15.04;17.851;15.454;15.646;"
                + "15.031;19.342;15.085;15.219;16.244;15.269;16.164;15.143;22.106;15.064;15.103;15.498;22.472;18.009;15.12;15.192;"
                + "16.015;15.039;20.262;16.604;15.238;15.016;15.373;15.758;17.285;15.7;15.028;15.212;21.43;19.124;24.156;15.073;"
                + "15.229;15.545;16.088;15.164;15.047;15.025;16.922;15.277;20.818;20.002;18.174;15.21;15.096;15.301;15.818;15.132;"
                + "16.507;15.017;15.257;16.415;15.162;18.916;15.183;23.265;17.417;15.02", metacenter.get("sampleData").getAsString());
        assertEquals("0.0", metacenter.get("samplePoints1").getAsString());
        assertEquals("10.3;13.2;5.9;3.6;11.0;4.8;8.1;10.8;6.1;11.5;7.2;8.6;5.0;12.2;11.3;5.4;3.3;4.3;10.0;9.1;12.7;11.8;6.6;7.7;"
                + "10.5;5.5;9.6;4.9;8.3;13.4;10.1;3.1;9.0;5.8;7.1;8.8;10.6;4.0;9.5;6.2;4.4;3.8;9.3;12.4;11.1;5.3;8.0;7.6;9.8;4.5;"
                + "11.6;12.9;6.7;8.5;6.8;12.1;3.5;11.4;9.2;7.9;3.4;5.2;11.9;12.6;7.0;9.7;4.1;6.3;13.1;10.4;8.2;7.4;5.7;7.5;10.9;8.7;"
                + "3.7;4.6;3.0;9.4;13.0;7.8;6.9;12.3;11.2;9.9;6.0;13.5;3.9;4.2;5.1;12.8;11.7;8.4;7.3;12.0;6.4;10.2;13.3;6.5;8.9;4.7;"
                + "12.5;3.2;5.6;10.7", metacenter.get("samplePoints2").getAsString());
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
        assertEquals("13.5;13.4;13.3;13.2;13.1;13.0;12.9;12.8;12.7;12.6;12.5;12.4;12.3;12.2;12.1;12.0;11.9;11.8;11.7;11.6;"
                + "11.5;11.4;11.3;11.2;11.1;11.0;10.9;10.8;10.7;10.6;10.5;10.4;10.3;10.2;10.1;10.0;9.9;9.8;9.7;9.6;9.5;9.4;9.3;9.2;9.1;9.0;"
                + "8.9;8.8;8.7;8.6;8.5;8.4;8.3;8.2;8.1;8.0;7.9;7.8;7.7;7.6;7.5;7.4;7.3;7.2;7.1;7.0;6.9;6.8;6.7;6.6;6.5;6.4;6.3;6.2;6.1;6.0;"
                + "5.9;5.8;5.7;5.6;5.5;5.4;5.3;5.2;5.1;5.0;4.9;4.8;4.7;4.6;4.5;4.4;4.3;4.2;4.1;4.0;3.9;3.8;3.7;3.6;3.5;3.4;3.3;3.2;3.1;3.0;"
                + "13.5;13.4;13.3;13.2;13.1;13.0;12.9;12.8;12.7;12.6;12.5;12.4;12.3;12.2;12.1;12.0;11.9;11.8;11.7;11.6;11.5;11.4;11.3;11.2;"
                + "11.1;11.0;10.9;10.8;10.7;10.6;10.5;10.4;10.3;10.2;10.1;10.0;9.9;9.8;9.7;9.6;9.5;9.4;9.3;9.2;9.1;9.0;8.9;8.8;8.7;8.6;8.5;"
                + "8.4;8.3;8.2;8.1;8.0;7.9;7.8;7.7;7.6;7.5;7.4;7.3;7.2;7.1;7.0;6.9;6.8;6.7;6.6;6.5;6.4;6.3;6.2;6.1;6.0;5.9;5.8;5.7;5.6;5.5;"
                + "5.4;5.3;5.2;5.1;5.0;4.9;4.8;4.7;4.6;4.5;4.4;4.3;4.2;4.1;4.0;3.9;3.8;3.7;3.6;3.5;3.4;3.3;3.2;3.1;3.0", draftFunction.get("sampleData").getAsString());
        assertEquals("7.3472E7;7.2754E7;7.2038E7;7.1323E7;7.0609E7;6.9897E7;6.9188E7;6.848E7;6.7774E7;6.7069E7;6.6367E7;"
                + "6.5666E7;6.4967E7;6.427E7;6.3575E7;6.2883E7;6.2192E7;6.1505E7;6.082E7;6.0139E7;5.9462E7;5.8788E7;5.8117E7;"
                + "5.745E7;5.6786E7;5.6125E7;5.5468E7;5.4813E7;5.4162E7;5.3514E7;5.287E7;5.2229E7;5.1591E7;5.0956E7;5.0325E7;"
                + "4.9696E7;4.9071E7;4.8448E7;4.7829E7;4.7212E7;4.6597E7;4.5986E7;4.5376E7;4.4769E7;4.4165E7;4.3562E7;4.2961E7;"
                + "4.2363E7;4.1766E7;4.1172E7;4.0579E7;3.9988E7;3.9399E7;3.8812E7;3.8227E7;3.7644E7;3.7063E7;3.6484E7;3.5907E7;"
                + "3.5332E7;3.4758E7;3.4187E7;3.3618E7;3.305E7;3.2485E7;3.1922E7;3.1361E7;3.0802E7;3.0245E7;2.969E7;2.9137E7;"
                + "2.8587E7;2.8038E7;2.7492E7;2.6948E7;2.6406E7;2.5866E7;2.5329E7;2.4794E7;2.426E7;2.373E7;2.3201E7;2.2675E7;"
                + "2.2151E7;2.163E7;2.1111E7;2.0594E7;2.0079E7;1.9567E7;1.9058E7;1.8551E7;1.8047E7;1.7545E7;1.7048E7;1.6553E7;"
                + "1.6062E7;1.5573E7;1.5088E7;1.4605E7;1.4126E7;1.3649E7;1.3177E7;1.2707E7;1.2241E7;1.1778E7;1.1319E7", draftFunction.get("samplePoints1").getAsString());
        assertEquals("-1000.0;1000.0", draftFunction.get("samplePoints2").getAsString());
        */
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
        /* TODO: correct sorting delayed to next commit #180
        assertEquals("-73.44;0.0;61.2;12.24;-122.4;-24.48;85.68;36.72;110.16;-97.92;48.96;73.44;-61.2;-12.24;122.4;24.48;-85.68;-36.72;-110.16;97.92;-48.96", bonjean.get("samplePoints1").getAsString());
        assertEquals("3.0;13.2;11.4;7.8;3.6;6.9;4.8;9.0;12.3;8.1;9.9;10.8;6.0;13.5;12.6;3.9;4.2;7.2;11.7;6.3;8.4;5.4;9.3;12.0;11.1;10.2;5.7;7.5;6.6;4.5;8.7;12.9;10.5;13.8;9.6", bonjean.get("samplePoints2").getAsString());
        */
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
