package dk.ange.parserbase;

import java.util.ArrayList;
import java.util.List;

/**
 * Accept one of the provided sequences.
 *
 * @param <T>
 *            The class of the data item, e.g. an Enum that has values such as TABLE_HEADER, TABLE_CONTENT_LINE...
 * @param <D>
 *            The actual type of the data item, e.g. String, Excel row...
 * @param <R>
 *            The result of parsing the input.
 */
public class OneOrOther<T, D, R> implements Sequence<T, D, R> {

    private final List<Sequence<T, D, R>> alternatives;

    /**
     * Initialize the instance.
     *
     * @param alternatives
     *            The sequences that this sequence will accept one of.
     */
    public OneOrOther(final List<Sequence<T, D, R>> alternatives) {
        this.alternatives = new ArrayList<>(alternatives);
    }

    @Override
    public ParseState<R> parse(final ItemProvider<T, D> itemProvider, final ParseState<R> initialState)
            throws ParseError {
        for (final Sequence<T, D, R> item : alternatives) {
            if (item.consumes(itemProvider.peek().getLexerType())) {
                return item.parse(itemProvider, initialState);
            }
        }
        throw new ParseError(toString() + ": Can't consume symbol '" + itemProvider.peek().getLexerType() + "'",
                itemProvider.getItemIdx(), itemProvider.describePositionType());
    }

    @Override
    public boolean consumes(final T type) {
        for (final Sequence<T, D, R> item : alternatives) {
            if (item.consumes(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder msg = new StringBuilder("OneOrOther " + super.toString() + " that wraps the following "
                + alternatives.size() + " item(s):\n");
        for (final Sequence<T, D, R> item : alternatives) {
            msg.append(item.toString());
            msg.append('\n');
        }
        msg.append("---- done describing oneOrOther instance ----");

        return msg.toString();
    }

}
