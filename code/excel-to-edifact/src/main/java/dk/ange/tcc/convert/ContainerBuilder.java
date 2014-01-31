package dk.ange.tcc.convert;

import static dk.ange.tcc.convert.SheetFunctions.cellString;
import static dk.ange.tcc.convert.SheetFunctions.pos;
import static org.stowbase.client.objects.Units.FOOT;

import dk.ange.tcc.convert.UndgNumberImdgCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.StowbaseURI;
import org.stowbase.client.objects.Container;
import org.stowbase.client.objects.DangerousGoods;
import org.stowbase.client.objects.Move;

import dk.ange.stowbase.parse.utils.Messages;

/**
 * Container builder. Will parse a single container using parseContainer() and then write it out using build().
 */
public final class ContainerBuilder {

    private String containerId;

    private final String vesselImo;

    private String isoCode;

    private int weight; // kg
    
    private Integer overWidthRight; // cm optional
    
    private Integer overWidthLeft; // cm optional
    
    private Integer overHeight; // cm optional

    private boolean isLiveReefer;
    
    private Double reeferTemperature;
    
    private String temperatureUnit;

    private boolean isEmpty;

    private String loadPort;

    private String dischargePort;

    private List<String> specialStowList;
    
    private String bookingNumber;
    
    private String slotPosition;
    
    private String containerCarrierCode;

    private List<DangerousGoods> dangerousGoodsList;

    /**
     * @param vesselImo
     * @param containerId
     */
    public ContainerBuilder(final String vesselImo, final String containerId) {
        this.containerId = containerId;
        this.vesselImo = vesselImo;
    }

    /**
     * Parse container out of Excel row.
     * 
     * @param row
     * @param messages
     * @param sheetName
     */
    public void parseContainer(final Row row, final Messages messages, final String sheetName) {
        parseContainer(row, messages, sheetName, null);
    }

