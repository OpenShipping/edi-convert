package dk.ange.stowbase.parse.vessel.stacks;

import dk.ange.stowbase.parse.vessel.VesselSheetParser.TransversePositiveDirection;

/**
 * Used for reading stack LCG and TCG from sheets 'PositionX0'
 */
final class PositionAction implements StackDataAction {

    private final TransversePositiveDirection transversePositiveDirection;

    PositionAction(final TransversePositiveDirection transversePositiveDirection) {
        this.transversePositiveDirection = transversePositiveDirection;
    }

    @Override
    public void call(final StackData stackData, final String type, final String data) {
        if (type.equals("# STACK LCG")) {
            stackData.posLcg = Double.parseDouble(data);
        } else if (type.equals("# STACK TCG")) {
            stackData.posTcg = Double.parseDouble(data) * transversePositiveDirection.signForPort();
        } else {
            throw new RuntimeException("Unknown type: " + type);
        }
    }

}
