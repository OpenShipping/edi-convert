package dk.ange.parserbase;

/**
 * A sequence that is repeated one or more times.
 *
 * @param <T>
 *            The class of the data item, e.g. an Enum that has values such as TABLE_HEADER, TABLE_CONTENT_LINE...
 * @param <D>
 *            The actual type of the data item, e.g. String, Excel row...
 * @param <R>
 *            The result of parsing the input.
 */
public class OneOrMore<T, D, R> implements Sequence<T, D, R> {

    private final Sequence<T, D, R> repeatable;

    /**
     * Initialize this instance.
     *
     * @param repeatable
     *            The sequence to repeat one or more times.
     */
    public OneOrMore(final Sequence<T, D, R> repeatable) {
        this.repeatable = repeatable;
    }

    public ParseState<R> parse(final ItemProvider<T, D> itemProvider, final ParseState<R> initialState)
            throws ParseError {
        ParseState<R> current = repeatable.parse(itemProvider, initialState);
        while (repeatable.consumes(itemProvider.peek().getLexerType())) {
            current = repeatable.parse(itemProvider, current);
        }
        return current;
    }

    public boolean consumes(final T type) {
        return repeatable.consumes(type);
    }

    @Override
    public String toString() {
        return "OneOrMore " + super.toString() + " that wraps " + repeatable.toString();
    }

}
