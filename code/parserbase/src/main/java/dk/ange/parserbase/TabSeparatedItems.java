package dk.ange.parserbase;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class that parses lines of tab-separated items into "some model R"
 *
 * @param <R>
 *            The model that the tab separated items should be parsed into.
 */
public abstract class TabSeparatedItems<R> implements DataItemParser<String, R> {

    final public ParseState<R> parse(final String item, final ParseState<R> parseState) {
        final String[] items = item.split("\t");
        final List<String> l = new ArrayList<>(items.length);
        for (final String s : items) {
            l.add(s.trim());
        }
        return parse(l, parseState);
    }

    /**
     * Process the list of tab-separated items from the original String.
     *
     * @param l
     *            An array-list of the items that were tab-separated in the original String.
     * @param parseState
     *            The state of the parser before parsing the items,
     * @return The state of the parser after parsing the items.
     */
    protected abstract ParseState<R> parse(final List<String> l, final ParseState<R> parseState);

    /**
     * A method that tries "very hard" to convert a String to a floating point value, even if it uses , instead of . to
     * separate decimal part.
     *
     * @param s
     *            The string to convert to a double.
     * @return The string converted to a double.
     */
    protected static double stringToDouble(final String s) {
        if ("na".equals(s.toLowerCase())) {
            return Double.NaN;
        }
        return Double.parseDouble(s.replaceAll(",", "."));
    }

}
