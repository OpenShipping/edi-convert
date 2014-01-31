package dk.ange.stowbase.edifact.format;

import java.io.PrintStream;

import dk.ange.stowbase.edifact.Tag;

/**
 *
 */
public class SegmentFormat extends AbstractSegmentFormat {

    private final Tag tag;

    /**
     * @param position
     * @param tag
     * @param name
     * @param mandatory
     * @param occurrences
     */
    public SegmentFormat(final String position, final String tag, final String name, final boolean mandatory,
            final int occurrences) {
        super(position, name, mandatory, occurrences);
        this.tag = Tag.getInstance(tag);
    }

    /**
     * @param builder
     */
    public SegmentFormat(final SegmentFormatBuilder builder) {
        super(builder);
        this.tag = Tag.getInstance(builder.tag);
    }

    /**
     * @return the tag
     */
    public Tag getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return SegmentFormat.class.getSimpleName() + "[" + tag + ", " + getPosition() + ", '" + getName() + "', "
                + (isMandatory() ? "M" : "C") + ", " + getOccurrences() + "]";
    }

    @Override
    public void write(final PrintStream out, final String prefix) {
        out.println(prefix + this.toString());
    }

}
