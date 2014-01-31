package dk.ange.parserbase;

/**
 * The interface for a sequence of actions in a parser grammar.
 * 
 * @param <T>
 *            The lexer type of the items to parse.
 * @param <D>
 *            The data type of the items to parse.
 * @param <R>
 *            The type of result of parsing the items.
 */
public interface Sequence<T, D, R> {

    /**
     * Parse the input using the node(s) that this sequence corresponds to.
     * 
     * @param itemProvider
     *            The object that can provide more input symbols on request.
     * @param initialState
     *            The current state of the parser.
     * @return The state of the parser, once the node(s) of this sequence have been consulted.
     * @throws ParseError
     *             Thrown in case of unrecoverable parse error.
     */
    public abstract ParseState<R> parse(final ItemProvider<T, D> itemProvider, final ParseState<R> initialState)
            throws ParseError;

    /**
     * The kinds of lexer type items ("first" in lex/yacc terms) this sequence can consume.
     * 
     * @param type
     *            The lexer type that we are trying to consume.
     * @return True if this sequence will consume a lexer type 'type', false if not.
     */
    public abstract boolean consumes(final T type);
}