    /**
     * Parse container out of Excel row
     * 
     * @param row
     * @param messages
     * @param sheetName
     * @param calls
     *            list of calls in schedule. Containers loaded outside schedule will be assumed to be onboard and
     *            containers discharged outside schedule will be assumed to stay onboard. If list is null all calls are
     *            treated as in schedule.
     */
    public void parseContainer(final Row row, Messages messages, final String sheetName, final Set<String> calls) {
        // See user documentation at
        // convert/grails-app/views/shared/_loadlistInstructionsTemplate.gsp
        // for name and position of fields

        // The parser ignores the first row of the sheet, and ignores all columns after S 18
        // Columns/Fields have fixed order, they _must_ appear in the order as documented
        // Rows without CONTAINER_ID are skipped silently
        {
            //final StringBuilder isoCodeBuilder = new StringBuilder();
            String stif = cellString(row.getCell(4)); // E  4 STIF_CODE
            String isoCodeFromStif = "";
            if (stif != null && stif.length() == 4) {
                // 22G0    20' x 8'6       GP      GENERAL PURPOSE         20DV
                if (stif.equals("20DV")) {
                    isoCodeFromStif = "22G0";
                // 22P1    20' x 8'6       PF      PLATFORM (FIXED ENDS)   20FR
                } else if (stif.equals("20FR")) {
                    isoCodeFromStif = "22P1";
                // 22R0    20' x 8'6       RE      REFRIGERATED    20RF
                } else if (stif.equals("20RF")) {
                    isoCodeFromStif = "22R0";
                // 22T2    20' x 8'6       TN      TANK (LIQUID)   20TK
                } else if (stif.equals("20TK")) {
                    isoCodeFromStif = "22T0";
                // 22U1    20' x 8'6       UT      OPEN TOP        20OT
                } else if (stif.equals("20OT")) {
                    isoCodeFromStif = "22U1";
                // 25G0    20' x 9'6       GP      GENERAL PURPOSE         20HC
                } else if (stif.equals("20HC")) {
                    isoCodeFromStif = "25G0";
                // 28G0    20' x 4'3       GP      GENERAL PURPOSE         20DV
                } else if (stif.equals("20DV")) {
                    isoCodeFromStif = "28G0";
                // 29P0    20' x <4'       PL      PLATFORM (PLAIN)        20PL
                } else if (stif.equals("20PL")) {
                    isoCodeFromStif = "29P0";
                // 42G0    40' x 8'6       GP      GENERAL PURPOSE         40DV
                } else if (stif.equals("40DV")) {
                    isoCodeFromStif = "42G0";
                // 42P1    40' x 8'6       PF      PLATFORM (FIXED ENDS)   40FR
                } else if (stif.equals("40FR")) {
                    isoCodeFromStif = "42P1";
                // 42R0    40' x 8'6       RE      REFRIGERATED    40RF
                } else if (stif.equals("40RF")) {
                    isoCodeFromStif = "42R0";
                // 42T2    40' x 8'6       TN      TANK (LIQUID)   40TK
                } else if (stif.equals("40TK")) {
                    isoCodeFromStif = "42T0";
                // 42U1    40' x 8'6       UT      OPEN TOP        40OT
                } else if (stif.equals("40OT")) {
                    isoCodeFromStif = "42U1";
                // 45G0    40' x 9'6       GP      GENERAL PURPOSE         40HC
                } else if (stif.equals("40HC")) {
                    isoCodeFromStif = "45G0";
                // 45P1    40' x 9'6       PF      PLATFORM (FIXED ENDS)   40SR
                } else if (stif.equals("40SR")) {
                    isoCodeFromStif = "45P1";
                // 45R0    40' x 9'6       RE      REFRIGERATED    40RH
                } else if (stif.equals("40RH")) {
                    isoCodeFromStif = "45R0";
                // 49P0    40' x <4'       PL      PLATFORM (PLAIN)        40PL
                } else if (stif.equals("40PL")) {
                    isoCodeFromStif = "49P0";
                // L5G0    45' x 9'6       GP      GENERAL PURPOSE         45HC
                } else if (stif.equals("45HC")) {
                    isoCodeFromStif = "L5G0";
                // L5R0    45' x 9'6       RE      REFRIGERATED    45RH
                } else if (stif.equals("45RH")) {
                    isoCodeFromStif = "L5R0";
                }  else {
                    messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 4 STIF_CODE '" + stif + "' unknown");
                    // isoCodeFromStif == null;
                }
            }

            String rawIsoCode = cellString(row.getCell(5)); // F  5 ISO_CODE
            this.isoCode = "";
            if (isoCodeFromStif.length() == 4) {
                this.isoCode = isoCodeFromStif;
            }
            if (rawIsoCode != null && rawIsoCode.length() == 4 ) {  // field 5 ISO_CODE wins over field 4 STIF_CODE
                this.isoCode = rawIsoCode;
            }
            if (isoCodeFromStif.length() == 4
                    && this.isoCode.length() == 4
                    && !this.isoCode.matches(isoCodeFromStif)) {
                messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 4 STIF_CODE '" + stif
                        + "' --> '" + isoCodeFromStif + "' and field 5 ISO code '" + rawIsoCode
                        + "' are inconsistent, accepted ISO_CODE '" + this.isoCode + "'");
            }
            if (this.isoCode.length() != 4) {
                messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 4 STIF_CODE '" + stif
                        + "' or field 5 ISO_CODE '" + rawIsoCode + "' did not generate a valid ISO code");
            }
        }
        
        // G 6 WEIGHT_KG
        {
            final String rawWeight = cellString(row.getCell(6)); // G  6 WEIGHT_KG
            try {
                final int weightInKg = (int)Math.round(Double.parseDouble(rawWeight));
                if (weightInKg < 1000) {
                    messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 6 WEIGHT_KG '" + weightInKg + "' expected at least 1000 kg");
                }
                this.weight = weightInKg;
            }
            catch (NumberFormatException ne) {
                messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 6 WEIGHT_KG '" + rawWeight + "' expected integer (in kg)");
            }
        }
        
        // H 7 EMPTY
        {
            final Cell cell = row.getCell(7); // 7 EMPTY
            if (cell == null) {
                this.isEmpty = false; // default N
            } else {
                final String emptyStatus = cellString(cell); 
                if (emptyStatus == null || emptyStatus.equals("N") || emptyStatus.equals("")) {
                    this.isEmpty = false;
                } else if (emptyStatus.equals("Y")) {
                    this.isEmpty = true;
                } else {
                    messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 7 EMPTY '" + emptyStatus + "' unknown, expected (Y|N)");
                }
            }
        }
        
        // I 8 REEF_LIVE
        {
            this.isLiveReefer = false;
            if (isoCode.equals("22R0") || isoCode.equals("45R0")) { // is a 20" RF or 40" HR reefer
                final Cell cell = row.getCell(8); // 8 REEF_LIVE
                if (cell == null) {
                    messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 8 REEF_LIVE (Y|N) expected");
                    
                }
                final String aliveStatus = cellString(cell); 
                if (aliveStatus == null || aliveStatus.equals("N") || aliveStatus.equals("")) { // default N
                  // already false
                } else if (aliveStatus.equals("Y")) {
                        this.isLiveReefer = true;
                } else {
                    messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 8 REEF_LIVE '" + aliveStatus + "' expected (Y|N)");
                }
            }
        }
        

        // J 9 REEF_TEMP
        {
            final Cell cell = row.getCell(9); //  J 9 REEF_TEMP
            if (!isLiveReefer || cell == null) {
                this.reeferTemperature = null;
	    } else {
                final String temperature = cellString(cell);
                if (temperature == null || temperature.equals("")) {
                    this.reeferTemperature = null;
		} else {
                    this.reeferTemperature = Double.parseDouble(temperature);
		}
            }
        }
        // K 10 TEMP_UNIT
        {
            final Cell cell = row.getCell(10); //  K 10 TEMP_UNIT
            if (!isLiveReefer || cell == null) {
                this.temperatureUnit = null;
            } else {
                final String unit = cellString(cell);
                if (unit == null || unit.equals("")) {
                    this.temperatureUnit = null;
                } else {
                    if (unit.equals("CEL") || unit.equals("FAH")) {
                        this.temperatureUnit = unit;
                    } else {
                        messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": field 10 TEMP_UNIT '" + unit + "' expected (CEL|FAH)");
                    }       
                }
            }
        }
     
        // L 11 IMO_DG
        {
            final Cell cell = row.getCell(11); // L 11 IMO_DG
            final String dangerousGoodsString = cellString(cell);
            if (dangerousGoodsString != null && dangerousGoodsString.length() != 0) {
                final List<DangerousGoods> dangerousGoodsListNew = new ArrayList<DangerousGoods>();
                final String[] split = dangerousGoodsString.split("\\s+");
                for (final String tuple : split) {
                    final String[] tupleSplit = tuple.split(",");
                    if (tupleSplit.length != 2) { // no IMO Class given, call lookup
                        // handling suffix 'L' for limited quantities, which is not used in imdgClass lookup
                        // not conforming to COPRAR 1.2, must be handled correctly by upgrade to COPRAR 2.0, see tickets #961 #962
                        String undgNumber = tupleSplit[0].replace("L","");
                        dangerousGoodsListNew.add(new DangerousGoods(tupleSplit[0], 
                                UndgNumberImdgCode.imdgClass(undgNumber)));
                    } else {
                        dangerousGoodsListNew.add(new DangerousGoods(tupleSplit[0], tupleSplit[1]));
                    }
                }
                this.dangerousGoodsList = dangerousGoodsListNew;
            } else {
                this.dangerousGoodsList = null;
            }
        }
       
        // M 12 OOG_HEIGHT
        {
            this.overHeight = null;
            final Cell cell = row.getCell(12); // M 12 OOG_HEIGHT
            if (cell != null) {
                final String value = cellString(cell); 
                if (!value.equals("")) {
                    this.overHeight = Integer.parseInt(value);
                }
            }
        }
        
        // N 13 OOG_RIGHT
        {
            this.overWidthRight = null;
            final Cell cell = row.getCell(13); // N 13 OOG_RIGHT
            if (cell != null) {
                final String value = cellString(cell); 
                if (!value.equals("")) {
                    this.overWidthRight = Integer.parseInt(value);
                }
            }
        }
        
        // O 14 OOG_LEFT
        {
            this.overWidthLeft = null;
            final Cell cell = row.getCell(14); // O 14 OOG_LEFT
            if (cell != null) {
                final String value = cellString(cell); 
                if (!value.equals("")) {
                    this.overWidthLeft = Integer.parseInt(value);
                }
            }
        }
        
        // P 15 SPECIAL_STOW
        {
            final Cell cell = row.getCell(15); // P 15 SPECIAL_STOW
            if (cell != null) {
                final String special = cellString(cell); 
                if (!special.equals("")) {
                    final List<String> specialList = new ArrayList<String>();
                    final String[] split = special.split("\\s+");
                    for (final String code : split) {
                        specialList.add(code);
                    }
                    this.specialStowList = specialList;
                }
            }
        }
        
        // Q 16 BOOKING_NO
        {
            final Cell cell = row.getCell(16); //  Q 16 BOOKING_NO
            this.bookingNumber = cellString(cell);
        }
        
        
        // R 17 SLOT_POSITION
        {
            final Cell cell = row.getCell(17); //  R 17 SLOT_POSITION
            String position = cellString(cell);
            if (position != null && position.length() == 5) { // bbbrrtt expected, but get sometimes brrtt from Excel
                position = "00" + position;
            }
            if (position != null && position.length() == 6) { // bbbrrtt expected, but get sometimes bbrrtt from Excel
                position = "0" + position;
            }
            if (position != null && position.length() > 0 && position.length() != 7) {
                messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1)
                        + ": field 17 R SLOT_POSITION format unknown, expected 'bbbrrtt', got '" + position + "'");
            } 
            this.slotPosition = position;
        }
        
        // S 18 CARRIER
        {
            final Cell cell = row.getCell(18); //  S 18 CARRIER
            this.containerCarrierCode = cellString(cell);
        }
        
        
        parseLoadMove(row, messages, sheetName, calls);      // C  2 POL
        parseDischargeMove(row, messages, sheetName, calls); // D  3 POD
  
    }

    private void parseLoadMove(final Row row, final Messages messages, final String sheetName, final Set<String> calls) {
        
        final Cell cell = row.getCell(2); // C  2 POL
        final String loadText = cell.getStringCellValue();
        if (calls == null || calls.contains(loadText)) {
            this.loadPort = loadText;
        } else {
            this.loadPort = null;
            messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": unknown load port '" + loadText + "' in cell '" + pos(cell)
                    + "', will assume the container is onboard");
        }
    }

    private void parseDischargeMove(final Row row, final Messages messages, final String sheetName,
            final Set<String> calls) {
        final Cell cell = row.getCell(3); // D  3 POD
        final String dischargeText = cell.getStringCellValue();
        if (calls == null || calls.contains(dischargeText)) {
            this.dischargePort = dischargeText;
        } else {
            this.dischargePort = null;
            messages.addSheetWarning(sheetName, "row " + (row.getRowNum()+1) + ": unknown discharge port '" + dischargeText + "' in cell '" + pos(cell)
                    + "', will assume the container should stay onboard");
        }
    }
    
    /**
     * @param containerId
     */
    public void setContainerId(final String containerId) {
        this.containerId = containerId;
    }

    /**
     * @return container weight in kg
     */
    public int getContainerWeight() {
        return this.weight;
    }
   
    /**
     * @param containerWeight
     */
    public void setContainerWeight(final int containerWeight) {
        this.weight = containerWeight;
    }

     /**
     * Insert the last parsed container into the stowbase and coprar
     * 
     * @param stowbase
     *            stowbase writer to write container to, if it is null it will be skipped
     * @param moves
     * @param ediFactExporter
     *            coprar writer to write container to, if it is null it will be skipped
     */
    public void build(final StowbaseObjectFactory stowbase, final References moves, final EdiFactExporter ediFactExporter) {
        if (stowbase != null) {
            buildJson(stowbase, moves);
        }
        if (ediFactExporter != null) {
            buildEdiFact(ediFactExporter);
        }
    }
    
    private void buildJson(final StowbaseObjectFactory stowbase, final References moves) {
        if (vesselImo == null || containerId == null && isoCode == null) {
            return;
        }
        final Container container = Container.createWithContainerId(stowbase, containerId);
        switch (isoCode.charAt(0)) {
        case '2':
            container.setLength20();
            break;
        case '4':
            container.setLength40();
            break;
        case 'L':
            container.setLength45();
            break;
        case 'P':
            container.put("lengthInM", 53 * FOOT);
            container.put("lengthName", StowbaseURI.forFootLength("53").toString());
            break;
        default:
            throw new RuntimeException("Json: Unsupported first char in '" + isoCode + "'");
        }
        switch (isoCode.charAt(1)) {
        case '2':
            container.setHeightDC();
            break;
        case '5':
            container.setHeightHC();
            break;
        case '8':  // TODO: PL container 4.3 foot high
            container.setHeightDC();
            break;
        case '9':  // TODO: PL container <4 foot high
            container.setHeightDC();
            break;        default:
            throw new RuntimeException("Json: Unsupported second char in '" + isoCode + "'");
        }
        container.put("rawIsoCode", isoCode);
        container.setGrossWeightInKg(weight);
        container.setLiveReefer(isLiveReefer);
        container.setIsEmpty(isEmpty);

        // Load move
        final Move load = Move.create(stowbase);
        if (loadPort != null) {
            load.setFromPort(loadPort);
        }
        load.setToVessel(vesselImo);
        load.setCargo(container);
        moves.add(load.getReference());

        // Discharge move
        final Move discharge = Move.create(stowbase);
        discharge.setFromVessel(vesselImo);
        if (dischargePort != null) {
            discharge.setToPort(dischargePort);
        }
        discharge.setCargo(container);
        moves.add(discharge.getReference());

        // Dangerous goods
        if (dangerousGoodsList != null) {
            container.setDangerousGoods(dangerousGoodsList);
        }
    }

    private void buildEdiFact(final EdiFactExporter ediFactExporter) {
        ediFactExporter.addContainer(containerId, isoCode, weight, overWidthRight, overWidthLeft, overHeight,
                isLiveReefer, isEmpty, bookingNumber, loadPort, dischargePort,
                dangerousGoodsList, reeferTemperature, temperatureUnit, specialStowList, slotPosition, containerCarrierCode);
    }

}
