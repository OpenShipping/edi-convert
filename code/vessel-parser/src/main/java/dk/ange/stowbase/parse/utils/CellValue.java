package dk.ange.stowbase.parse.utils;

/**
 * Value in a Cell
 */
public class CellValue {

    private static final CellValue HEADER_MISSING = new CellValue();

    private final boolean headerMissing;

    // If this is null this means that the header is present but the cell was empty
    private String data;

    private Header header;

    private CellValue() {
        this.headerMissing = true;
    }

    CellValue(final String data, final Header header) {
        this.headerMissing = false;
        this.data = data;
        this.header = header;
    }

    /**
     * @return CellValue for header missing
     */
    public static CellValue cellWithHeaderMissing() {
        return HEADER_MISSING;
    }

    /**
     * @return true if this cell is not present because the column header was not in row 1
     */
    public boolean headerMissing() {
        return headerMissing;
    }

    /**
     * @return true is this cell has data
     */
    public boolean hasData() {
        return !headerMissing && data != null;
    }

    private void checkHasData() {
        if (headerMissing) {
            throw new RuntimeException("Column '" + header + "' is missing");
        }
        if (data == null) {
            throw new RuntimeException("There is no data in the cell"); // TODO show where...
        }
    }

    /**
     * @param default_
     *            default value returned if the header is missing or the cell is empty
     * @return value as string
     */
    public String asString(final String default_) {
        if (hasData()) {
            return data;
        } else {
            return default_;
        }
    }

    /**
     * @return value as string
     */
    public String asString() {
        checkHasData();
        return data;
    }

    /**
     * @param default_
     *            default value returned if the header is missing or the cell is empty
     * @return value as int
     */
    public int asInt(final int default_) {
        if (hasData()) {
            return Integer.parseInt(data);
        } else {
            return default_;
        }
    }

    /**
     * @return value as int
     */
    public int asInt() {
        checkHasData();
        return Integer.parseInt(data);
    }

    /**
     * @param default_
     *            default value returned if the header is missing or the cell is empty
     * @return value as double
     */
    public double asDouble(final int default_) {
        if (hasData()) {
            return Double.parseDouble(data);
        } else {
            return default_;
        }
    }

    /**
     * @return value as double
     */
    public double asDouble() {
        checkHasData();
        return Double.parseDouble(data);
    }

}
