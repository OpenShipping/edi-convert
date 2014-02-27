package dk.ange.stowbase.parse.vessel.tanks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.LinearInterpolation2d;
import org.stowbase.client.objects.Tank;

import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import com.google.common.collect.PeekingIterator;

import dk.ange.stowbase.parse.utils.Header;
import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SingleSheetParser;

/**
 * Parse the "VarTanks" sheet.
 */
public final class VarTanksParser extends SingleSheetParser {

    private final Map<String, XlsVarTank> varTanks = new HashMap<>();

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public VarTanksParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    @Override
    public String getSheetName() {
        return "VarTanks";
    }

    private void parse() {
        if (sheetFound()) {
            try {
                parseSheet();
            } catch (final ParseException e) {
                addSheetWarning(e.getMessage());
            }
        }
    }

    private void parseSheet() {
        final PeekingIterator<Row> rowIterator = Iterators.peekingIterator(sheet.rowIterator());
        for (final Row row : new IterableIterator<Row>(rowIterator)) {
            final Cell cell = row.getCell(0);
            final Header key = Header.header(cellString(cell));
            if (key == null || key.toString().length() == 0) {
                continue; // Skip blank headers
            }
            if (key.equals(Header.header("Description"))) {
                final String tankname = cellString(row.getCell(1));
                try {
                    parseTank(tankname, rowIterator);
                } catch (final Exception e) {
                    addSheetWarning("Error when parsing a vartank: " + e.getMessage());
                }
            } else {
                addSheetWarning("Did not expect header '" + key + "' in cell " + pos(cell));
            }
        }
    }

    private void parseTank(final String description, final PeekingIterator<Row> rowIterator) {
        List<Double> volumes = null;
        List<Double> lcgs = null;
        List<Double> vcgs = null;
        List<Double> tcgs = null;
        List<Double> fsms = null;
        while (rowIterator.hasNext()) {
            final Row row = rowIterator.peek();
            final Cell cell = row.getCell(0);
            final Header key = Header.header(cellString(cell));
            if (key == null || key.toString().isEmpty() || key.equals(Header.header("Description"))) {
                break; // Stop parsing tank block on blank line
            }
            rowIterator.next();
            if (key.equals(Header.header("Volume in m3"))) {
                volumes = readNewVolumes(row, volumes);
            } else if (key.equals(Header.header("LCG in m"))) {
                lcgs = readNewNumbers(row, volumes, lcgs);
            } else if (key.equals(Header.header("VCG in m"))) {
                vcgs = readNewNumbers(row, volumes, vcgs);
            } else if (key.equals(Header.header("TCG in m"))) {
                tcgs = readNewNumbers(row, volumes, tcgs);
            } else if (key.equals(Header.header("Max FSM in m4"))) {
                fsms = readNewNumbers(row, volumes, fsms);
            } else {
                addSheetWarning("Did not expect header '" + key + "' in cell " + pos(cell));
            }
        }
        if (varTanks.containsKey(description)) {
            addSheetWarning("VarTank with description '" + description + "' duplicated, the second version ignored");
            return;
        }
        varTanks.put(description, new XlsVarTank(description, volumes, lcgs, vcgs, tcgs, fsms));
    }

    private List<Double> readNewNumbers(final Row row, final List<Double> volumes, final List<Double> oldNumbers) {
        if (volumes == null) {
            addSheetWarning("Data in row " + (row.getRowNum() + 1) + " must come after volumes, ignored");
            return oldNumbers;
        }
        final List<Double> newNumbers = readNewNumbers(row, oldNumbers);
        if (newNumbers.size() != volumes.size()) {
            addSheetWarning("Data in row " + (row.getRowNum() + 1) + " should have same length as volumes, ignored");
            return oldNumbers;
        }
        return newNumbers;
    }

    private List<Double> readNewVolumes(final Row row, final List<Double> oldNumbers) {
        final List<Double> volumes = readNewNumbers(row, oldNumbers);
        if (volumes.size() < 2) {
            addSheetWarning("Less than two entries in volumes in row " + (row.getRowNum() + 1)
                    + ", this row will be ignored");
            return null;
        }
        if (!Ordering.natural().isOrdered(volumes)) {
            addSheetWarning("Volumes in row " + (row.getRowNum() + 1) + " are not sorted"
                    + ", this row will be ignored" + volumes);
            return null;
        }
        final double volume0 = volumes.get(0);
        if (volume0 != 0.0) {
            addSheetWarning("First tank volume in row " + (row.getRowNum() + 1) + " is not 0.0 but " + volume0
                    + ", a tank volumen of 0.0 m^3 and with FSM 0.0 m^4 is expected to avoid wrong FSM values for small fill %");
        }
        return volumes;
    }

    private List<Double> readNewNumbers(final Row row, final List<Double> oldNumbers) {
        if (oldNumbers != null) {
            addSheetWarning("Duplicate data in row " + (row.getRowNum() + 1) + " ignored");
            return oldNumbers;
        }
        return readNumbers(row);
    }

    private List<Double> readNumbers(final Row row) {
        final List<Double> numbers = new ArrayList<Double>();
        for (final Cell cell : row) {
            if (cell.getColumnIndex() == 0) {
                continue; // Skip first column
            }
            numbers.add(readNumber(row, cell.getColumnIndex(), 1));
        }
        return numbers;
    }

