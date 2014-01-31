package dk.ange.parserbase;

/**
 * A pair of data item and data item class.
 * 
 * @param <T>
 *            The class of the data item, e.g. an Enum that has values such as TABLE_HEADER, TABLE_CONTENT_LINE...
 * @param <D>
 *            The actual type of the data item, e.g. String, Excel row...
 */
public interface LexedPair<T, D> {

    /**
     * Access the data item class.
     * 
     * @return The data item class.
     */
    T getLexerType();

    /**
     * Access the data item.
     * 
     * @return The data item.
     */
    D getData();
}
