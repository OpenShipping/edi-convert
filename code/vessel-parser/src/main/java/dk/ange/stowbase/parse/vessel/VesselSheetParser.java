package dk.ange.stowbase.parse.vessel;

import static dk.ange.stowbase.parse.utils.Header.header;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselProfile;
import org.stowbase.client.objects.VesselProfile.LongitudinalPositiveDirection;

import dk.ange.stowbase.parse.utils.Header;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse the sheet called "Vessel"
 */
public class VesselSheetParser extends SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VesselSheetParser.class);

    private final Map<Header, String> vesselMap = new HashMap<>();

    private LongitudinalPositiveDirection longitudinalPositiveDirection;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public VesselSheetParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    private void parse() {
        final Sheet sheet = getSheetMandatory("Vessel");
        readKeyValueSheet(sheet, vesselMap);
        log.debug("vesselMap = {}", vesselMap);
        final String longitudinalPositiveDirectionString = vesselMap.get(header("Longitudinal positive direction"));
        if (longitudinalPositiveDirectionString == null) {
            longitudinalPositiveDirection = LongitudinalPositiveDirection.FORE;
        } else {
            longitudinalPositiveDirection = LongitudinalPositiveDirection.valueOf(longitudinalPositiveDirectionString
                    .toUpperCase());
        }
        log.debug("longitudinalPositiveDirection=" + longitudinalPositiveDirection);
    }

    /**
     * Add the parser result to the vessel profile
     *
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        final String imoNumber = vesselMap.get(header("IMO number"));
        if (imoNumber != null) {
            vesselProfile.setImoCode(imoNumber);
        }
        final String name = vesselMap.get(header("Name"));
        if (name != null) {
            vesselProfile.setName(name);
        }
        if (longitudinalPositiveDirection != null) {
            vesselProfile.setLongitudinalPositiveDirection(longitudinalPositiveDirection);
        }
    }

    /**
     * @return Which direction is positive
     */
    public LongitudinalPositiveDirection getLongitudinalPositiveDirection() {
        return longitudinalPositiveDirection;
    }

}
