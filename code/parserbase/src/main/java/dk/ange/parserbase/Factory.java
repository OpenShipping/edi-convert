package dk.ange.parserbase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import dk.ange.parserbase.factoryhelperclasses.BackpatchableOptional;
import dk.ange.parserbase.factoryhelperclasses.BackpatchableZeroOrMore;

/**
 * A factory class for various classes that are useful when constructing a parser.
 * 
 * @param <T>
 *            The lexer type of the input to parse.
 * @param <D>
 *            The actual data type of the input to parse.
 * @param <R>
 *            The result of parsing the input.
 */
public abstract class Factory<T, D, R> {

    /**
     * This is where you should be making your parser.
     * 
     * @return The Sequence that is your parser.
     */
    public abstract Sequence<T, D, R> makeSequence();

    /**
     * Create a sequence that will be accepted one or more times. Equivalent to the regular expression metacharacter +
     * 
     * @param obj
     *            The objects to turn into a sequence.
     * @return A sequence that will be accepted one or more times.
     * 
     */
    public Sequence<T, D, R> oneOrMore(final Object... obj) {
        return new OneOrMore<T, D, R>(sequence(obj));
    }

    /**
     * @param obj
     *            The objects to make an optional sequence of.
     * @return A sequence that will be matched zero or one times.
     */
    public Sequence<T, D, R> optional(final Object... obj) {
        return new BackpatchableOptional<T, D, R>(sequence(obj));
    }

    /**
     * 
     * @param obj
     *            The objects to make an optional sequence of.
     * @return A sequence that will be matched zero, one or more times.
     */
    public Sequence<T, D, R> zeroOrMore(final Object... obj) {
        return new BackpatchableZeroOrMore<T, D, R>(sequence(obj));
    }

    /**
     * 
     * @param obj
     *            The different sequences that can be matched here.
     * @return A sequence that will match one of the provided optional sequences.
     */
    public Sequence<T, D, R> oneOrOther(final Object... obj) {
        final List<Sequence<T, D, R>> alternatives = objectsToSequences(obj);
        return new OneOrOther<T, D, R>(alternatives);
    }

    /**
     * 
     * @param objects
     *            A sequence of one or more sub-sequences.
     * @return A sequence consisting of one or more sub-sequences.
     */
    public Sequence<T, D, R> sequence(final Object... objects) {
        final List<Sequence<T, D, R>> sequences = objectsToSequences(objects);
        if (sequences.isEmpty()) {
            throw new IllegalStateException("Expected at least one parser item for input");
        }
        if (sequences.size() == 1) {
            return sequences.get(0);
        }
        return new CompoundSequence<T, D, R>(objectsToSequences(objects));
    }

    @SuppressWarnings("unchecked")
    private List<Sequence<T, D, R>> objectsToSequences(final Object... objects) {
        final List<Object> objectsAndEnd = new LinkedList<Object>(Arrays.asList(objects));
        objectsAndEnd.add(neutral);
        final List<Sequence<T, D, R>> res = new LinkedList<Sequence<T, D, R>>();
        T seenT = null;
        for (final Object obj : objectsAndEnd) {
            if (obj instanceof Sequence) {
                if (seenT != null) {
                    res.add(new SingleItemSequence<T, D, R>(seenT, neutral));
                }
                res.add((Sequence<T, D, R>) obj);
                seenT = null;
            } else if (obj instanceof DataItemParser) {
                if (seenT != null) {
                    res.add(new SingleItemSequence<T, D, R>(seenT, (DataItemParser<D, R>) obj));
                    seenT = null;
                }
            } else {
                // Assume object is type T
                if (seenT != null) {
                    res.add(new SingleItemSequence<T, D, R>(seenT, neutral));
                }
                seenT = (T) obj;
            }
        }
        return res;
    }

    /**
     * Does absolutely nothing.
     */
    protected final DataItemParser<D, R> neutral = new DataItemParser<D, R>() {

        public ParseState<R> parse(final D item, final ParseState<R> parseState) {
            return parseState;
        }

    };

}
