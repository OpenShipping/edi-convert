package dk.ange.parserbase.lexer;

import java.util.Iterator;

import dk.ange.parserbase.ItemProvider;
import dk.ange.parserbase.LexedPair;

/**
 * A lexer that is based on an iterator.
 *
 * @param <T>
 * @param <D>
 */
public class IteratorBasedLexer<T, D> implements ItemProvider<T, D> {

    private final Iterator<LexedPair<T, D>> iter;

    private LexedPair<T, D> top = null;

    private int itemIdx = 0;

    /**
     * Initialize this instance using an iterator.
     *
     * @param iter
     *            The iterator to base this lexer on.
     */
    public IteratorBasedLexer(final Iterator<LexedPair<T, D>> iter) {
        if (iter.hasNext()) {
            top = iter.next();
        }
        this.iter = iter;
    }

    public String describePositionType() {
        return "iterator position";
    }

    public int getItemIdx() {
        return itemIdx;
    }

    public LexedPair<T, D> peek() {
        if (top == null) {
            throw new IllegalStateException("Attempt to peek past end of input");
        }
        return top;
    }

    public LexedPair<T, D> pop() {
        if (top == null) {
            throw new IllegalStateException("Attempt to pop past end of input");
        }
        final LexedPair<T, D> res = top;
        if (iter.hasNext()) {
            top = iter.next();
        } else {
            top = null;
        }
        itemIdx++;
        return res;

    }

}
