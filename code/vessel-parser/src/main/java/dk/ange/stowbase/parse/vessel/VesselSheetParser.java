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

    private TransversePositiveDirection transversePositiveDirection;

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

        final String transversePositiveDirectionString = vesselMap.get(header("Transverse positive direction"));
        if (transversePositiveDirectionString == null) {
            transversePositiveDirection = TransversePositiveDirection.LEGACY;
        } else {
            transversePositiveDirection = TransversePositiveDirection.valueOf(transversePositiveDirectionString
                    .toUpperCase());
        }
        log.debug("transversePositiveDirection=" + transversePositiveDirection);
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
     * @return Which direction is positive in the longitudinal direction, both in the XLS sheet and in the output JSON.
     */
    public LongitudinalPositiveDirection getLongitudinalPositiveDirection() {
        return longitudinalPositiveDirection;
    }

    /**
     * @return Which direction is positive in the transverse direction, in the XLS sheet. The JSON has its own silly
     *         ideas about which sides that are positive in the transverse direction.
     */
    public TransversePositiveDirection getTransversePositiveDirection() {
        return transversePositiveDirection;
    }

    /**
     * Which direction is positive in the transverse direction. This is used only when reading the XLS file, when
     * writing the JSON file we transform to the old inconsistent standard.
     */
    public static enum TransversePositiveDirection {

        /**
         * Positive to port, this what is used in Stowbase and Shop.
         */
        PORT,

        /**
         * Positive to starboard, this is what we use Ange Stow.
         */
        STARBOARD,

        /**
         * Positive to port most of the time, but starboard for tanks.
         */
        LEGACY;

        /**
         * @return the sign that Port has or +1 if legacy mode
         */
        public int signForPort() {
            return signForPort(+1);
        }

        /**
         * @return the sign that Starboard has or +1 if legacy mode
         */
        public int signForStarboard() {
            return -signForPort(-1);
        }

        private int signForPort(final int legacySign) {
            switch (this) {
            case PORT:
                return +1;
            case STARBOARD:
                return -1;
            case LEGACY:
                return legacySign;
            default:
                throw new RuntimeException("Should never happen, '" + this + "'");
            }
        }

    }

}
