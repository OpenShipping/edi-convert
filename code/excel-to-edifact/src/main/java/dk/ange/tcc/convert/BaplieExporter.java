package dk.ange.tcc.convert;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.stowbase.client.objects.DangerousGoods;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.SegmentBuilder;

/**
 * BAPLIE file writer
 */
public class BaplieExporter implements EdiFactExporter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaplieExporter.class);

    private enum Version {
        D95B // BAPLIE 2.1.1 2007-10 SMDG User Group for Shipping Lines and Container Terminal   
    }


    private final Version version = Version.D95B;

    // FIXME should be final, ugly code in LoadlistParser changes this during parse
    private String vesselImo;

    private final String vesselName;

    private final OutputStream writer;

    private final String carrierCode = "";

    private final String carrierName = "";

    private final String baplieId = "000000000000";

    private final String vesselFlag = "";

    private final List<Segment> segments = new ArrayList<Segment>();

    private String voyageId;

    private String port;

    /**
     * @param vesselImo
     * @param vesselName
     * @param writer
     */
    public BaplieExporter(final String vesselImo, final String vesselName, final OutputStream writer) {
        if (writer == null) {
            throw new IllegalArgumentException("writer cannot be null");
        }
        this.vesselImo = vesselImo;
        this.vesselName = vesselName;
        this.writer = writer;
    }

    /* (non-Javadoc)
     * @see dk.ange.tcc.convert.EdiFactExporter#addContainer(java.lang.String, java.lang.String, int, java.lang.Integer, java.lang.Integer, java.lang.Integer, boolean, boolean, java.lang.String, java.lang.String, java.util.List, java.lang.Double, java.lang.String, java.util.List, java.lang.String)
     */
    public void addContainer(final String containerId, final String isocode, final int weight,
            final Integer overWidthRight, final Integer overWidthLeft, final Integer overHeight, 
            final boolean liveReefer, final boolean isEmpty, final String bookingNumber,
            final String loadPort, final String dischargePort,
            final List<DangerousGoods> dangerousGoodsList, final Double reeferTemperature, final String temperatureUnit,
            final List<String> specialStowList, final String slotPosition, final String containerCarrierCode) {
        final SegmentBuilder builder = new SegmentBuilder();
        
        // Group grp2 (C9999) LOC - GID - GDS - FTX - MEA - DIM - TMP - RNG - LOC - RFF - grp3 - grp4

        // LOC (M1) PLACE/LOCATION IDENTIFICATION (grp2)
        builder.setTag("LOC");
        builder.set(0, "147"); // Stowage Cell
        builder.set(1, slotPosition, "", "5"); // ISO format
        segments.add(builder.build());
        
        // GID (C1) GOODS ITEM DETAILS (grp2)

        // GDS (C9) NATURE OF CARGO (grp2)

        // FTX (C9) FREE TEXT (grp2)  - "HAN" = Handling Instructions
        // Example from Powerstow Baplie 1.2: FTX+HAN+++BDK' 
        // Example from MAC3 :  FTX+HAN+++UNDER' FTX+HAN+++DECK'
        // codes: (AFH) BDK DTY GAF ODK SP1 SP2 SP3 SP4
        if (specialStowList != null){
            for (final String code : specialStowList){
                builder.setTag("FTX");
                builder.set(0, "HAN");
                builder.set(1, "");
                builder.set(2, "");
                builder.set(3, code);
                segments.add(builder.build());
            }
        }
        
        // MEA (M9) MEASUREMENTS (grp2)
        builder.setTag("MEA");
        builder.set(0, "WT"); // gross weight)
        builder.set(1, "");
        builder.set(2, "KGM", Integer.toString(weight));
        segments.add(builder.build());

        // DIM (C9) DIMENSIONS (grp2)
        // "5" => Off-standard dimension front (over-length)
        // "6" => Off-standard dimension back (over-length)
        // "7" => Off-standard dimension right (over-width)
        // DIM+7+CMT::15'
        if (overWidthRight != null){
            builder.setTag("DIM");
            builder.set(0, "7");
            builder.set(1, "CMT", "", Integer.toString(overWidthRight)); // width sub-position 2
            segments.add(builder.build());
        }
        // "8" => Off-standard dimension left (over-width)
        // DIM+8+CMT::12'
        if (overWidthLeft != null){
            builder.setTag("DIM");
            builder.set(0, "8");
            builder.set(1, "CMT", "", Integer.toString(overWidthLeft)); // width sub-position 2
            segments.add(builder.build());
        }
        // "13" => Over-height non-ISO (BAPLIE 2.0)
        // "9" => Over-height (forgotten in BAPLIE 2.0 ??)
        // DIM+9+CMT:::23'
        if (overHeight != null){
            builder.setTag("DIM");
            builder.set(0, "9");
            builder.set(1, "CMT", "", "", Integer.toString(overHeight)); // height sub-position 3
            segments.add(builder.build());
        }
                
        // TMP (C1) TEMPERATURE (grp2)
        // RNG (C1) RANGE DETAILS (grp2)
        group2TmpRng(liveReefer, reeferTemperature, temperatureUnit);

        // LOC (C9) PLACE/LOCATION IDENTIFICATION (grp2)
        if(loadPort != null && loadPort.length() > 0) {
            builder.setTag("LOC");
            builder.set(0, "9");
            builder.set(1, loadPort, "139", "6");
        segments.add(builder.build());
        }
   
        if(dischargePort != null && dischargePort.length() > 0) {
            builder.setTag("LOC");
            builder.set(0, "11");
            builder.set(1, dischargePort, "139", "6");
            segments.add(builder.build());
        }
        
        // RFF (M9) REFERENCE (grp2)
        builder.setTag("RFF");
        if(bookingNumber != null && bookingNumber.length() > 0) {    
            builder.set(0, "BN", bookingNumber);
        } else {
            builder.set(0, "BN", "1");
        }
        segments.add(builder.build());

        // Group grp3 (C9) EQD - EQA - NAD

        // EQD (M1) EQUIPMENT DETAILS (grp3)
        builder.setTag("EQD");
        builder.set(0, "CN");                // EQUIPMENT QUALIFIER ‘CN’ Container, ‘BB’ Breakbulk, ‘SW’ Swapbody
        builder.set(1, containerId);         // EQUIPMENT IDENTIFICATION
        builder.set(2, isocode, "102", "5"); // EQUIPMENT SIZE AND TYPE 
        builder.set(3, "");                  // EQUIPMENT SUPPLIER
        builder.set(4, "");                  // EQUIPMENT STATUS ‘1' Continental, ‘2' Export, ‘3' Import, ‘6' Transhipment
        builder.set(5, isEmpty ? "4" : "5"); // FULL/EMPTY INDICATOR 
        segments.add(builder.build());
      
        
        // EQA (C9) EQUIPMENT ATTACHED (grp3)

        // NAD (C1) NAME AND ADDRESS (grp3)
        if (containerCarrierCode != null && containerCarrierCode.length() > 0) {
            builder.setTag("NAD");
            builder.set(0, "CA");                       // Carrier of the cargo
            builder.set(1, containerCarrierCode, "172", "ZZZ" ); // "172" (Carrier Code), "ZZZ" Code List Responsible Agency
            // "20" = BIC (Bureau International des Containeurs)
            // "166" = US National Motor Freight Classification Association (SCAC)
            // "ZZZ" = Mutually agreed
            segments.add(builder.build());
        }

        
        // Group grp4 (C999) DGS - FTX

        // DGS (M1) DANGEROUS GOODS (grp4)
        // FTX (C1) FREE TEXT (grp4)
        group4DgsFtxSG9(dangerousGoodsList);

    }

    

    // TMP (C1) TEMPERATURE (grp2)
    // RNG (C1) RANGE DETAILS (grp2)
    private void group2TmpRng(final boolean liveReefer, final Double reeferTemperature, final String temperatureUnit){
        final SegmentBuilder builder = new SegmentBuilder();

        if (liveReefer) {
            builder.setTag("TMP");
            builder.set(0, "2");
            // defaults "999", "CEL"
            String temperature = "999";
            String unit = "CEL";
            if (reeferTemperature != null){
                temperature = Double.toString(reeferTemperature);
            }
            if (temperatureUnit != null && temperatureUnit.equals("FAH")){
                unit = temperatureUnit; // only "CEL" and "FAH" allowed
            }
            builder.set(1, temperature, unit);
            segments.add(builder.build());
        }
        // RNG (C1) RANGE DETAILS (grp2)
    }
    

    // Group grp4 (C999) DGS - FTX
    // DGS (M1) DANGEROUS GOODS (grp4)
    // FTX (C1) FREE TEXT (grp4)
    private void group4DgsFtxSG9(final List<DangerousGoods> dangerousGoodsList) {
        final SegmentBuilder builder = new SegmentBuilder();
        // DGS (M1) DANGEROUS GOODS (grp4)
        if (dangerousGoodsList != null) {
            for (final DangerousGoods dg : dangerousGoodsList) {
                builder.setTag("DGS");
                builder.set(0, "IMD");
                if (dg.imdgClass == null) {
                    builder.set(1, "");
                } else {
                    builder.set(1, dg.imdgClass);
                }
                builder.set(2, dg.unNumber);
                segments.add(builder.build());
            }
        }
        // FTX (C1) FREE TEXT (grp4)    
    }
    
    private void insertFooter() {
        final SegmentBuilder builder = new SegmentBuilder();
        
        // UNT (M1) MESSAGE TRAILER
        builder.setTag("UNT");
        builder.set(0, Integer.toString(segments.size())); // Number of segments including UNT but excluding UNB/UNZ
        // just current size of segments list
        builder.set(1, baplieId);
        segments.add(builder.build());
        
        // UNZ (M1) INTERCHANGE TRAILER
        builder.setTag("UNZ");
        builder.set(0, "1");
        builder.set(1, baplieId);
        segments.add(builder.build());
    }

    /* (non-Javadoc)
     * @see dk.ange.tcc.convert.EdiFactExporter#flush()
     */
    public void flush() {
        insertHeader();
        insertFooter();
        try {
            for (final Segment segment : segments) {
                try {
                    segment.write(writer);
                } catch (final NullPointerException e) {
                    System.out.print("Failed to write a " + segment.getTag().toString());
                    throw e;
                }
            }
            writer.flush();
            log.info("Wrote {} segments", segments.size());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertHeader() {
        final ArrayList<Segment> header = new ArrayList<Segment>();
        final SegmentBuilder builder = new SegmentBuilder();

        // UNB (M1) INTERCHANGE HEADER
        builder.setTag("UNB");
        builder.set(0, "UNOA", "2");
        builder.set(1, "ANGE", ""); // Sender identification TODO
        builder.set(2, "ANGE"); // Receive identification TODO
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat date = new SimpleDateFormat("yyMMdd");
        final SimpleDateFormat time = new SimpleDateFormat("HHmm");
        builder.set(3, date.format(calendar.getTime()), time.format(calendar.getTime()));
        builder.set(4, baplieId);
        header.add(builder.build());

        // UNH (M1) MESSAGE HEADER
        builder.setTag("UNH");
        builder.set(0, baplieId);
        final String versionid;
        switch (version) {
        case D95B: {
            versionid = "95B";
            break;
        }
        default:
            throw new RuntimeException("Internal error: unknown version " + version);
        }
        builder.set(1, "BAPLIE", "D", versionid, "UN", "SMDG20");
        header.add(builder.build());

        // BGM (M1) BEGINNING OF MESSAGE
        builder.setTag("BGM");
        builder.set(0);
        builder.set(1, baplieId);
        builder.set(2, "9"); // Original
        header.add(builder.build());

        // DTM (M1) DATE/TIME/PERIOD
        final SimpleDateFormat datetime = new SimpleDateFormat("yyyyMMddHHmm");
        builder.setTag("DTM");
        builder.set(0, "137", datetime.format(calendar.getTime()), "201"); // Format of datetime
        header.add(builder.build());
       
        // Group grp1 : TDT - LOC - DTM - RFF - FTX. (M1)

        // TDT (M1) DETAILS OF TRANSPORT (grp1)
        builder.setTag("TDT");
        builder.set(0, "20");
        builder.set(1, voyageId);
        builder.set(2);
        builder.set(3);
        builder.set(4, carrierCode, "172", "20", carrierName); // Carrier
        builder.set(5);
        builder.set(6);
        builder.set(7, vesselImo, "146", "11", vesselName, vesselFlag);
        header.add(builder.build());
        
        // LOC (M9) PLACE/LOCATION IDENTIFICATION (grp1)
        // BAPLIE 2.1.1  two repetitions of LOC
        builder.setTag("LOC");
        builder.set(0, "5"); // Place of Departure
        builder.set(1, port, "139", "6"); // TODO: get right port in here
        header.add(builder.build());

        builder.setTag("LOC");
        builder.set(0, "61"); // Next port of call
        builder.set(1, port, "139", "6"); // TODO: get right port in here
        header.add(builder.build());

        // DTM (M99) DATE/TIME/PERIOD (grp1)
        // TODO: expand into these following 4 times, and get correct times from input:
        // "178" = actual date/time of arrival at senders port
        // "132" = estimated date or date/time of arrival at the next port of call
        // "133" = estimated date or date/time of departure at senders port
        // "136" = actual date/time of departure at senders port
        builder.setTag("DTM");
        builder.set(0, "178", datetime.format(calendar.getTime()), "201"); // TODO: get correct time of arrival at senders port
        header.add(builder.build());
   
        // RFF (C1) REFERENCE (grp1)

        segments.addAll(0, header);
    }

    /* (non-Javadoc)
     * @see dk.ange.tcc.convert.EdiFactExporter#setLoadPort(java.lang.String)
     */
    public void setLoadPort(final String loadPort) {
        this.port = loadPort;
    }

    /* (non-Javadoc)
     * @see dk.ange.tcc.convert.EdiFactExporter#setVesselImo(java.lang.String)
     */
    public void setVesselImo(final String vesselImo) {
        this.vesselImo = vesselImo;
    }

    /* (non-Javadoc)
     * @see dk.ange.tcc.convert.EdiFactExporter#setVoyageCode(java.lang.String)
     */
    public void setVoyageCode(final String voyageCode) {
        this.voyageId = voyageCode;
    }

}
