package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.utils.Tuple;
import ed.inf.adbs.minibase.utils.CompareUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for join operations.
 */
public class JoinOperator extends Operator {
    /** Left child operator */
    private Operator childLeft;
    /** Right child operator */
    private Operator childRight;
    /** List of join conditions */
    private final List<ComparisonAtom> comparisonAtomList;
    /** Join condition for this join (join on explicit condition) */
    private ComparisonAtom compareAtom;
    /** Join condition for this join (join on variable) */
    private Variable compareVar;

    /** Index of condition variable in the childLeft operation base relation */
    private int leftIndex;
    /** Index of condition variable in the childRight operation base relation */
    private int rightIndex;

    /** Current inner tuple */
    private Tuple inner;
    /** Current outer tuple */
    private Tuple outer;

    /**
     * Constructor for the JoinOperator class. Sets variables and parses the join condition list.
     *
     * @param childLeft left child operator
     * @param childRight right child operator
     * @param comparisonAtomList list of join conditions (can include non-applicable)
     */
    public JoinOperator(Operator childLeft, Operator childRight, List<ComparisonAtom> comparisonAtomList){
        this.childLeft = childLeft;
        this.childRight = childRight;
        this.comparisonAtomList = comparisonAtomList;
        parseComparisons(comparisonAtomList);
    }

    /**
     * Method for finding the condition that applies to this join. If there is no explicit
     * condition ComparisonAtom that applies, the method checks if there is a suitable
     * variable to join on.
     *
     * @param comparisonAtomList list of join conditions
     */
    private void parseComparisons(List<ComparisonAtom> comparisonAtomList){
        // check join conditions
        for (ComparisonAtom comp:comparisonAtomList){
            if (childLeft.getAtom().getTerms().contains(comp.getTerm1())
                    && childRight.getAtom().getTerms().contains(comp.getTerm2())) {
                compareAtom = comp;
                leftIndex = childLeft.getAtom().getTerms().indexOf(comp.getTerm1());
                rightIndex = childRight.getAtom().getTerms().indexOf(comp.getTerm2());
            } else if (childLeft.getAtom().getTerms().contains(comp.getTerm2())
                    && childRight.getAtom().getTerms().contains(comp.getTerm1())) {
                leftIndex = childLeft.getAtom().getTerms().indexOf(comp.getTerm2());
                rightIndex = childRight.getAtom().getTerms().indexOf(comp.getTerm1());
                // swap condition so that variable for childLeft is on left and the variable for childRight is on the right
                compareAtom = new ComparisonAtom(comp.getTerm2(),comp.getTerm1(),CompareUtil.swapCompare(comp.getOp()));
            }
        }

        // if no join condition, check for a variable
        if (compareAtom == null){
            for (Term leftTerm:childLeft.getAtom().getTerms()){
                if (leftTerm instanceof Variable){
                    for (Term rightTerm:childRight.getAtom().getTerms()){
                        if (leftTerm.equals(rightTerm)) {
                            compareVar = (Variable) leftTerm;
                            leftIndex = childLeft.getAtom().getTerms().indexOf(compareVar);
                            rightIndex = childRight.getAtom().getTerms().indexOf(compareVar);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Method for getting the next Tuple from the joined child operations. Outer loop iterates
     * over childLeft and inner loop iterates over childRight. If childRight is null it is reset,
     * until childLeft also is null.
     *
     * @return returns a joined Tuple
     * @throws IOException throws an error if reading from file at a leaf node was unsuccessful
     */
    @Override
    public Tuple getNextTuple() throws IOException {
        if (outer != null && inner != null){
            inner = childRight.getNextTuple();
            while (inner != null) {
                if (match(outer,inner)) {
                    List<Constant> terms = new ArrayList<>();
                    terms.addAll(outer.getFields());
                    terms.addAll(inner.getFields());

                    return new Tuple(terms);
                }
                inner = childRight.getNextTuple();
            }
        }

        outer = childLeft.getNextTuple();
        while (outer != null) {
            childRight.reset();
            inner = childRight.getNextTuple();
            while (inner != null) {
                if (match(outer,inner)) {
                    List<Constant> terms = new ArrayList<>(outer.getFields());
                    terms.addAll(inner.getFields());
                    return new Tuple(terms);
                }
                inner = childRight.getNextTuple();
            }
            outer = childLeft.getNextTuple();
        }
        return null;
    }

    /**
     * Method for checking whether two Tuples can be joined. If both compareAtom and
     * compareVar are null, a cartesian product is necessary and the function returns true.
     *
     * @param outer Tuple from the outer relation (leftChild)
     * @param inner Tuple from the inner relation (rightChild)
     * @return returns true if the tuples should be joined and added to output
     */
    private boolean match(Tuple outer, Tuple inner){
        if (compareAtom!=null){
            return CompareUtil.evaluateComparison(outer.getFields().get(leftIndex), inner.getFields().get(rightIndex), compareAtom.getOp());
        } else if (compareVar!=null){
            return outer.getFields().get(leftIndex).equals(inner.getFields().get(rightIndex));
        }

        return true;
    }

    /**
     * Method for resetting the JoinOperation. The next getNextTuple() call will start
     * reading at the beginning of both child operation outputs.
     */
    @Override
    public void reset() {
        childLeft.reset();
        childRight.reset();
    }

    /**
     * Method for retrieving the base RelationalAtom. The base relational atom is the atom that
     * results from joining the vase relations from the child operations.
     *
     * @return returns the base RelationalAtom
     */
    public RelationalAtom getAtom(){
        List<Term> atomTerms = new ArrayList<>();
        atomTerms.addAll(childLeft.getAtom().getTerms());
        atomTerms.addAll(childRight.getAtom().getTerms());
        return new RelationalAtom(childLeft.getAtom().getName(), atomTerms);
    }

    /**
     * Getter for the comparison atom variable
     *
     * @return returns a ComparisonAtom
     */
    public ComparisonAtom getCompareAtom() {
        return compareAtom;
    }

    /**
     * Getter for the comparison variable
     *
     * @return returns a Variable
     */
    public Variable getCompareVar() {
        return compareVar;
    }

    /**
     * Getter for the right child operation
     *
     * @param childRight right child operation
     */
    public void setChildRight(Operator childRight) {
        this.childRight = childRight;
    }

    /**
     * Getter for the left child operation
     *
     * @param childLeft left child operation
     */
    public void setChildLeft(Operator childLeft) {
        this.childLeft = childLeft;
    }

    /**
     * Method for re-computing the index values of the variables that should be
     * compared. Used when pushing projection down the query plan tree.
     */
    public void reloadIndex(){
        parseComparisons(comparisonAtomList);
    }
}
