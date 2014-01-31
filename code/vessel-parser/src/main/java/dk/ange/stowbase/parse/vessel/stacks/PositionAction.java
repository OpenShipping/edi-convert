package dk.ange.stowbase.parse.vessel.stacks;

/**
 * Used for reading stack LCG and TCG from sheets 'PositionX0'
 */
final class PositionAction implements StackDataAction {

    /**
     * An instance that all can use, it is state less
     */
    public static final PositionAction INSTANCE = new PositionAction();

    @Override
    public void call(final StackData stackData, final String type, final String data) {
        if (type.equals("# STACK LCG")) {
            stackData.posLcg = Double.parseDouble(data);
        } else if (type.equals("# STACK TCG")) {
            stackData.posTcg = Double.parseDouble(data);
        } else {
            throw new RuntimeException("Unknown type: " + type);
        }
    }

}
