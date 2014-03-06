package dk.ange.stowbase.edifact.parser;

import dk.ange.parserbase.ItemProvider;
import dk.ange.parserbase.ParseError;
import dk.ange.parserbase.ParseState;
import dk.ange.parserbase.Sequence;
import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.Tag;

/**
 * A class that peeks and interprets the next state based on the available input. It does not actually parse, as it does
 * not consume any input symbols.
 *
 * TODO: Implement three states explicitly. Ought to be in state NOT_SET until parsed, and have a RESET method.
 *
 * @author Anders Sewerin Johansen
 *
 */
public class PeekLoopOrExit implements Sequence<Tag, Segment, ContentHandler> {

    private Sequence<Tag, Segment, ContentHandler> headOfLoop;

    private final Sequence<Tag, Segment, ContentHandler> exitPoint;

    private boolean nextIsHeadOfLoop = false;

    private boolean notYetVisited = true;

    PeekLoopOrExit(final Sequence<Tag, Segment, ContentHandler> exitPoint) {
        this.exitPoint = exitPoint;
    }

    void setHeadOfLoop(final Sequence<Tag, Segment, ContentHandler> headOfLoop) {
        this.headOfLoop = headOfLoop;
    }

    @Override
    public boolean consumes(final Tag type) {
        assertReady();
        return headOfLoop.consumes(type) || exitPoint.consumes(type);
    }

    @Override
    @SuppressWarnings("unused")
    public ParseState<ContentHandler> parse(final ItemProvider<Tag, Segment> itemProvider,
            final ParseState<ContentHandler> initialState) throws ParseError {
        assertReady();
        if (!notYetVisited) {
            throw new RuntimeException("This instance should have been reset before being visited again. "
                    + "The grammar is probably buggy.");
        }
        notYetVisited = false;
        nextIsHeadOfLoop = false;
        final Tag nextTag = itemProvider.peek().getLexerType();
        if (headOfLoop.consumes(nextTag)) {
            nextIsHeadOfLoop = true;
        }
        return initialState;
    }

    boolean getNextIsHeadOfLoop() {
        if (notYetVisited) {
            throw new RuntimeException("It is an error to query this instance for what is the exit "
                    + "point if it hasn't yet been visited");
        }
        return nextIsHeadOfLoop;
    }

    private void assertReady() {
        if (exitPoint == null) {
            throw new RuntimeException("LoopOrExit not ready - exitPoint not set yet");
        }
    }

    /**
     * Reset this instance so it is ready to peek at the input (again).
     */
    public void setNotYetVisited() {
        this.notYetVisited = true;
    }
}
