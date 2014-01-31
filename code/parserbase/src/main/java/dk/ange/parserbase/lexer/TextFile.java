package dk.ange.parserbase.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * This class allows line-by-line iteration through a text file. The iterator's remove() method throws
 * UnsupportedOperatorException. The iterator wraps and rethrows IOExceptions as IllegalArgumentExceptions.
 */
public class TextFile implements Iterable<String> {

    // Used by the TextFileIterator class below
    private final InputStream input;

    /**
     * Create a new instance.
     * 
     * @param i
     *            The input stream
     */
    public TextFile(final InputStream i) {
        this.input = i;
    }

    // This is the one method of the Iterable interface
    public Iterator<String> iterator() {
        return new TextFileIterator();
    }

    // This non-static member class is the iterator implementation
    class TextFileIterator implements Iterator<String> {

        // The stream we're reading from
        final BufferedReader in;

        // Return value of next call to next()
        String nextline;

        TextFileIterator() {
            // Open the file and read and remember the first line.
            // We peek ahead like this for the benefit of hasNext().
            try {
                in = new BufferedReader(new InputStreamReader(input));
                nextline = in.readLine();
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        // If the next line is non-null, then we have a next line
        public boolean hasNext() {
            return nextline != null;
        }

        // Return the next line, but first read the line that follows it.
        public String next() {
            try {
                final String result = nextline;

                // If we haven't reached EOF yet
                if (nextline != null) {
                    nextline = in.readLine(); // Read another line
                    // Do not close stream because it could be a zipentry
                }

                // Return the line we read last time through.
                return result;
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        // The file is read-only; we don't allow lines to be removed.
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
