package dk.ange.stowbase.parse.vessel.lashing;

import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.LashingPattern;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse all lashing data, this will be multiple sheets
 */
public class AllLashingParser extends SheetsParser {

    private final LashingParser lashingParser;

    private final PatternsParser patternsParser;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public AllLashingParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        lashingParser = new LashingParser(stowbaseObjectFactory, messages, workbook);
        patternsParser = new PatternsParser(stowbaseObjectFactory, messages, workbook);
        if (lashingParser.sheetFound() != patternsParser.sheetFound()) {
            throw new RuntimeException("Found exactly one of the sheets 'Lashing' and 'Patterns'");
        }
    }

    /**
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (!lashingParser.sheetFound()) {
            return;
        }
        final LashingPattern lashingPattern = LashingPattern.create(stowbaseObjectFactory);
        lashingParser.addDataToLashingPattern(lashingPattern);
        patternsParser.addDataToLashingPattern(lashingPattern);
        vesselProfile.put("lashingPattern", lashingPattern.getReference());
    }

}
