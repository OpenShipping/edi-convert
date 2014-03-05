package dk.ange.parserbase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import dk.ange.parserbase.lexer.GenericLexedPair;
import dk.ange.parserbase.lexer.IteratorBasedLexer;

/**
 * Tests for compound sequence
 */
public class TestCompoundSequence extends TestCase {

    /**
     * Test the simplest possible creation sequence.
     */
    public void testCanCreate() {
        makeSequence(null, null);
    }

    /**
     * Document that compound sequence will reject an empty sequence.
     */
    @SuppressWarnings("unused")
    public void testRejectsEmptySequence() {
        try {
            new CompoundSequence<LexerType, String, Integer>(new LinkedList<Sequence<LexerType, String, Integer>>());
            fail("Should throw");
        } catch (final IllegalStateException e) { // NOPMD Expected
            // Expected
        }
    }

    /**
     * Test that a sequence correctly sets the "consumes" item.
     */
    public void testConsumes() {

        final Sequence<LexerType, String, Integer> seq = makeSequence(null, null);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        assertFalse(seq.consumes(LexerType.SECOND_KIND));
    }

    /**
     * Avoid a false positive on the coverage test
     */
    public void testToString() {

        final Sequence<LexerType, String, Integer> seq = makeSequence(new Action(42), new Action(999));

        assertNotNull(seq.toString());
        assertFalse("".equals(seq.toString()));
    }

    /**
     * Test that an exception is thrown with the correct position information if the wrong lexer type is encountered
     */
    public void testThrowsIfMeetsWrongKind() {

        final Sequence<LexerType, String, Integer> seq = makeSequence(null, null);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        final List<LexedPair<LexerType, String>> input = new LinkedList<>();

        input.add(new GenericLexedPair<>(LexerType.THIRD_KIND, ""));

        final IteratorBasedLexer<LexerType, String> lex = new IteratorBasedLexer<>(input.iterator());

        try {
            seq.parse(lex, new ParseState<>(0));
            fail("Should throw!");
        } catch (final ParseError e) {
            assertEquals(0, e.getPosition());
        }
    }

    /**
     * Test that an action is matched as expected.
     *
     * @throws ParseError
     */
    public void testActionIsExecuted() throws ParseError {
        final Action action = new Action(42);

        final Sequence<LexerType, String, Integer> seq = makeSequence(action, new Action(999));

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        final List<LexedPair<LexerType, String>> input = new LinkedList<>();

        input.add(new GenericLexedPair<>(LexerType.FIRST_KIND, "data item"));
        input.add(new GenericLexedPair<>(LexerType.SECOND_KIND, "data item"));

        final IteratorBasedLexer<LexerType, String> lex = new IteratorBasedLexer<>(input.iterator());

        assertEquals(0, action.executed);

        assertEquals(null, action.seenResult);

        assertEquals("", action.seenDataItem);

        final Integer parseRes = seq.parse(lex, new ParseState<>(667)).getResult();

        assertEquals(Integer.valueOf(999), parseRes);

        assertEquals(1, action.executed);

        assertEquals(Integer.valueOf(667), action.seenResult);

        assertEquals("data item", action.seenDataItem);
    }

    /**
     * Test that an action is matched as expected.
     *
     * @throws ParseError
     */
    public void testOtherActionIsExecuted() throws ParseError {
        final Action action = new Action(42);

        final Sequence<LexerType, String, Integer> seq = makeSequence(new Action(999), action);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        final List<LexedPair<LexerType, String>> input = new LinkedList<>();

        input.add(new GenericLexedPair<>(LexerType.FIRST_KIND, "data item"));
        input.add(new GenericLexedPair<>(LexerType.SECOND_KIND, "data item"));

        final IteratorBasedLexer<LexerType, String> lex = new IteratorBasedLexer<>(input.iterator());

        assertEquals(0, action.executed);

        assertEquals(null, action.seenResult);

        assertEquals("", action.seenDataItem);

        final Integer parseRes = seq.parse(lex, new ParseState<>(667)).getResult();

        assertEquals(Integer.valueOf(42), parseRes);

        assertEquals(1, action.executed);

        assertEquals(Integer.valueOf(999), action.seenResult);

        assertEquals("data item", action.seenDataItem);
    }

