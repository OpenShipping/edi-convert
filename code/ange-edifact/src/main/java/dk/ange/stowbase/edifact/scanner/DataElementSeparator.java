package dk.ange.stowbase.edifact.scanner;

/**
 * @author Kim Hansen
 */
public final class DataElementSeparator implements Token {

    /** The segment terminator byte in the edifact file */
    public static final byte BYTE = '+';

    /** The single instance of the SegmentTerminator */
    static final Token INSTANCE = new DataElementSeparator();

    private DataElementSeparator() {
        // Empty
    }

    @Override
    public String toString() {
        return "DataElementSeparator";
    }

}
