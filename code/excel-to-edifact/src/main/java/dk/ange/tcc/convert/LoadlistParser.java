package dk.ange.tcc.convert;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.lang.Math;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;

import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse "Load list"
 */
public final class LoadlistParser extends SheetsParser {

    /**
     * Type of EdiFact message
     */
    public enum EdiFactType {
        /**
         * UNDEFINED - if using Json export
         */
        UNDEFINED,
        /**
         * BAPLIE
         */
        BAPLIE,
        /**
         * COARRI
         */
        COARRI,
        /**
         * CORAR
         */
        COPRAR
    }

    private static final String SHEET_NAME = "Load list";

    private final String vesselImo;

    private final Set<String> calls;

    private final String vesselName;

    private final References moves = new References();

    private final OutputStream ediFactOutputStream;

    private boolean isProjections;

    private String loadPort;

    private String voyageCode;

    private EdiFactType ediFactType;

    private final Random randomGenerator = new Random();

    private final int stdDevContainerWeight = 0; // kg
    // private final int stdDevContainerWeight = 2000; // kg  // disabled Gaussian spread of load list weights
    private final int minContainerWeight = 4000; // kg
    private final int maxContainerWeight = 32000; // kg


    private LoadlistParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final String vesselImo, final Set<String> calls, final String vesselName,
            final OutputStream ediFactOutputStream, final EdiFactType ediFactType) {
        super(stowbaseObjectFactory, messages, workbook);
        this.vesselImo = vesselImo;
        this.calls = calls;
        this.vesselName = vesselName;
        this.ediFactOutputStream = ediFactOutputStream;
        this.isProjections = false;
        this.ediFactType = ediFactType;
    }

    /**
     * Create. For use in JSON conversion.
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     * @param vesselImo
     * @param calls
     * @param ediFactType  BAPLIE or COPRAR
     */
    public LoadlistParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final String vesselImo, final Collection<String> calls, final EdiFactType ediFactType) {
        this(stowbaseObjectFactory, messages, workbook, vesselImo, new HashSet<>(calls), "", null, ediFactType);
    }

    /**
     * Create. For use in EDIFACTconversion.
     *
     * @param vesselImo
     * @param vesselName
     * @param ediFactOutputstream
     * @param messages
     * @param workbook
     * @param ediFactType  BAPLIE or COPRAR
     */
    public LoadlistParser(final String vesselImo, final String vesselName, final OutputStream ediFactOutputstream,
            final Messages messages, final Workbook workbook, final EdiFactType ediFactType) {
        this(null, messages, workbook, vesselImo, null, vesselName, ediFactOutputstream, ediFactType);
    }

    /**
     * Parse
     */
    public void parse() {
        // See user documentation at
        // convert/grails-app/views/shared/_loadlistInstructionsTemplate.gsp
        // for name and position of fields

        this.isProjections = false;

        final Sheet sheet = getSheetMandatory(SHEET_NAME);

        // get first row, first column to figure out if sheet is plain CORAR or PROJECTIONS
        final Row firstRow = sheet.getRow(0);
        if (firstRow == null) {
            throw new RuntimeException("Error in sheet '" + SHEET_NAME + "' no rows found, sheet is empty");
        }
        String sheetTypeIndicator = cellString(firstRow.getCell(0)); // 0 CONTAINER_ID or CONTAINER_COUNT
        if (sheetTypeIndicator == null || sheetTypeIndicator.length() == 0) {
            throw new RuntimeException("Error in sheet '" + SHEET_NAME
                        + "' first cell is empty, expected value 'CONTAINER_ID' or 'CONTAINER_COUNT'");
        }
        if (sheetTypeIndicator.equals("CONTAINER_ID")) {
            this.isProjections = false;
        } else if (sheetTypeIndicator.equals("CONTAINER_COUNT")) {
            this.isProjections = true;
        } else {
            throw new RuntimeException("Error in sheet '" + SHEET_NAME
                    + "' first cell value is unknown, expected value 'CONTAINER_ID' or 'CONTAINER_COUNT',"
                    + " got '" + sheetTypeIndicator + "'");
        }

        // setting correct EDIFACT exporter
        EdiFactExporter ediFactExporter = null;
        if (ediFactOutputStream != null && this.ediFactType == EdiFactType.BAPLIE) {
            ediFactExporter = new BaplieExporter(vesselImo, vesselName, ediFactOutputStream);
        }  else if (ediFactOutputStream != null && this.ediFactType == EdiFactType.COARRI) {
            ediFactExporter = new CoarriExporter(vesselImo, vesselName, ediFactOutputStream);
        } else if (ediFactOutputStream != null && this.ediFactType == EdiFactType.COPRAR) {
            ediFactExporter = new CoprarExporter(vesselImo, vesselName, ediFactOutputStream);
        }

        // start parsing the sheet
        for (int r = 1; r < sheet.getPhysicalNumberOfRows(); ++r) { // Skip first row, content ignored
            final Row row = sheet.getRow(r);
            if (row == null) { // check for inconsistency,
                // seen in Excel sheets generated from LibreOffice 4.0.4.2 where deleting rows did not set consistent sheet.getPhysicalNumberOfRows()
                continue;
            }
            try {
                String containerId = "";
                Integer containerCount = 0;

                if (this.isProjections) { // projections - expand container list
                    final String containerNumberAsString = cellString(row.getCell(0)); // A  0 CONTAINER_COUNT
                    if (containerNumberAsString == null || containerNumberAsString.length() == 0) {
                        messages.addSheetWarning(SHEET_NAME, "row " + row.getRowNum() + " ignoring loadlist row without CONTAINER_COUNT");
                        continue; // Skip containers without container count
                    }
                    containerCount = Integer.parseInt(cellString(row.getCell(0)));
                    if (containerCount == null || containerCount < 1 || containerCount > 9999) {
                        throw new RuntimeException("Projections: expected a number 1 <= n <= 9999 in field CONTAINER_COUNT, got '" + containerCount + "'");
                    }
                } else {  // plain load list
                    containerId = cellString(row.getCell(0));  // A  0 CONTAINER_ID
                    if (containerId == null || containerId.length() == 0) {
                        messages.addSheetWarning(SHEET_NAME, "row " + row.getRowNum() + " ignoring loadlist row without CONTAINER_ID");
                        continue; // Skip containers without container ids
                    }
                    containerCount = 1;
                }

                int containerIdCounter = this.randomGenerator.nextInt(9990000);  // allows for additional 9999 generated containers
                final ContainerBuilder builder = new ContainerBuilder(vesselImo, containerId);
                builder.parseContainer(row, messages, sheet.getSheetName(), calls);
                for(int i=0; i < containerCount; ++i){
                    if (this.isProjections) {
                        builder.setContainerId(String.format("PROJ%07d", containerIdCounter));
                        if (this.stdDevContainerWeight > 0 &&  containerCount > 1) {
                            /**
                             * Generating containers with a fixed standard deviation on the weight
                             * since real world cargo distributions have a weight spread
                             */
                            long newWeight = builder.getContainerWeight() + Math.round(this.stdDevContainerWeight * this.randomGenerator.nextGaussian());
                            newWeight = Math.max(newWeight, this.minContainerWeight);
                            newWeight = Math.min(newWeight, this.maxContainerWeight);
                            builder.setContainerWeight((int)newWeight); // cast to int safe due to above max
                        }
                    }
                    builder.build(stowbaseObjectFactory, moves, ediFactExporter);
                    ++containerIdCounter;
                }

                // builder.build(stowbaseObjectFactory, moves, coprarExporter);
                final String rowLoadPort = cellString(row.getCell(2));
                if (loadPort == null) {
                    loadPort = rowLoadPort; // C  2 POL OK
                }
                if (ediFactType == EdiFactType.COPRAR && !loadPort.equals(rowLoadPort)) {
                    messages.addSheetWarning(SHEET_NAME, "row " + row.getRowNum()
                            + ": COPRAR containers must have same load port, expected '"
                            + loadPort + "', got '" + rowLoadPort + "'");
                }
                if (voyageCode == null) {
                    voyageCode = cellString(row.getCell(1)); // B  1 VOYAGE_NO
                }
            } catch (final Exception e) {
                messages.addSheetWarning(SHEET_NAME, "row " + row.getRowNum() + " error: " + e.getMessage());
                throw new RuntimeException("Error in sheet '" + SHEET_NAME + "' row " + row.getRowNum(), e);
            }
        }
        if (ediFactExporter != null) {
            ediFactExporter.setLoadPort(loadPort);
            if (vesselImo != null) {
                ediFactExporter.setVesselImo(vesselImo);
            }
            if (voyageCode == null) {
                throw new NullPointerException("voyageCode == null");
            }
            ediFactExporter.setVoyageCode(voyageCode);
            ediFactExporter.flush();
        }
    }

    /**
     * Add collected data to stowage object
     * @param stowage
     */
    public void addDataToStowage(final StowbaseObject stowage) {
        stowage.put("moves", moves);

    }

}
