package dk.ange.parserbase.lexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * A base class that does as much of the work as possible of writing a naiive lexer for
 * "files that can be parsed as lines of text"
 *
 * @param <T>
 *            THe lexer type to assign to the lines of text.
 */
public abstract class LineBasedTextFileLexerBase<T> extends LexerBase<T, String> {

    /**
     * Create this instance and lex the input.
     *
     * @param i
     *            The stream to read the input data from.
     *
     * @throws IOException
     */
    public LineBasedTextFileLexerBase(final InputStream i) throws IOException {
        super(i);
    }

    @SuppressWarnings("unused")
    @Override
    protected Iterator<String> getDataIterator(final InputStream i) throws IOException {
        return new TextFile(i).iterator();

    }

    @Override
    protected String getDummyDataItem() {
        return "";
    }

    @Override
    public String describePositionType() {
        return "line";
    }

}
