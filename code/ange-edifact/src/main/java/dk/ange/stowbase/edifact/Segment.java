package dk.ange.stowbase.edifact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.ange.stowbase.edifact.scanner.DataElementSeparator;
import dk.ange.stowbase.edifact.scanner.DataElementValueToken;
import dk.ange.stowbase.edifact.scanner.SegmentTerminator;
import dk.ange.stowbase.edifact.scanner.Token;

/**
 * Immutable
 */
public final class Segment {

    private final List<Composite> dataElements;

    private final Tag tag;

    /**
     * @param tokenList
     */
    public Segment(final List<Token> tokenList) {
        tag = initTag(tokenList);
        try {
            // Second Token is a DataElementSeparator
            if (!(tokenList.get(1) instanceof DataElementSeparator)) {
                throw new IllegalArgumentException("tokenList.get(1)=" + tokenList.get(1));
            }
            // Parse the rest
            this.dataElements = parse(tokenList.subList(2, tokenList.size()));
        } catch (final RuntimeException e) {
            throw new IllegalArgumentException("tokenList=" + tokenList, e);
        }
    }

    /**
     * @param tag
     * @param dataElements
     */
    public Segment(final Tag tag, final List<Composite> dataElements) {
        this.tag = tag;
        this.dataElements = new ArrayList<Composite>(dataElements);
    }

    private static Tag initTag(final List<Token> tokenList) {
        // First Token is the Tag
        if (!(tokenList.get(0) instanceof DataElementValueToken)) {
            throw new IllegalArgumentException("tokenList.get(0)=" + tokenList.get(0));
        }
        final DataElementValueToken tagToken = (DataElementValueToken) tokenList.get(0);
        return Tag.getInstance(tagToken.getString());
    }

    private List<Composite> parse(final List<Token> tokenList) {
        final List<Composite> dataElements1 = new ArrayList<Composite>();
        int firstToken = 0;
        for (int lastToken = 0; lastToken <= tokenList.size(); ++lastToken) {
            if (lastToken != tokenList.size() && !(tokenList.get(lastToken) instanceof DataElementSeparator)) {
                continue;
            }
            dataElements1.add(new Composite(tokenList.subList(firstToken, lastToken)));
            firstToken = lastToken + 1;
        }
        return Collections.unmodifiableList(dataElements1);
    }

    /**
     * @param i
     *            primary number of element, 0-based and not counting the tag
     * @param j
     *            secondary number of element, 0-based
     * @param default_
     *            value to return if element is not in segment
     * @return the numbered member or default_
     */
    public String get(final int i, final int j, final String default_) {
        if (i < 0 || j < 0) {
            throw new IllegalArgumentException("i[" + i + "] < 0 || j[" + j + "] < 0");
        }
        if (i >= dataElements.size()) {
            return default_;
        }
        final List<DataElement> list = dataElements.get(i).getComposite();
        if (j >= list.size()) {
            return default_;
        }
        final DataElement dataElement = list.get(j);
        if (dataElement == null) {
            return default_;
        } else {
            return dataElement.getString();
        }
    }

    /**
     * @param stream
     * @throws IOException
     */
    public void write(final OutputStream stream) throws IOException {
        stream.write(tag.toString().getBytes("Latin1"));
        stream.write(DataElementSeparator.BYTE);
        boolean first = true;
        for (final Composite dataElement : dataElements) {
            if (!first) {
                stream.write(DataElementSeparator.BYTE);
            }
            first = false;
            dataElement.write(stream);
        }
        stream.write(SegmentTerminator.BYTE);
    }

    @Override
    public String toString() {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write("Segment[".getBytes());
            write(os);
            os.write(']');
        } catch (final IOException e) {
            throw new RuntimeException(); // Should never happen
        }
        return os.toString();
    }

    /**
     * @return Returns the segments tag.
     */
    public final Tag getTag() {
        return tag;
    }

    /**
     * @return number of primary data elements in Segment
     */
    public int size() {
        return dataElements.size();
    }

    /**
     * @param i
     * @return number of secondary data elements in data element i
     */
    public int size(final int i) {
        return dataElements.get(i).getComposite().size();
    }

}
