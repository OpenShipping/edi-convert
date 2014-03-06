package dk.ange.stowbase.edifact.lexer;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator that skips the last element
 *
 * @param <T>
 *            Type in Iterator
 */
public class SkipLastIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;

    private T next;

    private boolean hasNext;

    /**
     * @param iterator
     */
    public SkipLastIterator(final Iterator<T> iterator) {
        this.iterator = iterator;
        updateNext();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public T next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        final T oldNext = next;
        updateNext();
        return oldNext;
    }

    private void updateNext() {
        next = iterator.next();
        hasNext = iterator.hasNext();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(SkipLastIterator.class.getSimpleName() + " can not remove");
    }

}
