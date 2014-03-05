package dk.ange.stowbase.parse.vessel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselLid;
import org.stowbase.client.objects.VesselProfile;
import org.stowbase.client.objects.VesselStack;

import dk.ange.stowbase.parse.utils.BRL;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.utils.SheetsParser;

/**
 * Parse 'Bays' sheet
 */
public class LidsParser extends SheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LidsParser.class);

    private final Map<BRL, VesselStack> exportedVesselStacks;

    private Map<LidKey, LidData> lids;

    /**
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     * @param exportedVesselStacks
     */
    public LidsParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final Map<BRL, VesselStack> exportedVesselStacks) {
        super(stowbaseObjectFactory, messages, workbook);
        log.debug("Construct");
        this.exportedVesselStacks = exportedVesselStacks;
        parse();
    }

    private void parse() {
        final Sheet sheetLids = getSheetOptional("Lids");
        if (sheetLids != null) {
            lids = new TreeMap<>();
            new LidsSectionParser().readSheet(sheetLids);
        }
        log.debug("lids = {}", lids);
    }

    /**
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        if (lids != null) {
            final Collection<VesselLid> vesselLids = new ArrayList<>();
            for (final LidKey lidKey : lids.keySet()) {
                final LidData lidData = lids.get(lidKey);
                final VesselLid lid = VesselLid.create(stowbaseObjectFactory);
                // Above
                final List<VesselStack> aboveDeck = new ArrayList<>();
                Collections.sort(lidData.aboveDeck);
                for (final BRL stackBrl : lidData.aboveDeck) {
                    aboveDeck.add(exportedVesselStacks.get(stackBrl));
                }
                lid.setVesselStacksOnTopLid(aboveDeck);
                // Below
                final List<VesselStack> belowDeck = new ArrayList<>();
                Collections.sort(lidData.belowDeck);
                for (final BRL stackBrl : lidData.belowDeck) {
                    belowDeck.add(exportedVesselStacks.get(stackBrl));
                }
                // belowDeck is sometime empty, this is a hack for creating compartments for macro-stowage
                lid.setVesselStacksBeneathLid(belowDeck);
                lid.put("name", lidKey.toString());
                vesselLids.add(lid);
            }
            vesselProfile.setVesselLids(vesselLids);
        }
    }

    private static class LidKey implements Comparable<LidKey> {
        final String bay;

        final String name;

        LidKey(final String bay, final String name) {
            this.bay = bay;
            this.name = name;
        }

        @Override
        public String toString() {
            return bay + "-" + name;
        }

        @Override
        public int compareTo(final LidKey o) {
            int cmp;
            cmp = BRL.intStringsCompare(bay, o.bay);
            if (cmp != 0) {
                return cmp;
            }
            cmp = name.compareTo(o.name);
            if (cmp != 0) {
                return cmp;
            }
            return 0;
        }
    }

    private static class LidData {

        final List<BRL> aboveDeck = new ArrayList<>();

        final List<BRL> belowDeck = new ArrayList<>();

    }

    private class LidsSectionParser extends BrlSectionParser {

        @Override
        protected void handleDataItem(final String sectionType, final BRL brl, final String cellString) {
            if (!sectionType.equals("# STACK LIDS")) {
                throw new ParseException("Unknown section type '" + sectionType + "'");
            }
            if (!exportedVesselStacks.containsKey(brl)) {
                throw new ParseException("Lid defined for unknown stack " + brl);
            }
            final String[] lidsNames = cellString.split(",");
            for (final String lidName : lidsNames) {
                final LidKey lidKey = new LidKey(brl.bay, lidName);
                if (!lids.containsKey(lidKey)) {
                    lids.put(lidKey, new LidData());
                }
                final LidData lidData = lids.get(lidKey);
                if (brl.level.equals("ABOVE")) {
                    lidData.aboveDeck.add(brl);
                } else {
                    lidData.belowDeck.add(brl);
                }
            }
        }

    }

}
