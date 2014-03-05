package dk.ange.stowbase.edifact.parser;

import dk.ange.parserbase.DataItemParser;
import dk.ange.parserbase.ParseState;
import dk.ange.stowbase.edifact.Segment;

/**
 * A class that interfaces the Edifact parser grammar to the ContentHandler interface. It basically signals the current
 * position to the user code via the ContentHandler instance.
 *
 * @author Anders Sewerin Johansen
 *
 */
public class EdifactParseAction implements DataItemParser<Segment, ContentHandler> {

    private final String pathToParent;

    private final String countedTagName;

    /**
     *
     * @param pathToParent
     *            The path to the segment group that this Segment is part of, eg. "LOC/" or "LOC/EQD". May be the empty
     *            string.
     * @param countedTagName
     *            An unambiguous name for the tag for this Segment. If it is the second possible "LOC" segment in this
     *            group, its counted tag name is "LOC2", rather than just "LOC". The first one is always the naked tag
     *            name ("LOC") rather than a counted one ("LOC1" will never occur).
     */
    public EdifactParseAction(final String pathToParent, final String countedTagName) {
        this.pathToParent = pathToParent;
        this.countedTagName = countedTagName;
    }

    public ParseState<ContentHandler> parse(final Segment item, final ParseState<ContentHandler> parseState) {
        if (!countedTagName.startsWith(item.getTag().toString())) {
            throw new RuntimeException("Expected " + countedTagName + " to start with " + item.getTag().toString());
        }
        parseState.getResult().segment(pathToParent + countedTagName, item);
        return parseState;
    }
}
