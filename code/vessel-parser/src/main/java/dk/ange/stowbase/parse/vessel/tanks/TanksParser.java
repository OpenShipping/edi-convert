package dk.ange.stowbase.parse.vessel.tanks;

import static dk.ange.stowbase.parse.utils.Header.header;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.Tank;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.Header;
import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SingleSheetParser;

/**
 * Parse the "Tanks" sheet.
 */
public final class TanksParser extends SingleSheetParser {

    private final VarTanksParser varTanksParser;

    private final Collection<XlsTank> tanks = new ArrayList<>();

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public TanksParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        varTanksParser = new VarTanksParser(stowbaseObjectFactory, messages, workbook);
        parse();
    }

    @Override
    public String getSheetName() {
        return "Tanks";
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
        final Iterator<Row> rowIterator = sheet.rowIterator();
        // Check the headers
        final Row firstRow = rowIterator.next();
        final Map<Header, Integer> keyMap = new HashMap<Header, Integer>();
        for (final Cell cell : firstRow) {
            keyMap.put(header(cellString(cell)), cell.getColumnIndex());
        }
        final int descriptionColumn = Header.headerColumnMandatory(keyMap, "Description");
        final int groupColumn = Header.headerColumnMandatory(keyMap, "Tank Group");
        final int capacityVolColumn = Header.headerColumnMandatory(keyMap, "Capacity in m3");
        final int capacityMassColumn = Header.headerColumnMandatory(keyMap, "Capacity in ton");
        final int densityColumn = Header.headerColumnMandatory(keyMap, "Density in ton/m3");
        final int foreEndColumn = Header.headerColumnMandatory(keyMap, "Fore End in m");
        final int aftEndColumn = Header.headerColumnMandatory(keyMap, "Aft End in m");
        final int lcgColumn = Header.headerColumnMandatory(keyMap, "LCG in m");
        final int vcgColumn = Header.headerColumnMandatory(keyMap, "VCG in m");
        final int tcgColumn = Header.headerColumnMandatory(keyMap, "TCG in m");
        final int fsmColumn = Header.headerColumnMandatory(keyMap, "Max FSM in m4");
        // Read all data lines
        for (final Row row : new IterableIterator<Row>(rowIterator)) {
            final String description = cellString(row.getCell(descriptionColumn));
            if (description == null || description.length() == 0 || description.startsWith("#")) {
                continue; // Skip unnamed tanks or tanks starting with # in the name
            }
            try {
                parseRow(row, description, capacityVolColumn, capacityMassColumn, densityColumn, foreEndColumn,
                        aftEndColumn, lcgColumn, vcgColumn, tcgColumn, fsmColumn, groupColumn);
            } catch (final Exception e) {
                addSheetWarning("Error when parsing a tank line: " + e.getMessage());
            }
        }
    }

    private void parseRow(final Row row, final String description, final int capacityVolColumn,
            final int capacityMassColumn, final int densityColumn, final int foreEndColumn, final int aftEndColumn,
            final int lcgColumn, final int vcgColumn, final int tcgColumn, final int fsmColumn, final int groupColumn) {
        final XlsTank tank = new XlsTank();
        tank.description = description;
        tank.group = cellString(row.getCell(groupColumn));
        validateTankGroup(tank.group, description);
        tank.density = readNumber(row, densityColumn, 1000);
        extractCapacity(row, description, capacityVolColumn, capacityMassColumn, tank);
        tank.foreEnd = readNumber(row, foreEndColumn, 1);
        tank.aftEnd = readNumber(row, aftEndColumn, 1);
        tank.lcg = readOptionalNumber(row, lcgColumn, 1);
        tank.vcg = readOptionalNumber(row, vcgColumn, 1);
        tank.tcg = readOptionalNumber(row, tcgColumn, 1);
        tank.fsm = readOptionalNumber(row, fsmColumn, 1);
        tanks.add(tank);
    }

    // FIXME Capacity in two (possibly inconsistent) fields should be handled better
    /**
     * tank.density must be set before this method is called
     */
    private static void extractCapacity(final Row row, final String description, final int capacityVolColumn,
            final int capacityMassColumn, final XlsTank tank) {
        boolean haveMC = true;
        final Cell MCcell = row.getCell(capacityMassColumn);
        final String MCstring = cellString(MCcell);
        boolean haveVC = true;
        final Cell VCcell = row.getCell(capacityVolColumn);
        final String VCstring = cellString(VCcell);
        if (MCcell == null || MCstring.length() == 0) {
            haveMC = false;
        }
        if (VCcell == null || VCstring.length() == 0) {
            haveVC = false;
        }
        if (!haveVC && !haveMC) {
            throw new ParseException("Tank: '" + description + "' could not be parsed, need capcacity. ");
        } else if (haveVC && !haveMC) {
            tank.volCapacity = readNumber(row, capacityVolColumn, 1);
            tank.massCapacity = tank.volCapacity * tank.density;
        } else if (haveMC && !haveVC) {
            tank.massCapacity = readNumber(row, capacityMassColumn, 1000);
            tank.volCapacity = tank.massCapacity / tank.density;
        } else {
            tank.massCapacity = readNumber(row, capacityMassColumn, 1000);
            tank.volCapacity = readNumber(row, capacityVolColumn, 1);
        }
    }

    private static void validateTankGroup(final String group, final String description) throws ParseException {
        if (group == null || group.isEmpty()) {
            throw new ParseException("Invalid tank group: '" + group + "' for tank '" + description + "'");
        }
    }

    /**
     * Add the parser result to the vessel profile
     *
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (sheetFound()) {
            final References tankReferences = new References();
            for (final XlsTank xlsTank : tanks) {
                final Tank tank = Tank.create(stowbaseObjectFactory);
                tank.setDescription(xlsTank.description);
                tank.setGroup(xlsTank.group);
                tank.setCapacityInKg(xlsTank.massCapacity);
                tank.setCapacityInM3(xlsTank.volCapacity);
                tank.setDensityInKgPrM3(xlsTank.density);
                tank.setAftEndInM(xlsTank.aftEnd);
                tank.setForeEndInM(xlsTank.foreEnd);
                varTanksParser.addDataToTank(tank, xlsTank.description, xlsTank.lcg, xlsTank.vcg, xlsTank.tcg,
                        xlsTank.fsm, xlsTank.volCapacity);
                tankReferences.add(tank.getReference());
            }
            vesselProfile.put("tanks", tankReferences);
            varTanksParser.checkAllDataIsUsed();
        }
    }

    /**
     * A line from the XLS sheet
     */
    private final static class XlsTank {
        String description;

        String group;

        double massCapacity;

        double volCapacity;

        double density;

        double aftEnd;

        double foreEnd;

        double lcg;

        double vcg;

        double tcg;

        double fsm;
    }

}
