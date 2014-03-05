package dk.ange.tcc.convert;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Utility function for working in POI sheets
 */
public final class SheetFunctions {

    private SheetFunctions() {
        throw new RuntimeException("Do not instantiate");
    }

    /**
     * Convert a Cell to its String form
     *
     * @param cell
     * @return cell as a String
     */
    public static String cellString(final Cell cell) {
        if (cell == null) {
            return null;
        }
        cell.setCellType(Cell.CELL_TYPE_STRING);
        return cell.toString().trim();
    }

    private static char indexToChar(final int columnIndex) {
        if (!(0 <= columnIndex && columnIndex <= 25)) {
            throw new IllegalArgumentException("columnIndex=" + columnIndex);
        }
        return (char) ('A' + columnIndex);
    }

    /**
     * @param cell
     * @return Position of the cell in "A1".."ZZ9999" coordinates
     */
    public static String pos(final Cell cell) {
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

}
