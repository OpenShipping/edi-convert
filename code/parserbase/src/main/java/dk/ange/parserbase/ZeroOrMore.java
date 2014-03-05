package dk.ange.parserbase;

/**
 * A sequence that is repeated zero, one or more times.
 *
 * @param <T>
 *            The class of the data item, e.g. an Enum that has values such as TABLE_HEADER, TABLE_CONTENT_LINE...
 * @param <D>
 *            The actual type of the data item, e.g. String, Excel row...
 * @param <R>
 *            The result of parsing the input.
 */
public class ZeroOrMore<T, D, R> implements Sequence<T, D, R> {

    private final Sequence<T, D, R> wrapped;

    /**
     * Create a new instance.
     *
     * @param repeatable
     *            The sequence to repeat zero, one or more times.
     * @param followedBy
     *            The sequence that follows the repeatable sequence.
     */
    public ZeroOrMore(final Sequence<T, D, R> repeatable, final Sequence<T, D, R> followedBy) {
        wrapped = new Optional<>(new OneOrMore<>(repeatable), followedBy);
    }

    public boolean consumes(final T type) {
        return wrapped.consumes(type);
    }

    public ParseState<R> parse(final ItemProvider<T, D> itemProvider, final ParseState<R> initialState)
            throws ParseError {
        return wrapped.parse(itemProvider, initialState);
    }

    @Override
    public String toString() {
        return "ZeroOrMore " + super.toString() + " that wraps " + wrapped.toString();
    }

}
