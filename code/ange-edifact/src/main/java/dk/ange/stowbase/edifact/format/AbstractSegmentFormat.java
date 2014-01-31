package dk.ange.stowbase.edifact.format;

import java.io.PrintStream;

/**
 * Base class for a segment format
 */
public abstract class AbstractSegmentFormat {

    private final String position;

    private final String name;

    private final boolean mandatory;

    private final int occurrences;

    AbstractSegmentFormat(final String position, final String name, final boolean mandatory, final int occurrences) {
        this.position = position;
        this.name = name;
        this.mandatory = mandatory;
        this.occurrences = occurrences;
    }

    AbstractSegmentFormat(final AbstractSegmentFormatBuilder builder) {
        this.position = builder.position;
        this.name = builder.name;
        this.mandatory = builder.mandatory;
        this.occurrences = builder.occurrences;
    }

    /**
     * @return the position
     */
    public String getPosition() {
        return position;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @return the occurrences
     */
    public int getOccurrences() {
        return occurrences;
    }

    /**
     * @param out
     * @param prefix
     */
    abstract public void write(PrintStream out, String prefix);

}
