package dk.ange.parserbase;

import java.util.ArrayList;
import java.util.List;

/**
 * Cretae a sequence that will be matched zero or one times.
 * 
 * @param <T>
 *            The lexer type of the data items.
 * @param <D>
 *            The data type of the data items.
 * @param <R>
 *            The result type of parsing the data items.
 */
public class Optional<T, D, R> implements Sequence<T, D, R> {

    private final Sequence<T, D, R> wrapped;

    /**
     * 
     * @param optional
     *            The sequence to execute zero or one times.
     * @param followedBy
     *            The sequence that follows the optional part.
     */
    public Optional(final Sequence<T, D, R> optional, final Sequence<T, D, R> followedBy) {
        this.wrapped = new OneOrOther<T, D, R>(makeList(new CompoundSequence<T, D, R>(makeList(optional, followedBy)),
                followedBy));
    }

    private List<Sequence<T, D, R>> makeList(final Sequence<T, D, R> item1, final Sequence<T, D, R> item2) {
        final List<Sequence<T, D, R>> res = new ArrayList<Sequence<T, D, R>>(2);
        res.add(item1);
        res.add(item2);
        return res;
    }

    public boolean consumes(final T type) {
        return wrapped.consumes(type);
    }

    public ParseState<R> parse(final ItemProvider<T, D> itemProvider, final ParseState<R> initialState)
            throws ParseError {
        return wrapped.parse(itemProvider, initialState);
    }

    @Override
    public String toString() {
        return "Optional " + super.toString() + " that wraps " + wrapped.toString();
    }

}
