package dk.ange.stowbase.edifact.scanner;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Kim Hansen
 */
public final class EdifactScanner implements Iterator<Token> {

    private final InputStream is;

    private Token next;

    /**
     * @param is
     */
    public EdifactScanner(final InputStream is) {
        if (is.markSupported()) {
            this.is = is;
        } else {
            this.is = new BufferedInputStream(is);
        }
        updateNext();
    }

    public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final Token oldNext = next;
        updateNext();
        return oldNext;
    }

    private void updateNext() {
        try {
            next = getNext();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Token getNext() throws IOException {
        final int b = readNonLineTermination();
        if (b == -1) {
            return null;
        }
        if (b == SegmentTerminator.BYTE) {
            return SegmentTerminator.INSTANCE;
        }
        if (b == DataElementSeparator.BYTE) {
            return DataElementSeparator.INSTANCE;
        }
        if (b == CompositeDataElementSeparator.BYTE) {
            return CompositeDataElementSeparator.INSTANCE;
        }
        if (b == RepetitionSeparator.BYTE) {
            return RepetitionSeparator.INSTANCE;
        }
        return readDataElementValue(b);
    }

    /**
     * The escape char in the edifact docs.
     */
    public static final byte RELEASE_CHARACTER = '?';

    private Token readDataElementValue(final int b) throws IOException {
        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        int nextByte = b;
        while (true) {
            if (nextByte == RELEASE_CHARACTER) {
                nextByte = readNonLineTermination();
                if (!isServiceCharacter(nextByte)) {
                    throw new IllegalStateException("Released b=" + (char) nextByte);
                }
                if (nextByte == -1) {
                    throw new IllegalStateException("End of file after RELEASE_CHARACTER");
                }
            } else {
                if (isServiceCharacter(nextByte)) {
                    is.reset();
                    return new DataElementValueToken(bs.toString("Latin1"));
                }
            }

            bs.write(nextByte);

            nextByte = readNonLineTermination();

            if (nextByte == -1) {
                return new DataElementValueToken(bs.toString("Latin1"));
            }
        }
    }

    /**
     * A service character need to be escaped in the output.
     * 
     * @param b
     * @return Returns true if b is a service character.
     */
    public static boolean isServiceCharacter(final int b) {
        return (b == SegmentTerminator.BYTE) || (b == DataElementSeparator.BYTE)
                || (b == CompositeDataElementSeparator.BYTE) || (b == RepetitionSeparator.BYTE)
                || (b == RELEASE_CHARACTER);
    }

    private int readNonLineTermination() throws IOException {
        int b;
        do {
            is.mark(1);
            b = is.read();
        } while (b == '\r' || b == '\n');
        return b;
    }

    @Override
    public String toString() {
        return EdifactScanner.class.getSimpleName();
    }

    public boolean hasNext() {
        return next != null;
    }

    public void remove() {
        throw new UnsupportedOperationException(EdifactScanner.class.getSimpleName() + " can not remove()");
    }

}
