package dk.ange.stowbase.parse.vessel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapping of bays between 'BayName', 'TwentyFore', 'Forty' and 'TwentyAft'. Can also determine fore/aft'ness of 20 foot
 * bays.
 */
public class BaysMapping {

    private final List<BayLabels> data;

    private BaysMapping(final List<BayLabels> data) {
        this.data = Collections.unmodifiableList(data);
    }

    /**
     * @return all bay names
     */
    public Collection<String> bayNames() {
        final List<String> bayNames = new ArrayList<>();
        for (final BayLabels bayLabels : data) {
            bayNames.add(bayLabels.bayName);
        }
        return bayNames;
    }

    /**
     * @param bayNumber
     *            a 20 or 40 name
     * @return the bay name
     * @throws IllegalArgumentException
     *             if the name is not found
     */
    public String bayName(final String bayNumber) {
        if (bayNumber == null) {
            throw new NullPointerException("bayNumber == null");
        }
        for (final BayLabels bayLabels : data) {
            if (bayNumber.equals(bayLabels.twentyFore) || bayNumber.equals(bayLabels.forty)
                    || bayNumber.equals(bayLabels.twentyAft)) {
                return bayLabels.bayName;
            }
        }
        throw new IllegalArgumentException("bayNumber '" + bayNumber + "' is not found");
    }

    /**
     * @param bayNumber
     * @return Return position of the bay
     * @throws IllegalArgumentException
     *             if the bay is not found
     */
    public TwentyForeAftForty position(final String bayNumber) {
        if (bayNumber == null) {
            throw new NullPointerException("bayNumber == null");
        }
        for (final BayLabels bayLabels : data) {
            if (bayNumber.equals(bayLabels.twentyFore)) {
                return TwentyForeAftForty.TWENTY_FORE;
            }
            if (bayNumber.equals(bayLabels.forty)) {
                return TwentyForeAftForty.FORTY;
            }
            if (bayNumber.equals(bayLabels.twentyAft)) {
                return TwentyForeAftForty.TWENTY_AFT;
            }
        }
        throw new IllegalArgumentException("bayNumber '" + bayNumber + "' is not found");
    }

    /**
     * The builder for {@link BaysMapping}
     */
    public static class BaysMappingBuilder {
        private final List<BayLabels> data = new ArrayList<>();

        private final UniqueCheck<String> uniqueBayName = new UniqueCheck<>();

        private final UniqueCheck<String> uniqueBayNumber = new UniqueCheck<>();

        /**
         * @param bayName
         * @param twentyFore
         * @param forty
         * @param twentyAft
         */
        public void add(final String bayName, final String twentyFore, final String forty, final String twentyAft) {
            uniqueBayName.check(bayName);
            final String nonEmptyTwentyFore = mapEmptyToNull(twentyFore);
            final String nonEmptyForty = mapEmptyToNull(forty);
            final String nonEmptyTwentyAft = mapEmptyToNull(twentyAft);
            uniqueBayNumber.check(nonEmptyTwentyFore);
            uniqueBayNumber.check(nonEmptyForty);
            uniqueBayNumber.check(nonEmptyTwentyAft);
            data.add(new BayLabels(bayName, nonEmptyTwentyFore, nonEmptyForty, nonEmptyTwentyAft));
        }

        /**
         * @return a new BaysMapping
         */
        public BaysMapping build() {
            return new BaysMapping(data);
        }
    }

    private static class BayLabels {
        final String bayName;

        final String twentyFore;

        final String forty;

        final String twentyAft;

        BayLabels(final String bayName, final String twentyFore, final String forty, final String twentyAft) {
            this.bayName = bayName;
            this.twentyFore = twentyFore;
            this.forty = forty;
            this.twentyAft = twentyAft;
        }
    }

    private static String mapEmptyToNull(final String string) {
        if (string != null && string.isEmpty()) {
            return null;
        }
        return string;
    }

    private static class UniqueCheck<T> {
        private final Set<T> set = new HashSet<>();

        void check(final T unique) {
            if (unique == null) {
                return;
            }
            if (!set.add(unique)) {
                throw new RuntimeException("'" + unique + "' has already been seen");
            }
        }
    }

    /**
     * Describe fore/aft'ness of a bay
     */
    public static enum TwentyForeAftForty {
        /**
         * Is a 20' bay in the fore part of a pairing
         */
        TWENTY_FORE,
        /**
         * Is a 40' bay
         */
        FORTY,
        /**
         * Is a 20' bay in the aft part of a pairing
         */
        TWENTY_AFT;

        /**
         * @return "sign" of fore/aft'ness. Fore is +1, Aft is -1, Forty is 0
         */
        public int sign() {
            switch (this) {
            case TWENTY_FORE:
                return 1;
            case FORTY:
                return 0;
            case TWENTY_AFT:
                return -1;
            }
            throw new RuntimeException("Can not happen");
        }

        /**
         * @return true if bay is 20'
         */
        public boolean isTwenty() {
            switch (this) {
            case TWENTY_FORE:
            case TWENTY_AFT:
                return true;
            case FORTY:
                return false;
            }
            throw new RuntimeException("Can not happen");
        }
    }

}
