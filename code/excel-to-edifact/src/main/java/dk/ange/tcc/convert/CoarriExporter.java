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
 * COARRI file writer
 */
public class CoarriExporter implements EdiFactExporter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CoprarExporter.class);

    private enum Version {
        D95B,  // COARRI 1.2 1996-10 SMDG User Group for Shipping Lines and Container Terminals
        D00B   // COARRI 2.0 2003-03 SMDG User Group for Shipping Lines and Container Terminals
    }

    private final Version version = Version.D95B; // only using version D95B, because that is what Angelstow accepts

    // FIXME should be final, ugly code in LoadlistParser changes this during parse
    private String vesselImo;

    private final String vesselName;

    private final OutputStream writer;

    private final String carrierCode = "";

    private final String carrierName = "";

    private final String coarriId = "000000000000";

    private final String vesselFlag = "";

    private final List<Segment> segments = new ArrayList<>();

    private String voyageId;

    private String port;

    private int containerCount = 0;

    /**
     * @param vesselImo
     * @param vesselName
     * @param writer
     */
    public CoarriExporter(final String vesselImo, final String vesselName, final OutputStream writer) {
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
            final List<String> specialStowList,  final String slotPosition, final String containerCarrierCode)
    {
        if (version == Version.D95B) {
            coarri12addContainer(containerId, isocode, weight,
                    overWidthRight, overWidthLeft, overHeight,
                    liveReefer, isEmpty, bookingNumber,
                    loadPort, dischargePort,
                    dangerousGoodsList, reeferTemperature, temperatureUnit,
                    specialStowList,  slotPosition , containerCarrierCode);
        }  else if (version == Version.D00B) {
            coarri20addContainer(containerId, isocode, weight,
                    overWidthRight, overWidthLeft, overHeight,
                    liveReefer, isEmpty, bookingNumber,
                    loadPort, dischargePort,
                    dangerousGoodsList, reeferTemperature, temperatureUnit,
                    specialStowList,  slotPosition , containerCarrierCode);
        }
    }


    private void coarri12addContainer(final String containerId, final String isocode, final int weight,
            final Integer overWidthRight, final Integer overWidthLeft, final Integer overHeight,
            final boolean liveReefer, final boolean isEmpty, final String bookingNumber,
            final String loadPort, final String dischargePort,
            final List<DangerousGoods> dangerousGoodsList, final Double reeferTemperature, final String temperatureUnit,
            final List<String> specialStowList, final String slotPosition, final String containerCarrierCode)
    {
        ++containerCount;

        // comments show position in Coarri 1.2 standard

        // 0130 group 3  M 9999
        // EQD - RFF - TMD - DTM - LOC - MEA - DIM - TMP - RNG - SEL -
        // FTX - DGS - EQA - PIA - grp4 - grp5 - NAD

        // 0140 EQD Equipment details M 1
        segmentEQD(containerId, isocode, isEmpty);

        // 0150 RFF Reference C 9
        segmentRFF(bookingNumber);

        // 0160 TMD Transport Movement Details C 9

        // 0170 DTM Date/Time/Period C 9

        // 0180 LOC Place/Location C 9
        segmentLOCload(loadPort);
        segmentLOCdisch(dischargePort);
        segmentLOCstow(slotPosition);

        // 0190 MEA Measurements C 9
        segmentMEA(weight);

        // 0200 DIM Dimensions C 9
        segmentDIM(overWidthRight, overWidthLeft, overHeight);

        // 0210 TMP temperature C 9
        // 0220 RNG Range Details C 9
        coarri20group7TmpRng(liveReefer, reeferTemperature, temperatureUnit); // Coarri 1.2 has no group here, but uses same segment order

        // 0230 SEL Seal Number C 9

        // 0240 FTX Free Text C 9
        segmentFTXhandling(specialStowList);

        // 0250 DGS Dangerous Goods C 9
        coarri20group8DgsFtxSG9(dangerousGoodsList); // Coarri 1.2 has no group here, but uses same segment DGS here

        // 0260 EQA Attached equipment C 9

        // 0270 PIA Additional Product ID

        // 0280  group 4  C 9

        // 0290 DAM Damage M 1

        // 0300 COD Component details C 1

        segmentNADcarrier(containerCarrierCode);
    }

    private void coarri20addContainer(final String containerId, final String isocode, final int weight,
            final Integer overWidthRight, final Integer overWidthLeft, final Integer overHeight,
            final boolean liveReefer, final boolean isEmpty, final String bookingNumber,
            final String loadPort, final String dischargePort,
            final List<DangerousGoods> dangerousGoodsList, final Double reeferTemperature, final String temperatureUnit,
            final List<String> specialStowList, final String slotPosition, final String containerCarrierCode)
    {
        ++containerCount;

        // comments show position in Coarri 2.0 standard

        // R 0200 Segment Group 6:  EQD-RFF-TMD-DTM-SG7-MEA-DIM-SG8-SEL-FTX-SG9-EQA-HAN-SG11-SG12-NAD

        // M 0210 EQD Equipment details M 1
        segmentEQD(containerId, isocode, isEmpty);

        // O 0220 RFF Reference C 9
        segmentRFF(bookingNumber);

        // O 240 TMD Transport Movement Details C 9

        // R 250 DTM Date Time Period C 9

        // R 0260 Segment Group 7: LOC  C 9

        // M 0270 LOC Place/Location M 1
        segmentLOCload(loadPort);
        segmentLOCdisch(dischargePort);
        segmentLOCstow(slotPosition);

        // O 0290 MEA Measurements C 9
        segmentMEA(weight);

        // O 0300 DIM Dimensions C 9
        segmentDIM(overWidthRight, overWidthLeft, overHeight);

        // O 0310 Segment Group 7: TMP-RNG C 9
        coarri20group7TmpRng(liveReefer, reeferTemperature, temperatureUnit);

        // O 0340 SEL Seal Number C 9

        // O 0350 FTX Free Text C 9
        segmentFTXhandling(specialStowList);

        // O 0370 segment Group 8: DGS-FTX-SG9 C
        coarri20group8DgsFtxSG9(dangerousGoodsList);

        // O 0440 EQA Attached Equipment C 9

        // O 0470 HAN Handling Instructions C

        // 0 0480 Segment Group 11: DAM-COD

        // M 0490 DAM M 1

        // O 0500 COD C 1

        // END 0 0480 Segment Group 11: DAM-COD

        // O 0510 Segment Group 12: TDT-LOC-DTM  C 1

        // M 0520 TDT Details of Transport M 1

        // O 0530 LOC Place/Location Identification C 9

        // O 0540 DTM Date/Time/Period C 9

        // END O 0510 Segment Group 12: TDT-LOC-DTM  C 1

        // O 0550 NAD Name and Address C 9
        segmentNADcarrier(containerCarrierCode);
        // END 0200 Segment Group 6:  EQD-RFF-TMD-DTM-SG7-MEA-DIM-SG8-SEL-FTX-SG9-EQA-HAN-SG11-SG12-NAD
    }


    private void segmentEQD(final String containerId, final String isocode, final boolean isEmpty) {
        // M 0210 EQD Equipment details M 1
        final SegmentBuilder builder = new SegmentBuilder();
        builder.setTag("EQD");
        builder.set(0, "CN");                // EQUIPMENT QUALIFIER ‘CN’ Container, ‘BB’ Breakbulk, ‘SW’ Swapbody
        builder.set(1, containerId);         // EQUIPMENT IDENTIFICATION
        builder.set(2, isocode, "102", "5"); // EQUIPMENT SIZE AND TYPE
        builder.set(3, "");                  // EQUIPMENT SUPPLIER
        builder.set(4, "");                  // EQUIPMENT STATUS ‘1' Continental, ‘2' Export, ‘3' Import, ‘6' Transhipment
        builder.set(5, isEmpty ? "4" : "5"); // FULL/EMPTY INDICATOR
        segments.add(builder.build());
    }

    private void segmentRFF(final String bookingNumber) {
        // O 0220 RFF Reference C 9
       final SegmentBuilder builder = new SegmentBuilder();
       builder.setTag("RFF");
       if(bookingNumber != null && bookingNumber.length() > 0) {
           builder.set(0, "BN", bookingNumber);
       } else {
           builder.set(0, "BN", "1");
       }
       segments.add(builder.build());
    }

    private void segmentLOCload(final String loadPort){
        if(loadPort != null && loadPort.length() > 0) {
            final SegmentBuilder builder = new SegmentBuilder();
            builder.setTag("LOC");
            builder.set(0, "9");
            builder.set(1, loadPort, "139", "6");
            segments.add(builder.build());
        }
    }

    private void segmentLOCdisch(final String dischargePort) {
        if(dischargePort != null && dischargePort.length() > 0) {
            final SegmentBuilder builder = new SegmentBuilder();
            builder.setTag("LOC");
            builder.set(0, "11");
            builder.set(1, dischargePort, "139", "6");
            segments.add(builder.build());
        }
    }

    private void segmentLOCstow(final String slotPosition){
        if(slotPosition != null && slotPosition.length() > 0) {
            final SegmentBuilder builder = new SegmentBuilder();
            builder.setTag("LOC");
            builder.set(0, "147"); // Stowage Cell
            builder.set(1, slotPosition, "", "5"); // ISO format
            segments.add(builder.build());
        }
    }

    private void segmentMEA(final int weight){
        // O 0290 MEA Measurements C 9
        final SegmentBuilder builder = new SegmentBuilder();
        builder.setTag("MEA");
        builder.set(0, "AAE");
        if (version == Version.D00B) {
            builder.set(1, "AET");
        } else {
            builder.set(1, "G");
        }
        builder.set(2, "KGM", Integer.toString(weight));
        segments.add(builder.build());
    }

    private void segmentDIM(final Integer overWidthRight, final Integer overWidthLeft, final Integer overHeight){
        // O 0300 DIM Dimensions C 9
        final SegmentBuilder builder = new SegmentBuilder();
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
        // "13" => Over-height non-ISO (COARRI 2.0)
        // "9" => Over-height (forgotten in COARRI 2.0 ??)
        // DIM+9+CMT:::23'
        if (overHeight != null){
            builder.setTag("DIM");
            builder.set(0, "9");
            builder.set(1, "CMT", "", "", Integer.toString(overHeight)); // height sub-position 3
            segments.add(builder.build());
        }
    }

    // O 0310 Segment Group 7: TMP-RNG C
    // (no group here)
    private void coarri20group7TmpRng(final boolean liveReefer, final Double reeferTemperature, final String temperatureUnit){
        final SegmentBuilder builder = new SegmentBuilder();
        // M 0320 TMP Temperature M 1
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

        // O 0330 RNG Range Details C 1

        // END O 0310 Segment Group 7: TMP-RNG C
    }

    private void segmentFTXhandling(final List<String> specialStowList) {
        // O 0350 FTX Free Text C 9
        final SegmentBuilder builder = new SegmentBuilder();
        // Example from Powerstow coprar 1.2: FTX+HAN+++BDK'
        // Example from MAC3 :  FTX+HAN+++UNDER' FTX+HAN+++DECK'
        // codes: (AFH) BDK DTY GAF ODK SP1 SP2 SP3 SP4
        if (specialStowList != null){
            for (final String code : specialStowList){
                builder.setTag("FTX");
                switch (version) {
                case D95B:
                    builder.set(0, "HAN");
                    break;
                case D00B:
                    builder.set(0, "LOI");  // Loading instruction
                    break;
                default:
                    throw new RuntimeException("Internal error: unknown version " + version);
                }
                builder.set(1, "");
                builder.set(2, "");
                builder.set(3, code);
                segments.add(builder.build());
            }
        }
    }

    // O 0370 segment Group 9: DGS-FTX-SG9 C 99
    private void coarri20group8DgsFtxSG9(final List<DangerousGoods> dangerousGoodsList) {
        // M 0380 DGS Dangerous Goods M
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

        // O 0390 FTX Free Text C 9

        // END O 0370 segment Group 9: DGS-FTX-SG9 C 99
    }

    private void segmentNADcarrier(final String containerCarrierCode){
        // O 0550 NAD Name and Address C 9
        final SegmentBuilder builder = new SegmentBuilder();
        if (containerCarrierCode != null && containerCarrierCode.length() > 0) {
           // ?? COPRAR 2.0 and BAPLIE 2.1.1 semantics
           // ?? COPRAR 1.2 allows only 'CF' Container operator, not 'CA' Carrier of the cargo, but we cheat
           builder.setTag("NAD");
           builder.set(0, "CA");                       // Carrier of the cargo
           builder.set(1, containerCarrierCode, "172", "ZZZ" ); // "172" (Carrier Code), "ZZZ" Code List Responsible Agency
           // "20" = BIC (Bureau International des Containeurs)
           // "166" = US National Motor Freight Classification Association (SCAC)
           // "ZZZ" = Mutually agreed
           segments.add(builder.build());
        }
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
        builder.set(1, coarriId);
        segments.add(builder.build());

        builder.setTag("UNZ");
        builder.set(0, "1");
        builder.set(1, coarriId);
        segments.add(builder.build());
    }

    /**
     * Write out the entire Coarri
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
        final ArrayList<Segment> header = new ArrayList<>();
        final SegmentBuilder builder = new SegmentBuilder();

        builder.setTag("UNB");
        builder.set(0, "UNOA", "2");
        builder.set(1, "ANGE", ""); // Sender identification TODO
        builder.set(2, "ANGE"); // Receive identification TODO
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat date = new SimpleDateFormat("yyMMdd");
        final SimpleDateFormat time = new SimpleDateFormat("HHmm");
        builder.set(3, date.format(calendar.getTime()), time.format(calendar.getTime()));
        builder.set(4, coarriId);
        header.add(builder.build());

        builder.setTag("UNH");
        builder.set(0, coarriId);
        final String versionid;
        switch (version) {
        case D95B:
            versionid = "95B";
            break;
        case D00B:
            versionid = "00B";
            break;
        default:
            throw new RuntimeException("Internal error: unknown version " + version);
        }
        builder.set(1, "COARRI", "D", versionid, "UN");
        header.add(builder.build());

        builder.setTag("BGM");
        builder.set(0, "45");
        builder.set(1, coarriId);
        builder.set(2, "9"); // Original
        header.add(builder.build());

        if (version == Version.D00B) {
            final SimpleDateFormat datetime = new SimpleDateFormat("yyyyMMddHHmm");
            builder.setTag("DTM");
            builder.set(0, "137", datetime.format(calendar.getTime()), "203"); // Format of datetime
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
        builder.set(0, "9"); // Load COARRI
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
