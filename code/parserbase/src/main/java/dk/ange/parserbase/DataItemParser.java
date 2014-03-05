package dk.ange.parserbase;

/**
 * A parser for a single data item.
 *
 * @param <D>
 *            The type of the data item to parse.
 * @param <R>
 *            The result of parsing the input.
 */
public interface DataItemParser<D, R> {

    /**
     * Parse a single data item.
     *
     * @param item
     *            The data item to parse.
     * @param parseState
     *            The parse state before parsing.
     * @return The parse state after parsing.
     */
    ParseState<R> parse(D item, ParseState<R> parseState);

}
