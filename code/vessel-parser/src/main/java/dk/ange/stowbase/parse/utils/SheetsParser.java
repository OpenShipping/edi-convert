package dk.ange.stowbase.parse.utils;

import static dk.ange.stowbase.parse.utils.Header.header;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;

/**
 * Base class of all the parts of the parsers
 */
public abstract class SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SheetsParser.class);

    /**
     * Factory of stowbase objects
     */
    protected final StowbaseObjectFactory stowbaseObjectFactory;

    /**
     * Receiver of the messages generated when parsing
     */
    protected final Messages messages;

    /**
     * The workbook the parser is parsing
     */
    protected final Workbook workbook;

    /**
     * Simple constructor
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public SheetsParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        this.stowbaseObjectFactory = stowbaseObjectFactory;
        this.messages = messages;
        this.workbook = workbook;
    }

    /**
     * Get the sheet. Will also call messages.addParsedSheets if the sheet is in the workbook.
     *
     * @param name
     * @return The sheet or null if it not there
     */
    protected Sheet getSheetOptional(final String name) {
        final Sheet sheet = workbook.getSheet(name);
        if (sheet != null) {
            messages.addParsedSheets(sheet);
        }
        return sheet;
    }

    /**
     * Get the sheet. Will fail is sheets of both names exists, will warn if sheet with the old name is used. Will also
     * call messages.addParsedSheets if the sheet is in the workbook.
     *
     * @param name
     *            Name of sheet
     * @param oldName
     *            Old name of sheet
     * @return The sheet or null if it not there
     */
    protected Sheet getSheetOptionalWithOldNameNoMessages(final String name, final String oldName) {
        final Sheet sheet = workbook.getSheet(name);
        final Sheet oldSheet = workbook.getSheet(oldName);
        if (sheet == null) {
            if (oldSheet == null) {
                return null;
            } else {
                return oldSheet;
            }
        } else {
            if (oldSheet == null) {
                return sheet;
            } else {
                throw new ParseException("Both " + name + " and " + oldName + " sheets exists");
            }
        }
    }

    /**
     * Get the sheet. Will fail is sheets of both names exists, will warn if sheet with the old name is used. Will also
     * call messages.addParsedSheets if the sheet is in the workbook.
     *
     * @param name
     *            Name of sheet
     * @param oldName
     *            Old name of sheet
     * @return The sheet or null if it not there
     */
    protected Sheet getSheetOptionalWithOldName(final String name, final String oldName) {
        final Sheet sheet = getSheetOptionalWithOldNameNoMessages(name, oldName);
        if (sheet != null) {
            messages.addParsedSheets(sheet);
            if (sheet == workbook.getSheet(oldName)) {
                messages.addSheetWarning(sheet, "Sheet is given old name, rename to '" + name + "'");
            }
        }
        return sheet;
    }

    /**
     * @param name
     * @return The sheet
     * @throws ParseException
     *             if the sheet is not in the workbook
     */
    protected Sheet getSheetMandatory(final String name) {
        final Sheet sheet = getSheetOptional(name);
        if (sheet == null) {
            throw new ParseException("Mandatory sheet '" + name + "' is missing");
        }
        return sheet;
    }

    /**
     * Convert a Cell to its String form
     *
     * @param cell
     * @return cell as a String
     */
    protected static String cellString(final Cell cell) {
        if (cell == null) {
            return null;
        }
        final DataFormatter df = new DataFormatter();
        final Workbook workbook2 = cell.getRow().getSheet().getWorkbook();
        final FormulaEvaluator formulaEval = workbook2.getCreationHelper().createFormulaEvaluator();
        return df.formatCellValue(cell, formulaEval);
    }

    /**
     * @param sheet
     * @param map
     */
    protected void readKeyValueSheet(final Sheet sheet, final Map<Header, String> map) {
        for (final Row row : new IterableIterator<>(sheet.rowIterator())) {
            final Header key = header(cellString(row.getCell(0)));
            if (key == null) {
                continue; // Skip rows with empty headers (data in column A)
            }
            if (map.containsKey(key)) {
                messages.addSheetWarning(sheet, "Key '" + cellString(row.getCell(0))
                        + "' used more than once, will use the first value");
                continue;
            }
            map.put(key, cellString(row.getCell(1)));
        }
    }

    /**
     * @param sheet
     * @return data for the sheet
     */
    public List<RowData> readColumnSheet(final Sheet sheet) {
        final List<RowData> data = new ArrayList<>();
        final Iterator<Row> rowIterator = sheet.rowIterator();
        // Check the headers
        final Row firstRow = rowIterator.next();
        final Map<Header, Integer> keyMap = new LinkedHashMap<>();
        for (final Cell cell : firstRow) {
            keyMap.put(header(cellString(cell)), cell.getColumnIndex());
        }
        // Read all data lines
        for (final Row row : new IterableIterator<>(rowIterator)) {
            final Map<Header, CellValue> rowDataData = new LinkedHashMap<>();
            for (final Entry<Header, Integer> e : keyMap.entrySet()) {
                final Header header = e.getKey();
                final int columnIndex = e.getValue();
                final Cell cell = row.getCell(columnIndex);
                final CellValue value = new CellValue(cellString(cell), header);
                rowDataData.put(header, value);
            }
            final RowData rowData = new RowData(rowDataData);
            data.add(rowData);
        }
        return data;
    }

    /**
     * @param cell
     * @return The position of the Cell in the workbook in the format used in the program
     */
    protected static String pos(final Cell cell) {
        final String sheetName = cell.getSheet().getSheetName();
        final int columnIndex = cell.getColumnIndex();
        final String columnString;
        if (columnIndex < 26) {
            columnString = "" + indexToChar(columnIndex);
        } else if (columnIndex < 26 + 26 * 26) {
            columnString = "" + indexToChar(columnIndex / 26 - 1) + indexToChar(columnIndex % 26);
        } else {
            columnString = "HIGH";
        }
        final int rowIndex = cell.getRowIndex();
        final int rowName = rowIndex + 1;
        return sheetName + "!" + columnString + rowName;
    }

    private static char indexToChar(final int columnIndex) {
        if (!(0 <= columnIndex && columnIndex <= 25)) {
            throw new IllegalArgumentException("columnIndex=" + columnIndex);
        }
        return (char) ('A' + columnIndex);
    }

    /**
     * @param row
     * @param column
     * @param factor
     * @return number read from row
     * @throws ParseException
     *             if the number could not be parsed
     */
    protected static double readNumber(final Row row, final int column, final double factor) {
        final double d = readOptionalNumber(row, column, factor);
        if (Double.isNaN(d)) {
            final Cell cell = row.getCell(column);
            throw new ParseException("Number missing in cell " + pos(cell));
        }
        return d;
    }

    /**
     * Read an optional number, if the cell is blank return NaN
     *
     * @param row
     * @param column
     *            column number, can be -1 if the column is missing, then this function return NaN
     * @param factor
     * @return the number or NaN
     * @throws ParseException
     *             if the number could not be parsed
     */
    protected static double readOptionalNumber(final Row row, final int column, final double factor) {
        if (column == -1) {
            return Double.NaN;
        }
        final Cell cell = row.getCell(column);
        final String string = cellString(cell);
        if (string == null || string.isEmpty()) {
            return Double.NaN;
        }
        final double d;
        try {
            d = Double.parseDouble(string);
        } catch (final NumberFormatException e) {
            throw new ParseException("Could not transform '" + string + "' to a number, see cell " + pos(cell));
        }
        return factor * d;
    }

    /**
     * Parse all sections in a sheet, take action on each data member in the method handleDataItem()
     */
    public abstract static class SectionParser {

        /**
         * @param sheet
         */
        @SuppressWarnings("null")
        public void readSheet(final Sheet sheet) {
            String sectionType = null;
            boolean nextLineIsTagTitleLine = false;
            String sectionTag = null;
            Map<Integer, String> columnTitles = null;
            for (final Row row : new IterableIterator<>(sheet.rowIterator())) {
                log.trace("Line {}", row.getRowNum());
                final Cell cell0 = row.getCell(0);
                log.trace("cell0={}", cell0);
                if (cell0 == null) {
                    continue;
                }
                final String cell0String = cellString(cell0);
                if (cell0String.startsWith("#")) {
                    sectionType = cell0String;
                    log.debug("TYPE {}", sectionType);
                    if (nextLineIsTagTitleLine) {
                        throw new ParseException("Title lines starting with # without data in between in " + pos(cell0));
                    }
                    sectionTag = null;
                    columnTitles = null;
                    nextLineIsTagTitleLine = true;
                } else if (nextLineIsTagTitleLine) {
                    nextLineIsTagTitleLine = false;
                    sectionTag = cell0String;
                    log.debug("TAG {}", sectionTag);
                    columnTitles = new TreeMap<>();
                    for (int cellIndex = 1; cellIndex < row.getLastCellNum(); ++cellIndex) {
                        final Cell cell = row.getCell(cellIndex);
                        final String cellString = cellString(cell);
                        if (cellString == null || cellString.isEmpty()) {
                            continue;
                        }
                        if (columnTitles.values().contains(cellString)) {
                            throw new ParseException("Duplicated data in column '" + cellString + "' found in "
                                    + pos(cell) + ". columnTitles=" + columnTitles);
                        }
                        columnTitles.put(cellIndex, cellString);
                    }
                    log.debug("TITLES {}", columnTitles);
                } else {
                    final String rowTitle = cell0String;
                    log.trace("DATA {}: {}", rowTitle, row);
                    for (final int cellIndex : columnTitles.keySet()) {
                        final Cell cell = row.getCell(cellIndex);
                        final String cellString = cellString(cell);
                        if (cellString == null || cellString.isEmpty()) {
                            continue;
                        }
                        final String columnTitle = columnTitles.get(cellIndex);
                        try {
                            handleDataItem(sectionType, sectionTag, rowTitle, columnTitle, cellString, cell);
                        } catch (final RuntimeException e) {
                            throw new ParseException(e.getMessage() + ", problem caused by " + pos(cell)
                                    + " containing '" + cellString + "'", e);
                        }
                    }
                }
            }

        }

        /**
         * Handle the data, will be called once for each cell of data in the sheet
         *
         * @param sectionType
         * @param sectionTag
         * @param rowTitle
         * @param columnTitle
         * @param cellString
         * @param cell
         */
        protected abstract void handleDataItem(final String sectionType, final String sectionTag,
                final String rowTitle, final String columnTitle, final String cellString, final Cell cell);
    }

    /**
     * Parse a sheet where all the sections are BRL data
     */
    public static abstract class BrlSectionParser extends SectionParser {
        @Override
        protected final void handleDataItem(final String sectionType, final String sectionTag, final String rowTitle,
                final String columnTitle, final String cellString, final Cell cell) {
            {
                final BRL brl = new BRL(columnTitle, rowTitle, sectionTag);
                log.trace("{}: {} <- {}", new Object[] { sectionType, brl, cellString });
                handleDataItem(sectionType, brl, cellString);
            }
        }

        /**
         * The action to take on all data
         *
         * @param sectionType
         * @param brl
         * @param cellString
         */
        protected abstract void handleDataItem(String sectionType, BRL brl, String cellString);
    }

}
