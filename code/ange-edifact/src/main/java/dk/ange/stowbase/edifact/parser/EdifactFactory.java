package dk.ange.stowbase.edifact.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dk.ange.parserbase.CompoundSequence;
import dk.ange.parserbase.DataItemParser;
import dk.ange.parserbase.ItemProvider;
import dk.ange.parserbase.OneOrMore;
import dk.ange.parserbase.ParseError;
import dk.ange.parserbase.ParseState;
import dk.ange.parserbase.Sequence;
import dk.ange.parserbase.SingleItemSequence;
import dk.ange.parserbase.ZeroOrMore;
import dk.ange.stowbase.edifact.Segment;
import dk.ange.stowbase.edifact.Tag;
import dk.ange.stowbase.edifact.format.AbstractSegmentFormat;
import dk.ange.stowbase.edifact.format.SegmentFormat;
import dk.ange.stowbase.edifact.format.SegmentGroupFormat;

/**
 * A class capable of generating a parser for an EDIFACT document based on the provided format description.
 *
 * TODO: Counters for max number of repetitions are not enforced. This is only relevant if we want to make a validator
 * really, as we prefer a forgiving parser to a strict one.
 *
 *
 * @author Anders Sewerin Johansen
 *
 */
public class EdifactFactory /* extends Factory<Tag, Segment, ContentHandler> */{

    private final static DataItemParser<Segment, ContentHandler> NEUTRAL = new DataItemParser<Segment, ContentHandler>() {

        public ParseState<ContentHandler> parse(final Segment item, final ParseState<ContentHandler> parseState) {
            // Do nothing on purpose.
            return parseState;
        }
    };

    private static final SingleItemSequence<Tag, Segment, ContentHandler> END_OF_FILE_SEQUENCE = new SingleItemSequence<>(
            Tag.END_OF_EDIFACT_FILE, NEUTRAL);

    private static class SegmentGroupSequence implements Sequence<Tag, Segment, ContentHandler> {

        private final Sequence<Tag, Segment, ContentHandler> followedBy, groupAsSequence;

        private final PeekLoopOrExit loopOrExit;

        private int numExecutions = 0;

        private final boolean groupIsMandatory;

        private final String pathToParent;

        SegmentGroupSequence(final SegmentGroupFormat group, final Sequence<Tag, Segment, ContentHandler> followedBy,
                final String pathToParent) {
            this.followedBy = followedBy;
            loopOrExit = new PeekLoopOrExit(followedBy);
            groupAsSequence = groupToSequence(group, loopOrExit, pathToParent);
            loopOrExit.setHeadOfLoop(groupAsSequence);
            this.groupIsMandatory = group.isMandatory();
            this.pathToParent = pathToParent;
        }

        public boolean consumes(final Tag type) {
            if (groupIsMandatory && numExecutions == 0) {
                return groupAsSequence.consumes(type);
            }
            return groupAsSequence.consumes(type) || followedBy.consumes(type);
        }

        public ParseState<ContentHandler> parse(final ItemProvider<Tag, Segment> itemProvider,
                final ParseState<ContentHandler> initialState) throws ParseError {

            numExecutions = 0;

            // If bypass, go ahead and bypass
            if (!groupIsMandatory) {
                final Tag firstTag = itemProvider.peek().getLexerType();
                if (followedBy.consumes(firstTag)) {
                    return followedBy.parse(itemProvider, initialState);
                }
            }

            // If not, we need to loop until exit

            ParseState<ContentHandler> currentState = initialState;

            final String groupPosition;
            if (pathToParent.endsWith("/")) {
                groupPosition = pathToParent.substring(0, pathToParent.length() - 1);
            } else {
                groupPosition = pathToParent;
            }
            while (true) {
                numExecutions++;
                loopOrExit.setNotYetVisited();
                initialState.getResult().startGroup(groupPosition);
                try {
                    final Tag nextTag = itemProvider.peek().getLexerType();
                    // This check may be redundant, but better to be sure and provide a readable error message
                    if (!groupAsSequence.consumes(nextTag)) {
                        throw new RuntimeException("Expected to be able to consume " + nextTag + ", wrapped is:\n"
                                + groupAsSequence);
                    }
                    // Parse the segment once
                    currentState = groupAsSequence.parse(itemProvider, currentState);
                } finally {
                    initialState.getResult().endGroup(groupPosition);
                }

                // Are we done? If so, follow the exit point.
                if (!loopOrExit.getNextIsHeadOfLoop()) {
                    return followedBy.parse(itemProvider, currentState);
                }

            }
        }
    }

