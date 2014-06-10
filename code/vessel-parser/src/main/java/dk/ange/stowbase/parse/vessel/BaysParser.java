package dk.ange.stowbase.parse.vessel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetsParser;
import dk.ange.stowbase.parse.vessel.BaysMapping.BaysMappingBuilder;

/**
 * Parse 'Bays' sheet
 */
public class BaysParser extends SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaysParser.class);

    private BaysMapping baysMapping;

    /**
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public BaysParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        readBays();
    }

    private void readBays() {
        final Sheet sheet = getSheetMandatory("Bays");
        final BaysMappingBuilder builder = new BaysMappingBuilder();
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); ++rowIndex) {
            final Row row = sheet.getRow(rowIndex);
            final String bayName = cellString(row.getCell(0));
            if (bayName == null || bayName.length() == 0) {
                continue;
            }
            log.trace("bayName={}", bayName);
            final String twentyFore = cellString(row.getCell(1));
            final String forty = cellString(row.getCell(2));
            final String twentyAft = cellString(row.getCell(3));
            final String cargoSpace = cellString(row.getCell(4));
            builder.add(bayName, twentyFore, forty, twentyAft, cargoSpace);
        }
        baysMapping = builder.build();
        log.debug("baysMapping = {}", baysMapping);
    }

    /**
     * @return BaysMapping
     */
    public BaysMapping getBaysMapping() {
        return baysMapping;
    }

}
