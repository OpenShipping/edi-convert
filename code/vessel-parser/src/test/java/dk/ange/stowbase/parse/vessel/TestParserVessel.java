package dk.ange.stowbase.parse.vessel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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
     */
    @Test
    public void convertLegoWithStability() {
        convertLegoWithStability("lego-maersk-w-stability.xls");
        convertLegoWithStability("lego-maersk-w-stability.xlsx");
    }

    private void convertLegoWithStability(final String fileName) {
        final Result result;
        try (final InputStream inputStream = TestParserVessel.class.getResourceAsStream(fileName)) {
            result = ParseVessel.parse(inputStream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        assertNotNull("Be able to display stack trace", result.messages.getDeveloperStatus());
        assertNull("Should not get a stack trace:\n" + result.messages.getDeveloperStatus(),
                result.messages.getException());
        assertNotNull(result.json);
        assertTrue(result.messages.getStatus().startsWith("OK"));
        assertEquals("" //
                + "OK\n" //
                + "Parsed the following sheets: [Vessel, Bays, Tier20, Tier40, Reef20, Reef40, Height20,"
                + " Height40, Pos20, Pos40, Dg20, Dg40, DG, Tanks, VarTanks, ConstWgts, Stability, Hydrostatics,"
                + " MetaCenter, HullWgtDistr, Bonjean, StressLimits]\n" //
                + "Dg40: Unknown rule 'X' ignored. The known rule is 'Z'.\n" //
                + "DG: Unknown DG class '7.7'\n" //
                + "DG: Known DG classes: [1.1-1.6, 1.4S, 2.1, 2.2, 2.3, 3, 3(B), 3(C),"
                + " 4.1, 4.2, 4.3, 4.3(A), 4.3(B), 4.3(C), 4.3(D), 5.1, 5.2,"
                + " 6.1, 6.1(A), 6.1(B), 6.1(C), 6.1(D), 8, 8(A), 8(B), 8(C), 8(D), 9]\n" //
                + "DG: Unknown permission 'X' in cell DG!D20, it will be ignored. Known permissions are [P, N]\n" //
                + "Tanks: The volume capacity written in Tanks!B4 is 490.00 while the one derived from"
                + " mass capacity and density is 487.80\n", //
                result.messages.getStatus());
        assertNotNull(result.messages.getStatus());

        final Bundle bundle = StowbaseReader.readStowbaseData(result.json);
        assertNotNull(bundle);
        assertEquals(203, bundle.size());

        final BundleStowbaseObject vesselProfile = bundle.single("vesselProfile");
        assertNotNull(vesselProfile);

        validateStacks(vesselProfile.get("vesselStacks").getAsObjects());

        validateTanks(vesselProfile.get("tanks").getAsObjects());

        final BundleStowbaseObject stability = vesselProfile.get("stability").getAsSingleObject();
        assertNotNull(stability.get("classificationSociety"));
        assertEquals("DNV", stability.get("classificationSociety").getAsString());

        validateHolds(vesselProfile.get("holds").getAsObjects());
    }

    private void validateStacks(final List<BundleStowbaseObject> vesselStacks) {
        { // index 0, 0200A
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
        { // index 1, 0200B
            final BundleStowbaseObject stack = vesselStacks.get(1);
            assertEquals("0", stack.get("rowName").getAsString());
            assertEquals("2", stack.get("overlappingFeuBay").getAsString());
            final BundleStowbaseObject stackSupport = stack.get("vesselStackSupports").getAsObjects().get(2);
            assertEquals("2", stackSupport.get("bayName").getAsString());
            assertEquals(Arrays.asList("2"), stackSupport.get("dcTiersFromBelow").getAsStringList());
            assertEquals(true, stackSupport.get("imoForbidden").getAsBoolean());
        }
        { // index 3, 0201B
            final BundleStowbaseObject stack = vesselStacks.get(3);
            assertEquals("1", stack.get("rowName").getAsString());
            assertEquals("2", stack.get("overlappingFeuBay").getAsString());
            final BundleStowbaseObject stackSupport = stack.get("vesselStackSupports").getAsObjects().get(2);
            assertEquals("2", stackSupport.get("bayName").getAsString());
            assertEquals(Arrays.asList("2"), stackSupport.get("dcTiersFromBelow").getAsStringList());
            assertNull(stackSupport.get("imoForbidden"));
        }
        { // index 5, 0202B
            final BundleStowbaseObject stack = vesselStacks.get(5);
            assertEquals("2", stack.get("rowName").getAsString());
            assertEquals("2", stack.get("overlappingFeuBay").getAsString());
            final BundleStowbaseObject stackSupport = stack.get("vesselStackSupports").getAsObjects().get(2);
            assertEquals("2", stackSupport.get("bayName").getAsString());
            assertEquals(Arrays.asList("2"), stackSupport.get("dcTiersFromBelow").getAsStringList());
            assertNull(stackSupport.get("imoForbidden"));
        }
        { // index 6, 0203A
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
        {
            final BundleStowbaseObject hold2 = holds.get(1);
            assertEquals("2", hold2.get("name").getAsString());
            assertEquals(Arrays.asList("10", "14"), hold2.get("feubays").getAsStringList());
            final List<String> acceptsImo = hold2.get("acceptsImo").getAsStringList();
            assertTrue(acceptsImo.contains("2.2"));
            assertTrue(!acceptsImo.contains("2.3"));
        }
        assertEquals("?", getPermissionAbove(holds.get(0), "7.7"));
        assertEquals("P", getPermissionAbove(holds.get(1), "7.7"));
        assertEquals("N", getPermissionAbove(holds.get(2), "7.7"));
    }

    private String getPermissionAbove(final BundleStowbaseObject hold, final String class_) {
        final boolean permitted = hold.get("dgAbovePermitted").getAsStringList().contains(class_);
        final boolean notPermitted = hold.get("dgAboveNotPermitted").getAsStringList().contains(class_);
        if (permitted) {
            if (notPermitted) {
                fail("Both permitted and notPermitted? '" + class_ + "' " + hold);
                throw new RuntimeException("Will exit at fail");
            } else {
                return "P";
            }
        } else {
            if (notPermitted) {
                return "N";
            } else {
                return "?";
            }
        }
    }

}
