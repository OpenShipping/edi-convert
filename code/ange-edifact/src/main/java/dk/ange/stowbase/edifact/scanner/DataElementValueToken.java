package dk.ange.stowbase.edifact.scanner;

/**
 * @author Kim Hansen
 */
public final class DataElementValueToken implements Token {

    private final String string;

    DataElementValueToken(final String string) {
        this.string = string;
    }

    /**
     * @return Returns the string in the data element
     */
    public String getString() {
        return string;
    }

    @Override
    public String toString() {
        return "DataElementValueToken:\"" + string + "\"";
    }

}
