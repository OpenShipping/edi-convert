package dk.ange.stowbase.parse.vessel.stacks;

/**
 * Save stack top and bottom. Understands the types "# STACK POSITION" and "# STACK HEIGHT".
 */
final class HeightAction implements StackDataAction {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HeightAction.class);

    /**
     * An instance that all can use, it is state less
     */
    public static final HeightAction INSTANCE = new HeightAction();

    @Override
    public void call(final StackData stackData, final String type, final String data) {
        if (type.equals("# STACK POSITION")) {
            log.trace("Stack Height String = {}", data);
            stackData.posBottom = Double.parseDouble(data);
            log.trace("Stack Bottom Position = {}", stackData.posBottom);
        } else if (type.equals("# STACK HEIGHT")) {
            stackData.posTop = stackData.posBottom + Double.parseDouble(data);
            // Here has to come an exeption if the stackData.posTop unset.
            log.trace("Stack Height = {}", stackData.posTop);
        } else {
            throw new RuntimeException("Unknown type: " + type);
        }
    }

}
