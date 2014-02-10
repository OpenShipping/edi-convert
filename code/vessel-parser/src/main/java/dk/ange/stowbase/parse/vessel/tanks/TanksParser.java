package dk.ange.stowbase.parse.vessel.tanks;

import static dk.ange.stowbase.parse.utils.Header.header;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.Header;
import dk.ange.stowbase.parse.utils.IterableIterator;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse the "Tanks" sheet.
 */
public final class TanksParser extends SheetsParser {

    private static final String SHEET_NAME = "Tanks";

    private final VarTanksParser varTanksParser;

    private final Collection<VarTank> varTanks = new ArrayList<>();

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

    private void parse() {
        final Sheet sheet = getSheetOptional(SHEET_NAME);
        if (sheet == null) {
            return;
        }
        try {
            parseSheet(sheet);
        } catch (final ParseException e) {
            messages.addSheetWarning(SHEET_NAME, e.getMessage());
        }
    }

    private void parseSheet(final Sheet sheet) {
        final Iterator<Row> rowIterator = sheet.rowIterator();
        // Check the headers
        final Row firstRow = rowIterator.next();
        final Map<Header, Integer> keyMap = new HashMap<Header, Integer>();
        for (final Cell cell : firstRow) {
            keyMap.put(header(cellString(cell)), cell.getColumnIndex());
        }
        final int descriptionColumn = Header.headerColumnMadatory(keyMap, "Description");
        final int capacityVolColumn = Header.headerColumnMadatory(keyMap, "Capacity in m3");
        final int capacityMassColumn = Header.headerColumnMadatory(keyMap, "Capacity in ton");
        final int densityColumn = Header.headerColumnMadatory(keyMap, "Density in ton/m3");
        final int foreEndColumn = Header.headerColumnOptional(keyMap, "Fore End in m");
        final int aftEndColumn = Header.headerColumnOptional(keyMap, "Aft End in m");
        // These headers are also mandatory, but putting in values are optional. If one value is present they must all
        // be present, and if none are there the tank must be a vartank and thus presented in the vartanks sheet.
        final int lcgColumn = Header.headerColumnMadatory(keyMap, "LCG in m");
        final int vcgColumn = Header.headerColumnMadatory(keyMap, "VCG in m");
        final int tcgColumn = Header.headerColumnMadatory(keyMap, "TCG in m");
        final int fsmColumn = Header.headerColumnMadatory(keyMap, "Max FSM in m4");
        // Optional column (should this be changed to mandatory?)
        final int groupColumn = Header.headerColumnOptional(keyMap, "Tank Group");
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
                messages.addSheetWarning(SHEET_NAME, "Error when parsing a tank line: " + e.getMessage());
            }
        }
    }

    private void parseRow(final Row row, final String description, final int capacityVolColumn,
            final int capacityMassColumn, final int densityColumn, final int foreEndColumn, final int aftEndColumn,
            final int lcgColumn, final int vcgColumn, final int tcgColumn, final int fsmColumn, final int groupColumn) {
        final Cell lcgCell = row.getCell(lcgColumn);
        final String lcgString = cellString(lcgCell);
        final Cell vcgCell = row.getCell(vcgColumn);
        final String vcgString = cellString(vcgCell);
        final Cell tcgCell = row.getCell(tcgColumn);
        final String tcgString = cellString(tcgCell);
        final Cell fsmCell = row.getCell(fsmColumn);
        final String fsmString = cellString(fsmCell);
        final boolean vartank = (lcgCell == null || lcgString.length() == 0)
                && (vcgCell == null || vcgString.length() == 0) && (tcgCell == null || tcgString.length() == 0)
                && (fsmCell == null || fsmString.length() == 0);

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
        double VC;
        double MC;
        final double density = readNumber(row, densityColumn, 1000);
        if (!haveVC && !haveMC) {
            throw new ParseException("Tank: '" + description + "' could not be parsed, need capcacity. ");
        } else if (haveVC && !haveMC) {
            VC = readNumber(row, capacityVolColumn, 1);
            MC = VC * density;
        } else if (haveMC && !haveVC) {
            MC = readNumber(row, capacityMassColumn, 1000);
            VC = MC / density;
        } else {
            MC = readNumber(row, capacityMassColumn, 1000);
            VC = readNumber(row, capacityVolColumn, 1);
        }
        double FoEn = 0.0;
        if (foreEndColumn != -1) {
            FoEn = readNumber(row, foreEndColumn, 1);
        }
        double AftEn = 0.0;
        if (aftEndColumn != -1) {
            AftEn = readNumber(row, aftEndColumn, 1);
        }
        final String tankgroup;
        if (groupColumn != -1) {
            tankgroup = cellString(row.getCell(groupColumn));
            validateTankGroup(tankgroup, description);
        } else {
            tankgroup = null;
        }
        if (!vartank) {
            final Tank tank = new Tank();
            tank.description = description;
            // We do not necessarily need both, but can estimate to other from the first via the density.
            tank.volCapacity = VC;
            tank.massCapacity = MC;
            tank.density = density;
            tank.foreEnd = FoEn;
            tank.aftEnd = AftEn;
            tank.lcg = readNumber(row, lcgColumn, 1);
            tank.vcg = readNumber(row, vcgColumn, 1);
            tank.tcg = readNumber(row, tcgColumn, 1);
            tank.fsm = readNumber(row, fsmColumn, 1);
            tank.group = tankgroup;
            varTanks.add(convertTankToVartank(tank));

        } else {
            final VarTank variabletank = varTanksParser.getVartank(description);
            if (variabletank != null) {
                variabletank.volCapacity = VC;
                variabletank.massCapacity = MC;
                variabletank.density = density;
                variabletank.foreEnd = FoEn;
                variabletank.aftEnd = AftEn;
                variabletank.group = tankgroup;
                varTanks.add(variabletank);
            } else {
                throw new ParseException("Could not find vartank description for Tank: " + description
                        + " Check that it exists in the varTanks sheet");
            }
        }
    }

    private VarTank convertTankToVartank(final Tank tank) {
        final VarTank vartank = new VarTank(stowbaseObjectFactory);
        vartank.description = tank.description;
        vartank.volCapacity = tank.volCapacity;
        vartank.massCapacity = tank.massCapacity;
        vartank.density = tank.density;
        vartank.foreEnd = tank.foreEnd;
        vartank.aftEnd = tank.aftEnd;
        vartank.group = tank.group;

        vartank.lcgfunction = new HashMap<Double, Double>();
        vartank.lcgfunction.put(0.0, tank.lcg);
        vartank.lcgfunction.put(tank.volCapacity, tank.lcg);

        vartank.vcgfunction = new HashMap<Double, Double>();
        vartank.vcgfunction.put(0.0, tank.vcg);
        vartank.vcgfunction.put(tank.volCapacity, tank.vcg);

        vartank.tcgfunction = new HashMap<Double, Double>();
        vartank.tcgfunction.put(0.0, tank.tcg);
        vartank.tcgfunction.put(tank.volCapacity, tank.tcg);

        vartank.fsmfunction = new HashMap<Double, Double>();
        vartank.fsmfunction.put(0.0, 0.0);
        vartank.fsmfunction.put(0.05 * tank.volCapacity, tank.fsm);
        vartank.fsmfunction.put(0.95 * tank.volCapacity, tank.fsm);
        vartank.fsmfunction.put(tank.volCapacity, 0.0);

        return vartank;
    }

    /**
     * Add the parser result to the vessel profile
     *
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (varTanks == null) {
            return;
        }
        final References tankReferences = new References();
        for (final VarTank vartank : varTanks) {
            tankReferences.add(vartank.toStowbaseObject().getReference());
        }
        vesselProfile.put("tanks", tankReferences);
    }

    private void validateTankGroup(final String group, final String description) throws ParseException {
        if (group != null && !group.isEmpty()) {
            // nothing for now
        } else {
            throw new ParseException("invalid tank group: '" + group + "' for tank " + description);
        }
    }

    private final class Tank {
        String description;

        String group;

        double massCapacity;

        double volCapacity;

        double foreEnd;

        double aftEnd;

        double density;

        double lcg;

        double vcg;

        double tcg;

        double fsm;
    }

}
