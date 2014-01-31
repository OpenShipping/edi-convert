package dk.ange.stowbase.parse;

import java.io.IOException;

/**
 * Parse all the known vessels in ../vessel-archive
 */
public class ParseMultipleVessels {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ParseMultipleVessels.class);

    private static final String[] FILES = { //
    "UASC/A13/A13_template_vessel.xls", //
            "UASC/A4/A4_template_vessel.xls", //
            "UASC/A7/A7_template_vessel.xls", //
            // "UASC/Ambassador_Bridge/Ambassador-Bridge-9409027.xls", // Converted from nsd+stab
            "UASC/Hansa_Limburg/Hansa-Limburg.xls", //
            "UASC/Ibn_Hazm/Ibn-Hazm-9319569.xls", //
            "UASC/Maliakos/Maliakos.xls", //
            "UASC/Noble_Acrux/Noble-Acrux-9433066.xls", //
            "UASC/Thomas_Mann/Thomas-Mann-9248667.xls", //
    };

    /**
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        log.info("Will parse {} files", FILES.length);
        for (final String file : FILES) {
            XlsVesselToJson.convert("../../../vessel-profiles/" + file);
        }
        log.info("DONE.");
    }

}
