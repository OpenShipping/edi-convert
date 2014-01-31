package dk.ange.stowbase.edifact.lexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Can read all data from an Iterator into a List
 */
public abstract class IteratorToList {

    /**
     * @param <T>
     * @param iterator
     *            Iterator to read from, will be read to the end
     * @return a list with all the data from iterator
     */
    public static <T> List<T> read(final Iterator<T> iterator) {
        final List<T> list = new ArrayList<T>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

}
