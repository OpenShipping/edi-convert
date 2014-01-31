package dk.ange.parserbase;

/**
 * The kind of exception that is thrown during parsing of various grammars.
 */
public class ParseError extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2536082365892646339L;

    private final int position;

    private final String positionDescription;

    /**
     * @param message
     *            A human readable reason for this exception to occur.
     * @param cause
     *            The exception that caused it.
     * @param position
     *            The position in input (0-based)
     * @param positionDescription
     *            A description of the positions in input ("line", "spreadsheet row"...)
     */
    public ParseError(final String message, final Throwable cause, final int position, final String positionDescription) {
        super(message, cause);
        this.position = position;
        this.positionDescription = positionDescription;
    }

    /**
     * @param message
     *            A human readable reason for this exception to occur.
     * @param position
     *            The position in input (0-based)
     * @param positionDescription
     *            A description of the positions in input ("line", "spreadsheet row"...)
     */
    public ParseError(final String message, final int position, final String positionDescription) {
        super(message);
        this.position = position;
        this.positionDescription = positionDescription;
    }

    @Override
    public String getMessage() {
        return "Parse error at " + positionDescription + " " + position + " (0 is first) : " + super.getMessage();
    }

    /**
     * Access the position of this parse error in the input.
     * 
     * @return The position of this parse error in the input.
     */
    public int getPosition() {
        return position;
    }

}
