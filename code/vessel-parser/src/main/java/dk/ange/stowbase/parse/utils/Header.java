package dk.ange.stowbase.parse.utils;

import java.util.Map;

/**
 * A wrapper around string that makes it compare less strict. It ignores space and case.
 * 
 * <p>
 * <code>
 * import static dk.ange.stowbase.parse.utils.Header.header;
 * </code>
 */
public final class Header {

    private final String header;

    private final String simple;

    /**
     * Simple constructor
     * 
     * @param header
     */
    private Header(final String header) {
        this.header = header;
        this.simple = headerSimplify(header);
    }

    /**
     * Factory
     * 
     * @param string
     * @return header
     */
    public static Header header(final String string) {
        if (string == null) {
            return null;
        }
        return new Header(string);
    }

    private static String headerSimplify(final String header) {
        return header.replace(" ", "").toLowerCase();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((simple == null) ? 0 : simple.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Header)) {
            return false;
        }
        final Header other = (Header) obj;
        if (simple == null) {
            if (other.simple != null) {
                return false;
            }
        } else if (!simple.equals(other.simple)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return header;
    }

    /**
     * Check that the header is in the map and return the value. If not throw {@link ParseException}.
     * 
     * @param keyMap
     * @param headerString
     * @return Column number
     */
    public static int headerColumnMadatory(final Map<Header, Integer> keyMap, final String headerString) {
        final Header header = header(headerString);
        if (!keyMap.containsKey(header)) {
            throw new ParseException("Could not find header '" + header + "' among " + keyMap.keySet());
        }
        return keyMap.get(header);
    }

    /**
     * Check that the header is in the map and return the value. If not return -1
     * 
     * @param keyMap
     * @param headerString
     * @return Column number
     */
    public static int headerColumnOptional(final Map<Header, Integer> keyMap, final String headerString) {
        final Header header = header(headerString);
        if (!keyMap.containsKey(header)) {
            return -1;
        }
        return keyMap.get(header);
    }

}
