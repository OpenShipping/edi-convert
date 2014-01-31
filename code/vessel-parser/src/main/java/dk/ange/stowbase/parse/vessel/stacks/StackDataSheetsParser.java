package dk.ange.stowbase.parse.vessel.stacks;

import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;

import dk.ange.stowbase.parse.utils.BRL;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Base class of all the parts of the parsers
 */
public abstract class StackDataSheetsParser extends SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StackDataSheetsParser.class);

    /**
     * Simple constructor
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public StackDataSheetsParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        log.debug("Construct");
    }

    /**
     * @param sheet
     * @param data
     * @param action
     */
    protected void readStacksData(final Sheet sheet, final Map<BRL, StackData> data, final StackDataAction action) {
        readStacksData(sheet, data, action, false);
    }

    /**
     * @param sheet
     * @param data
     * @param action
     * @param createStackData
     */
    protected void readStacksData(final Sheet sheet, final Map<BRL, StackData> data, final StackDataAction action,
            final boolean createStackData) {
        new StackDataActionSectionParser(data, action, createStackData).readSheet(sheet);
    }

    private static class StackDataActionSectionParser extends BrlSectionParser {

        private final Map<BRL, StackData> data;

        private final StackDataAction action;

        private final boolean createStackData;

        StackDataActionSectionParser(final Map<BRL, StackData> data, final StackDataAction action,
                final boolean createStackData) {
            this.data = data;
            this.action = action;
            this.createStackData = createStackData;
        }

        @Override
        protected void handleDataItem(final String sectionType, final BRL brl, final String cellString) {
            {
                if (!data.containsKey(brl)) {
                    if (createStackData) {
                        data.put(brl, new StackData());
                    } else {
                        throw new ParseException("Can not add new stack " + brl);
                    }
                }
                final StackData stackData = data.get(brl);
                try {
                    action.call(stackData, sectionType, cellString);
                } catch (final RuntimeException e) {
                    throw new RuntimeException("Error when reading data: " + e.getMessage(), e);
                }
            }
        }
    }

}
