package dk.ange.stowbase.edifact.parser;

/**
 * A generic pair a la C++ std::pair.
 *
 * Pairs two instances of types F and S, whatever they might be.
 *
 * @author Anders Sewerin Johansen
 *
 * @param <F>
 *            Type of first item in pair.
 * @param <S>
 *            Type of second item in pair.
 */
public class Pair<F, S> {

    private final F first;

    private final S second;

    /**
     * Bind two item into a pair.
     *
     * @param first
     *            The first item in the pair.
     * @param second
     *            The second item in the pair.
     */
    public Pair(final F first, final S second) {
        this.first = first;
        this.second = second;
    }

    /**
     *
     * @return The first item of this pair.
     */
    public F getFirst() {
        return first;
    }

    /**
     *
     * @return The second item of this pair.
     */
    public S getSecond() {
        return second;
    }
}
