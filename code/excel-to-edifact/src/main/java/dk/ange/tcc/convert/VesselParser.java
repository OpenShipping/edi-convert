package dk.ange.tcc.convert;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.StowbaseURN;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse "Vessel"
 */
public final class VesselParser extends SheetsParser {

    private String imoNumber = null;

    /**
     * Create and parse.
     * 
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public VesselParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    private void parse() {
        final Sheet sheet = getSheetOptional("Vessel");
        if (sheet != null) {
            final Row row = sheet.getRow(1);
            imoNumber = row == null ? null : cellString(row.getCell(0)); // ?: trick used to handle row==null
            if (imoNumber == null) {
                throw new ParseException("Could not read the IMO number from the Excel sheet,"
                        + " it should be in the Vessel sheet in cell A2");
            }
        }
    }

    /**
     * Adds moves to stowage
     * 
     * @param stowage
     */
    public void addDataToStowage(final StowbaseObject stowage) {
        if (imoNumber != null) {
            stowage.put("vessel", StowbaseURN.vessel(imoNumber, null).toString());
        }
    }

    /**
     * @return the IMO number
     */
    public String getImoNumber() {
        return imoNumber;
    }

}
