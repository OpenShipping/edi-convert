package dk.ange.stowbase.edifact;

/**
 * Immutable
 */
public final class DataElement {

    private final String string;

    /**
     * @param string
     */
    public DataElement(final String string) {
        if (string == null) {
            throw new NullPointerException("string cannot be null");
        }
        this.string = string;
    }

    /**
     * @return Returns String value.
     */
    public String getString() {
        return string;
    }

    @Override
    public String toString() {
        return string;
    }

}
