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
 * @author Martin Westring Sørensen Container class to hold information about a variable tank
 */
public class varTank {
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

    /**
     * Create a variable tank object
     * 
     * @param sbObjectFactory
     */
    public varTank(final StowbaseObjectFactory sbObjectFactory) {
        stowbaseObjectFactory = sbObjectFactory;
    }

    final StowbaseObjectFactory stowbaseObjectFactory;

    String group;

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
        vartank.put("lcgFunction", vartankCurve("lcg", lcgfunction));
        vartank.put("vcgFunction", vartankCurve("vcg", vcgfunction));
        vartank.put("tcgFunction", vartankCurve("tcg", tcgfunction));
        vartank.put("fsmFunction", vartankCurve("fsm", fsmfunction));

        return vartank;
    }

    private Reference vartankCurve(final String functiontype, final Map<Double, Double> data) {
        final LinearInterpolation2d Curve = LinearInterpolation2d.create(stowbaseObjectFactory);
        Curve.setInput1("volume");
        Curve.setInput2("dummy");
        Curve.setOutput(functiontype);
        final List<Double> volumes = new ArrayList<Double>();
        final List<Double> datavalues = new ArrayList<Double>();
        volumes.addAll(data.keySet());
        for (final Double volume : volumes) {
            datavalues.add(data.get(volume));
        }
        Curve.setSamplePoints1(volumes);
        Curve.setSamplePoints2(Arrays.asList(new Double[] { 0.0 }));
        Curve.setSampleData(datavalues);

        return Curve.getReference();
    }

}
