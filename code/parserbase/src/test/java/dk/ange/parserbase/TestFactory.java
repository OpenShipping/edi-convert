package dk.ange.parserbase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import dk.ange.parserbase.lexer.GenericLexedPair;
import dk.ange.parserbase.lexer.IteratorBasedLexer;

/**
 * Tests for the Factory class, and the various ways in which we create grammars/state machines.
 */
public class TestFactory extends TestCase {

    private class Model {
        List<Integer> seen = new LinkedList<>();
    }

    private class Lexer extends IteratorBasedLexer<LexerType, Integer> {

        Lexer(final Iterator<LexedPair<LexerType, Integer>> iter) {
            super(iter);
        }

    }

    private final DataItemParser<Integer, Model> addingAction = new DataItemParser<Integer, Model>() {

        public ParseState<Model> parse(final Integer item, final ParseState<Model> parseState) {
            parseState.getResult().seen.add(item);
            return parseState;
        }
    };

    /**
     * Test running a single item sequence through a simple single item grammar.
     *
     * @throws ParseError
     */
    public void testSingleItemPass() throws ParseError {

        final Factory<LexerType, Integer, Model> simpleFactory = new Factory<LexerType, Integer, Model>() {

            @Override
            public Sequence<LexerType, Integer, Model> makeSequence() {

                return sequence(LexerType.FIRST_KIND, addingAction);
            }
        };

        final List<LexedPair<LexerType, Integer>> input = splice(lexList(LexerType.FIRST_KIND), intList(42));

        final Model m = simpleFactory.makeSequence().parse(new Lexer(input.iterator()),
                new ParseState<>(new Model())).getResult();

        assertFalse(m.seen.isEmpty());

        assertEquals(1, m.seen.size());

        assertEquals(Integer.valueOf(42), m.seen.get(0));
    }

    /**
     * Test running a single item sequence through a simple single item grammar.
     */
    public void testSingleItemFail() {

        final Factory<LexerType, Integer, Model> simpleFactory = new Factory<LexerType, Integer, Model>() {

            @Override
            public Sequence<LexerType, Integer, Model> makeSequence() {

                return sequence(LexerType.FIRST_KIND, addingAction);
            }
        };

        final List<LexedPair<LexerType, Integer>> input = splice(lexList(LexerType.SECOND_KIND), intList(42));

        expectParseFail(simpleFactory, input);

        final List<LexedPair<LexerType, Integer>> input2 = splice(lexList(LexerType.THIRD_KIND), intList(42));

        expectParseFail(simpleFactory, input2);
    }

    /**
     * Test that parsing works with two simple items.
     *
     * @throws ParseError
     */
    public void testSeveralItemsPass() throws ParseError {
        final Factory<LexerType, Integer, Model> simpleFactory = new Factory<LexerType, Integer, Model>() {

            @Override
            public Sequence<LexerType, Integer, Model> makeSequence() {

                return sequence(LexerType.FIRST_KIND, addingAction, LexerType.SECOND_KIND, addingAction);
            }
        };

        final List<LexedPair<LexerType, Integer>> input = splice(lexList(LexerType.FIRST_KIND, LexerType.SECOND_KIND),
                intList(42, 667));

        final Model m = simpleFactory.makeSequence().parse(new Lexer(input.iterator()),
                new ParseState<>(new Model())).getResult();

        assertFalse(m.seen.isEmpty());

        assertEquals(2, m.seen.size());

        assertEquals(Integer.valueOf(42), m.seen.get(0));

        assertEquals(Integer.valueOf(667), m.seen.get(1));
    }

    /**
     * Test that parsing works with two simple items.
     *
     */
    public void testSeveralItemsFail() {
        final Factory<LexerType, Integer, Model> simpleFactory = new Factory<LexerType, Integer, Model>() {

            @Override
            public Sequence<LexerType, Integer, Model> makeSequence() {

                return sequence(LexerType.FIRST_KIND, addingAction, LexerType.SECOND_KIND, addingAction);
            }
        };

        for (final LexerType firstItem : LexerType.values()) {
            if (LexerType.FIRST_KIND.equals(firstItem)) {
                continue;
            }
            final List<LexedPair<LexerType, Integer>> input = splice(lexList(firstItem), intList(42));

            expectParseFail(simpleFactory, input);
        }

        for (final LexerType secondItem : LexerType.values()) {
            if (LexerType.SECOND_KIND.equals(secondItem)) {
                continue;
            }
            final List<LexedPair<LexerType, Integer>> input = splice(lexList(LexerType.FIRST_KIND, secondItem),
                    intList(42, 667));

            expectParseFail(simpleFactory, input);
        }
    }

    private void expectParseFail(final Factory<LexerType, Integer, Model> simpleFactory,
            final List<LexedPair<LexerType, Integer>> input2) {
        try {
            simpleFactory.makeSequence().parse(new Lexer(input2.iterator()), new ParseState<>(new Model()));
            fail("Expected an exception here");
        } catch (final ParseError e) { // NOPMD Expected
            // Expected
        }
    }

    private static List<LexedPair<LexerType, Integer>> splice(final List<LexerType> lexList, final List<Integer> intList) {
        assertEquals(lexList.size(), intList.size());
        final List<LexedPair<LexerType, Integer>> res = new LinkedList<>();
        for (int i = 0; i < lexList.size(); ++i) {
            res.add(new GenericLexedPair<>(lexList.get(i), intList.get(i)));
        }
        return res;
    }

    private static List<LexerType> lexList(final LexerType... items) {
        return Arrays.asList(items);
    }

    private static List<Integer> intList(final Integer... items) {
        return Arrays.asList(items);
    }

}
