package dk.ange.parserbase.factoryhelperclasses;

import junit.framework.TestCase;
import dk.ange.parserbase.DataItemParser;
import dk.ange.parserbase.ItemProvider;
import dk.ange.parserbase.LexedPair;
import dk.ange.parserbase.LexerType;
import dk.ange.parserbase.ParseError;
import dk.ange.parserbase.ParseState;
import dk.ange.parserbase.Sequence;
import dk.ange.parserbase.SingleItemSequence;

/**
 * Test the base class for sequences that have optional parts, and need to be backpatched when stitched together.
 */
public class TestBackpatchableFollowedBy extends TestCase {

    private static class Helper extends BackpatchableFollowedBy<LexerType, String, Integer> {

        LexedPair<LexerType, String> consumed = null;

        Sequence<LexerType, String, Integer> seenOptionalPart = null;

        Sequence<LexerType, String, Integer> seenFollowedBy = null;

        Helper(final Sequence<LexerType, String, Integer> optionalPart) {
            super(optionalPart);
        }

        @Override
        protected Sequence<LexerType, String, Integer> makeWrapped(
                final Sequence<LexerType, String, Integer> optionalPart1,
                final Sequence<LexerType, String, Integer> followedBy) {

            seenOptionalPart = optionalPart1;
            seenFollowedBy = followedBy;

            return new Sequence<LexerType, String, Integer>() {

                public boolean consumes(final LexerType type) {
                    return optionalPart1.consumes(type) || followedBy.consumes(type);
                }

                @SuppressWarnings("unused")
                public ParseState<Integer> parse(final ItemProvider<LexerType, String> itemProvider,
                        final ParseState<Integer> initialState) throws ParseError {
                    consumed = itemProvider.pop();
                    return initialState;
                }
            };
        }

    }

    /**
     * Test simple creation.
     */
    public void testCanCreate() {
        new Helper(null);
    }

    /**
     * Does absolutely nothing.
     */
    protected final DataItemParser<String, Integer> neutral = new DataItemParser<String, Integer>() {

        public ParseState<Integer> parse(final String item, final ParseState<Integer> parseState) {
            return parseState;
        }

    };

    /**
     * Test setFollowedBy works as expected
     */
    public void testSetFollowedBy() {
        final Sequence<LexerType, String, Integer> optional = new SingleItemSequence<LexerType, String, Integer>(
                LexerType.FIRST_KIND, neutral);
        final Sequence<LexerType, String, Integer> followedBy = new SingleItemSequence<LexerType, String, Integer>(
                LexerType.SECOND_KIND, neutral);

        final Helper h = new Helper(optional);
        assertNull(h.consumed);
        assertNull(h.seenFollowedBy);
        assertNull(h.seenOptionalPart);

        h.setFollowedBy(followedBy);

        assertNull(h.consumed);
        assertSame(followedBy, h.seenFollowedBy);
        assertSame(optional, h.seenOptionalPart);

        assertTrue(h.consumes(LexerType.FIRST_KIND));
        assertTrue(h.consumes(LexerType.SECOND_KIND));
        assertFalse(h.consumes(LexerType.THIRD_KIND));

    }

}
