package dk.ange.parserbase;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import dk.ange.parserbase.lexer.GenericLexedPair;
import dk.ange.parserbase.lexer.IteratorBasedLexer;

/**
 * Test the "Optional" sequence (execute once or skip)
 */
public class TestOptional extends TestCase {

    /**
     * Test the simplest possible creation sequence.
     */
    public void testCanCreate() {
        makeSequence(null, null);
    }

    /**
     * Test that a sequence correctly sets the "consumes" item.
     */
    public void testConsumes() {

        final Sequence<LexerType, String, Integer> seq = makeSequence(null, null);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        assertTrue(seq.consumes(LexerType.SECOND_KIND));
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

    private Sequence<LexerType, String, Integer> makeSequence(final DataItemParser<String, Integer> action1,
            final DataItemParser<String, Integer> action2) {
        final Sequence<LexerType, String, Integer> seq1 = new SingleItemSequence<>(
                LexerType.FIRST_KIND, action1);
        final Sequence<LexerType, String, Integer> seq2 = new SingleItemSequence<>(
                LexerType.SECOND_KIND, action2);

        return new Optional<>(seq1, seq2);
    }

}
