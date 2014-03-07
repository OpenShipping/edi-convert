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
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.Reference;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.LinearInterpolation2d;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.Header;
import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SingleSheetParser;

/**
 * Parse the "Hydrostatics" sheet.
 */
public class HydrostaticsParser extends SingleSheetParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HydrostaticsParser.class);

    private Collection<TableRow> tableRows;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public HydrostaticsParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    @Override
    public String getSheetName() {
        return "Hydrostatics";
    }

    private void parse() {
        if (!sheetFound()) {
            return;
        }
        tableRows = new ArrayList<>();
        try {
            parseSheet();
        } catch (final ParseException e) {
            addSheetWarning(e.getMessage());
        }
    }

    private void parseSheet() {
        final Iterator<Row> rowIterator = sheet.rowIterator();

        final Row firstRow = rowIterator.next();
        final Map<Header, Integer> keyMap = new HashMap<>();
        for (final Cell cell : firstRow) {
            keyMap.put(header(cellString(cell)), cell.getColumnIndex());
        }
        final int draftColumn = Header.headerColumnMandatory(keyMap, "Draft in m");
        final int displacementColumn = Header.headerColumnMandatory(keyMap, "Displacement in ton");
        final int lcbColumn = Header.headerColumnMandatory(keyMap, "LCB in m");
        final int mctColumn = Header.headerColumnMandatory(keyMap, "MCT in ton m / cm");

        for (final Row row : new IterableIterator<>(rowIterator)) {
            try {
                parseRow(row, draftColumn, displacementColumn, lcbColumn, mctColumn);
            } catch (final Exception e) {
                log.debug("Error when parsing a hydrostatics line", e);
                addSheetWarning("Error when parsing a hydrostatics line: " + e.getMessage());
            }
        }
    }

    private void parseRow(final Row row, final int draftColumn, final int displacementColumn, final int lcbColumn,
            final int mctColumn) {
        final TableRow tableRow = new TableRow();
        tableRow.draft = readNumber(row, draftColumn, 1);
        tableRow.displacement = readNumber(row, displacementColumn, 1000);
        tableRow.lcb = readNumber(row, lcbColumn, 1);
        tableRow.mct = readNumber(row, mctColumn, 100000);
        tableRows.add(tableRow);
    }

    /**
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (tableRows == null) {
            return;
        }
        vesselProfile.put("draftFunction", draftFunction());
        vesselProfile.put("trimFunction", trimFunction());
    }

    private Reference draftFunction() {
        final LinearInterpolation2d draftFunction = LinearInterpolation2d.create(stowbaseObjectFactory);
        draftFunction.setInput1("displacement");
        draftFunction.setInput2("lcg");
        draftFunction.setOutput("draft");
        final List<Double> displacements = new ArrayList<>(tableRows.size());
        final List<Double> drafts = new ArrayList<>(tableRows.size());
        for (final TableRow tableRow : tableRows) {
            displacements.add(tableRow.displacement);
            drafts.add(tableRow.draft);
        }
        draftFunction.setSamplePoints1(displacements);
        draftFunction.setSamplePoints2(Arrays.asList(new Double[] { -1000.0, 1000.0 }));
        drafts.addAll(drafts);
        draftFunction.setSampleData(drafts);
        return draftFunction.getReference();
    }

    private Reference trimFunction() {
        final LinearInterpolation2d draftFunction = LinearInterpolation2d.create(stowbaseObjectFactory);
        draftFunction.setInput1("displacement");
        draftFunction.setInput2("lcg");
        draftFunction.setOutput("trim");
        final List<Double> displacements = new ArrayList<>(tableRows.size());
        final List<Double> lcgs = new ArrayList<>(tableRows.size());
        final Map<Double, TableRow> displacementLookup = new HashMap<>(tableRows.size());
        double lcbMin = Double.POSITIVE_INFINITY;
        double lcbMax = Double.NEGATIVE_INFINITY;
        for (final TableRow tableRow : tableRows) {
            displacements.add(tableRow.displacement);
            lcbMin = Math.min(lcbMin, tableRow.lcb);
            lcbMax = Math.max(lcbMax, tableRow.lcb);
            displacementLookup.put(tableRow.displacement, tableRow);
        }
        lcgs.add(lcbMin - 100);
        lcgs.add(lcbMin);
        lcgs.add(lcbMax);
        lcgs.add(lcbMax + 100);
        draftFunction.setSamplePoints1(displacements);
        draftFunction.setSamplePoints2(lcgs);
        final List<Double> data = new ArrayList<>(displacements.size() * lcgs.size());
        for (final Double lcg : lcgs) {
            for (final Double displacement : displacements) {
                final TableRow tableRow = displacementLookup.get(displacement);
                data.add(displacement * (tableRow.lcb - lcg) / tableRow.mct);
            }
        }
        draftFunction.setSampleData(data);
        return draftFunction.getReference();
    }

    private static final class TableRow {
        double draft;

        double displacement;

        double lcb;

        double mct;

        @Override
        public String toString() {
            return "TableRow [displacement=" + displacement + ", draft=" + draft + ", lcb=" + lcb + ", mct=" + mct
                    + "]";
        }
    }

}
