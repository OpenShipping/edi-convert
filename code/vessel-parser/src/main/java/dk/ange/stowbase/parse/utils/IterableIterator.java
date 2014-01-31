package dk.ange.stowbase.parse.utils;

import java.util.Iterator;

/**
 * Wrapper around an Iterator that turns it into an Iterable that just returns the iterator. Use like this in foreach
 * loop:
 * <p>
 * <code>
 * for (final Row row : new IterableIterator<Row>(sheet.rowIterator())) {
 * </code>
 * 
 * @param <T>
 *            Type to iterate over
 */
public class IterableIterator<T> implements Iterable<T> {

    private final Iterator<T> iterator;

    /**
     * The constructor
     * 
     * @param iterator
     */
    public IterableIterator(final Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

}
