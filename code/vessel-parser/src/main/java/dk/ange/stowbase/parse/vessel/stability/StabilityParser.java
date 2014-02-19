package dk.ange.stowbase.parse.vessel.stability;

import static dk.ange.stowbase.parse.utils.Header.header;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.Header;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse the "Stability" sheet.
 */
public class StabilityParser extends SheetsParser {

    private static final String SHEET_NAME = "Stability";

    private Map<Header, String> map;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public StabilityParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    void parse() {
        final Sheet sheet = getSheetOptional(SHEET_NAME);
        if (sheet == null) {
            return;
        }
        map = new HashMap<Header, String>();
        readKeyValueSheet(sheet, map);
    }

    /**
     * Add the parser result to the vessel profile
     *
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (map == null) {
            return;
        }
        final StowbaseObject stability = stowbaseObjectFactory.create("stability");
        vesselProfile.put("stability", stability.getReference());
        putNummericValue("Hull weight in ton", 1000, stability, "hullWeightInKg");
        putNummericValue("Hull LCG in m", 1, stability, "hullLcgInM");
        putNummericValue("Hull VCG in m", 1, stability, "hullVcgInM");
        putNummericValue("Vessel LPP in m", 1, stability, "vesselLppInM");
        putNummericValue("Observer LCG in m", 1, stability, "observerLcgInM");
        putNummericValue("Observer VCG in m", 1, stability, "observerVcgInM");

        readClassificationSociety(stability);

        for (final Header key : map.keySet()) {
            messages.addSheetWarning(SHEET_NAME, "Unused key '" + key + "'");
        }
    }

    private void readClassificationSociety(final StowbaseObject stability) {
        final String society = putStringValue("Classification society", stability, "classificationSociety");
        if (society != null) {
            final List<String> knownSocieties = Arrays.asList("GL", "LR", "DNV");
            if (!knownSocieties.contains(society)) {
                messages.addSheetWarning(SHEET_NAME, "Unknown society '" + society + "', the parser knows: "
                        + knownSocieties);
            }
        }
    }

    /**
     * If map has key get it as a number, multiply by factor and put result into stowbaseObject using stobaseKey. Will
     * also clear the value from the map.
     */
    private void putNummericValue(final String keyString, final double factor, final StowbaseObject stowbaseObject,
            final String stowbaseKey) {
        final Header keyHeader = header(keyString);
        if (map.containsKey(keyHeader)) {
            final String string = map.get(keyHeader);
            final double d;
            try {
                d = Double.parseDouble(string);
            } catch (final NumberFormatException e) {
                messages.addSheetWarning(SHEET_NAME, "Could not transform '" + string
                        + "' to a number, see line with '" + keyHeader + "'");
                return;
            }
            stowbaseObject.put(stowbaseKey, factor * d);
            map.remove(keyHeader);
        }
    }

    /**
     * If map has key get it and put result into stowbaseObject using stowbaseKey. Will also clear the value from the
     * map.
     *
     * @return the value (or null)
     */
    private String putStringValue(final String keyString, final StowbaseObject stowbaseObject, final String stowbaseKey) {
        final Header keyHeader = header(keyString);
        final String value = map.get(keyHeader);
        if (value != null) {
            stowbaseObject.put(stowbaseKey, value);
            map.remove(keyHeader);
        }
        return value;
    }

}
