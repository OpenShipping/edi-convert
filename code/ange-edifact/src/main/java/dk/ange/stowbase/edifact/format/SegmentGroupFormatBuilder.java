package dk.ange.stowbase.edifact.format;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SegmentGroupFormatBuilder extends AbstractSegmentFormatBuilder {

    /**
     *
     */
    public List<AbstractSegmentFormatBuilder> members;

    /**
     *
     */
    public int groupBoundaryColumn;

    /**
     * @param position
     * @param name
     * @param mandatory
     * @param occurrences
     * @param groupBoundaryColumn
     */
    public SegmentGroupFormatBuilder(final String position, final String name, final boolean mandatory,
            final int occurrences, final int groupBoundaryColumn) {
        super(position, name, mandatory, occurrences);
        this.members = new ArrayList<AbstractSegmentFormatBuilder>();
        this.groupBoundaryColumn = groupBoundaryColumn;
    }

    @Override
    public SegmentGroupFormat build() {
        return new SegmentGroupFormat(this);
    }

}
