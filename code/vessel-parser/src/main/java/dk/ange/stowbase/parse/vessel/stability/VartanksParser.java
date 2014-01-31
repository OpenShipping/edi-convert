package dk.ange.stowbase.parse.vessel.stability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;

import dk.ange.stowbase.parse.utils.Header;
import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse the "VarTanks" sheet.
 */
public final class VartanksParser extends SheetsParser {

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VartanksParser.class);

    private static final String SHEET_NAME = "VarTanks";

    private Collection<varTank> vartanks;

    /**
     * returns the parsed variable tanks
     * 
     * @param description
     * @return Collection<varTank>
     */

    public varTank getVartank(final String description) {
        if (vartanks.isEmpty()) {
            return null;
        } else {
            for (final varTank tank : vartanks) {
                if (tank.description == description) {
                    return tank;
                }
            }
        }
        return null;
    }

    /**
     * Construct and parse
     * 
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public VartanksParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    private void parse() {
        final Sheet sheet = getSheetOptional(SHEET_NAME);
        if (sheet == null) {
            return;
        }
        vartanks = new ArrayList<varTank>();
        try {
            parseSheet(sheet);
        } catch (final ParseException e) {
            messages.addSheetWarning(SHEET_NAME, e.getMessage());
        }
    }

    private void parseSheet(final Sheet sheet) {
        final Iterator<Row> rowIterator = sheet.rowIterator();
        for (final Row row : new IterableIterator<Row>(rowIterator)) {
            final Header key = Header.header(cellString(row.getCell(0)));
            if (key == null || key.toString().length() == 0 || key.toString().startsWith("#")) {
                continue; // Skip unnamed tanks or tanks starting with # in the name
            }
            if (key.equals(Header.header("Description"))) {
                final String tankname = cellString(row.getCell(1));
                try {
                    parsetank(tankname, rowIterator);
                } catch (final Exception e) {
                    messages.addSheetWarning(SHEET_NAME, "Error when parsing a vartank: " + e.getMessage());
                }
            }
        }
    }

    private void parsetank(final String description, final Iterator<Row> rowIterator) {
        List<Double> volumes = new ArrayList<Double>();
        List<Double> lcgs = new ArrayList<Double>();
        List<Double> vcgs = new ArrayList<Double>();
        List<Double> tcgs = new ArrayList<Double>();
        List<Double> fsms = new ArrayList<Double>();
        for (int counter = 0; counter < 5; counter++) {
            final Row row = rowIterator.next();
            final Header rowkey = Header.header(cellString(row.getCell(0)));
            if (rowkey.equals(Header.header("Volume in m3"))) {
                volumes = valuelist(row);
            } else if (rowkey.equals(Header.header("LCG in m"))) {
                lcgs = valuelist(row);
            } else if (rowkey.equals(Header.header("VCG in m"))) {
                vcgs = valuelist(row);
            } else if (rowkey.equals(Header.header("TCG in m"))) {
                tcgs = valuelist(row);
            } else if (rowkey.equals(Header.header("Max FSM in m4"))) {
                fsms = valuelist(row);
            }
        }
        createTank(description, volumes, lcgs, vcgs, tcgs, fsms);
    }

    private List<Double> valuelist(final Row row) {
        final List<Double> rv = new ArrayList<Double>();
        for (final Cell cell : row) {
            if (cell.getColumnIndex() > 0) {
                rv.add(readNumber(row, cell.getColumnIndex(), 1));
            }
        }
        return rv;
    }

    private void createTank(final String description, final List<Double> volumes, final List<Double> lcgs,
            final List<Double> vcgs, final List<Double> tcgs, final List<Double> fsms) {
        final varTank vartank = new varTank(stowbaseObjectFactory);
        vartank.description = description;
        // We add the lcgs first
        if (volumes.size() == lcgs.size()) {
            final Map<Double, Double> map = new HashMap<Double, Double>();
            for (int i = 0; i < volumes.size(); i++) {
                map.put(volumes.get(i), lcgs.get(i));
            }
            vartank.lcgfunction = map;
        } else {
            throw new ParseException("Could not match volumes to lcgs for tank: " + description + " volumes.size():"
                    + volumes.size() + "!=volumes.size():" + lcgs.size());
        }
        // We then add the vcgs
        if (volumes.size() == vcgs.size()) {
            final Map<Double, Double> map = new HashMap<Double, Double>();
            for (int i = 0; i < volumes.size(); i++) {
                map.put(volumes.get(i), vcgs.get(i));
            }
            vartank.vcgfunction = map;
        } else {
            throw new ParseException("Could not match volumes to vcgs for tank: " + description + " volumes.size():"
                    + volumes.size() + "!=volumes.size():" + vcgs.size());
        }
        // We then add the tcgs
        if (volumes.size() == tcgs.size()) {
            final Map<Double, Double> map = new HashMap<Double, Double>();
            for (int i = 0; i < volumes.size(); i++) {
                map.put(volumes.get(i), tcgs.get(i));
            }
            vartank.tcgfunction = map;
        } else {
            throw new ParseException("Could not match volumes to tcgs for tank: " + description + " volumes.size():"
                    + volumes.size() + "!=volumes.size():" + tcgs.size());
        }
        // We add the fsms
        if (volumes.size() == fsms.size()) {
            final Map<Double, Double> map = new HashMap<Double, Double>();
            for (int i = 0; i < volumes.size(); i++) {
                map.put(volumes.get(i), fsms.get(i));
            }
            vartank.fsmfunction = map;
        } else {
            throw new ParseException("Could not match volumes to fsms for tank: " + description + " volumes.size():"
                    + volumes.size() + "!=volumes.size():" + fsms.size());
        }
        vartanks.add(vartank);
    }

}
