package dk.ange.stowbase.parse.vessel.stacks;

/**
 * An action is a way to record StackData
 */
public interface StackDataAction {

    /**
     * Save data in stackData based on its type
     * 
     * @param stackData
     *            object to store data in
     * @param type
     *            type of data
     * @param data
     *            the data to store
     */
    public void call(final StackData stackData, final String type, final String data);

}
