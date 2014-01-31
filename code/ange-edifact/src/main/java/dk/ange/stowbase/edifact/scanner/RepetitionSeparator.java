package dk.ange.stowbase.edifact.scanner;

/**
 * @author Kim Hansen
 */
public final class RepetitionSeparator implements Token {

    /** The repetition separator byte in the edifact file */
    public static final byte BYTE = '*';

    /** The single instance of the RepetitionSeparator */
    static final Token INSTANCE = new RepetitionSeparator();

    private RepetitionSeparator() {
        // Empty
    }

    @Override
    public String toString() {
        return "RepetitionSeparator";
    }

}
