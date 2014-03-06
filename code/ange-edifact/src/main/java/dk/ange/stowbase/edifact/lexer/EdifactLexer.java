package dk.ange.stowbase.edifact.lexer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.scanner.EdifactScanner;
import dk.ange.stowbase.edifact.scanner.SegmentTerminator;
import dk.ange.stowbase.edifact.scanner.Token;

/**
 * @author Kim Hansen
 */
public final class EdifactLexer implements Iterator<Segment> {

    private final EdifactScanner scanner;

    private Segment next;

    /**
     * @param stream
     */
    public EdifactLexer(final InputStream stream) {
        scanner = new EdifactScanner(stream);
        updateNext();
    }

    private void updateNext() {
        if (scanner.hasNext()) {
            final List<Token> tokenList = new ArrayList<>();
            while (true) {
                final Token token = scanner.next();
                if (token instanceof SegmentTerminator) {
                    next = new Segment(tokenList);
                    break;
                } else {
                    tokenList.add(token);
                }
            }
        } else {
            next = null;
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Segment next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final Segment oldNext = next;
        updateNext();
        return oldNext;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(EdifactLexer.class.getSimpleName() + " can not remove()");
    }

}
