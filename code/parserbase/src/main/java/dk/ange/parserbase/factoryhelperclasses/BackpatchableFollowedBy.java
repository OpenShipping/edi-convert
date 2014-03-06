package dk.ange.parserbase.factoryhelperclasses;

import dk.ange.parserbase.HasFollowedBy;
import dk.ange.parserbase.ItemProvider;
import dk.ange.parserbase.ParseError;
import dk.ange.parserbase.ParseState;
import dk.ange.parserbase.Sequence;

/**
 * Some sequences require knowledge of what follows them to function correctly. Specifically the ones that have an
 * "optional" part, as they need to know what to skip ahead to. This is the case for "Optional" and "ZeroOrMore".
 *
 * This base class allows for this knowledge to be provided once the sequence has been provided, via delayed creation.
 *
 * @param <T>
 *            The lexer type of the input to parse.
 * @param <D>
 *            The actual data type of the input to parse.
 * @param <R>
 *            The result of parsing the input.
 */
public abstract class BackpatchableFollowedBy<T, D, R> implements Sequence<T, D, R>, HasFollowedBy<T, D, R> {
    private Sequence<T, D, R> wrapped = null;

    private final Sequence<T, D, R> optionalPart;

    /**
     * Create this instance.
     *
     * @param optionalPart
     *            The part of the sequence wich may or may not be executed.
     */
    protected BackpatchableFollowedBy(final Sequence<T, D, R> optionalPart) {
        this.optionalPart = optionalPart;
    }

    @Override
    public boolean consumes(final T type) {
        if (wrapped == null) {
            throw new IllegalStateException("FollowedBy was never set for this item");
        }
        return wrapped.consumes(type);
    }

    @Override
    public ParseState<R> parse(final ItemProvider<T, D> itemProvider, final ParseState<R> initialState)
            throws ParseError {
        if (wrapped == null) {
            throw new IllegalStateException("FollowedBy was never set for this item. This is probably "
                    + "due to an error in this grammar (subsequence ends in "
                    + "Optional or ZeroOrMore). This is a known limitation in "
                    + "the parser classes, as it really ought to work - sorry.");
        }
        return wrapped.parse(itemProvider, initialState);
    }

    @Override
    public void setFollowedBy(final Sequence<T, D, R> followedBy) {
        if (wrapped != null) {
            throw new IllegalStateException("FollowedBy was already set for this instance. This is probably an "
                    + "error in your grammar, resulting from reusing a sequence");
        }
        wrapped = makeWrapped(optionalPart, followedBy);
    }

    @Override
    public String toString() {
        if (wrapped == null) {
            return "FactoryFollowedBy " + super.toString() + " wrapping not set";
        } else {
            return "FactoryFollowedBy " + super.toString() + " wrapping " + wrapped.toString();
        }
    }

    /**
     * Implement as a factory method that creates the intended kind of sequence that is "followed by" something else.
     *
     * @param optionalPart1
     *            The part to be executed 0, 1 or more times, depending on what sequence is created.
     * @param followedBy
     *            The part that follows the optional part.
     * @return A sequence that is configured with an optional part (optionally repeatable), and another sequence that
     *         will follow it.
     */
    protected abstract Sequence<T, D, R> makeWrapped(final Sequence<T, D, R> optionalPart1,
            final Sequence<T, D, R> followedBy);

}
