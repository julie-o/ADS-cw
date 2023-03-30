package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.utils.Tuple;

import java.io.IOException;
import java.util.*;

/**
 * Class for sum aggregate operations.
 */
public class SumOperator extends Operator {
    /** Child operator */
    private final Operator child;
    /** Aggregation function */
    private final SumAggregate agg;
    /** Base relational atom */
    private final RelationalAtom atom;
    /** List of variables to group on */
    private final List<Variable> group;
    /** Tuples in the grouped output */
    private List<Tuple> tuples = null;
    /** Boolean to block non-grouping getNextTuple() after non-grouping sum aggregation */
    private boolean block;


    /**
     * Constructor for the SumOperator class.
     *
     * @param child child operator
     * @param agg aggregation function class instance
     * @param atom base relational atom
     * @param group list of variables to group on
     */
    public SumOperator(Operator child, SumAggregate agg, RelationalAtom atom, List<Variable> group){
        this.child = child;
        this.agg = agg;
        this.atom = atom;
        this.group = group;
        this.block = false;
    }

    /**
     * Method for getting the tuples from the result of grouping and aggregation. This is a
     * blocking operator that only first uses helper methods to compute the aggregations by
     * iterating over all the child operation and only then returns results.
     *
     * @return returns a tuple with the grouping variables and aggregation result
     * @throws IOException throws an error if reading from file at a leaf node was unsuccessful
     */
    @Override
    public Tuple getNextTuple() throws IOException {
        if (block) return null;
        if (group.size()==0){
            List<Constant> sum = Collections.singletonList(new IntegerConstant(noGrouping()));
            block = true;
            return new Tuple(sum);
        } else {
            // group if grouping has not been done, otherwise retrieve a tuple from the list
            if (tuples==null){
                withGrouping();
            }
            if (tuples.size()==0){
                return null;
            } else {
                Tuple tuple = tuples.get(0);
                tuples.remove(0);
                return tuple;
            }
        }
    }

    /**
     * Helper method for getNextTuple() for grouping on a list of variables. Stores the
     * grouped Tuples to the "tuples"-variable
     *
     * @throws IOException throws an error if reading from file at a leaf node was unsuccessful
     */
    private void withGrouping() throws IOException {
        tuples = new ArrayList<>();

        // group by updating a map where the key is a list of constants
        Map<List<Constant>,Integer> grouped = new HashMap<>();

        // iterate through child operator
        Tuple nextTuple = child.getNextTuple();
        while (nextTuple != null) {
            List<Constant> groupby = new ArrayList<>();
            for (Term term:group){
                int index = atom.getTerms().indexOf(term);
                if (index>=0){
                    groupby.add(nextTuple.getFields().get(index));
                }
            }

            Integer oldValue = grouped.get(groupby);
            if (oldValue==null){
                grouped.put(groupby, sumForRow(nextTuple));
            } else {
                grouped.put(groupby, oldValue+sumForRow(nextTuple));
            }

            nextTuple = child.getNextTuple();
        }

        // for each list of constants, add a new Tuple to the output (with the constants and integer value as fields)
        for (List<Constant> key : grouped.keySet()){
            List<Constant> tupleTerms = new ArrayList<>(key);
            tupleTerms.add(new IntegerConstant(grouped.get(key)));
            tuples.add(new Tuple(tupleTerms));
        }
    }

    /**
     * Helper method for getNextTuple() for computing an aggregate on the entire child
     * operator. Used when there are no grouping variables specified when initialising operator.
     *
     * @return an integer computed using the aggregate function
     * @throws IOException throws an error if reading from file at a leaf node was unsuccessful
     */
    private int noGrouping() throws IOException {
        int sum = 0;

        // iterate over child operator
        Tuple nextTuple = child.getNextTuple();
        while (nextTuple != null) {
            sum = sum + sumForRow(nextTuple);
            nextTuple  = child.getNextTuple();
        }
        return sum;
    }

    /**
     * Helper method for computing the (product) aggregate value for a single input
     * tuple from the child operator.
     *
     * @param tuple tuple to compute aggregate value for
     * @return returns an integer
     */
    private int sumForRow(Tuple tuple){
        int sumForRow = getValue(agg.getProductTerms().get(0),tuple);
        for (int i=1;i<agg.getProductTerms().size();i++){
            Term term = agg.getProductTerms().get(i);
            sumForRow = sumForRow * getValue(term,tuple);
        }
        return sumForRow;
    }

    /**
     * Helper method for sumForRow() for extracting integers given a term. If the term is
     * a variable, the value is extracted from the input tuple. If the term is a constant,
     * the integer value from that constant instance is returned.
     *
     * @param term variable to extract from tuple or a constant
     * @param tuple tuple to extract constant from
     * @return returns an integer
     */
    private int getValue(Term term, Tuple tuple){
        if (term instanceof Variable) {
            int index = atom.getTerms().indexOf(term);
            if (index>=0){
                if (!(tuple.getFields().get(index) instanceof IntegerConstant)) throw new IllegalArgumentException("Field is not an integer");
                IntegerConstant i = (IntegerConstant) tuple.getFields().get(index);
                return i.getValue();
            }
        } else if (term instanceof IntegerConstant){
            return ((IntegerConstant) term).getValue();
        }

        throw new IllegalArgumentException("Term not in tuple: term=" + term + ", tuple=" + tuple);
    }

    /**
     * Method for resetting the SumOperator. The next getNextTuple() call will start
     * reading at the beginning of the child operation output.
     */
    @Override
    public void reset() {
        child.reset();
    }

    /**
     * Method for retrieving the base RelationalAtom
     *
     * @return returns the base RelationalAtom
     */
    @Override
    public RelationalAtom getAtom() {
        return atom;
    }

}
