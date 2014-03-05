package dk.ange.parserbase;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import dk.ange.parserbase.lexer.GenericLexedPair;
import dk.ange.parserbase.lexer.IteratorBasedLexer;

/**
 * Tests for some of the most ordinary parsing cases.
 */
public class TestSingleItemSequence extends TestCase {

    /**
     * Test the simples possible creation sequence.
     */
    @SuppressWarnings("unused")
    public void testCanCreate() {
        new SingleItemSequence<String, String, String>(null, null);
    }

    /**
     * Test that a sequence correctly sets the "consumes" item.
     */
    public void testConsumes() {

        final SingleItemSequence<LexerType, String, Integer> seq = new SingleItemSequence<>(
                LexerType.FIRST_KIND, null);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));
    }

    /**
     * Avoid a false positive on the coverage test
     */
    public void testToString() {
        final Sequence<LexerType, String, Integer> seq = new SingleItemSequence<>(
                LexerType.FIRST_KIND, null);
        assertNotNull(seq.toString());
        assertFalse("".equals(seq.toString()));
    }

    /**
     * Test that an exception is thrown with the correct position information if the wrong lexer type is encountered
     */
    public void testThrowsIfMeetsWrongKind() {

        final Sequence<LexerType, String, Integer> seq = makeSequence(null);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        final List<LexedPair<LexerType, String>> input = new LinkedList<>();

        input.add(new GenericLexedPair<>(LexerType.SECOND_KIND, ""));

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

        final Sequence<LexerType, String, Integer> seq = makeSequence(action);

        assertTrue(seq.consumes(LexerType.FIRST_KIND));

        final List<LexedPair<LexerType, String>> input = new LinkedList<>();

        input.add(new GenericLexedPair<>(LexerType.FIRST_KIND, "data item"));

        final IteratorBasedLexer<LexerType, String> lex = new IteratorBasedLexer<>(input.iterator());

        assertEquals(0, action.executed);

        assertEquals(null, action.seenResult);

        assertEquals("", action.seenDataItem);

        final Integer parseRes = seq.parse(lex, new ParseState<>(667)).getResult();

        assertEquals(Integer.valueOf(42), parseRes);

        assertEquals(1, action.executed);

        assertEquals(Integer.valueOf(667), action.seenResult);

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

        final Sequence<LexerType, String, Integer> seq = makeSequence(action);

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

    private Sequence<LexerType, String, Integer> makeSequence(final DataItemParser<String, Integer> action) {
        return new SingleItemSequence<>(LexerType.FIRST_KIND, action);
    }

}
