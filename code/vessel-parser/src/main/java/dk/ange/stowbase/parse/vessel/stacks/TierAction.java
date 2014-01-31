package dk.ange.stowbase.parse.vessel.stacks;

/**
 * Save tier positions. Understands types "# TOP TIER" and "# BOTTOM TIER".
 */
class TierAction implements StackDataAction {

    /**
     * An instance that all can use, it is stateless
     */
    public static final TierAction INSTANCE = new TierAction();

    @Override
    public void call(final StackData stackData, final String type, final String string) {
        if (type.equals("# TOP TIER")) {
            stackData.tierTop = Integer.parseInt(string);
        } else if (type.equals("# BOTTOM TIER")) {
            stackData.tierBottom = Integer.parseInt(string);
        } else {
            throw new RuntimeException("Unknown type " + type);
        }
        if ( stackData.tierTop != 0 && stackData.tierBottom != 0
                &&  stackData.tierTop < stackData.tierBottom){
            throw new RuntimeException("top tier < bottom tier: "
                + stackData.tierTop + " < " + stackData.tierBottom);
        }
    }

}
