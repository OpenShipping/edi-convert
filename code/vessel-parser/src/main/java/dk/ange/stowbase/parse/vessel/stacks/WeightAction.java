package dk.ange.stowbase.parse.vessel.stacks;

/**
 * Save weight. Understands the type "# STACK WEIGHT".
 */
class WeightAction implements StackDataAction {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WeightAction.class);

    /**
     * An instance that all can use, it is stateless
     */
    public static final WeightAction INSTANCE = new WeightAction();

    @Override
    public void call(final StackData stackData, final String type, final String string) {
        if (type.equals("# STACK WEIGHT")) {
            stackData.maxWeight = Double.parseDouble(string) * 1000;
            log.trace("Stack Weight = {}", stackData.maxWeight);
        }
    }

}
