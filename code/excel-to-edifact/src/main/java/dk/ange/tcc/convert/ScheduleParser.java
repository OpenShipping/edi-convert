package dk.ange.tcc.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse "Schedule"
 */
public final class ScheduleParser extends SheetsParser {

    private static final String SHEET_NAME = "Schedule";

    private final List<String> calls = new ArrayList<>();

    /**
     * Create and parse.
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public ScheduleParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    private void parse() {
        final Sheet sheet = getSheetOptional(SHEET_NAME);
        if (sheet != null) {
            for (int r = 1; r < sheet.getPhysicalNumberOfRows(); ++r) { // Skip first row
                final Row row = sheet.getRow(r);
                try {
                    final String portCode = cellString(row.getCell(0));
                    calls.add(portCode);
                    final StowbaseObject call = stowbaseObjectFactory.create("call");
                    call.put("port", "urn:stowbase.org:port:unlocode=" + portCode);
                } catch (final Exception e) {
                    throw new RuntimeException("Error was in row " + row.getRowNum(), e);
                }
            }
            messages.addSheetWarning(SHEET_NAME, "Read calls: " + calls); // Should be info, not warning
        }
    }

    /**
     * Adds moves to stowage
     *
     * @param stowage
     */
    public void addDataToStowage(@SuppressWarnings("unused") final StowbaseObject stowage) {
        // stowage.put("schedule", "TODO");
    }

    /**
     * @return list of calls
     */
    public List<String> getCalls() {
        return Collections.unmodifiableList(calls);
    }

}
