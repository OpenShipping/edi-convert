package dk.ange.stowbase.edifact.scanner;

/**
 * @author Kim Hansen
 */
public final class CompositeDataElementSeparator implements Token {

    /** The segment terminator byte in the edifact file */
    public static final byte BYTE = ':';

    /** The single instance of the SegmentTerminator */
    static final Token INSTANCE = new CompositeDataElementSeparator();

    private CompositeDataElementSeparator() {
        // Empty
    }

    @Override
    public String toString() {
        return "CompositeDataElementSeparator";
    }

}
