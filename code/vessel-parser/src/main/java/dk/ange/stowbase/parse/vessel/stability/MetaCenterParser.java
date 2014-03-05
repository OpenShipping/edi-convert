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
 * Parse the "MetaCenter" sheet.
 */
public class MetaCenterParser extends SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MetaCenterParser.class);

    private Map<Double, Map<Double, Double>> metaCenterMatrix;

    private Map<Integer, Double> keyMap;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public MetaCenterParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    private void parse() {
        final Sheet sheet = getSheetOptionalWithOldName("MetaCenter", "MetaCentre");
        if (sheet == null) {
            return;
        }
        metaCenterMatrix = new HashMap<>();
        try {
            parseSheet(sheet);
        } catch (final ParseException e) {
            messages.addSheetWarning(sheet, e.getMessage());
        }
    }

    private void parseSheet(final Sheet sheet) {
        final Iterator<Row> rowIterator = sheet.rowIterator();
        final Row firstRow = rowIterator.next();
        keyMap = new HashMap<>();
        metaCenterMatrix = new HashMap<>();
        for (final Cell cell : firstRow) {
            try {
                final String cell0String = cellString(cell);
                if (!cell0String.startsWith("#") && cell0String.length() != 0) {
                    keyMap.put(cell.getColumnIndex(), Double.parseDouble(cell0String));
                }
            } catch (final Exception e) {
                log.debug("Error when parsing a MetaCenter trim header #" + cell.getColumnIndex(), e);
                messages.addSheetWarning(sheet, "Error when parsing a metacenter trim header #" + cell.toString()
                        + " Error: " + e.getMessage());
            }
        }
        for (final Row row : new IterableIterator<>(rowIterator)) {
            try {
                parseRow(row);
            } catch (final Exception e) {
                log.debug("Error when parsing a metacenter line", e);
                messages.addSheetWarning(sheet, "Error when parsing a metacenter line: " + e.getMessage());
            }
        }
    }

    private void parseRow(final Row row) {
        final Map<Double, Double> metacenterRow = new HashMap<>();
        for (final Cell cell : row) {
            final String cell0String = cellString(cell);
            if (!cell0String.startsWith("#")) {
                if (cell.getColumnIndex() != 0) {
                    metacenterRow.put(keyMap.get(cell.getColumnIndex()), readNumber(row, cell.getColumnIndex(), 1));
                }
            }
        }
        if (metacenterRow.size() > 0) {
            final Double draft = readNumber(row, 0, 1);
            // log.debug("row size: " + bonjean_row.size() + "Draft: " + draft);
            metaCenterMatrix.put(draft, metacenterRow);
        }
    }

    /**
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (metaCenterMatrix == null) {
            return;
        }
        vesselProfile.put("metacentreCurve", metacentreCurve());
    }

    private Reference metacentreCurve() {
        final LinearInterpolation2d bonjeanCurve = LinearInterpolation2d.create(stowbaseObjectFactory);
        bonjeanCurve.setInput2("draftInM");
        bonjeanCurve.setInput1("trimInM");
        bonjeanCurve.setOutput("metaCentreInMAboveKeel");
        final List<Double> drafts = new ArrayList<>();
        final List<Double> trims = new ArrayList<>();
        final List<Double> metacentres = new ArrayList<>();
        drafts.addAll(metaCenterMatrix.keySet());
        trims.addAll(metaCenterMatrix.get(drafts.get(0)).keySet());
        for (final Double draft : drafts) {
            for (final Double trim : trims) {
                metacentres.add(metaCenterMatrix.get(draft).get(trim));
            }
        }
        bonjeanCurve.setSamplePoints2(drafts);
        bonjeanCurve.setSamplePoints1(trims);
        bonjeanCurve.setSampleData(metacentres);
        return bonjeanCurve.getReference();
    }

}
