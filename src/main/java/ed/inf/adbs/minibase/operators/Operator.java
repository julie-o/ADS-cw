package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.utils.Tuple;
import ed.inf.adbs.minibase.utils.WriteCSV;

import java.io.IOException;

/**
 * Abstract class for the Operators.
 */
public abstract class Operator {
    /**
     * Method for retrieving the next tuple from the child-operator or file.
     *
     * @return returns a Tuple
     * @throws IOException throws an error if reading from file at a leaf node was unsuccessful
     */
    public abstract Tuple getNextTuple() throws IOException;

    /**
     * Method for resetting the getNextTuple() method, such that it starts reading at the
     * beginning of the child-operator the next time getNextTuple() is called.
     */
    public abstract void reset();

    /**
     * Method for printing the results of the query. If there is a valid WriteCSV instance, the
     * Tuples are dumped in a file, otherwise to standard output.
     */
    public void dump() {
        try {
            Tuple nextTuple = getNextTuple();
            while (nextTuple != null) {
                if (WriteCSV.getWriteCSV()!=null){
                    WriteCSV writer = WriteCSV.getWriteCSV();
                    writer.write(nextTuple);
                } else {
                    System.out.print(nextTuple);
                }
                nextTuple = getNextTuple();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for returning the base relational atom in the operation
     * @return returns a relational atom
     */
    public abstract RelationalAtom getAtom();
}
