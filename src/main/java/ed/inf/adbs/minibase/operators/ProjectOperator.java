package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.utils.Tuple;

import java.io.IOException;
import java.util.*;

/**
 * Class for projection operations.
 */
public class ProjectOperator extends Operator {
    /** Buffer that keeps track of seen Tuples. Needed for Set semantics */
    private Set<Tuple> outputBuffer;
    /** Child operator */
    private final Operator child;
    /** List of variables that should be in the output */
    private final List<Variable> outputVars;
    /** Indexes of the variables in the relational atom */
    private List<Integer> outputIndexes;
    /** Base relational atom */
    private RelationalAtom atom;

    /**
     * Constructor for the ProjectOperator class.
     *
     * @param child child operator
     * @param outputVars list of variables that should be in the output
     * @param atom base relational atom
     */
    public ProjectOperator(Operator child, List<Variable> outputVars, RelationalAtom atom) {
        this.child = child;
        this.outputVars = outputVars;
        this.outputBuffer = new HashSet<>();
        this.atom = atom;
        outputIndexes();
        updateAtom();
    }

    /**
     * Method to get the indexes of the variables in the base atom as a list. The index of
     * the variable in the base atom specifies the index of the constants in the Tuple from
     * the child operator. The list is sorted by the order of the outputVars parameter.
     */
    private void outputIndexes(){
        List<Integer> indexes = new ArrayList<>();
        for (Variable outputVar : outputVars) {
            int index = atom.getTerms().indexOf(outputVar);
            if (index >= 0) {
                indexes.add(index);
            }
        }
        outputIndexes = indexes;
    }

    /**
     * Updates the base relational atom so that it matches the output of the projection with
     * regard to the kept variables and variable order.
     */
    private void updateAtom(){
        List<Term> terms = new ArrayList<>();
        for (Integer index:outputIndexes){
            if (!terms.contains(atom.getTerms().get(index))) terms.add(atom.getTerms().get(index));
        }
        atom = new RelationalAtom(atom.getName(),terms);
    }

    /**
     * Method for reading the next tuple from the child operation and returning only the
     * specified projection variables.
     *
     * @return returns a tuple with only the terms specified by outputVars
     * @throws IOException throws an error if reading from file at a leaf node was unsuccessful
     */
    @Override
    public Tuple getNextTuple() throws IOException {
        Tuple nextTuple = child.getNextTuple();
        while (nextTuple != null) {
            List<Constant> keepFields = new ArrayList<>();

            for (Integer index:outputIndexes){
                keepFields.add(nextTuple.getFields().get(index));
            }

            Tuple keepTuple = new Tuple(keepFields);

            if (!outputBuffer.contains(keepTuple)) {
                outputBuffer.add(keepTuple);
                return keepTuple;
            } else {
                nextTuple  = child.getNextTuple();
            }
        }
        return null;
    }

    /**
     * Method for resetting the ProjectOperation. The next getNextTuple() call will start
     * reading at the beginning of the child operation output. The outputBuffer variable
     * is also reset.
     */
    @Override
    public void reset() {
        outputBuffer = new HashSet<>();
        child.reset();
    }

    /**
     * Method for retrieving the base RelationalAtom
     *
     * @return returns the base RelationalAtom
     */
    public RelationalAtom getAtom(){
        return this.atom;
    }
}
