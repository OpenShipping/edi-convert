package dk.ange.parserbase;

import java.util.List;

import junit.framework.TestCase;

/**
 * Test the TabSeparatedItems helper class for parsing files consisting of tab-separated items.
 */
public class TestTabSeparatedItems extends TestCase {

    private static class HelperClass extends TabSeparatedItems<String> {

        List<String> seen = null;

        @Override
        protected ParseState<String> parse(final List<String> l, final ParseState<String> parseState) {
            seen = l;
            return parseState;
        }
    }

    /**
     * Test simple creation.
     */
    public void testCanCreate() {
        new HelperClass();
    }

    /**
     * Test that an empty string is turned into one empty string item
     */
    public void testEmptyString() {
        final HelperClass parser = new HelperClass();
        final ParseState<String> parseState = new ParseState<String>("");
        assertNull(parser.seen);
        final ParseState<String> parseResult = parser.parse("", parseState);
        assertSame(parseState, parseResult);
        assertNotNull(parser.seen);

        assertEquals(1, parser.seen.size());
        assertEquals("", parser.seen.get(0));
    }

    /**
     * Check that the class converts a one item string correctly.
     */
    public void testOneString() {
        final HelperClass parser = new HelperClass();
        final ParseState<String> parseState = new ParseState<String>("");
        assertNull(parser.seen);
        final ParseState<String> parseResult = parser.parse("1", parseState);
        assertSame(parseState, parseResult);
        assertNotNull(parser.seen);

        assertEquals(1, parser.seen.size());
        assertEquals("1", parser.seen.get(0));
    }

    /**
     * Check that the class converts a two item string correctly.
     */
    public void testTwoStrings() {
        final HelperClass parser = new HelperClass();
        final ParseState<String> parseState = new ParseState<String>("");
        assertNull(parser.seen);
        final ParseState<String> parseResult = parser.parse("1\t2", parseState);
        assertSame(parseState, parseResult);
        assertNotNull(parser.seen);

        assertEquals(2, parser.seen.size());
        assertEquals("1", parser.seen.get(0));
        assertEquals("2", parser.seen.get(1));
    }

    private class Converter extends TabSeparatedItems<Double> {

        @Override
        protected ParseState<Double> parse(final List<String> l, final ParseState<Double> parseState) {
            return new ParseState<Double>(stringToDouble(l.get(0)));
        }

    }

    /**
     * Test that the internal string to double conversion works
     */
    public void testConversionOfFloat() {
        final Converter converter = new Converter();

        parseAndCompare(converter, new Double(0.0), "0.0");
        parseAndCompare(converter, new Double(0.0), "0,0");

        parseAndCompare(converter, new Double(1.1), "1.1");
        parseAndCompare(converter, new Double(1.1), "1,1");

        parseAndCompare(converter, new Double(-1.1), "-1.1");
        parseAndCompare(converter, new Double(-1.1), "-1,1");

    }

    private void parseAndCompare(final Converter converter, final Double expected, final String input) {
        assertEquals(expected, converter.parse(input, new ParseState<Double>(667.666)).getResult());
    }

}
