package dk.ange.stowbase.parse.vessel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.BRL;
import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.vessel.stacks.StackData;
import dk.ange.stowbase.parse.vessel.stacks.StackDataSheetsParser;

/**
 * Parse 'DG' sheet
 */
public class DgParser extends StackDataSheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DgParser.class);

    private final BaysMapping baysMapping;

    private final Map<BRL, StackData> data20;

    private final Map<BRL, StackData> data40;

    private HashMap<String, List<String>> bays2hold;

    private HashMap<String, List<String>> holdAcceptsImoClass;

    private References holds;

    /**
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     * @param baysMapping
     * @param data20
     * @param data40
     */
    public DgParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final BaysMapping baysMapping, final Map<BRL, StackData> data20,
            final Map<BRL, StackData> data40) {
        super(stowbaseObjectFactory, messages, workbook);
        log.debug("Construct");
        this.baysMapping = baysMapping;
        this.data20 = data20;
        this.data40 = data40;
        readImo();
    }

    private void readImo() {
        final Sheet sheetImo = getSheetOptionalWithOldName("DG", "IMO");
        if (sheetImo != null) {
            final HashSet<String> bayNames = new HashSet<String>(baysMapping.bayNames());
            parseImoData(sheetImo, bayNames);
        }
    }

    @SuppressWarnings("null")
    private void parseImoData(final Sheet sheet, final Set<String> feuBays) {
        boolean titleLine = false;
        String type = null;
        final List<String> bays = new ArrayList<String>();
        Map<BRL, StackData> stackData = null;
        String level = null;
        final String[] validLevelsArray = { "ABOVE", "BELOW" };
        final List<String> validLevels = Arrays.asList(validLevelsArray);
        // a map holdmap(bay) giving as output the hold for the input bay
        final HashMap<String, String> holdmap = new HashMap<String, String>();
        for (final Row row : new IterableIterator<Row>(sheet.rowIterator())) {
            log.trace("Line {}", row.getRowNum());
            final Cell cell0 = row.getCell(0);
            log.trace("cell0={}", cell0);
            if (cell0 == null) {
                continue;
            }
            final String cell0String = cellString(cell0);
            if (cell0String.startsWith("#")) {
                log.trace("TYPE  {}", cell0String);
                type = cell0String.toUpperCase();
                if (titleLine) {
                    throw new RuntimeException("titleLine==true in TYPE line");
                }
                titleLine = true;
                final String[] valid_headers = { "# HOLDS", "# IMO STACKS 20", "# IMO STACKS 40" };
                if (!Arrays.asList(valid_headers).contains(type)) {
                    throw new RuntimeException("IMO sheet had invalid header '" + type + "'");
                }
            } else if (titleLine) {
                log.trace("TITLE {}", cell0String);
                bays.clear();
                if ("# HOLDS".equals(type)) {
                    if (!"feubay".equalsIgnoreCase(cell0String)) {
                        throw new RuntimeException("Expected a row starting with 'FEUBAY' at " + pos(cell0)
                                + ", but got '" + cell0String + "'");
                    }
                    bays2hold = new HashMap<String, List<String>>();
                    holdAcceptsImoClass = new HashMap<String, List<String>>();
                } else if ("# IMO STACKS 20".equals(type) || "# IMO STACKS 40".equals(type)) {
                    if (type.endsWith("40")) {
                        stackData = data40;
                    } else {
                        stackData = data20;
                    }
                    level = cell0String.toUpperCase();
                    if (!validLevels.contains(level)) {
                        throw new RuntimeException("Invalid level '" + level + "'");
                    }

                } else {
                    throw new RuntimeException("Internal error: Unhandle type '" + type + "' in IMO sheet");
                }
                for (int cellIndex = 1; cellIndex < row.getLastCellNum(); ++cellIndex) {
                    final Cell cell = row.getCell(cellIndex);
                    final String bay = cellString(cell);
                    if (bay.length() > 0) {
                        if ("# HOLDS".equals(type) && !feuBays.contains(bay)) {
                            throw new RuntimeException("Hold data used undeclared feubay '" + bay + "'");
                        } else {
                            bays.add(bay);
                        }
                    }
                }
                titleLine = false;
            } else if (cell0String != null && cell0String.length() > 0) {
                log.trace("DATA  {}", cell0String);
                if ("# HOLDS".equals(type)) {
                    if (!("holds".equalsIgnoreCase(cell0String) || "ALLOWS 1.4S".equalsIgnoreCase(cell0String) || cell0String
                            .matches("^ALLOWS \\d(\\.[1-6])?$"))) {
                        throw new RuntimeException("Expected a row starting with 'HOLDS' or 'ALLOWS [imo class]' at "
                                + pos(cell0) + ", but got '" + cell0String + "'");
                    }
                    if (row.getLastCellNum() - 1 > feuBays.size()) {
                        throw new RuntimeException("Unexpected data in last cell in row " + row.getLastCellNum());
                    }
                    if ("holds".equalsIgnoreCase(cell0String)) {
                        for (int cellIndex = 1; cellIndex < row.getLastCellNum(); ++cellIndex) {
                            final Cell cell = row.getCell(cellIndex);
                            final String hold = cellString(cell);
                            if (hold != null && hold.length() > 0) {
                                log.trace("hold {} in bay {}", hold, bays.get(cellIndex - 1));
                                List<String> baysInHold = bays2hold.get(hold);
                                if (baysInHold == null) {
                                    baysInHold = new ArrayList<String>();
                                    bays2hold.put(hold, baysInHold);
                                }
                                baysInHold.add(bays.get(cellIndex - 1));
                                holdmap.put(bays.get(cellIndex - 1), hold);
                            }
                        }
                    } else {
                        if (cell0String.matches("ALLOWS\\s\\d(\\.\\d[A-S]?)?")) {
                            String imoClass = cell0String;
                            imoClass = imoClass.replaceFirst("ALLOWS\\s", "");
                            for (int cellIndex = 1; cellIndex < row.getLastCellNum(); ++cellIndex) {
                                final Cell cell = row.getCell(cellIndex);
                                final String accepts = cellString(cell);
                                if ("Y".equals(accepts)) {
                                    final String hold = holdmap.get(bays.get(cellIndex - 1));
                                    log.trace("hold {} accepts {}", hold, imoClass);
                                    if (hold != null && hold.length() > 0) {
                                        List<String> imoClassesInHold = holdAcceptsImoClass.get(hold);
                                        if (imoClassesInHold == null) {
                                            imoClassesInHold = new ArrayList<String>();
                                            holdAcceptsImoClass.put(hold, imoClassesInHold);
                                        }
                                        imoClassesInHold.add(imoClass);
                                    }
                                }
                            }
                        }
                    }
                } else if ("# IMO STACKS 20".equals(type) || "# IMO STACKS 40".equals(type)) {
                    if (row.getLastCellNum() - 1 > bays.size()) {
                        throw new RuntimeException("Unexpected data in last cell in row " + row.getLastCellNum());
                    }
                    final String r = cell0String;
                    if (r == null || r.length() == 0) {
                        throw new RuntimeException("No row number in " + type + " at " + pos(cell0));
                    }
                    for (int cellIndex = 1; cellIndex < row.getLastCellNum(); ++cellIndex) {
                        final Cell cell = row.getCell(cellIndex);
                        final String cellContents = cellString(cell);
                        final boolean imoStackForbidden = cellContents != null && cellContents.equalsIgnoreCase("Z");
                        if (!imoStackForbidden) {
                            continue;
                        }
                        final String bay = bays.get(cellIndex - 1);
                        final BRL brl = new BRL(bay, r, level);
                        final StackData data = stackData.get(brl);
                        if (data == null) {
                            throw new RuntimeException("No stack at " + brl.toString() + " at " + pos(cell));
                        }
                        data.imoForbidden = true;
                    }
                } else {
                    throw new RuntimeException("Internal error: Unhandle type '" + type + "' in IMO sheet");
                }
            }
        }
    }

    /**
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        export();
        if (holds != null) {
            vesselProfile.put("holds", holds);
        }
    }

    private void export() {
        if (bays2hold != null) {
            holds = new References();
            for (final String hold : bays2hold.keySet()) {
                final StowbaseObject so = stowbaseObjectFactory.create("hold");
                so.put("feubays", bays2hold.get(hold));
                if (!(holdAcceptsImoClass.get(hold) == null)) {
                    so.put("acceptsImo", holdAcceptsImoClass.get(hold));
                }
                holds.add(so.getReference());
            }
        }
    }

}
