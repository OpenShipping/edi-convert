package dk.ange.stowbase.parse.vessel.stacks;

/**
 * Save reefer positions. Understands types "# TOP REEFER TIER" and "# BOTTOM REEFER TIER".
 */
class ReeferAction implements StackDataAction {

    /**
     * An instance that all can use, it is stateless
     */
    public static final ReeferAction INSTANCE = new ReeferAction();

    @Override
    public void call(final StackData stackData, final String type, final String string) {
        if (type.equals("# TOP REEFER TIER")) {
            stackData.reeferTierTop = Integer.parseInt(string);
        } else if (type.equals("# BOTTOM REEFER TIER")) {
            stackData.reeferTierBottom = Integer.parseInt(string);
        } else {
            throw new RuntimeException("Unknown type " + type);
        }
        if ( stackData.reeferTierTop != 0 && stackData.reeferTierBottom != 0
                &&  stackData.reeferTierTop < stackData.reeferTierBottom){
            throw new RuntimeException("reefer top tier < reefer bottom tier: "
                + stackData.reeferTierTop + " < " + stackData.reeferTierBottom);
        }
    }

}
