package dk.ange.parserbase;


/**
 * A test action that records the number of times it has been executed etc.
 */
public class Action implements DataItemParser<String, Integer> {

    int executed = 0;

    Integer seenResult = null;

    String seenDataItem = "";

    private final Integer setTo;

    Action(final Integer setTo) {
        this.setTo = setTo;
    }

    @Override
    public ParseState<Integer> parse(final String item, final ParseState<Integer> parseState) {
        executed++;
        seenResult = parseState.getResult();
        seenDataItem = item;

        final ParseState<Integer> res = new ParseState<>(setTo);

        return res;
    }

}
