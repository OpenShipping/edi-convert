package dk.ange.tcc.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.export.WriterExporter;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;

/**
 * Parse a stowage
 */
public final class StowageParser {

    private final ByteArrayOutputStream jsonOutput = new ByteArrayOutputStream();

    private final Messages messages = new Messages();

    /**
     * Simple helper function that converts the Excel file to Stowage (compressed JSON)
     * 
     * @param excelStream
     * @return The parser result
     */
    public static Result parse(final InputStream excelStream) {
        return new StowageParser().parseAndCatch(excelStream);
    }

    /**
     * Struct for parser result
     */
    public static class Result {
        /**
         * Generated JSON
         */
        public final byte[] jsonData;

        /**
         * Messages from the parser
         */
        public final Messages messages;

        private Result(final byte[] jsonData, final Messages messages) {
            this.jsonData = jsonData;
            this.messages = messages;
        }
    }

    private Result parseAndCatch(final InputStream excelStream) {
        byte[] jsonData = null;
        try {
            parseInputStream(excelStream);
            jsonData = jsonOutput.toByteArray();
        } catch (final Exception e) {
            messages.setException(e);
        }
        return new Result(jsonData, messages);
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
        final StowbaseObjectFactory stowbaseObjectFactory;
        final WriterExporter jsonWriterExporter;
        final GZIPOutputStream compressedOutputStream;
        try {
            compressedOutputStream = new GZIPOutputStream(jsonOutput);
            jsonWriterExporter = new WriterExporter(new OutputStreamWriter(compressedOutputStream, "UTF-8"));
            stowbaseObjectFactory = jsonWriterExporter.stowbaseObjectFactory();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        parseWorkbook(workbook, stowbaseObjectFactory);

        jsonWriterExporter.flush("stowage.sto");
        try {
            compressedOutputStream.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseWorkbook(final Workbook workbook, final StowbaseObjectFactory stowbaseObjectFactory) {
        messages.setWorkbook(workbook);
        // Create sub parsers and parse
        final VesselParser vesselParser = new VesselParser(stowbaseObjectFactory, messages, workbook);
        final ScheduleParser scheduleParser = new ScheduleParser(stowbaseObjectFactory, messages, workbook);
        final LoadlistParser loadlistParser = new LoadlistParser(stowbaseObjectFactory, messages, workbook,
                vesselParser.getImoNumber(), scheduleParser.getCalls(), LoadlistParser.EdiFactType.UNDEFINED);
        loadlistParser.parse();
        final ProjectionsParser projectionsParser = new ProjectionsParser(stowbaseObjectFactory, messages, workbook,
                vesselParser.getImoNumber(), scheduleParser.getCalls());
        projectionsParser.parse();
        // Combine results
        final StowbaseObject stowage = stowbaseObjectFactory.create("stowage");
        vesselParser.addDataToStowage(stowage);
        scheduleParser.addDataToStowage(stowage);
        loadlistParser.addDataToStowage(stowage);
        projectionsParser.addDataToStowage(stowage);
    }

}
