package dk.ange.parserbase;

/**
 * A parse state, capable of creating a result, once parsing is over.
 *
 * @param <T>
 *            The result of parsing the input.
 */
public class ParseState<T> {
    /** The internally contained result, which is most likely mutable */
    protected T result;

    /**
     * Saves the (mutable) result object
     *
     * @param containedResult
     */
    public ParseState(final T containedResult) {
        this.result = containedResult;
    }

    /**
     * Create a result, or throw an exception.
     *
     * @return The result of parsing the input.
     */
    public T getResult() {
        return result;
    }

}
