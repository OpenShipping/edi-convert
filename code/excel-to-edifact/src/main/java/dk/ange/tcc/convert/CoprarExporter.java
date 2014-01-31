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
 * COPRAR file writer
 */
public class CoprarExporter implements EdiFactExporter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CoprarExporter.class);

    private enum Version {
        D95B,  // COPRAR 1.2 1996-10 SMDG User Group for Shipping Lines and Container Terminals
        D00B   // COPRAR 2.0 2003-03 SMDG User Group for Shipping Lines and Container Terminals
    }

    private final Version version = Version.D95B;

    // FIXME should be final, ugly code in LoadlistParser changes this during parse
    private String vesselImo;

    private final String vesselName;

    private final OutputStream writer;

    private final String carrierCode = "";

    private final String carrierName = "";

    private final String coprarId = "000000000000";

    private final String vesselFlag = "";

    private final List<Segment> segments = new ArrayList<Segment>();

    private String voyageId;

    private String port;

    private int containerCount = 0;

    /**
     * @param vesselImo
     * @param vesselName
     * @param writer
     */
    public CoprarExporter(final String vesselImo, final String vesselName, final OutputStream writer) {
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
            final String loadPort /* unused */, final String dischargePort,
            final List<DangerousGoods> dangerousGoodsList, final Double reeferTemperature, final String temperatureUnit,
            final List<String> specialStowList,  final String slotPosition /* unused */, final String containerCarrierCode)
    {
        ++containerCount;

        final SegmentBuilder builder = new SegmentBuilder();
        // comments show position in Coprar 2.0 standard, in parenthesis position in Coprar 1.2 standard
        
        // M 0210 Segment Group 6: EQD-RFF-EQN-TMD- DTM-LOC-MEA-DIM-SG7-SEL-FTX-SG8-EQA-HAN-SG10-NAD M
        // (0110 Segment Group 3)
        
        // M 0220 EQD Equipment details M 1
        // (0150 EQD)
        builder.setTag("EQD");
        builder.set(0, "CN");                // EQUIPMENT QUALIFIER ‘CN’ Container, ‘BB’ Breakbulk, ‘SW’ Swapbody
        builder.set(1, containerId);         // EQUIPMENT IDENTIFICATION
        builder.set(2, isocode, "102", "5"); // EQUIPMENT SIZE AND TYPE 
        builder.set(3, "");                  // EQUIPMENT SUPPLIER
        builder.set(4, "");                  // EQUIPMENT STATUS ‘1' Continental, ‘2' Export, ‘3' Import, ‘6' Transhipment
        builder.set(5, isEmpty ? "4" : "5"); // FULL/EMPTY INDICATOR 
        segments.add(builder.build());

        // O 0230 RFF Reference C 9
        // (0160 REF)
        builder.setTag("RFF");
        if(bookingNumber != null && bookingNumber.length() > 0) {    
            builder.set(0, "BN", bookingNumber);
        } else {
            builder.set(0, "BN", "1");
        }
        segments.add(builder.build());
            
        // O 240 EQN NUmber of Units C 1
        // (0170 EQN)
        
        // O 250 TMD Transport Movement Details C 9
        // (0180 TMD)
        
        // O 260 DTM Date Time Period C 9
        // (0190 DTM)
        
        // R 0270 LOC Place/Location C 9
        // (0200 LOC)
        builder.setTag("LOC");
        builder.set(0, "11");
        builder.set(1, dischargePort, "139", "6");
        segments.add(builder.build());

        // O 0280 MEA Measurements C 9
        // (0210 MEA)
        builder.setTag("MEA");
        builder.set(0, "AAE");
        if (version == Version.D00B) {
            builder.set(1, "AET");
        } else {
            builder.set(1, "G");
        }
        builder.set(2, "KGM", Integer.toString(weight));
        segments.add(builder.build());

        // O 0290 DIM Dimensions C 9
        // (0220 DIM)
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
        // "13" => Over-height non-ISO (COPRAR 2.0)
        // "9" => Over-height (forgotten in COPRAR 2.0 ??)
        // DIM+9+CMT:::23'
        if (overHeight != null){
            builder.setTag("DIM");
            builder.set(0, "9");
            builder.set(1, "CMT", "", "", Integer.toString(overHeight)); // height sub-position 3
            segments.add(builder.build());
        }
        
        // O 0300 Segment Group 7: TMP-RNG C
        // (no group here)
        group7TmpRng(liveReefer, reeferTemperature, temperatureUnit);
        
        // O 0330 SEL Seal Number C 9
        // (0250 SEL)
        
        // O 0340 FTX Free Text C 9
        // (0260 FTX)
        // Example from Powerstow coprar 1.2: FTX+HAN+++BDK' 
        // Example from MAC3 :  FTX+HAN+++UNDER' FTX+HAN+++DECK'
        // codes: (AFH) BDK DTY GAF ODK SP1 SP2 SP3 SP4
        if ((version == Version.D95B) && (specialStowList != null)){ // COPRAR 1.2
            for (final String code : specialStowList){
                builder.setTag("FTX");
                builder.set(0, "HAN");
                builder.set(1, "");
                builder.set(2, "");
                builder.set(3, code);
                segments.add(builder.build());
            }
        }

        // O 0360 segment Group 8: DGS-FTX-SG9 C
        // (no group here)
        group8DgsFtxSG9(dangerousGoodsList);
                
        // O 0430 EQA Attached Equipment C 9
        // (0280 EQA)
        
        // O 0440 HAN Handling Instructions C 9
        // (does not exist, 0340 FTX Free Text is used for handling instructions)
        
        // O 0450 Segment Group 10: TDT-DTM-RFF-SG11 C 1
        // (0290 Segment Group 4)
        
        // M 0460 TDT Details of Transport M 1
        // (0300 TDT)
        
        // (0310 REF)
        // (0320 LOC)
        
        // O 0470 DTM Date Time Period C 9
        // (0330 DTM)
        
        // 0 0480 RFF Reference C 9
        
        // O 0490 Segment Group 11: LOC 
        // M 0500 LOC Location M 1
        // END O 0490 Segment Group 11: LOC
        
        // END O 0450 Segment Group 10: TDT-DTM-RFF-SG11 C 1
        
        // O 0520 NAD Name and Address C 9
        // (0340 NAD)
        if (containerCarrierCode != null && containerCarrierCode.length() > 0) {
            // COPRAR 2.0 and BAPLIE 2.1.1 semantics
            // COPRAR 1.2 allows only 'CF' Container operator, not 'CA' Carrier of the cargo, but we cheat
            builder.setTag("NAD");
            builder.set(0, "CA");                       // Carrier of the cargo
            builder.set(1, containerCarrierCode, "172", "ZZZ" ); // "172" (Carrier Code), "ZZZ" Code List Responsible Agency
            // "20" = BIC (Bureau International des Containeurs)
            // "166" = US National Motor Freight Classification Association (SCAC)
            // "ZZZ" = Mutually agreed
            segments.add(builder.build());
        }
        
        
        // END 0210 Segment Group 6: EQD-RFF-EQN-TMD- DTM-LOC-MEA-DIM-SG7-SEL-FTX-SG8-EQA-HAN-SG10-NAD M
    }

    
    // O 0300 Segment Group 7: TMP-RNG C
    // (no group here)
    private void group7TmpRng(final boolean liveReefer, final Double reeferTemperature, final String temperatureUnit){
        final SegmentBuilder builder = new SegmentBuilder();
        // M 0310 TMP Temperature M 1
        // (0230 TMP)
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
    
        // O 0320 RNG Range Details C 1
        // (0240 RNG)
    
        // END O 0300 Segment Group 7: TMP-RNG C
    }
    
    // O 0360 segment Group 8: DGS-FTX-SG9 C
    // (no group here)
    private void group8DgsFtxSG9(final List<DangerousGoods> dangerousGoodsList) {
        // M 0370 DGS Dangerous Goods M
        // (0270 DGS)
        final SegmentBuilder builder = new SegmentBuilder();
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
    
        // O 0380 FTX Free Text C 9
    
        // O 0400 segment Group 9: CTA-COM
        // M 410 CTA Contact Information M 1
        // O 420 COM Communication Contact
        // END O 0400 segment Group 9: CTA-COM
    
        // END O 0360 segment Group 8: DGS-FTX-SG9 C 
    }
    
    private void insertFooter() {
        final SegmentBuilder builder = new SegmentBuilder();

        builder.setTag("CNT");
        builder.set(0, "16", Integer.toString(containerCount));
        segments.add(builder.build());

        builder.setTag("UNT");
        builder.set(0, Integer.toString(segments.size())); // Number of segments including UNT but excluding UNB/UNZ =>
                                                           // just
        // current size of segments list
        builder.set(1, coprarId);
        segments.add(builder.build());

        builder.setTag("UNZ");
        builder.set(0, "1");
        builder.set(1, coprarId);
        segments.add(builder.build());
    }

    /**
     * Write out the entire Coprar
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

        builder.setTag("UNB");
        builder.set(0, "UNOA", "2");
        builder.set(1, "ANGE", ""); // Sender identification TODO
        builder.set(2, "ANGE"); // Receive identification TODO
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat date = new SimpleDateFormat("yyMMdd");
        final SimpleDateFormat time = new SimpleDateFormat("HHmm");
        builder.set(3, date.format(calendar.getTime()), time.format(calendar.getTime()));
        builder.set(4, coprarId);
        header.add(builder.build());

        builder.setTag("UNH");
        builder.set(0, coprarId);
        final String versionid;
        switch (version) {
        case D95B: {
            versionid = "95B";
            break;
        }
        case D00B:
            versionid = "00B";
            break;
        default:
            throw new RuntimeException("Internal error: unknown version " + version);
        }
        builder.set(1, "COPRAR", "D", versionid, "UN");
        header.add(builder.build());

        builder.setTag("BGM");
        builder.set(0, "45");
        builder.set(1, coprarId);
        builder.set(2, "9"); // Original
        header.add(builder.build());

        if (version == Version.D00B) {
            builder.setTag("DTM");
            builder.set(0, "137"); // Document datetime
            final SimpleDateFormat datetime = new SimpleDateFormat("yyyyMMddHHmm");
            builder.set(1, datetime.format(calendar.getTime()));
            builder.set(2, "203"); // Format of datetime
            header.add(builder.build());
        }
        if (version == Version.D95B) {
            builder.setTag("RFF");
            builder.set(0, "XXX", "1");
            header.add(builder.build());
        }
        
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

        builder.setTag("LOC");
        builder.set(0, "9"); // Load COPRAR
        builder.set(1, port, "139", "6");
        header.add(builder.build());

        builder.setTag("NAD");
        builder.set(0, "CF");
        if( carrierCode != null && carrierCode.length() > 0) {
            builder.set(1, carrierCode, "172", "20");
        } else {
            builder.set(1, "ZZZ", "172", "20");
        }
        header.add(builder.build());

        segments.addAll(0, header);
    }

    /**
     * @param loadPort
     */
    public void setLoadPort(final String loadPort) {
        this.port = loadPort;
    }

    /**
     * @param vesselImo
     */
    public void setVesselImo(final String vesselImo) {
        this.vesselImo = vesselImo;
    }

    /**
     * @param voyageCode
     */
    public void setVoyageCode(final String voyageCode) {
        this.voyageId = voyageCode;
    }

}
