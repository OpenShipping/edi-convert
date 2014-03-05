package dk.ange.stowbase.edifact;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.ange.stowbase.edifact.scanner.CompositeDataElementSeparator;
import dk.ange.stowbase.edifact.scanner.DataElementValueToken;
import dk.ange.stowbase.edifact.scanner.EdifactScanner;
import dk.ange.stowbase.edifact.scanner.Token;

/**
 * Immutable
 *
 * @author Kim Hansen
 */
public final class Composite {

    private final DataElement standalone;

    private List<DataElement> composite = null;

    /**
     * @param tokenList
     */
    public Composite(final List<Token> tokenList) {
        if (tokenList.size() == 0) {
            standalone = null;
            composite = Collections.emptyList();
            return;
        }

        boolean lastWasValue;
        final Token token0 = tokenList.get(0);
        if (token0 instanceof CompositeDataElementSeparator) {
            standalone = null;
            lastWasValue = false;
        } else if (token0 instanceof DataElementValueToken) {
            standalone = new DataElement(((DataElementValueToken) token0).getString());
            if (tokenList.size() == 1) {
                return;
            }
            lastWasValue = true;
        } else {
            throw new IllegalArgumentException("token0=" + token0);
        }

        composite = new ArrayList<>();
        composite.add(standalone);
        for (int tNo = 1; tNo < tokenList.size(); ++tNo) {
            final Token token = tokenList.get(tNo);
            if (token instanceof CompositeDataElementSeparator) {
                if (!lastWasValue) {
                    composite.add(null);
                }
                lastWasValue = false;
            } else if (token instanceof DataElementValueToken) {
                if (lastWasValue) {
                    throw new IllegalArgumentException("token=" + token);
                }
                composite.add(new DataElement(((DataElementValueToken) token).getString()));
                lastWasValue = true;
            } else {
                throw new IllegalArgumentException("token=" + token);
            }
        }
        composite = Collections.unmodifiableList(composite);
    }

    /**
     * Simple constructor that will transform all input to data elements in the composite
     *
     * @param dataElements
     */
    public Composite(final String... dataElements) {
        if (dataElements.length == 0) {
            standalone = null;
            composite = Collections.emptyList();
        } else {
            standalone = new DataElement(dataElements[0]);
            if (dataElements.length == 1) {
                composite = null;
            } else {
                final ArrayList<DataElement> list = new ArrayList<>();
                for (final String dataelement : dataElements) {
                    list.add(new DataElement(dataelement));
                }
                composite = list;
            }
        }
    }

    /**
     * @return Returns the value of the DataElement.
     * @throws IllegalStateException
     *             if the DataElement is a composite data element.
     */
    public DataElement getStandalone() {
        if (composite != null && composite.size() > 1) {
            throw new IllegalStateException("composite=" + composite);
        }
        return standalone;
    }

    /**
     * @return Returns a list with the values of the DataElements. If the DataElement is a stand alone data element the
     *         it will return a list with the the value as the only item in the list.
     */
    public List<DataElement> getComposite() {
        if (composite != null) {
            return composite;
        }
        // We have one item, create a list and cache it
        composite = new ArrayList<>(1);
        composite.add(standalone);
        composite = Collections.unmodifiableList(composite);
        return composite;
    }

    /**
     * Writes the Segment to the stream in the EDIFACT format.
     *
     * @param stream
     * @throws IOException
     *             if the stream throws an IOException.
     */
    public void write(final OutputStream stream) throws IOException {
        boolean first = true;
        for (final DataElement value : getComposite()) {
            if (!first) {
                stream.write(CompositeDataElementSeparator.BYTE);
            }
            first = false;
            if (value != null) {
                final byte[] bytes = value.toString().getBytes("Latin1");
                for (final byte b : bytes) {
                    if (EdifactScanner.isServiceCharacter(b)) {
                        stream.write(EdifactScanner.RELEASE_CHARACTER);
                    }
                    stream.write(b);
                }
            }
        }
    }

    @Override
    public String toString() {
        if (standalone == null) {
            return "Composite[]";
        }
        if (composite != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Composite[");
            sb.append(composite.get(0).toString());
            for (final DataElement de : composite.subList(1, composite.size())) {
                sb.append(':');
                sb.append(de.toString());
            }
            sb.append(']');
            return sb.toString();
        } else {
            return standalone.toString();
        }
    }

}
