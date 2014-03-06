package dk.ange.stowbase.parse.vessel.dg;

import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.BRL;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetsParser;
import dk.ange.stowbase.parse.vessel.BaysMapping;
import dk.ange.stowbase.parse.vessel.stacks.StackData;

/**
 * Parse 'DG' sheet
 */
public class DgParser extends SheetsParser implements VesselProfileDataAdder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DgParser.class);

    private final VesselProfileDataAdder realParser;

    /**
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     * @param baysMapping
     * @param data20
     * @param data40
     */
    public DgParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final BaysMapping baysMapping, final Map<BRL, StackData> data20,
            final Map<BRL, StackData> data40) {
        super(stowbaseObjectFactory, messages, workbook);
        log.debug("Construct");
        final Sheet sheet = getSheetOptionalWithOldNameNoMessages("DG", "IMO");
        if (sheet == null) {
            realParser = null;
        } else {
            final Cell cell0 = sheet.getRow(0).getCell(0);
            final String string0 = cellString(cell0);
            if (string0.equals("# HOLDS")) {
                realParser = new OldDgParser(stowbaseObjectFactory, messages, workbook, baysMapping, data20, data40);
            } else {
                throw new RuntimeException("NEW TODO ");
            }
        }
    }

    /**
     * @param vesselProfile
     */
    @Override
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (realParser != null) {
            realParser.addDataToVesselProfile(vesselProfile);
        }
    }

}
