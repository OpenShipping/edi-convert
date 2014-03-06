package dk.ange.stowbase.parse.utils;

/**
 * Object that can register warnings about a sheet
 */
public interface SheetWarner {

    /**
     * @param warning
     *            Warning to add to the messages
     */
    public abstract void addSheetWarning(String warning);

}
