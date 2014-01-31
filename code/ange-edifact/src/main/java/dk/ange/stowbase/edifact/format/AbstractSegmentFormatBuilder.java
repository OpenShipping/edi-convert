package dk.ange.stowbase.edifact.format;

/**
 * Base class for a segment format
 */
public abstract class AbstractSegmentFormatBuilder {

    /**
     * 
     */
    public String position;

    /**
     * 
     */
    public String name;

    /**
     * 
     */
    public boolean mandatory;

    /**
     * 
     */
    public int occurrences;

    AbstractSegmentFormatBuilder(final String position, final String name, final boolean mandatory,
            final int occurrences) {
        this.position = position;
        this.name = name;
        this.mandatory = mandatory;
        this.occurrences = occurrences;
    }

    /**
     * @return TODO
     */
    abstract public AbstractSegmentFormat build();

}
