package ed.inf.adbs.minibase.utils;

import ed.inf.adbs.minibase.base.Constant;
import java.util.List;

/**
 * Class to represent a tuple in the database
 */
public class Tuple {
    /** The values of the tuple as a list of Constants*/
    private final List<Constant> fields;

    /**
     * Constructor for the Tuple. Sets the private variable to be the input
     * list of Constants.
     *
     * @param fields List of Constants
     */
    public Tuple(List<Constant> fields) {
        this.fields = fields;
    }

    /**
     * Getter method for accessing the Tuple fields.
     *
     * @return returns a List of Constants
     */
    public List<Constant> getFields() {
        return fields;
    }

    /**
     * Overridden toString method. The fields are printed comma separated, and
     * a newline is added at the end.
     *
     * @return returns the tuple as a string
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Constant c:fields){
            s.append(c.toString()).append(",");
        }
        return s.substring(0, s.length() - 1) + "\n";
    }

    /**
     * Overridden hashCode method.
     *
     * @return returns hashcode of the fields list.
     */
    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    /**
     * Overridden equals method. Two Tuples are equal if the fields are equal.
     *
     * @param o Object to compare this Tuple to
     * @return returns true if the input Object o is a Tuple and the fields are
     * equal to this tuples fields.
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof Tuple){
            Tuple comp = (Tuple) o;
            return this.fields.equals(comp.getFields());
        }
        return false;
    }
}
