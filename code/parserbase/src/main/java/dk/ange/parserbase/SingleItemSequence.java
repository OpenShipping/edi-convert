package dk.ange.parserbase;

/**
 * An "atomic" sequence that associates one action with one lexer type item.
 *
 * @param <T>
 *            The lexer type of the data items.
 * @param <D>
 *            The data type of the data items.
 * @param <R>
 *            The result type of parsing the items.
 */
public class SingleItemSequence<T, D, R> implements Sequence<T, D, R> {

    private final T consumes;

    private final DataItemParser<D, R> consumer;

    /**
     * Create an atomic sequence.
     *
     * @param consumes
     *            The lexer type to accept.
     * @param consumer
     *            The action to use to process the data item, and modify the result.
     */
    public SingleItemSequence(final T consumes, final DataItemParser<D, R> consumer) {
        this.consumes = consumes;
        this.consumer = consumer;
    }

    public ParseState<R> parse(final ItemProvider<T, D> itemProvider, final ParseState<R> initialState)
            throws ParseError {
        if (!consumes.equals(itemProvider.peek().getLexerType())) {
            throw new ParseError("Expected to see an item of type '" + consumes + "' but instead got type '"
                    + itemProvider.peek().getLexerType() + "'", itemProvider.getItemIdx(), itemProvider
                    .describePositionType());
        }
        final D item = itemProvider.pop().getData();
        try {
            return consumer.parse(item, initialState);
        } catch (final RuntimeException e) {
            throw new ParseError("Error parsing item '" + item + "'", e, itemProvider.getItemIdx() - 1, itemProvider
                    .describePositionType());
        }
    }

    public boolean consumes(final T type) {
        return consumes.equals(type);
    }

    @Override
    public String toString() {
        return "SingleItemSequence " + super.toString() + " that consumes lexer item " + consumes.toString();
    }

}
