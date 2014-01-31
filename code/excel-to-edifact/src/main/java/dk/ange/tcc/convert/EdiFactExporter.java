package dk.ange.tcc.convert;

import java.util.List;

import org.stowbase.client.objects.DangerousGoods;

/**
 * @author Marc Cromme
 * Interface to encapsulate COPRAR and BAPLI exporters
 */
public interface EdiFactExporter {

    /**
     * Add a container to the BAPLIE
     *
     * @param containerId
     * @param isocode
     * @param weight  kg
     * @param overWidthRight cm
     * @param overWidthLeft cm
     * @param overHeight cm
     * @param liveReefer boolean
     * @param isEmpty boolean
     * @param bookingNumber
     * @param loadPort
     * @param dischargePort
     * @param dangerousGoodsList
     * @param reeferTemperature
     * @param temperatureUnit (CEL|FAH) only, no safty check
     * @param specialStowList list of special stow codes
     * @param slotPosition in the number format bbbrrtt
     * @param containerCarrierCode
     */
    public abstract void addContainer(String containerId, String isocode, int weight, Integer overWidthRight,
            Integer overWidthLeft, Integer overHeight, boolean liveReefer, boolean isEmpty, String bookingNumber,
            String loadPort, String dischargePort, List<DangerousGoods> dangerousGoodsList, Double reeferTemperature,
            String temperatureUnit, List<String> specialStowList, String slotPosition, final String containerCarrierCode);

    /**
     * Write out the entire Baplie
     */
    public abstract void flush();

    /**
     * @param loadPort
     */
    public abstract void setLoadPort(String loadPort);

    /**
     * @param vesselImo
     */
    public abstract void setVesselImo(String vesselImo);

    /**
     * @param voyageCode
     */
    public abstract void setVoyageCode(String voyageCode);

}
