package dk.ange.stowbase.parse.vessel.stability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.stowbase.client.Reference;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.LinearInterpolation2d;

/**
 * Container class to hold information about a variable tank
 */
public class VarTank {

    private final StowbaseObjectFactory stowbaseObjectFactory;

    String description;

    double massCapacity;

    double volCapacity;

    double foreEnd;

    double aftEnd;

    double density;

    Map<Double, Double> lcgfunction;

    Map<Double, Double> vcgfunction;

    Map<Double, Double> tcgfunction;

    Map<Double, Double> fsmfunction;

    // Used in TanksParser
    String group;

    /**
     * Create a variable tank object
     *
     * @param sbObjectFactory
     */
    public VarTank(final StowbaseObjectFactory sbObjectFactory) {
        stowbaseObjectFactory = sbObjectFactory;
    }

    // Used in TanksParser
    StowbaseObject toStowbaseObject() {
        final StowbaseObject vartank = stowbaseObjectFactory.create("tank");
        vartank.put("description", description);
        vartank.put("capacityInM3", volCapacity);
        vartank.put("capacityInKg", massCapacity);
        vartank.put("densityInKgprM3", density);
        vartank.put("foreEndInM", foreEnd);
        vartank.put("aftEndInM", aftEnd);
        if (group != null) {
            vartank.put("group", group);
        }
        vartank.put("lcgFunction", varTankCurve("lcg", lcgfunction));
        vartank.put("vcgFunction", varTankCurve("vcg", vcgfunction));
        vartank.put("tcgFunction", varTankCurve("tcg", tcgfunction));
        vartank.put("fsmFunction", varTankCurve("fsm", fsmfunction));
        return vartank;
    }

    private Reference varTankCurve(final String functiontype, final Map<Double, Double> data) {
        final LinearInterpolation2d curve = LinearInterpolation2d.create(stowbaseObjectFactory);
        curve.setInput1("volume");
        curve.setInput2("dummy");
        curve.setOutput(functiontype);
        final List<Double> volumes = new ArrayList<>();
        final List<Double> dataValues = new ArrayList<>();
        volumes.addAll(data.keySet());
        for (final Double volume : volumes) {
            dataValues.add(data.get(volume));
        }
        curve.setSamplePoints1(volumes);
        curve.setSamplePoints2(Arrays.asList(0.0));
        curve.setSampleData(dataValues);
        return curve.getReference();
    }

}
