package dk.ange.stowbase.edifact.baplie;

import java.util.ArrayList;
import java.util.List;

import org.stowbase.client.References;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.Container;
import org.stowbase.client.objects.DangerousGoods;
import org.stowbase.client.objects.Move;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.parser.ContentHandler;

/**
 * Put BAPLIE into stowbase
 */
public class BaplieContentHandler implements ContentHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaplieContentHandler.class);

    private final StowbaseObjectFactory stowbase;

    private final String vesselImo;

    private Container container;

    private int bay;

    private int row;

    private int tier;

    private Segment temperature;

    private String loadPort;

    private String dischargePort;

    /*
     * Implementation of altXxxPort is taken from some old code, I don't know which file the real use case came from.
     */
    private String altLoadPort;

    private String altDischargePort;

    private int count = 0;

    private final References moves = new References();

    private final List<DangerousGoods> dangerousGoodsList = new ArrayList<DangerousGoods>();

    /**
     * @param stowbase
     * @param vesselImo
     */
    public BaplieContentHandler(final StowbaseObjectFactory stowbase, final String vesselImo) {
        this.stowbase = stowbase;
        this.vesselImo = vesselImo;
    }

    public void segment(final String position, final Segment segment) {
        if (position.startsWith("LOC/")) {
            if (position.equals("LOC/LOC")) {
                // LOC+147+0180884::5'
                if (segment.get(0, 0, "").equals("147")) {
                    final String containerPosition = segment.get(1, 0, "missing");
                    bay = Integer.parseInt(containerPosition.substring(0, 3));
                    row = Integer.parseInt(containerPosition.substring(3, 5));
                    tier = Integer.parseInt(containerPosition.substring(5, 7));
                } else {
                    throw new RuntimeException("Didn't expect this: " + segment);
                }
                ++count;
            } else if (position.equals("LOC/GDS")) {
                // Ignore: Segment[GDS+REEF']
            } else if (position.equals("LOC/FTX")) {
                // Ignore: Segment[FTX+AAA+++/']
            } else if (position.equals("LOC/MEA")) {
                if (segment.get(0, 0, "").equals("WT")) {
                    if (segment.get(2, 0, "").equals("KGM")) {
                        container.setGrossWeightInKg(Integer.parseInt(segment.get(2, 1, null)));
                    } else {
                        throw new RuntimeException("Didn't expect this: " + segment.get(2, 0, null));
                    }
                } else {
                    throw new RuntimeException("Didn't expect this: " + segment);
                }
            } else if (position.equals("LOC/DIM")) { // Segment[DIM+9+CMT:::79']
                // TODO OOG, copy code from python
            } else if (position.equals("LOC/TMP")) { // Segment[TMP+2+-004:FAH']
                temperature = segment;
            } else if (position.equals("LOC/LOC2")) {
                final String locQualifier = segment.get(0, 0, "missing");
                // LOC+11+GBSOU:139:6'
                if (locQualifier.equals("6")) { // ???
                    altLoadPort = segment.get(1, 0, null);
                } else if (locQualifier.equals("9")) { // Place/port of loading
                    loadPort = segment.get(1, 0, null);
                } else if (locQualifier.equals("11")) { // Place/port of discharge
                    dischargePort = segment.get(1, 0, null);
                } else if (locQualifier.equals("12")) { // Port of discharge
                    altDischargePort = segment.get(1, 0, null);
                } else if (locQualifier.equals("83")) { // Place of delivery (by on carriage)
                    // ignore
                } else {
                    throw new RuntimeException("Didn't expect this: " + segment);
                }
            } else if (position.equals("LOC/RFF")) {
                // Ignore: Segment[RFF+BM:1']
            } else if (position.equals("LOC/EQD/EQD")) {
                // Segment[EQD+CN+MSKU 4527802+L2G0+++4']
                if (segment.get(0, 0, "").equals("CN")) {
                    final String containerId = segment.get(1, 0, null);
                    if (containerId != null) {
                        container.setContainerId(containerId);
                    }
                    container.put("rawIsoCode", segment.get(2, 0, null));
                    final String isEmptyString = segment.get(5, 0, null);
                    if (isEmptyString == null) {
                        // Skip
                    } else if (isEmptyString.equals("4")) {
                        container.setIsEmpty(true);
                    } else if (isEmptyString.equals("5")) {
                        container.setIsEmpty(false);
                    } else {
                        throw new RuntimeException("Didn't expect this: " + isEmptyString);
                    }
                } else {
                    throw new RuntimeException("Didn't expect this: " + segment);
                }
            } else if (position.equals("LOC/EQD/NAD")) {
                // Ignore: Segment[NAD+CA+MSK:172:20'] means shipped by Maersk
            } else if (position.equals("LOC/DGS/DGS")) { // Segment[DGS+IMD+4.1+2556']
                dangerousGoodsList.add(new DangerousGoods(segment.get(2, 0, ""), segment.get(1, 0, "")));
            } else if (position.equals("LOC/DGS/FTX")) {
                // Ignore: Segment[FTX+AAA+++INFLAMMABLE SOLID'] dg explanation
            } else {
                log.info("Unknown segment in container: " + position + " " + segment);
            }
        }
    }

    private void clearContainer() {
        container = null;
        bay = -1;
        row = -1;
        tier = -1;
        temperature = null;
        loadPort = null;
        altLoadPort = null;
        dischargePort = null;
        altDischargePort = null;
        dangerousGoodsList.clear();
    }

    public void startGroup(final String position) {
        if (position.equals("LOC")) {
            clearContainer();
            container = Container.create(stowbase);
        }
        // System.out.println("startGroup: " + position);
    }

    public void endGroup(final String position) {
        if (position.equals("LOC")) {
            container.setLiveReefer(temperature != null);
            if (!dangerousGoodsList.isEmpty()) {
                container.setDangerousGoods(dangerousGoodsList);
            }
            // Load move
            if (loadPort == null) {
                loadPort = altLoadPort;
            }
            final Move loadMove = Move.create(stowbase);
            moves.add(loadMove.getReference());
            if (loadPort != null && !loadPort.equals("VSL")) {
                loadMove.setFromPort(loadPort);
            }
            loadMove.setToSlot(vesselImo, bay, row, tier);
            loadMove.setCargo(container);
            // Discharge move
            if (dischargePort == null) {
                dischargePort = altDischargePort;
            }
            final Move dischargeMove = Move.create(stowbase);
            moves.add(dischargeMove.getReference());
            dischargeMove.setFromSlot(vesselImo, bay, row, tier);
            if (dischargePort != null) { // no dischargePort ???
                dischargeMove.setToPort(dischargePort);
            }
            dischargeMove.setCargo(container);
            // Clear
            clearContainer();
        }
        // System.out.println("endGroup:   " + position);
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @return Created moves
     */
    public References getMoves() {
        return moves;
    }

}
