package dk.ange.stowbase.parse.utils;

/**
 * We throw this exception when the parsing fails
 */
public class ParseException extends RuntimeException {

    /**
     * Constructor like in {@link RuntimeException}
     */
    public ParseException() {
        super();
    }

    /**
     * Constructor like in {@link RuntimeException}
     *
     * @param message
     * @param cause
     */
    public ParseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor like in {@link RuntimeException}
     *
     * @param message
     */
    public ParseException(final String message) {
        super(message);
    }

    /**
     * Constructor like in {@link RuntimeException}
     *
     * @param cause
     */
    public ParseException(final Throwable cause) {
        super(cause);
    }

}
