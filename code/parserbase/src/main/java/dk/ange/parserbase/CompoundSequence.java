package dk.ange.parserbase;

import java.util.LinkedList;
import java.util.List;

/**
 * A sequence that consists of one or more sequences.
 *
 * @param <T>
 *            The lexer type of the data items.
 * @param <D>
 *            The data type of the data items.
 * @param <R>
 *            The type of the result of parsing the items.
 */
public class CompoundSequence<T, D, R> implements Sequence<T, D, R>, HasFollowedBy<T, D, R> {

    private List<Sequence<T, D, R>> sequence;

    private final List<Sequence<T, D, R>> unfixedSequence;

    private final List<Sequence<T, D, R>> followedBy = new LinkedList<>();

    /**
     * Create an instance.
     *
     * @param sequence
     *            The sequence of one or more subsequences.
     */
    public CompoundSequence(final List<Sequence<T, D, R>> sequence) {
        if (sequence.isEmpty()) {
            throw new IllegalStateException("Attempt to make empty compound sequence");
        }
        this.unfixedSequence = sequence;
    }

    public ParseState<R> parse(final ItemProvider<T, D> itemProvider, final ParseState<R> initialState)
            throws ParseError {
        initSequence();
        ParseState<R> currentState = initialState;
        for (final Sequence<T, D, R> item : sequence) {
            currentState = item.parse(itemProvider, currentState);
        }
        return currentState;
    }

    private void initSequence() {
        if (sequence != null) {
            return;
        }
        sequence = fixSequence(unfixedSequence);
    }

    @SuppressWarnings("unchecked")
    private List<Sequence<T, D, R>> fixSequence(final List<Sequence<T, D, R>> sequence2) {
        final List<Sequence<T, D, R>> res = new LinkedList<>();
        for (int i = 0; i < sequence2.size(); ++i) {
            final Sequence<T, D, R> item = sequence2.get(i);
            res.add(item);
            if (item instanceof HasFollowedBy) {
                // If the item is a compound sequence and does not end in an hasFollowedBy, then we should not really
                // count
                // it as a HasFollowedBy after all
                if (item instanceof CompoundSequence) {
                    final CompoundSequence<T, D, R> itemAsCompound = (CompoundSequence<T, D, R>) item;
                    if (!(itemAsCompound.lastIsHasFollowedBy())) {
                        continue;
                    }
                }
                final HasFollowedBy<T, D, R> itemAsFollowedBy = (HasFollowedBy<T, D, R>) item;
                final List<Sequence<T, D, R>> restOfList = sequence2.subList(i + 1, sequence2.size());

                if (restOfList.isEmpty()) {
                    if (followedBy.isEmpty()) {
                        if (!(item instanceof CompoundSequence)) {
                            throw new IllegalStateException("Can't set followed-by for '" + item
                                    + "' as there is no followed-by here. " + "This is probably "
                                    + "due to an error in this grammar (subsequence ends in "
                                    + "Optional or ZeroOrMore). This is a known limitation in "
                                    + "the parser classes, as it really ought to work - sorry.");
                        }
                    } else {
                        itemAsFollowedBy.setFollowedBy(new OneOrOther<>(followedBy));
                    }
                } else {
                    itemAsFollowedBy.setFollowedBy(new CompoundSequence<>(restOfList));
                }
                return res;
            }
        }
        return res;
    }

    private boolean lastIsHasFollowedBy() {
        return (unfixedSequence.get(unfixedSequence.size() - 1) instanceof HasFollowedBy<?, ?, ?>);
    }

    public boolean consumes(final T type) {
        initSequence();
        return sequence.get(0).consumes(type);
        // return unfixedSequence.get(0).consumes(type); // FIXME This may not be correct
        // return sequence.get(0).consumes(type);
    }

    @Override
    public String toString() {
        final StringBuilder msg = new StringBuilder("CompoundSequence " + super.toString() + " that wraps these "
                + unfixedSequence.size() + " item(s):\n");
        for (final Sequence<T, D, R> item : unfixedSequence) {
            msg.append(item.toString());
            msg.append('\n');
        }
        msg.append("---- done describing compound sequence ----");
        return msg.toString();
    }

    public void setFollowedBy(final Sequence<T, D, R> followedBy) {
        if (sequence != null) {
            throw new RuntimeException("Adding to followedBy too late!");
        }
        this.followedBy.add(followedBy);
    }

}
