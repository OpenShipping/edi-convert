package dk.ange.stowbase.parse.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data for a Row in a column based sheet
 */
public class RowData {

    private final Map<Header, CellValue> data;

    /**
     * @param data
     */
    public RowData(final Map<Header, CellValue> data) {
        this.data = new LinkedHashMap<>(data);
    }

    /**
     * @param headerString
     * @return data in the row for the given header
     */
    public CellValue get(final String headerString) {
        final Header header = Header.header(headerString);
        final CellValue value = data.get(header);
        if (value == null) {
            return CellValue.cellWithHeaderMissing();
        }
        return value;
    }

}
