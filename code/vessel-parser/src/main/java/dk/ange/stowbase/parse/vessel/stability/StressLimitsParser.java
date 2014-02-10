package dk.ange.stowbase.parse.vessel.stability;

import static dk.ange.stowbase.parse.utils.Header.header;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.Reference;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.LinearInterpolation2d;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.Header;
import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse the "StressLimits" sheet.
 */
public class StressLimitsParser extends SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StressLimitsParser.class);

    private enum Type {
        BENDING_MIN, BENDING_MAX, TORSION_MIN, TORSION_MAX, SHEAR_MIN, SHEAR_MAX
    }

    private Collection<TableRow> tableRows;

    private Map<Header, Integer> keyMap;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public StressLimitsParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    private void parse() {
        final Sheet sheet = getSheetOptional("StressLimits");
        if (sheet == null) {
            return;
        }
        tableRows = new ArrayList<TableRow>();
        try {
            parseSheet(sheet);
        } catch (final ParseException e) {
            messages.addSheetWarning(sheet, e.getMessage());
        }
    }

    private void parseSheet(final Sheet sheet) {
        final Iterator<Row> rowIterator = sheet.rowIterator();

        final Row firstRow = rowIterator.next();
        keyMap = new HashMap<Header, Integer>();
        for (final Cell cell : firstRow) {
            keyMap.put(header(cellString(cell)), cell.getColumnIndex());
        }
        final int lengthColumn = Header.headerColumnMandatory(keyMap, "station (m)");
        final int bendingPosColumn = (keyMap.containsKey(header("Bending Torque positive (GNm)")) ? keyMap
                .get(header("Bending Torque positive (GNm)")) : -1);
        final int bendingNegColumn = keyMap.containsKey(header("Bending Torque negative (GNm)")) ? keyMap
                .get(header("Bending Torque negative (GNm)")) : -1;
        final int shearPosColumn = keyMap.containsKey(header("Shear Force positive (MN)")) ? keyMap
                .get(header("Shear Force positive (MN)")) : -1;
        final int shearNegColumn = keyMap.containsKey(header("Shear Force negative (MN)")) ? keyMap
                .get(header("Shear Force negative (MN)")) : -1;
        final int torsionPosColumn = keyMap.containsKey(header("Torsion Torque positive (MNm)")) ? keyMap
                .get(header("Torsion Torque positive (MNm)")) : -1;
        final int torsionNegColumn = keyMap.containsKey(header("Torsion Torque negative (MNm)")) ? keyMap
                .get(header("Torsion Torque negative (MNm)")) : -1;
        for (final Row row : new IterableIterator<Row>(rowIterator)) {
            try {
                parseRow(row, lengthColumn, bendingPosColumn, bendingNegColumn, shearPosColumn, shearNegColumn,
                        torsionPosColumn, torsionNegColumn);
            } catch (final Exception e) {
                log.debug("Error when parsing a stresslimits line", e);
                messages.addSheetWarning(sheet, "Error when parsing a stresslimits line: " + e.getMessage());
            }
        }
    }

    private void parseRow(final Row row, final int lengthColumn, final int bendingPosColumn,
            final int bendingNegColumn, final int shearPosColumn, final int shearNegColumn, final int torsionPosColumn,
            final int torsionNegColumn) {
        final TableRow tableRow = new TableRow();
        if (lengthColumn >= 0) {
            tableRow.lcg = readNumber(row, lengthColumn, 1);
            if (bendingPosColumn > -1) {
                tableRow.bending_max = readNumber(row, bendingPosColumn, 1000000000);
            }
            if (bendingNegColumn > -1) {
                tableRow.bending_min = readNumber(row, bendingNegColumn, 1000000000);
            }
            if (shearPosColumn > -1) {
                tableRow.shear_max = readNumber(row, shearPosColumn, 1000000);
            }
            if (shearNegColumn > -1) {
                tableRow.shear_min = readNumber(row, shearNegColumn, 1000000);
            }
            if (torsionPosColumn > -1) {
                tableRow.torsion_max = readNumber(row, torsionPosColumn, 1000000);
            }
            if (torsionNegColumn > -1) {
                tableRow.torsion_min = readNumber(row, torsionNegColumn, 1000000);
            }
            tableRows.add(tableRow);
        }
    }

    /**
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (tableRows == null) {
            return;
        }
        if (keyMap.containsKey(header("Bending Torque positive (GNm)"))) {
            vesselProfile.put("bending_minFunction", stresslimitFunction(Type.BENDING_MIN));
        }
        if (keyMap.containsKey(header("Bending Torque negative (GNm)"))) {
            vesselProfile.put("bending_maxFunction", stresslimitFunction(Type.BENDING_MAX));
        }
        if (keyMap.containsKey(header("Shear Force positive (MN)"))) {
            vesselProfile.put("shear_minFunction", stresslimitFunction(Type.SHEAR_MIN));
        }
        if (keyMap.containsKey(header("Shear Force negative (MN)"))) {
            vesselProfile.put("shear_maxFunction", stresslimitFunction(Type.SHEAR_MAX));
        }
        if (keyMap.containsKey(header("Torsion Torque positive (MNm)"))) {
            vesselProfile.put("torsion_minFunction", stresslimitFunction(Type.TORSION_MIN));
        }
        if (keyMap.containsKey(header("Torsion Torque negative (MNm)"))) {
            vesselProfile.put("torsion_maxFunction", stresslimitFunction(Type.TORSION_MAX));
        }
    }

    private Reference stresslimitFunction(final Type type) {
        String valuestring = "";
        switch (type) {
        case BENDING_MAX:
            valuestring = "bending_maximum";
            break;
        case BENDING_MIN:
            valuestring = "bending_minimum";
            break;
        case TORSION_MAX:
            valuestring = "torsion_maximum";
            break;
        case TORSION_MIN:
            valuestring = "torsion_minimum";
            break;
        case SHEAR_MAX:
            valuestring = "shear_maximum";
            break;
        case SHEAR_MIN:
            valuestring = "shear_minimum";
            break;
        }
        final LinearInterpolation2d stresslimitFunction = LinearInterpolation2d.create(stowbaseObjectFactory);
        stresslimitFunction.setInput1("lcg");
        stresslimitFunction.setInput2("draft");
        stresslimitFunction.setOutput(valuestring);
        final List<Double> lcg = new ArrayList<Double>(tableRows.size());
        final List<Double> stresslimit = new ArrayList<Double>(tableRows.size());
        for (final TableRow tableRow : tableRows) {
            switch (type) {
            case BENDING_MAX:
                stresslimit.add(tableRow.bending_max);
                break;
            case BENDING_MIN:
                stresslimit.add(tableRow.bending_min);
                break;
            case TORSION_MAX:
                stresslimit.add(tableRow.torsion_max);
                break;
            case TORSION_MIN:
                stresslimit.add(tableRow.torsion_min);
                break;
            case SHEAR_MAX:
                stresslimit.add(tableRow.shear_max);
                break;
            case SHEAR_MIN:
                stresslimit.add(tableRow.shear_min);
                break;
            }
            lcg.add(tableRow.lcg);
        }
        stresslimitFunction.setSamplePoints1(lcg);
        stresslimitFunction.setSamplePoints2(Arrays.asList(new Double[] { 0.0 }));
        stresslimitFunction.setSampleData(stresslimit);
        return stresslimitFunction.getReference();
    }

    private static final class TableRow {
        double lcg;

        double bending_min;

        double bending_max;

        double shear_min;

        double shear_max;

        double torsion_min;

        double torsion_max;

        @Override
        public String toString() {
            return "TableRow [lcg=" + lcg + ", bending_min=" + bending_min + ", bending_max=" + bending_max
                    + ", shear_min=" + shear_min + ", shear_max=" + shear_max + ", torsion_min=" + torsion_min
                    + ", torsion_max=" + torsion_max + "]";
        }
    }
}
