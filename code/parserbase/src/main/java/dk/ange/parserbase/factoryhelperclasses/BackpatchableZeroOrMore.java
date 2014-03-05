package dk.ange.parserbase.factoryhelperclasses;

import dk.ange.parserbase.Sequence;
import dk.ange.parserbase.ZeroOrMore;

/**
 * Extend the base class to create ZeroOrMore instances once the followed-by part is provided.
 *
 * @param <T>
 *            The lexer type of the input to parse.
 * @param <D>
 *            The actual data type of the input to parse.
 * @param <R>
 *            The result of parsing the input.
 */
public class BackpatchableZeroOrMore<T, D, R> extends BackpatchableFollowedBy<T, D, R> {

    /**
     * Initialize super with the optional part of this sequence.
     *
     * @param optionalPart
     *            The optional part of this sequence.
     */
    public BackpatchableZeroOrMore(final Sequence<T, D, R> optionalPart) {
        super(optionalPart);
    }

    @Override
    protected Sequence<T, D, R> makeWrapped(final Sequence<T, D, R> optionalPart, final Sequence<T, D, R> followedBy) {
        return new ZeroOrMore<>(optionalPart, followedBy);
    }

    @Override
    public String toString() {
        return "FactoryZeroOrMore " + super.toString();
    }

}
