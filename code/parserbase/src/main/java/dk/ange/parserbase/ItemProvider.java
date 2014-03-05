package dk.ange.parserbase;

/**
 * An interface for "something that can provide lexed pairs, a position and a description of the position"
 *
 * @param <T>
 *            The lexer type of the data items.
 * @param <D>
 *            The data type of the data items.
 */
public interface ItemProvider<T, D> {

    /**
     *
     * @return The front item in the queue, of any. Throws if queue is empty.
     */
    LexedPair<T, D> peek();

    /**
     * Removes and returns the front item in the queue, if any.
     *
     * @return The front item in the queue, if any.
     */
    LexedPair<T, D> pop();

    /**
     *
     * @return The index of the front item in the queue, 0 is first.
     */
    int getItemIdx();

    /**
     * Describe the "kind" of position we are iterating over, e.g. "line", "row"...
     *
     * @return A description of the kind of position we are iterating over, e.g. "line", "row"...
     */
    String describePositionType();

}
