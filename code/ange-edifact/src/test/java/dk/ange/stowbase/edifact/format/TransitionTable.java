package dk.ange.stowbase.edifact.format;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.ange.stowbase.edifact.Tag;

/**
 * This is dead code that never was finished. It might be a good place to start if we need to speed up the EDIFACT
 * parser.
 */
public class TransitionTable {

    private final Map<StateInput, SegmentFormat> transitionTable;

    private final List<SegmentFormat> segments;

    /**
     * @param segmentGroupFormat
     */
    public TransitionTable(final SegmentGroupFormat segmentGroupFormat) {
        this.segments = getSegments(segmentGroupFormat);
        transitionTable = new HashMap<>();
        SegmentFormat state = null;
        final Collection<SegmentFormat> conditionals = new ArrayList<>();
        for (final SegmentFormat nextState : segments) {
            isNull(transitionTable.put(new StateInput(state, nextState.getTag()), nextState));
            for (final SegmentFormat conditional : conditionals) {
                transitionTable.put(new StateInput(conditional, nextState.getTag()), nextState);
            }
            if (nextState.isMandatory()) {
                conditionals.clear();
            }
            conditionals.add(nextState);
            if (nextState.getOccurrences() > 1) {
                transitionTable.put(new StateInput(nextState, nextState.getTag()), nextState);
            }
            state = nextState;
        }
        // TODO repeat o>1 groups in transitionTable
        // 21 (EQD), LOC -> 11
        /*
         * If nextState.isLastInGroup
         *
         * Double pop ?
         */
        // TODO skip Conditional groups in transitionTable
    }

    /**
     * @param o
     * @throws RuntimeException
     *             if o is not null
     */
    private void isNull(final Object o) {
        if (o != null) {
            throw new RuntimeException("is not null: " + o);
        }
    }

    private List<SegmentFormat> getSegments(final SegmentGroupFormat group) {
        final List<SegmentFormat> segments1 = new ArrayList<>();
        getSegments(segments1, group);
        return segments1;
    }

    private void getSegments(final List<SegmentFormat> segments1, final SegmentGroupFormat group2) {
        for (final AbstractSegmentFormat member : group2.getMembers()) {
            if (member instanceof SegmentFormat) {
                segments1.add((SegmentFormat) member);
            } else if (member instanceof SegmentGroupFormat) {
                getSegments(segments1, (SegmentGroupFormat) member);
            } else {
                throw new RuntimeException("Unknown class " + member);
            }
        }
    }

    /**
     * @param out
     */
    public void write(final PrintStream out) {
        // Create list of possible states (segments)
        final List<SegmentFormat> states = new ArrayList<>(segments);
        states.add(0, null);
        // Create list of possible inputs (tags)
        final Set<Tag> seenTag = new HashSet<>();
        final List<Tag> inputs = new ArrayList<>();
        for (final SegmentFormat format : segments) {
            if (seenTag.contains(format.getTag())) {
                continue;
            }
            inputs.add(format.getTag());
            seenTag.add(format.getTag());
        }
        // Header
        out.print("   ");
        for (final SegmentFormat state : states) {
            out.print(String.format("%3d", states.indexOf(state)));
        }
        out.println();
        // Table
        for (final Tag input : inputs) {
            out.print(input);
            for (final SegmentFormat state : states) {
                final SegmentFormat transition = transitionTable.get(new StateInput(state, input));
                if (transition != null) {
                    out.print(String.format("%3d", states.indexOf(transition)));
                } else {
                    out.print("  .");
                }
            }
            out.println();
        }
    }

    /**
     * @param state
     * @param input
     * @return the new state
     */
    public SegmentFormat getTransition(final SegmentFormat state, final Tag input) {
        return transitionTable.get(new StateInput(state, input));
    }

    /**
     * Tuple of state and input, used as key in transition table
     */
    private static class StateInput {
        public final SegmentFormat state;

        public final Tag input;

        public StateInput(final SegmentFormat state, final Tag input) {
            this.state = state;
            this.input = input;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((input == null) ? 0 : input.hashCode());
            result = prime * result + ((state == null) ? 0 : state.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StateInput other = (StateInput) obj;
            if (input == null) {
                if (other.input != null) {
                    return false;
                }
            } else if (!input.equals(other.input)) {
                return false;
            }
            if (state == null) {
                if (other.state != null) {
                    return false;
                }
            } else if (!state.equals(other.state)) {
                return false;
            }
            return true;
        }
    }

}
