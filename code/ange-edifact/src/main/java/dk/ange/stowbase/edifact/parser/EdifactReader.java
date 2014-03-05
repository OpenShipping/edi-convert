package dk.ange.stowbase.edifact.parser;

import java.util.Iterator;
import java.util.NoSuchElementException;

import dk.ange.parserbase.LexedPair;
import dk.ange.parserbase.ParseError;
import dk.ange.parserbase.ParseState;
import dk.ange.parserbase.lexer.GenericLexedPair;
import dk.ange.parserbase.lexer.IteratorBasedLexer;
import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.Tag;
import dk.ange.stowbase.edifact.format.SegmentGroupFormat;
import dk.ange.stowbase.edifact.lexer.SkipLastIterator;

/**
 *
 */
public class EdifactReader {

    private ContentHandler contentHandler;

    private SegmentGroupFormat group;

    /**
     * @param contentHandler
     */
    public void setContentHandler(final ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    /**
     * @param group
     */
    public void setSegmentTable(final SegmentGroupFormat group) {
        this.group = group;
    }

    /**
     * @param segments
     */
    public void parse(final Iterator<Segment> segments) {
        if (contentHandler == null) {
            throw new RuntimeException("Cannot parse before content handler has been set");
        }
        if (group == null) {
            throw new RuntimeException("Cannot parse before segment table has been set");
        }

        final ParseState<ContentHandler> initialState = new ParseState<ContentHandler>(contentHandler);

        try {
            EdifactFactory.makeSequence(group).parse(
                    new IteratorBasedLexer<Tag, Segment>(new ModifyIterator(segments)), initialState);
        } catch (final ParseError e) {
            throw new RuntimeException("Error parsing document", e);
        }
    }

    /**
     * Appends Tag.END_OF_EDIFACT_FILE, extracts Tag from Segment. Skips first and last element.
     */
    private static final class ModifyIterator implements Iterator<LexedPair<Tag, Segment>> {
        private final Iterator<Segment> iterator;

        private boolean hasSentEOF = false;

        public ModifyIterator(final Iterator<Segment> iterator) {
            iterator.next(); // Skip first element
            this.iterator = new SkipLastIterator<Segment>(iterator);
        }

        public boolean hasNext() {
            return !hasSentEOF;
        }

        public LexedPair<Tag, Segment> next() {
            if (hasSentEOF) {
                throw new NoSuchElementException();
            }
            if (iterator.hasNext()) {
                final Segment segment = iterator.next();
                return new GenericLexedPair<Tag, Segment>(segment.getTag(), segment);
            } else {
                hasSentEOF = true;
                return new GenericLexedPair<Tag, Segment>(Tag.END_OF_EDIFACT_FILE, null);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