    /**
     * An exception during action execution should be translated into a ParseError at the correct position.
     */
    public void testThrowsCorrectlyIfActionThrows() {
        final RuntimeException exceptionToThrow = new RuntimeException("I'm the root of all that's evil");

        final DataItemParser<String, Integer> action = new DataItemParser<String, Integer>() {

            public ParseState<Integer> parse(final String item, final ParseState<Integer> parseState) {
                throw exceptionToThrow;
            }
        };

        final Sequence<LexerType, String, Integer> seq = makeSequence(action, null);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        final List<LexedPair<LexerType, String>> input = new LinkedList<>();

        input.add(new GenericLexedPair<>(LexerType.FIRST_KIND, "data item"));

        final IteratorBasedLexer<LexerType, String> lex = new IteratorBasedLexer<>(input.iterator());

        try {
            seq.parse(lex, new ParseState<>(667)).getResult();
            fail("Should have thrown");
        } catch (final ParseError e) {
            assertEquals(0, e.getPosition());
            assertSame(exceptionToThrow, e.getCause());
        }
    }

    private static class Followable extends SingleItemSequence<LexerType, String, Integer> implements
            HasFollowedBy<LexerType, String, Integer> {

        Followable(final LexerType consumes, final DataItemParser<String, Integer> consumer) {
            super(consumes, consumer);
        }

        Sequence<LexerType, String, Integer> followedBy = null;

        public void setFollowedBy(final Sequence<LexerType, String, Integer> followedBy) {
            this.followedBy = followedBy;
        }

        @Override
        public ParseState<Integer> parse(final ItemProvider<LexerType, String> itemProvider,
                final ParseState<Integer> parseState) throws ParseError {
            final ParseState<Integer> first = super.parse(itemProvider, parseState);
            return followedBy.parse(itemProvider, first);
        }

    }

    /**
     * Test that the compound sequence correctly stitches together items that have followed-by at runtime.
     *
     * @throws ParseError
     */
    public void testSetsFollowedByCorrectly() throws ParseError {
        final Action action = new Action(42);
        final Sequence<LexerType, String, Integer> seq1 = new Followable(LexerType.FIRST_KIND, new Action(999));
        final Sequence<LexerType, String, Integer> seq2 = new SingleItemSequence<>(
                LexerType.SECOND_KIND, action);
        final List<Sequence<LexerType, String, Integer>> alts = new ArrayList<>(2);
        alts.add(seq1);
        alts.add(seq2);

        final Sequence<LexerType, String, Integer> seq = new CompoundSequence<>(alts);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        final List<LexedPair<LexerType, String>> input = new LinkedList<>();

        input.add(new GenericLexedPair<>(LexerType.FIRST_KIND, "data item"));
        input.add(new GenericLexedPair<>(LexerType.SECOND_KIND, "data item"));

        final IteratorBasedLexer<LexerType, String> lex = new IteratorBasedLexer<>(input.iterator());

        assertEquals(0, action.executed);

        assertEquals(null, action.seenResult);

        assertEquals("", action.seenDataItem);

        final Integer parseRes = seq.parse(lex, new ParseState<>(667)).getResult();

        assertEquals(Integer.valueOf(42), parseRes);

        assertEquals(1, action.executed);

        assertEquals(Integer.valueOf(999), action.seenResult);

        assertEquals("data item", action.seenDataItem);
    }

    /**
     * Test that the compound sequence correctly rejected a followed-by items without an item following it at runtime.
     *
     * @throws ParseError
     *
     */
    public void testRejectsMalformedSequence() throws ParseError {
        final Sequence<LexerType, String, Integer> seq1 = new Followable(LexerType.FIRST_KIND, new Action(999));
        final List<Sequence<LexerType, String, Integer>> alts = new ArrayList<>(2);
        alts.add(seq1);

        final List<LexedPair<LexerType, String>> input = new LinkedList<>();

        input.add(new GenericLexedPair<>(LexerType.FIRST_KIND, "data item"));
        input.add(new GenericLexedPair<>(LexerType.SECOND_KIND, "data item"));

        final IteratorBasedLexer<LexerType, String> lex = new IteratorBasedLexer<>(input.iterator());

        try {
            new CompoundSequence<>(alts).parse(lex, new ParseState<>(667)).getResult();
            fail("Should throw");
        } catch (final IllegalStateException e) { // NOPMD Expected
            // Expected
        }
    }

    private Sequence<LexerType, String, Integer> makeSequence(final DataItemParser<String, Integer> action1,
            final DataItemParser<String, Integer> action2) {
        final Sequence<LexerType, String, Integer> seq1 = new SingleItemSequence<>(
                LexerType.FIRST_KIND, action1);
        final Sequence<LexerType, String, Integer> seq2 = new SingleItemSequence<>(
                LexerType.SECOND_KIND, action2);
        final List<Sequence<LexerType, String, Integer>> alts = new ArrayList<>(2);
        alts.add(seq1);
        alts.add(seq2);

        return new CompoundSequence<>(alts);
    }

}