    private static Sequence<Tag, Segment, ContentHandler> groupToSequence(final SegmentGroupFormat group,
            final Sequence<Tag, Segment, ContentHandler> followedBy, final String pathToParent) {

        final List<AbstractSegmentFormat> members = new LinkedList<>(group.getMembers());
        final List<Pair<AbstractSegmentFormat, String>> membersReversed = reversedMembers(pairWithUniqueTags(members));

        Sequence<Tag, Segment, ContentHandler> current = followedBy;
        for (final Pair<AbstractSegmentFormat, String> pair : membersReversed) {
            final AbstractSegmentFormat member = pair.getFirst();
            final String countedTagName = pair.getSecond();
            if (member instanceof SegmentFormat) {
                current = itemToSequence(((SegmentFormat) member), current, pathToParent, countedTagName);
            } else if (member instanceof SegmentGroupFormat) {
                current = new SegmentGroupSequence(((SegmentGroupFormat) member), current, pathToParent
                        + countedTagName + "/");
            }
        }
        return current;
    }

    private static List<Pair<AbstractSegmentFormat, String>> pairWithUniqueTags(
            final List<AbstractSegmentFormat> members) {
        final List<Pair<AbstractSegmentFormat, String>> res = new ArrayList<>(
                members.size());
        final Map<String, Integer> countMap = new HashMap<>();

        for (final AbstractSegmentFormat item : members) {
            final String uncountedTag;
            if (item instanceof SegmentFormat) {
                uncountedTag = ((SegmentFormat) item).getTag().toString();
            } else if (item instanceof SegmentGroupFormat) {
                uncountedTag = getFirstTagInMembers((SegmentGroupFormat) item);
            } else {
                throw new RuntimeException(
                        "Expected to find an instance of either SegmentFormat or SegmentGroupFormat, instead got '"
                                + item.getClass() + "'");
            }
            final String countedTag;
            if (countMap.containsKey(uncountedTag)) {
                final int thisCount = countMap.get(uncountedTag) + 1;
                countedTag = uncountedTag + thisCount;
                countMap.put(uncountedTag, thisCount);
            } else {
                countedTag = uncountedTag;
                countMap.put(uncountedTag, 1);
            }
            res.add(new Pair<>(item, countedTag));
        }
        return res;
    }

    private static String getFirstTagInMembers(final SegmentGroupFormat group) {
        final AbstractSegmentFormat firstAsAbstractSegment = group.getMembers().get(0);
        if (!(firstAsAbstractSegment instanceof SegmentFormat)) {
            throw new RuntimeException("Expected to find an instance of SegmentFormat, instead found "
                    + firstAsAbstractSegment.getClass());
        }
        return ((SegmentFormat) firstAsAbstractSegment).getTag().toString();
    }

    private static List<Pair<AbstractSegmentFormat, String>> reversedMembers(
            final List<Pair<AbstractSegmentFormat, String>> members) {
        final List<Pair<AbstractSegmentFormat, String>> membersReversed = new ArrayList<>(
                members);
        Collections.reverse(membersReversed);
        return membersReversed;
    }

    private static Sequence<Tag, Segment, ContentHandler> itemToSequence(final SegmentFormat item,
            final Sequence<Tag, Segment, ContentHandler> followedBy, final String pathToParent,
            final String countedTagName) {
        final Sequence<Tag, Segment, ContentHandler> toWrap = new SingleItemSequence<>(item
                .getTag(), new EdifactParseAction(pathToParent, countedTagName));
        if (item.isMandatory()) {
            final List<Sequence<Tag, Segment, ContentHandler>> compositeSequence = new ArrayList<>(
                    2);
            compositeSequence.add(new OneOrMore<>(toWrap));
            compositeSequence.add(followedBy);
            return new CompoundSequence<>(compositeSequence);
        } else {
            return new ZeroOrMore<>(toWrap, followedBy);
        }
    }

    /**
     * Make a sequence that will parse an Edifact file that conforms to the format described by group.
     *
     * @param group
     *            A description of this particular kind of Edifact file.
     * @return A sequence that will parse an Edifact file that conforms to the format described by group.
     */
    public static Sequence<Tag, Segment, ContentHandler> makeSequence(final SegmentGroupFormat group) {
        return new SegmentGroupSequence(group, END_OF_FILE_SEQUENCE, ""); // No parent for first segment;
    }

}
