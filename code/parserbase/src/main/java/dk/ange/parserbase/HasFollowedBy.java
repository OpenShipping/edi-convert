package dk.ange.parserbase;

/**
 * An interface for "sequences that are followed by other sequences".
 * 
 * Used for delayed creation of sequences with optional parts.
 * 
 * @param <T>
 *            The lexer type of the data items.
 * @param <D>
 *            The data type of the data items.
 * @param <R>
 *            The type of the result of parsing the items.
 */
public interface HasFollowedBy<T, D, R> {

    /**
     * 
     * @param followedBy
     *            The sequence this sequence is followed by.
     */
    public void setFollowedBy(final Sequence<T, D, R> followedBy);

}
