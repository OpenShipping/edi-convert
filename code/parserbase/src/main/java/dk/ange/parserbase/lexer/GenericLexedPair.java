package dk.ange.parserbase.lexer;

import dk.ange.parserbase.LexedPair;

/**
 * The most generic implementation of a lexed pair that can be made.
 *
 * @param <T>
 * @param <D>
 */
public class GenericLexedPair<T, D> implements LexedPair<T, D> {

    private final T lexedType;

    private final D dataType;

    /**
     * Initialize this instance with immutable references to a pair.
     *
     * @param lexedType
     *            The lexer type of this pair.
     * @param dataType
     *            The data content of this pair.
     */
    public GenericLexedPair(final T lexedType, final D dataType) {
        this.lexedType = lexedType;
        this.dataType = dataType;
    }

    public D getData() {
        return dataType;
    }

    public T getLexerType() {
        return lexedType;
    }

}
