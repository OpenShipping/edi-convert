package dk.ange.stowbase.parse.vessel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.export.WriterExporter;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.vessel.dg.DgParser;
import dk.ange.stowbase.parse.vessel.stability.BonjeanParser;
import dk.ange.stowbase.parse.vessel.stability.ConstWgtsParser;
import dk.ange.stowbase.parse.vessel.stability.HullWgtDistrParser;
import dk.ange.stowbase.parse.vessel.stability.HydrostaticsParser;
import dk.ange.stowbase.parse.vessel.stability.MetaCenterParser;
import dk.ange.stowbase.parse.vessel.stability.StabilityParser;
import dk.ange.stowbase.parse.vessel.stability.StressLimitsParser;
import dk.ange.stowbase.parse.vessel.stacks.StacksParser;
import dk.ange.stowbase.parse.vessel.tanks.TanksParser;

/**
 * Parse a vessel
 */
public final class ParseVessel {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ParseVessel.class);

    private final StringWriter stringWriter = new StringWriter();

    private final WriterExporter writerExporter = new WriterExporter(stringWriter);

    private final StowbaseObjectFactory stowbaseObjectFactory = writerExporter.stowbaseObjectFactory();

    private final Messages messages = new Messages();

    /**
     * Simple helper function that converts the Excel file to Json
     *
     * @param excelStream
     * @return The parser result
     */
    public static Result parse(final InputStream excelStream) {
        return new ParseVessel().parseAndCatch(excelStream);
    }

    /**
     * Struct for parser result
     */
    public static class Result {
        /**
         * Generated JSON
         */
        public final String json;

        /**
         * Messages from the parser
         */
        public final Messages messages;

        private Result(final String json, final Messages messages) {
            this.json = json;
            this.messages = messages;
        }
    }

    private Result parseAndCatch(final InputStream excelStream) {
        String json = null;
        try {
            parseInputStream(excelStream);
            json = stringWriter.toString();
        } catch (final Exception e) {
            log.debug("Caught exception: {}", e);
            messages.setException(e);
        }
        return new Result(json, messages);
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
        parseWorkbook(workbook);
    }

    private void parseWorkbook(final Workbook workbook) {
        messages.setWorkbook(workbook);
        final VesselProfile vesselProfile = VesselProfile.create(stowbaseObjectFactory);

        final VesselSheetParser vesselSheetParser = new VesselSheetParser(stowbaseObjectFactory, messages, workbook);
        vesselSheetParser.addDataToVesselProfile(vesselProfile);
        final BaysParser baysParser = new BaysParser(stowbaseObjectFactory, messages, workbook);
        final StacksParser stacksParser = new StacksParser(stowbaseObjectFactory, messages, workbook,
                vesselSheetParser.getLongitudinalPositiveDirection(), baysParser.getBaysMapping());

        final DgParser dgParser = new DgParser(stowbaseObjectFactory, messages, workbook, baysParser.getBaysMapping(),
                stacksParser.getData20(), stacksParser.getData40());
        stacksParser.addDataToVesselProfile(vesselProfile); // must be after DG parsing
        new LidsParser(stowbaseObjectFactory, messages, workbook, stacksParser.getExportedVesselStacks())
                .addDataToVesselProfile(vesselProfile);
        dgParser.addDataToVesselProfile(vesselProfile);

        new TanksParser(stowbaseObjectFactory, messages, workbook).addDataToVesselProfile(vesselProfile);
        new ConstWgtsParser(stowbaseObjectFactory, messages, workbook).addDataToVesselProfile(vesselProfile);
        new StabilityParser(stowbaseObjectFactory, messages, workbook).addDataToVesselProfile(vesselProfile);
        new HydrostaticsParser(stowbaseObjectFactory, messages, workbook).addDataToVesselProfile(vesselProfile);
        new MetaCenterParser(stowbaseObjectFactory, messages, workbook).addDataToVesselProfile(vesselProfile);

        new HullWgtDistrParser(stowbaseObjectFactory, messages, workbook).addDataToVesselProfile(vesselProfile);
        new BonjeanParser(stowbaseObjectFactory, messages, workbook).addDataToVesselProfile(vesselProfile);
        new StressLimitsParser(stowbaseObjectFactory, messages, workbook).addDataToVesselProfile(vesselProfile);

        log.debug("Finished writing the vessel profile");
        writerExporter.flush("vessel.json");
    }

    /**
     * @param content
     * @return The content gzip compressed
     */
    public static byte[] compress(final byte[] content) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(content);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

}
