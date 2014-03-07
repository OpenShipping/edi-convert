package dk.ange.stowbase.parse.utils;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;

/**
 * Helper class for parsers that only parse a single sheet
 */
public abstract class SingleSheetParser extends SheetsParser implements SheetWarner {

    /**
     * The sheet we are parsing, will be null if it's not in the workbook
     */
    protected final Sheet sheet;

    /**
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public SingleSheetParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        sheet = getSheetOptional(getSheetName()); // For now this only supports optional sheets
    }

    /**
     * @return The name of the sheet to parse
     */
    abstract public String getSheetName();

    /**
     * @return true if the sheet was found in the workbook
     */
    public boolean sheetFound() {
        return sheet != null;
    }

    @Override
    public void addSheetWarning(final String warning) {
        messages.addSheetWarning(sheet, warning);
    }

}
