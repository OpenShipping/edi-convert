package dk.ange.parserbase.factoryhelperclasses;

import dk.ange.parserbase.Optional;
import dk.ange.parserbase.Sequence;

/**
 * Extend the base class to create Optional instances once the followed-by part is provided.
 *
 * @param <T>
 *            The lexer type of the input to parse.
 * @param <D>
 *            The actual data type of the input to parse.
 * @param <R>
 *            The result of parsing the input.
 */
public class BackpatchableOptional<T, D, R> extends BackpatchableFollowedBy<T, D, R> {

    /**
     * Initialize super with the optional part of this sequence.
     *
     * @param optionalPart
     *            The optional part of this sequence.
     */
    public BackpatchableOptional(final Sequence<T, D, R> optionalPart) {
        super(optionalPart);
    }

    @Override
    protected Sequence<T, D, R> makeWrapped(final Sequence<T, D, R> optionalPart, final Sequence<T, D, R> followedBy) {
        return new Optional<>(optionalPart, followedBy);
    }

    @Override
    public String toString() {
        return "FactoryOptional " + super.toString();
    }

}
