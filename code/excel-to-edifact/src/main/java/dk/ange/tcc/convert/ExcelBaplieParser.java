package dk.ange.tcc.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;

/**
 * Helper class to handle the conversion from Excel to BAPLIE
 */
public class ExcelBaplieParser {

    private final ByteArrayOutputStream baplieOutput = new ByteArrayOutputStream();

    private final Messages messages = new Messages();

    final private String vesselImo;

    final private String vesselName;

    /**
     * @param vesselImo
     * @param vesselName
     */
    public ExcelBaplieParser(final String vesselImo, final String vesselName) {
        this.vesselImo = vesselImo;
        this.vesselName = vesselName;
    }

    /**
     * Simple helper function that converts the Excel file to BAPLIE
     *
     * @param excelStream
     * @param vesselImo
     * @param vesselName
     * @return The parser result
     */
    public static Result parse(final InputStream excelStream, final String vesselImo, final String vesselName) {
        return new ExcelBaplieParser(vesselImo, vesselName).parseAndCatch(excelStream);
    }

    /**
     * Structure for parser result
     */
    public static class Result {
        /**
         * Generated BAPLIE
         */
        public final byte[] baplieData;

        /**
         * Messages from the parser
         */
        public final Messages messages;

        private Result(final byte[] baplieData, final Messages messages) {
            this.baplieData = baplieData;
            this.messages = messages;
        }
    }

    private Result parseAndCatch(final InputStream excelStream) {
        byte[] baplieData = null;
        try {
            parseInputStream(excelStream);
            baplieData = baplieOutput.toByteArray();
        } catch (final Exception e) {
            messages.setException(e);
        }
        return new Result(baplieData, messages);
    }

    private void parseInputStream(final InputStream inputStream) {
        final Workbook workbook;
        try {
            workbook = new HSSFWorkbook(inputStream);
        } catch (final IOException e) {
            if (Pattern.matches("Invalid header signature; read 0x[0-9A-F]+, expected 0xE11AB1A1E011CFD0",
                    e.getMessage())) {
                throw new ParseException("Not an Excel file");
            }
            throw new RuntimeException(e);
        }
        parseSetupOutput(workbook);
    }

    private void parseSetupOutput(final Workbook workbook) {
        parseWorkbook(workbook, baplieOutput);
    }

    private void parseWorkbook(final Workbook workbook, final OutputStream ediFactOutputstream) {
        messages.setWorkbook(workbook);
        // Create sub parsers and parse
        new LoadlistParser(vesselImo, vesselName, ediFactOutputstream, messages, workbook, LoadlistParser.EdiFactType.BAPLIE).parse();
    }

}
