package dk.ange.parserbase.lexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dk.ange.parserbase.ItemProvider;
import dk.ange.parserbase.LexedPair;

/**
 * A base class that does much of the annoying lifting of implementing a lexer.
 *
 * @param <T>
 *            The lexer type of the items.
 * @param <D>
 *            The actual data type of the items.
 */
public abstract class LexerBase<T, D> implements ItemProvider<T, D> {

    private final ItemProvider<T, D> iterBasedLexer;

    /**
     * Create this instance from an InputStream and lex the input.
     *
     * @param i
     *            The stream to read the input data from.
     *
     * @throws IOException
     */
    public LexerBase(final InputStream i) throws IOException {
        this.iterBasedLexer = new IteratorBasedLexer<>(lexStream(i).iterator());
    }

    /**
     * Create this instance from an iterator for data type items, and lex the input.
     *
     * @param iter
     *            An iterator for a collection of data items.
     */
    public LexerBase(final Iterator<D> iter) {
        this.iterBasedLexer = new IteratorBasedLexer<>(lexIter(iter).iterator());
    }

    private final List<LexedPair<T, D>> lexStream(final InputStream i) throws IOException {
        final Iterator<D> dataIter = getDataIterator(i);
        return lexIter(dataIter);
    }

    private List<LexedPair<T, D>> lexIter(final Iterator<D> dataIter) {
        final List<LexedPair<T, D>> res = new LinkedList<>();
        while (dataIter.hasNext()) {
            final D dataItem = dataIter.next();
            LexedPair<T, D> pair;
            try {
                pair = new GenericLexedPair<>(identifyDataItem(dataItem), dataItem);
            } catch (final LexerIdentificationFailureException e) {
                throw new RuntimeException(
                        "Can't identify item in " + describePositionType() + " " + this.getItemIdx(), e);
            }
            res.add(pair);

        }
        res.add(new GenericLexedPair<>(getEofType(), getDummyDataItem()));

        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see dk.ange.orion.parser.sequence.ItemProvider#peek()
     */
    @Override
    public final LexedPair<T, D> peek() {
        return iterBasedLexer.peek();
    }

    /*
     * (non-Javadoc)
     *
     * @see dk.ange.orion.parser.sequence.ItemProvider#pop()
     */
    @Override
    public final LexedPair<T, D> pop() {
        return iterBasedLexer.pop();
    }

    /*
     * (non-Javadoc)
     *
     * @see dk.ange.orion.parser.sequence.ItemProvider#getItemIdx()
     */
    @Override
    public final int getItemIdx() {
        if (iterBasedLexer == null) {
            return -1;
        }
        return iterBasedLexer.getItemIdx();
    }

    /**
     * Find the lexer type for a data item.
     *
     * @param dataItem
     *            The item to identify.
     * @return The lexer type of this data item.
     * @throws LexerIdentificationFailureException
     *             Thrown if this item can't be identified.
     */
    protected abstract T identifyDataItem(D dataItem) throws LexerIdentificationFailureException;

    /**
     * Get us an iterable collection or some such containing the items in the input stream
     *
     * @param i
     *            The input stream
     * @return An iterable that can iterate over the items in the stream.
     * @throws IOException
     */
    protected abstract Iterator<D> getDataIterator(InputStream i) throws IOException;

    /**
     * Get an instance of D that we can safely ignore.
     *
     * @return An instance of D that we can safely ignore.
     */
    protected abstract D getDummyDataItem();

    /**
     *
     * @return The specific kind of T that signals end-of-file
     */
    protected abstract T getEofType();

}