    /**
     * @param tank
     * @param description
     * @param lcg
     * @param vcg
     * @param tcg
     * @param fsm
     * @param capacityInM3
     */
    public void addDataToTank(final Tank tank, final String description, final double lcg, final double vcg,
            final double tcg, final double fsm, final double capacityInM3) {
        final XlsVarTank xlsVarTank;
        if (varTanks.containsKey(description)) {
            xlsVarTank = varTanks.get(description);
            xlsVarTank.dataIsUsed = true;
        } else {
            xlsVarTank = new XlsVarTank("fake", null, null, null, null, null);
        }

        {
            final LinearInterpolation2d function;
            if (xlsVarTank.lcgs == null) {
                function = constFunction(capacityInM3, lcg);
            } else {
                function = function(xlsVarTank.volumes, xlsVarTank.lcgs);
                final double last = last(xlsVarTank.lcgs);
                if (!Double.isNaN(lcg) && lcg != last) {
                    addSheetWarning("In VarTanks data for '" + xlsVarTank.description + "' the last value for LCG is "
                            + last + " while the data in the Tanks sheet is " + lcg);
                }
            }
            function.setOutput("lcg");
            tank.setLcgFunction(function);
        }

        {
            final LinearInterpolation2d function;
            if (xlsVarTank.vcgs == null) {
                function = constFunction(capacityInM3, vcg);
            } else {
                function = function(xlsVarTank.volumes, xlsVarTank.vcgs);
                final double last = last(xlsVarTank.vcgs);
                if (!Double.isNaN(vcg) && vcg != last) {
                    addSheetWarning("In VarTanks data for '" + xlsVarTank.description + "' the last value for VCG is "
                            + last + " while the data in the Tanks sheet is " + vcg);
                }
            }
            function.setOutput("vcg");
            tank.setVcgFunction(function);
        }

        {
            final LinearInterpolation2d function;
            if (xlsVarTank.tcgs == null) {
                function = constFunction(capacityInM3, tcg);
            } else {
                function = function(xlsVarTank.volumes, xlsVarTank.tcgs);
                final double last = last(xlsVarTank.tcgs);
                if (!Double.isNaN(tcg) && tcg != last) {
                    addSheetWarning("In VarTanks data for '" + xlsVarTank.description + "' the last value for TCG is "
                            + last + " while the data in the Tanks sheet is " + tcg);
                }
            }
            function.setOutput("tcg");
            tank.setTcgFunction(function);
        }

        {
            final LinearInterpolation2d function;
            if (xlsVarTank.fsms == null) {
                function = hatFunction(capacityInM3, fsm);
            } else {
                function = function(xlsVarTank.volumes, xlsVarTank.fsms);
                final double max = Collections.max(xlsVarTank.fsms);
                if (!Double.isNaN(fsm) && fsm != max) {
                    addSheetWarning("In VarTanks data for '" + xlsVarTank.description
                            + "' the highest value for FSM is " + max + " while the data in the Tanks sheet is " + fsm);
                }
            }
            function.setOutput("fsm");
            tank.setFsmFunction(function);
        }
    }

    private static <T> T last(final List<T> list) {
        return list.get(list.size() - 1);
    }

    private LinearInterpolation2d function(final List<Double> volumes, final List<Double> values) {
        final LinearInterpolation2d function = LinearInterpolation2d.create(stowbaseObjectFactory);
        function.setInput1("volume");
        function.setInput2("dummy");
        function.setSamplePoints1(volumes);
        function.setSamplePoints2(Arrays.asList(0.0));
        function.setSampleData(values);
        return function;
    }

    private LinearInterpolation2d constFunction(final double capacityInM3, final double value) {
        return function(Arrays.asList(0.0, capacityInM3), Arrays.asList(value, value));
    }

    private LinearInterpolation2d hatFunction(final double capacityInM3, final double value) {
        return function(Arrays.asList(0.0, 0.05 * capacityInM3, 0.95 * capacityInM3, capacityInM3),
                Arrays.asList(0.0, value, value, 0.0));
    }

    /**
     * A block from the XLS sheet for a single tank
     */
    private final static class XlsVarTank {
        String description;

        List<Double> volumes;

        List<Double> lcgs;

        List<Double> vcgs;

        List<Double> tcgs;

        List<Double> fsms;

        boolean dataIsUsed = false;

        XlsVarTank(final String description, final List<Double> volumes, final List<Double> lcgs,
                final List<Double> vcgs, final List<Double> tcgs, final List<Double> fsms) {
            this.description = description;
            this.volumes = volumes;
            this.lcgs = lcgs;
            this.vcgs = vcgs;
            this.tcgs = tcgs;
            this.fsms = fsms;
        }
    }

    /**
     * Checks that addDataToTank has been called on all parsed var tanks, if some has been missed it will be written to
     * the log
     */
    public void checkAllDataIsUsed() {
        for (final XlsVarTank varTank : varTanks.values()) {
            if (!varTank.dataIsUsed) {
                addSheetWarning("VarTank '" + varTank.description + "' was not mentioned in the Tanks sheet");
            }
        }
    }

}
