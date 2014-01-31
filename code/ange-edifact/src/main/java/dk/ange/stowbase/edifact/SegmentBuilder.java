package dk.ange.stowbase.edifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple Segment builder.
 */
public final class SegmentBuilder {

    private Tag tag = null;

    private final List<Composite> dataElements = new ArrayList<Composite>();

    /**
     * @param string
     */
    public void setTag(final String string) {
        if (string == null) {
            throw new NullPointerException("string == null");
        }
        if (tag != null) {
            throw new IllegalStateException("Old tag = '" + tag + "', expected null");
        }
        this.tag = Tag.getInstance(string);
    }

    /**
     * Use data to create a new composite and set it at position i, if the composite is not long enough it will be
     * extended with empty composites.
     * 
     * @param i
     * @param data
     */
    public void set(final int i, final String... data) {
        try {
            while (i < dataElements.size()) {
                dataElements.add(new Composite());
            }
            if (i < dataElements.size()) {
                dataElements.set(i, new Composite(data));
            } else {
                dataElements.add(new Composite(data));
            }
        } catch (final RuntimeException e) {
            throw new RuntimeException(this + ".set(" + i + ", " + Arrays.toString(data) + ") failed with: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Build and clear
     * 
     * @return Build the Segment
     */
    public Segment build() {
        try {
            return new Segment(tag, dataElements);
        } finally {
            tag = null;
            dataElements.clear();
        }
    }

    @Override
    public String toString() {
        return "" + tag + dataElements;
    }

}
