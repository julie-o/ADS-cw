package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.utils.Tuple;
import ed.inf.adbs.minibase.utils.CompareUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class for select operations.
 */
public class SelectOperator extends Operator{
    /** Child operator */
    private final Operator child;
    /** List of condition atoms to select by */
    private final List<ComparisonAtom> comparisonAtomList;
    /** Base relational atom */
    private final RelationalAtom atom;

    /**
     * Constructor for the SelectOperator class.
     *
     * @param child child operation
     * @param comparisonAtomList list of condition atoms to select by
     * @param atom base relational atom
     */
    public SelectOperator(Operator child, List<ComparisonAtom> comparisonAtomList, RelationalAtom atom) {
        this.child = child;
        this.atom = atom;
        this.comparisonAtomList = removeIrrelevantComparisons(comparisonAtomList);
    }

    /**
     * Method to remove conditions that do not apply to this base relation (i.e. if no
     * variable in the comparison atom exists in this relation).
     *
     * @param comparisonAtomList list of condition atoms
     * @return returns a minimized list of condition atoms
     */
    private List<ComparisonAtom> removeIrrelevantComparisons(List<ComparisonAtom> comparisonAtomList){
        List<ComparisonAtom> addComparisons = new ArrayList<>();
        for (ComparisonAtom comp:comparisonAtomList){
            if (atom.getTerms().contains(comp.getTerm1()) || atom.getTerms().contains(comp.getTerm2())){
                addComparisons.add(comp);
            }
        }
        return addComparisons;
    }

    /**
     * Method for reading the next tuple from the child operation that matches all the
     * selection criteria.
     *
     * @return returns a tuple that matches the selection criteria or null if there is no results
     * @throws IOException throws an error if reading from file at a leaf node was unsuccessful
     */
    @Override
    public Tuple getNextTuple() throws IOException {
        Tuple nextTuple = child.getNextTuple();

        while (nextTuple != null) {
            if (match(nextTuple) && (constantMatch(nextTuple))) {
                return nextTuple;
            }
            nextTuple  = child.getNextTuple();
        }
        return null;
    }

    /**
     * Helper method for getNextTuple(). Checks whether the constants in the input tuple match
     * the constants in the base relational atom.
     *
     * @param tuple tuple to check condition for
     * @return returns true if all constants match or there are no constants
     */
    private boolean constantMatch(Tuple tuple){
        for (int i =0;i<atom.getTerms().size();i++){
            Term term = atom.getTerms().get(i);
            Constant field = tuple.getFields().get(i);
            if (term instanceof Constant) {
                if (term instanceof StringConstant && field instanceof StringConstant){
                    if (!Objects.equals(((StringConstant) term).getValue(), ((StringConstant) field).getValue())){
                        return false;
                    }
                } else if (term instanceof IntegerConstant && field instanceof IntegerConstant ) {
                    if (!Objects.equals(((IntegerConstant) term).getValue(), ((IntegerConstant) field).getValue())){
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method for checking whether a tuple matches the selection criteria. Iterates through
     * all criteria in the comparisonAtomList-variable.
     *
     * @param tuple a tuple to check criteria for
     * @return returns true if the tuple matches the criteria
     */
    private boolean match(Tuple tuple){
        for (ComparisonAtom comp:comparisonAtomList){
            if (!matchOneCondition(comp,tuple)){
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method for match(). Checks whether a Tuple passes single selection condition.
     *
     * @param comp selection condition
     * @param tuple tuple to check condition for
     * @return returns true if the tuple matches the condition
     */
    private boolean matchOneCondition(ComparisonAtom comp,Tuple tuple){
        if (atom.getTerms().size()!=tuple.getFields().size()) throw new IllegalArgumentException("Mismatch in matchOnePredicate");

        Constant tupleTerm1= null;
        if (comp.getTerm1() instanceof Variable){
            int index = atom.getTerms().indexOf(comp.getTerm1());
            if (index >= 0){
                tupleTerm1 = tuple.getFields().get(index);
            }
        } else {
            tupleTerm1 = (Constant) comp.getTerm1();
        }

        Constant tupleTerm2 = null;
        if (comp.getTerm2() instanceof Variable){
            int index = atom.getTerms().indexOf(comp.getTerm2());
            if (index >= 0){
                tupleTerm2 = tuple.getFields().get(index);
            }
        } else {
            tupleTerm2 = (Constant) comp.getTerm2();
        }

        if (tupleTerm1==null || tupleTerm2==null){
            return false;
        } else {
            return CompareUtil.evaluateComparison(tupleTerm1,tupleTerm2,comp.getOp());
        }
    }

    /**
     * Method for resetting the SelectOperation. The next getNextTuple() call will start
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
    public RelationalAtom getAtom(){
        return this.atom;
    }
}
