package dk.ange.stowbase.parse.vessel.stacks;

/**
 * Save reefer positions. Understands types "# TOP REEFER TIER" and "# BOTTOM REEFER TIER".
 */
class FourtyFiveAction implements StackDataAction {

    /**
     * An instance that all can use, it is stateless
     */
    public static final FourtyFiveAction INSTANCE = new FourtyFiveAction();

    @Override
    public void call(final StackData stackData, final String type, final String string) {
        if (type.equals("# TOP TIER")) {
            stackData.fourtyfiveTierTop = Integer.parseInt(string);
        } else if (type.equals("# BOTTOM TIER")) {
            stackData.fourtyfiveTierBottom = Integer.parseInt(string);
        } else {
            throw new RuntimeException("Unknown type " + type);
        }
        if ( stackData.fourtyfiveTierTop != 0 && stackData.fourtyfiveTierBottom != 0
                &&  stackData.fourtyfiveTierTop < stackData.fourtyfiveTierBottom){
            throw new RuntimeException("45 top tier < 45 bottom tier: "
                + stackData.fourtyfiveTierTop + " < " + stackData.fourtyfiveTierBottom);
        }
    }

}
