package dk.ange.stowbase.parse.vessel.stacks;

/**
 * Data for a single stack, struct
 */
public class StackData {

    int tierTop = 0;

    int tierBottom = 0;

    int reeferTierTop = 0;

    int reeferTierBottom = 0;

    int fourtyfiveTierTop = 0;

    int fourtyfiveTierBottom = 0;

    double posTop = Double.NaN;

    double posBottom = Double.NaN;

    double posLcg = Double.NaN;

    double posTcg = Double.NaN;

    double maxWeight = Double.NaN;

    /**
     * Mark if IMO is forbidden in this stack
     */
    public boolean imoForbidden = false;

    @Override
    public String toString() {
        return "StackData[maxWeight=" + maxWeight + ", posBottom=" + posBottom + ", posLcg=" + posLcg + ", posTcg="
                + posTcg + ", posTop=" + posTop + ", tierBottom=" + tierBottom + ", tierTop=" + tierTop
                + ", reeferTierBottom=" + reeferTierBottom + ", reeferTierTop=" + reeferTierTop
                + ", fourtyfiveTierTop=" + fourtyfiveTierTop + ", fourtyfiveTierBotton=" + fourtyfiveTierBottom
                + "', imoForbidden=" + imoForbidden + "]";
    }

}
