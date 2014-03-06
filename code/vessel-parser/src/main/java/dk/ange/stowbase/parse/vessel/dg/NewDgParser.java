package dk.ange.stowbase.parse.vessel.dg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselProfile;

import dk.ange.stowbase.parse.utils.Level;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SheetWarner;
import dk.ange.stowbase.parse.utils.SingleSheetParser;
import dk.ange.stowbase.parse.vessel.BaysMapping;

/**
 * Parse 'DG' sheet
 */
public class NewDgParser extends SingleSheetParser implements VesselProfileDataAdder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NewDgParser.class);

    private final BaysMapping baysMapping;

    private final Map<String, DgRules> dgRulesMap = new HashMap<>();

    /**
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     * @param baysMapping
     */
    public NewDgParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final BaysMapping baysMapping) {
        super(stowbaseObjectFactory, messages, workbook);
        log.debug("Construct");
        this.baysMapping = baysMapping;
        parse();
    }

    @Override
    public String getSheetName() {
        return "DG";
    }

    private void parse() {
        if (sheetFound()) {
            parseSheet();
        } else {
            // Remove this Exception when the OldDgParser is removed
            throw new RuntimeException(
                    "Name of DG sheet in new format must be 'DG', the old name 'IMO' can not be used any more.");
        }
    }

    private void parseSheet() {
        final Set<String> unknownPermissions = new HashSet<>();
        final Set<String> knownClasses = new LinkedHashSet<>(Arrays.asList("1.1-1.6", "1.4S", "2.1", "2.2", "2.3", "3",
                "3(B)", "3(C)", "4.1", "4.2", "4.3", "4.3(A)", "4.3(B)", "4.3(C)", "4.3(D)", "5.1", "5.2", "6.1",
                "6.1(A)", "6.1(B)", "6.1(C)", "6.1(D)", "8", "8(A)", "8(B)", "8(C)", "8(D)", "9"));
        final Set<String> unknownClasses = new HashSet<>();
        new CargoSpaceSectionParser(baysMapping, this) {
            private boolean hasWarned = false;

            @Override
            protected void handleDataItem(final String sectionType, final Level level, final String cargoSpace,
                    final String class_, final String permissionString) {
                if (!knownClasses.contains(class_) && !unknownClasses.contains(class_)) {
                    addSheetWarning("Unknown DG class '" + class_ + "'");
                    unknownClasses.add(class_);
                    if (!hasWarned) {
                        addSheetWarning("Known DG classes: " + knownClasses);
                        hasWarned = true;
                    }
                }

                if (!dgRulesMap.containsKey(cargoSpace)) {
                    dgRulesMap.put(cargoSpace, new DgRules());
                }
                final DgRules dgRules = dgRulesMap.get(cargoSpace);

                switch (permissionString) {
                case "P":
                    switch (level) {
                    case ABOVE:
                        dgRules.abovePermitted.add(class_);
                        break;
                    case BELOW:
                        dgRules.belowPermitted.add(class_);
                        break;
                    }
                    break;
                case "N":
                    switch (level) {
                    case ABOVE:
                        dgRules.aboveNotPermitted.add(class_);
                        break;
                    case BELOW:
                        dgRules.belowNotPermitted.add(class_);
                        break;
                    }
                    break;
                default:
                    if (!unknownPermissions.contains(permissionString)) {
                        unknownPermissions.add(permissionString);
                        addSheetWarning("Unknown permission '" + permissionString + "', it will be ignored."
                                + " Known permissions are [P, N]");
                    }
                    break;
                }
            }
        }.readSheet(sheet);
    }

    @Override
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        final References holds = new References();
        final Map<String, List<String>> cargoSpaces = baysMapping.cargoSpaces();
        for (final Entry<String, List<String>> entry : cargoSpaces.entrySet()) {
            final String cargoSpace = entry.getKey();
            final List<String> bays = entry.getValue();
            final StowbaseObject hold = stowbaseObjectFactory.create("hold");
            hold.put("name", cargoSpace);
            hold.put("feubays", bays);
            final DgRules dgRules = dgRulesMap.get(cargoSpace);
            if (dgRules != null) {
                putIfNotNull(hold, "dgAbovePermitted", dgRules.abovePermitted);
                putIfNotNull(hold, "dgAboveNotPermitted", dgRules.aboveNotPermitted);
                putIfNotNull(hold, "acceptsImo", dgRules.belowPermitted); // Strange naming caused by history
                putIfNotNull(hold, "dgBelowNotPermitted", dgRules.belowNotPermitted);
            }
            holds.add(hold.getReference());
        }

        if (!holds.isEmpty()) {
            vesselProfile.put("holds", holds);
        }
    }

    private void putIfNotNull(final StowbaseObject hold, final String key, final List<String> list) {
        if (list != null) {
            hold.put(key, list);
        }
    }

    private static class DgRules {
        List<String> abovePermitted = new ArrayList<>();

        List<String> aboveNotPermitted = new ArrayList<>();

        List<String> belowPermitted = new ArrayList<>();

        List<String> belowNotPermitted = new ArrayList<>();
    }

    /**
     * Parse a sheet where all the sections are Cargo Space data
     */
    public static abstract class CargoSpaceSectionParser extends SectionParser {
        private final Set<String> cargoSpaces;

        private final SheetWarner sheet;

        /**
         * @param baysMapping
         *            used for getting set of known cargo spaces
         * @param sheet
         *            sheet warnings are registered to
         */
        public CargoSpaceSectionParser(final BaysMapping baysMapping, final SheetWarner sheet) {
            this.cargoSpaces = baysMapping.cargoSpaces().keySet();
            this.sheet = sheet;
        }

        @Override
        protected final void handleDataItem(final String sectionType, final String sectionTag, final String rowTitle,
                final String columnTitle, final String cellString) {
            {
                final Level level = Level.valueOf(sectionTag);
                final String cargoSpace = columnTitle;
                if (!cargoSpaces.contains(cargoSpace)) {
                    sheet.addSheetWarning("Unknown cargo space '" + cargoSpace
                            + "', cargo spaces are defined in the 'Bays' sheet");
                    return;
                }
                // Log message example: # PERMITTED CLASSES: BELOW-3 <- 8(B), N
                log.trace("{}: {}-{} <- {}, {}", new Object[] { sectionType, level, cargoSpace, rowTitle, cellString });
                handleDataItem(sectionType, level, cargoSpace, rowTitle, cellString);
            }
        }

        /**
         * The action to take on all data
         *
         * @param sectionType
         * @param level
         * @param cargoSpace
         * @param rowTitle
         * @param cellString
         */
        protected abstract void handleDataItem(String sectionType, Level level, String cargoSpace, String rowTitle,
                String cellString);
    }

}
