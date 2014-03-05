package dk.ange.stowbase.edifact;

import java.util.HashMap;
import java.util.Map;

/**
 * The three letter tag that marks the meaning of each segment.
 */
public final class Tag {

    private static final Map<String, Tag> instances = new HashMap<>();

    /**
     * The synthetic end-of-file marker used in parsing of EDIFACT files.
     */
    public final static Tag END_OF_EDIFACT_FILE = new Tag("END_OF_EDIFACT_FILE");

    /**
     * @param string
     * @return Returns the Tag
     */
    public static Tag getInstance(final String string) {
        if (!string.matches("[A-Z]{3}")) {
            throw new RuntimeException("!string.matches(\"[A-Z]{3}\"), string='" + string + "'");
        }
        Tag tag = instances.get(string);
        if (tag != null) {
            return tag;
        }
        tag = new Tag(string);
        instances.put(string, tag);
        return tag;
    }

    private final String string;

    private Tag(final String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

}
