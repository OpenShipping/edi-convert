package dk.ange.stowbase.edifact.format;

/**
 *
 */
public class SegmentFormatBuilder extends AbstractSegmentFormatBuilder {

    /**
     *
     */
    public String tag;

    /**
     * @param position
     * @param tag
     * @param name
     * @param mandatory
     * @param occurrences
     */
    public SegmentFormatBuilder(final String position, final String tag, final String name, final boolean mandatory,
            final int occurrences) {
        super(position, name, mandatory, occurrences);
        this.tag = tag;
    }

    @Override
    public SegmentFormat build() {
        return new SegmentFormat(this);
    }

}
