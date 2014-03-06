package dk.ange.stowbase.parse.utils;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Simple wrapper around a sheet that it will warn about
 */
public class SheetWrapperWarner implements SheetWarner {

    private final Messages messages;

    private final Sheet sheet;

    /**
     * @param messages
     * @param sheet
     */
    public SheetWrapperWarner(final Messages messages, final Sheet sheet) {
        this.messages = messages;
        this.sheet = sheet;
    }

    @Override
    public void addSheetWarning(final String warning) {
        messages.addSheetWarning(sheet, warning);
    }

}
