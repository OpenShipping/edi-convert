package dk.ange.stowbase.parse.utils;

/**
 * Bay, Row, Level triplet. Immutable.
 */
public class BRL implements Comparable<BRL> {

    /**
     * Bay
     */
    public final String bay;

    /**
     * Row
     */
    public final String row;

    /**
     * Level
     */
    public final String level;

    /**
     * @param bay
     * @param row
     * @param level
     */
    public BRL(final String bay, final String row, final String level) {
        validateNotNull(bay, "bay");
        validateNotNull(row, "row");
        validateNotNull(level, "level");
        this.bay = bay;
        this.row = row;
        this.level = level;
    }

    private void validateNotNull(final Object check, final String name) {
        if (check == null) {
            throw new NullPointerException(name + " is null");
        }
    }

    /**
     * @param otherBay
     * @return a new BRL with changed bay or the same BRL if otherBay is the same as bay
     */
    public BRL withOtherBay(final String otherBay) {
        if (bay.equals(otherBay)) {
            return this;
        } else {
            return new BRL(otherBay, row, level);
        }
    }

    @Override
    public String toString() {
        return "BRL[" + bay + "," + row + "," + level + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bay == null) ? 0 : bay.hashCode());
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + ((row == null) ? 0 : row.hashCode());
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
        final BRL other = (BRL) obj;
        if (bay == null) {
            if (other.bay != null) {
                return false;
            }
        } else if (!bay.equals(other.bay)) {
            return false;
        }
        if (level == null) {
            if (other.level != null) {
                return false;
            }
        } else if (!level.equals(other.level)) {
            return false;
        }
        if (row == null) {
            if (other.row != null) {
                return false;
            }
        } else if (!row.equals(other.row)) {
            return false;
        }
        return true;
    }

    /**
     * Compares based on the bay value as a number
     */
    @Override
    public int compareTo(final BRL o) {
        final int cmpBay = intStringsCompare(bay, o.bay);
        if (cmpBay != 0) {
            return cmpBay;
        }

        final int cRow = row.compareTo(o.row);
        // TODO order row by number: final int cRow = intStringsCompare(row, o.row);
        if (cRow != 0) {
            return cRow;
        }

        return level.compareTo(o.level);
    }

    /**
     * Compare string1 and string2 when handling that they might be integers. Numbers are compared before string,
     * numbers and strings are individually compared using normal ordering.
     *
     * @param string1
     * @param string2
     * @return string1 <=> string2
     */
    public static int intStringsCompare(final String string1, final String string2) {
        final Integer integer1 = parseInt(string1);
        final Integer integer2 = parseInt(string2);
        if (integer1 == null) {
            if (integer2 == null) {
                return string1.compareTo(string2);
            } else {
                return -1;
            }
        } else {
            if (integer2 == null) {
                return 1;
            } else {
                return integer1.compareTo(integer2);
            }
        }
    }

    private static Integer parseInt(final String s) {
        try {
            return Integer.parseInt(s);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

}
