package dk.ange.stowbase.parse.vessel.dg;

import org.stowbase.client.objects.VesselProfile;

/**
 * An object that can add data to the vessel profile
 */
public interface VesselProfileDataAdder {

    /**
     * @param vesselProfile
     */
    public abstract void addDataToVesselProfile(VesselProfile vesselProfile);

}
