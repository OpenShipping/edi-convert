package dk.ange.stowbase.edifact.format;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SegmentGroupFormat extends AbstractSegmentFormat {

    private final List<AbstractSegmentFormat> members;

    /**
     * @param position
     * @param name
     * @param mandatory
     * @param occurrences
     * @param members
     *            Must not be changed after use in this constructor
     */
    public SegmentGroupFormat(final String position, final String name, final boolean mandatory, final int occurrences,
            final List<AbstractSegmentFormat> members) {
        super(position, name, mandatory, occurrences);
        this.members = Collections.unmodifiableList(members);
    }

    /**
     * @param builder
     */
    public SegmentGroupFormat(final SegmentGroupFormatBuilder builder) {
        super(builder);
        final List<AbstractSegmentFormat> newMembers = new ArrayList<AbstractSegmentFormat>(builder.members.size());
        for (final AbstractSegmentFormatBuilder member : builder.members) {
            newMembers.add(member.build());
        }
        this.members = Collections.unmodifiableList(newMembers);
    }

    /**
     * @return the members
     */
    public List<AbstractSegmentFormat> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return SegmentGroupFormat.class.getSimpleName() + "[" + getPosition() + ", '" + getName() + "', "
                + (isMandatory() ? "M" : "C") + ", " + getOccurrences() + "]";
    }

    @Override
    public void write(final PrintStream out, final String prefix) {
        out.println(prefix + this.toString());
        for (final AbstractSegmentFormat member : members) {
            member.write(out, prefix + "  ");
        }
    }

}
