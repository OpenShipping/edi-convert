package dk.ange.stowbase.parse.vessel.stacks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.VesselProfile;
import org.stowbase.client.objects.VesselStack;
import org.stowbase.client.objects.VesselStackSupport;

import com.google.common.collect.Iterables;

import dk.ange.stowbase.parse.utils.BRL;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.ParseException;
import dk.ange.stowbase.parse.vessel.BaysMapping;
import dk.ange.stowbase.parse.vessel.BaysMapping.TwentyForeAftForty;
import dk.ange.stowbase.parse.vessel.VesselSheetParser.LongitudinalPositiveDirection;

/**
 * Parse stacks
 */
public class StacksParser extends StackDataSheetsParser {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StacksParser.class);

    private final LongitudinalPositiveDirection longitudinalPositiveDirection;

    private final BaysMapping baysMapping;

    private final Map<BRL, StackData> data20;

    private final Map<BRL, StackData> data40;

    private final boolean fileHasPos2040;

    private final Map<BRL, VesselStack> exportedVesselStacks = new HashMap<>();

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     * @param longitudinalPositiveDirection
     * @param baysMapping
     */
    public StacksParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook, final LongitudinalPositiveDirection longitudinalPositiveDirection,
            final BaysMapping baysMapping) {
        super(stowbaseObjectFactory, messages, workbook);
        this.longitudinalPositiveDirection = longitudinalPositiveDirection;
        this.baysMapping = baysMapping;
        data20 = readTier20();
        data40 = readTier40();
        readSlots45();
        readReef20();
        readReef40();
        readWgt2040();
        readHeight2040();
        fileHasPos2040 = readPos2040();
    }

    private Map<BRL, StackData> readTier20() {
        final Sheet sheetTier20 = getSheetMandatory("Tier20");
        final Map<BRL, StackData> data = new HashMap<>();
        readStacksData(sheetTier20, data, TierAction.INSTANCE, true);
        log.debug("data20 = {}", data);
        return Collections.unmodifiableMap(data);
    }

    private Map<BRL, StackData> readTier40() {
        final Sheet sheetTier40 = getSheetMandatory("Tier40");
        final Map<BRL, StackData> data = new HashMap<>();
        readStacksData(sheetTier40, data, TierAction.INSTANCE, true);
        log.debug("data40 = {}", data);
        return Collections.unmodifiableMap(data);
    }

    private void readWgt2040() {
        final Sheet sheetWgt20 = getSheetOptionalWithOldName("Wgt20", "StackWeight20");
        final Sheet sheetWgt40 = getSheetOptionalWithOldName("Wgt40", "StackWeight40");
        if (sheetWgt20 != null && sheetWgt40 != null) {
            readStacksData(sheetWgt20, data20, WeightAction.INSTANCE);
            readStacksData(sheetWgt40, data40, WeightAction.INSTANCE);
        } else if (sheetWgt20 == null && sheetWgt40 == null) {
            // Do nothing
        } else {
            throw new ParseException("Only one of the StackWeight sheets exists");
        }
        log.debug("data20 = {}", data20);
        log.debug("data40 = {}", data40);
    }

    private void readHeight2040() {
        final Sheet sheetHeight20 = getSheetOptionalWithOldName("Height20", "StackHeight20");
        final Sheet sheetHeight40 = getSheetOptionalWithOldName("Height40", "StackHeight40");
        if (sheetHeight20 != null && sheetHeight40 != null) {
            readStacksData(sheetHeight20, data20, HeightAction.INSTANCE);
            readStacksData(sheetHeight40, data40, HeightAction.INSTANCE);
        } else if (sheetHeight20 == null && sheetHeight40 == null) {
            // Leave out
        } else {
            throw new ParseException("Only one of the StackHeight sheets exists");
        }
        log.trace("data20 = {}", data20);
        log.trace("data40 = {}", data40);
    }

    /**
     * Read the longitudinal and transverse positions of the stacks.
     *
     * @return true if the data sheets are in the file
     */
    private boolean readPos2040() {
        final Sheet sheetPos20 = getSheetOptionalWithOldName("Pos20", "Position20");
        final Sheet sheetPos40 = getSheetOptionalWithOldName("Pos40", "Position40");
        log.trace("sheetPosition20: {}", sheetPos20);
        final boolean sheetsInFile;
        if (sheetPos20 != null && sheetPos40 != null) {
            sheetsInFile = true;
            readStacksData(sheetPos20, data20, PositionAction.INSTANCE);
            readStacksData(sheetPos40, data40, PositionAction.INSTANCE);
        } else if (sheetPos20 == null && sheetPos40 == null) {
            sheetsInFile = false;
        } else {
            throw new ParseException("Only one of the Position sheets exists");
        }
        log.debug("data20 = {}", data20);
        log.debug("data40 = {}", data40);
        return sheetsInFile;
    }

    private void readReef20() {
        final Sheet sheetReefer20 = getSheetOptionalWithOldName("Reef20", "ReeferSlots20");
        if (sheetReefer20 != null) {
            readStacksData(sheetReefer20, data20, ReeferAction.INSTANCE);
        }
        log.debug("data20 = {}", data20);
    }

    private void readReef40() {
        final Sheet sheetReefer40 = getSheetOptionalWithOldName("Reef40", "ReeferSlots40");
        if (sheetReefer40 != null) {
            readStacksData(sheetReefer40, data40, ReeferAction.INSTANCE);
        }
        log.debug("data40 = {}", data40);
    }

    private void readSlots45() {
        final Sheet sheetSlots45 = getSheetOptional("Slots45");
        if (sheetSlots45 != null) {
            readStacksData(sheetSlots45, data40, FourtyFiveAction.INSTANCE);
        }
        log.debug("data40 = {}", data40);
    }

    /**
     * Add the parser result to the vessel profile
     *
     * @param vesselProfile
     */
    public void addDataToVesselProfile(final VesselProfile vesselProfile) {
        final SortedMap<BRL, Collection<BRL>> combinedToBayName = new TreeMap<>();
        for (final BRL brl : Iterables.concat(data20.keySet(), data40.keySet())) {
            final String bayName = baysMapping.bayName(brl.bay);
            if (bayName == null) {
                throw new RuntimeException("bayName==null for brl=" + brl);
            }
            final BRL bayNameBrl = brl.withOtherBay(bayName);
            if (!combinedToBayName.containsKey(bayNameBrl)) {
                combinedToBayName.put(bayNameBrl, new ArrayList<BRL>());
            }
            combinedToBayName.get(bayNameBrl).add(brl);
        }
        log.debug("combinedToBayName = {}", combinedToBayName);

        final Collection<VesselStack> vesselStacks = new ArrayList<>();
        for (final BRL stackBrl : combinedToBayName.keySet()) {
            final VesselStack stack = createStowbaseStack(stackBrl, combinedToBayName);
            vesselStacks.add(stack);
            exportedVesselStacks.put(stackBrl, stack);
        }
        vesselProfile.setVesselStacks(vesselStacks);
    }

    private VesselStack createStowbaseStack(final BRL stackBrl, final SortedMap<BRL, Collection<BRL>> combinedToBayName) {
        final Collection<BRL> stackSupportBrls = combinedToBayName.get(stackBrl);
        final Collection<VesselStackSupport> vesselStackSupports = new ArrayList<>();
        double stackCenterToTheFore = 0.0;
        int stackCount = 0;
        for (final BRL stackSupportBrl : stackSupportBrls) {
            final TwentyForeAftForty position = baysMapping.position(stackSupportBrl.bay);
            final StackData stackData = (position.isTwenty() ? data20 : data40).get(stackSupportBrl);
            final VesselStackSupport vesselStackSupport = makeVesselStackSupport(vesselStackSupports, stackSupportBrl,
                    stackData);
            if (position.isTwenty()) {
                vesselStackSupport.setTeuBayLength();
            } else {
                vesselStackSupport.setFeuBayLength();
            }
            if (stackData.imoForbidden) {
                vesselStackSupport.forbidImo();
            }
            stackCenterToTheFore += stackData.posLcg - longitudinalPositiveDirection.signForFore() * position.sign()
                    * 10 * 0.3048;
            ++stackCount;
        }
        stackCenterToTheFore /= stackCount;
        final VesselStack vesselStack = VesselStack.create(stowbaseObjectFactory);
        vesselStack.setRowName(stackBrl.row);
        vesselStack.setVesselStackSupports(vesselStackSupports);
        vesselStack.setCenterToTheForeInM(stackCenterToTheFore);
        vesselStack.put("overlappingFeuBay", stackBrl.bay);
        return vesselStack;
    }

    private VesselStackSupport makeVesselStackSupport(final Collection<VesselStackSupport> vesselStackSupports,
            final BRL stackSupportBrl, final StackData stackData) {
        final VesselStackSupport vesselStackSupport = VesselStackSupport.create(stowbaseObjectFactory,
                stackData.posLcg, stackData.posBottom, stackData.posTcg);
        if (!fileHasPos2040) {
            vesselStackSupport.remove("centerToTheForeInM");
            vesselStackSupport.remove("centerToThePortInM");
        }
        vesselStackSupports.add(vesselStackSupport);
        vesselStackSupport.setBayName(stackSupportBrl.bay);
        final List<String> dcTiersFromBelow = new ArrayList<>();
        for (int tier = stackData.tierBottom; tier <= stackData.tierTop; tier += 2) {
            dcTiersFromBelow.add(Integer.toString(tier));
        }
        vesselStackSupport.setDcTiersFromBelow(dcTiersFromBelow);
        final List<String> dcReeeferTiersFromBelow = new ArrayList<>();
        for (int tier = stackData.reeferTierBottom; tier <= stackData.reeferTierTop; tier += 2) {
            if (tier > 0) {
                dcReeeferTiersFromBelow.add(Integer.toString(tier));
            }
        }
        if (!dcReeeferTiersFromBelow.isEmpty()) {
            vesselStackSupport.setDcReeferTiersFromBelow(dcReeeferTiersFromBelow);
        }
        final List<String> dcFourtyFiveTiersFromBelow = new ArrayList<>();
        for (int tier = stackData.fourtyfiveTierBottom; tier <= stackData.fourtyfiveTierTop; tier += 2) {
            if (tier > 0) {
                dcFourtyFiveTiersFromBelow.add(Integer.toString(tier));
            }
        }
        if (!dcFourtyFiveTiersFromBelow.isEmpty()) {
            vesselStackSupport.setDcFourtyFiveTiersFromBelow(dcFourtyFiveTiersFromBelow);
        }
        if (!Double.isNaN(stackData.maxWeight)) {
            vesselStackSupport.setMaxWeightInKg(stackData.maxWeight);
        }
        if (!Double.isNaN(stackData.posTop)) {
            vesselStackSupport.setTopAboveInM(stackData.posTop);
        }
        return vesselStackSupport;
    }

    /**
     * @return data for 20 feet stacks
     */
    public Map<BRL, StackData> getData20() {
        return data20;
    }

    /**
     * @return data for 40 feet stacks
     */
    public Map<BRL, StackData> getData40() {
        return data40;
    }

    /**
     * @return Map with the exported vessel stacks, will be null before addDataToVesselProfile() has been called
     */
    public Map<BRL, VesselStack> getExportedVesselStacks() {
        return exportedVesselStacks;
    }

}
