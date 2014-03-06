package dk.ange.stowbase.parse.vessel.stacks;

import dk.ange.stowbase.parse.utils.SheetWarner;

/**
 * Sets the imoForbidden based on content of "# DG STACK RULES" sections
 */
public class DgStacksAction implements StackDataAction {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DgStacksAction.class);

    private final SheetWarner sheetWarner;

    /**
     * @param sheetWarner
     */
    public DgStacksAction(final SheetWarner sheetWarner) {
        this.sheetWarner = sheetWarner;
    }

    @Override
    public void call(final StackData stackData, final String type, final String data) {
        if (type.equals("# DG STACK RULES")) {
            log.trace("Stack rules String = {}", data);
            switch (data) {
            case "Z":
                stackData.imoForbidden = true;
                break;
            default:
                sheetWarner.addSheetWarning("Unknown rule '" + data + "' ignored. The known rule is 'Z'.");
                break;
            }
        } else {
            throw new RuntimeException("Unknown type: " + type);
        }
    }

}
