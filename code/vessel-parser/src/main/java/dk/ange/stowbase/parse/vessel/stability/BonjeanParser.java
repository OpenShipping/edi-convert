package dk.ange.stowbase.parse.vessel.stability;

import java.util.ArrayList;
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

import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse the "Bonjean" sheet.
 */

public class BonjeanParser extends SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BonjeanParser.class);

    private static final String SHEET_NAME = "Bonjean";

    private Map<Double, Map<Double, Double>> bonjean_matrix;

    private Map<Integer, Double> keyMap;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public BonjeanParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    private void parse() {
        final Sheet sheet = getSheetOptional(SHEET_NAME);
        if (sheet == null) {
            return;
        }
        bonjean_matrix = new HashMap<>();

        try {
            parseSheet(sheet);
        } catch (final ParseException e) {
            messages.addSheetWarning(SHEET_NAME, e.getMessage());
        }
    }

    private void parseSheet(final Sheet sheet) {
        final Iterator<Row> rowIterator = sheet.rowIterator();
        final Row firstRow = rowIterator.next();
        keyMap = new HashMap<>();
        for (final Cell cell : firstRow) {
            try {
                final String cell0String = cellString(cell);
                if (!cell0String.startsWith("#")) {
                    keyMap.put(cell.getColumnIndex(), Double.parseDouble(cell0String));
                }
            } catch (final Exception e) {
                log.debug("Error when parsing a bonjean bay header #" + cell.getColumnIndex(), e);
                messages.addSheetWarning(SHEET_NAME, "Error when parsing a bonjean bay header #" + cell.getColumnIndex()
                        + " Error: " + e.getMessage());
            }
        }
        for (final Row row : new IterableIterator<>(rowIterator)) {
            try {
                parseRow(row);
            } catch (final Exception e) {
                log.debug("Error when parsing a bonjean line", e);
                messages.addSheetWarning(SHEET_NAME, "Error when parsing a bonjean line: " + e.getMessage());
            }
        }
    }

    private void parseRow(final Row row) {
        final Map<Double, Double> bonjean_row = new HashMap<>();

        for (final Cell cell : row) {
            final String cell0String = cellString(cell);
            if (!cell0String.startsWith("#")) {
                if (cell.getColumnIndex() != 0) {
                    bonjean_row.put(keyMap.get(cell.getColumnIndex()), readNumber(row, cell.getColumnIndex(), 1));
                }
            }
        }
        if (bonjean_row.size() > 0) {
            final Double draft = readNumber(row, 0, 1);
            // log.debug("row size: " + bonjean_row.size() + "Draft: " + draft);
            bonjean_matrix.put(draft, bonjean_row);
        }
    }

    /**
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (bonjean_matrix == null) {
            return;
        }
        vesselProfile.put("bonjeanCurve", bonjeanCurve());
    }

    private Reference bonjeanCurve() {
        final LinearInterpolation2d bonjeanCurve = LinearInterpolation2d.create(stowbaseObjectFactory);
        bonjeanCurve.setInput2("draft");
        bonjeanCurve.setInput1("lcg");
        bonjeanCurve.setOutput("bonjeanArea");
        final List<Double> drafts = new ArrayList<>();
        final List<Double> lcgs = new ArrayList<>();
        final List<Double> bonjeanAreas = new ArrayList<>();
        drafts.addAll(bonjean_matrix.keySet());
        lcgs.addAll(bonjean_matrix.get(drafts.get(0)).keySet());
        for (final Double draft : drafts) {
            for (final Double lcg : lcgs) {
                bonjeanAreas.add(bonjean_matrix.get(draft).get(lcg));
            }
        }
        bonjeanCurve.setSamplePoints2(drafts);
        bonjeanCurve.setSamplePoints1(lcgs);
        bonjeanCurve.setSampleData(bonjeanAreas);
        return bonjeanCurve.getReference();
    }

}
