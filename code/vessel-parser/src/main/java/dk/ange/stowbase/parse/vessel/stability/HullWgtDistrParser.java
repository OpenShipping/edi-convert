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
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse the "HullWgtDistr" sheet.
 */

public class HullWgtDistrParser extends SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HullWgtDistrParser.class);

    private List<BlockData> hullweight_blocks;

    private Map<Integer, String> keyMap;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public HullWgtDistrParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    private void parse() {
        final Sheet sheet = getSheetOptionalWithOldName("HullWgtDistr", "Hullweight");
        if (sheet == null) {
            return;
        }
        try {
            parseSheet(sheet);
        } catch (final ParseException e) {
            messages.addSheetWarning(sheet, e.getMessage());
        }
    }

    private void parseSheet(final Sheet sheet) {
        hullweight_blocks = new ArrayList<BlockData>();
        final Iterator<Row> rowIterator = sheet.rowIterator();
        final Row firstRow = rowIterator.next();
        keyMap = new HashMap<Integer, String>();
        for (final Cell cell : firstRow) {
            final String cell0String = cellString(cell);
            if (!cell0String.startsWith("#")) {
                keyMap.put(cell.getColumnIndex(), cell0String);
            }
        }

        for (final Row row : new IterableIterator<Row>(rowIterator)) {
            try {
                parseRow(row);
            } catch (final Exception e) {
                log.debug("Error when parsing a Hullweight line", e);
                messages.addSheetWarning(sheet, "Error when parsing a hullweight line: " + e.getMessage());
            }
        }
    }

    private void parseRow(final Row row) {

        double aftlcg = Double.NaN;
        double forelcg = Double.NaN;
        double aftdensity = Double.NaN;
        double foredensity = Double.NaN;
        for (final Cell cell : row) {
            final String cell0String = cellString(cell);
            if (!cell0String.startsWith("#")) {
                if (cell.getColumnIndex() == 0) {
                    aftlcg = readNumber(row, cell.getColumnIndex(), 1);
                } else if (cell.getColumnIndex() == 1) {
                    forelcg = readNumber(row, cell.getColumnIndex(), 1);
                } else if (cell.getColumnIndex() == 2) {
                    aftdensity = readNumber(row, cell.getColumnIndex(), 1000);
                } else if (cell.getColumnIndex() == 3) {
                    foredensity = readNumber(row, cell.getColumnIndex(), 1000);
                }
            }
        }
        if (!Double.isNaN(aftlcg) && !Double.isNaN(forelcg) && !Double.isNaN(aftdensity) && !Double.isNaN(foredensity)) {
            final BlockData blockData = new BlockData();
            blockData.aftLcg = aftlcg;
            blockData.foreLcg = forelcg;
            blockData.aftDensity = aftdensity;
            blockData.foreDensity = foredensity;
            blockData.tcg = 0;
            hullweight_blocks.add(blockData);
        }
    }

    /**
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (hullweight_blocks == null) {
            return;
        } else {
            final References blockReferences = new References();

            for (final BlockData hullblock : hullweight_blocks) {
                blockReferences.add(hullblock.toStowbaseObject(stowbaseObjectFactory).getReference());
            }
            vesselProfile.put("hullweightBlocks", blockReferences);
        }
    }

    private static final class BlockData {
        double aftLcg;

        double foreLcg;

        double aftDensity;

        double foreDensity;

        double tcg;

        @Override
        public String toString() {
            return "Block [Aftlcg=" + aftLcg + ", foreLcg=" + foreLcg + ", aftDensity=" + aftDensity + ", foreDensity="
                    + foreDensity + ", tcg=" + tcg + "]";
        }

        StowbaseObject toStowbaseObject(final StowbaseObjectFactory stowbaseObjectFactory) {
            final StowbaseObject block = stowbaseObjectFactory.create("block");
            block.put("aftLcgInM", aftLcg);
            block.put("foreLcgInM", foreLcg);
            block.put("aftDensityInKgPrM", aftDensity);
            block.put("foreDensityInKgPrM", foreDensity);
            block.put("tcgInM", tcg);
            return block;
        }
    }
}
