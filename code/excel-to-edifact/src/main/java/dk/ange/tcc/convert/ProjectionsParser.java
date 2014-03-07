package dk.ange.tcc.convert;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse "Projections"
 */
public final class ProjectionsParser extends SheetsParser {

    private static final String SHEET_NAME = "Projections";

    private final String vesselImo;

    private final Set<String> calls;

    private final String vesselName;

    private final References moves = new References();

    private final OutputStream coprarOutputStream;

    private String loadPort;

    private String voyageCode;

    private ProjectionsParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final String vesselImo, final Set<String> calls, final String vesselName,
            final OutputStream coprarOutputStream) {
        super(stowbaseObjectFactory, messages, workbook);
        this.vesselImo = vesselImo;
        this.calls = calls;
        this.vesselName = vesselName;
        this.coprarOutputStream = coprarOutputStream;
    }

    /**
     * Create. For use in JSON conversion.
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     * @param vesselImo
     * @param calls
     */
    public ProjectionsParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final String vesselImo, final Collection<String> calls) {
        this(stowbaseObjectFactory, messages, workbook, vesselImo, new HashSet<>(calls), "", null);
    }

    /**
     * Create. For use in COPRAR conversion.
     *
     * @param vesselImo
     * @param vesselName
     * @param coprarOutputstream
     * @param messages
     * @param workbook
     */
    public ProjectionsParser(final String vesselImo, final String vesselName, final OutputStream coprarOutputstream,
            final Messages messages, final Workbook workbook) {
        this(null, messages, workbook, vesselImo, null, vesselName, coprarOutputstream);
    }

    /**
     * Parse
     */
    public void parse() {
        final Sheet sheet = getSheetMandatory(SHEET_NAME);
        final CoprarExporter coprarExporter = coprarOutputStream != null ? new CoprarExporter(vesselImo, vesselName,
                coprarOutputStream) : null;
        int containerIdCounter = 0;
        final Random random_generator = new Random();
        for (int r = 1; r < sheet.getPhysicalNumberOfRows(); ++r) { // Skip first row
            final Row row = sheet.getRow(r);
            try {
                final String containerNumberAsString = cellString(row.getCell(0));
                if (containerNumberAsString == null || containerNumberAsString.length() == 0) {
                    continue; // Skip containers without container numbers
                }
                /**
                 * TODO: make an abstract parent class for the projections and the loadlist container builder and have
                 * the specialized container builders handle the whole generation.
                 */
                final int numberOfContainers = Integer.parseInt(cellString(row.getCell(0)));
                final double stdDevContainerWeight = Double.parseDouble(cellString(row.getCell(1)));
                final ContainerBuilder builder = new ContainerBuilder(vesselImo, "");
                builder.parseContainer(row, messages, sheet, calls);
                for (int i = 0; i < numberOfContainers; ++i) {
                    builder.setContainerId(String.format("TEMP%07d", containerIdCounter));
                    /**
                     * Generating containers with a user-defined standard deviation on the weight since real world cargo
                     * distributions have a weight spread - in the range 3 to 32 tons
                     */
                    long newWeight = Math.max(
                            Math.round(builder.getContainerWeight()) + 1000
                                    * Math.round(stdDevContainerWeight * random_generator.nextGaussian()), 3000);
                    newWeight = Math.min(newWeight, 32000);
                    builder.setContainerWeight((int) newWeight); // cast to int safe due to above max
                    builder.build(stowbaseObjectFactory, moves, coprarExporter);
                    ++containerIdCounter;
                }
                if (loadPort == null) {
                    loadPort = cellString(row.getCell(7));
                }
                if (voyageCode == null) {
                    voyageCode = cellString(row.getCell(3));
                }
            } catch (final Exception e) {
                throw new RuntimeException("Error was in row " + row.getRowNum(), e);
            }
        }
        if (coprarExporter != null) {
            coprarExporter.setLoadPort(loadPort);
            if (vesselImo != null) {
                coprarExporter.setVesselImo(vesselImo);
            }
            if (voyageCode == null) {
                throw new NullPointerException("voyageCode == null");
            }
            coprarExporter.setVoyageCode(voyageCode);
            coprarExporter.flush();
        }
    }

    /**
     * Adds moves to stowage
     *
     * @param stowage
     */
    public void addDataToStowage(final StowbaseObject stowage) {
        stowage.put("moves", moves);

    }

}
