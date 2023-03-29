package ed.inf.adbs.minibase.structures;

import ed.inf.adbs.minibase.base.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectOperator extends Operator{
    Operator child;
    List<ComparisonAtom> comparisonAtomList;
    RelationalAtom atom;

    public SelectOperator(Operator child, List<ComparisonAtom> comparisonAtomList, RelationalAtom atom) {
        this.child = child;
        this.atom = atom;
        this.comparisonAtomList = removeIrrelevantComparisons(comparisonAtomList);
    }

    private List<ComparisonAtom> removeIrrelevantComparisons(List<ComparisonAtom> comparisonAtomList){
        List<ComparisonAtom> addComparisons = new ArrayList<>();
        for (ComparisonAtom comp:comparisonAtomList){
            if (atom.getTerms().contains(comp.getTerm1()) || atom.getTerms().contains(comp.getTerm2())){
                addComparisons.add(comp);
            }
        }
        return addComparisons;
    }

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

    private boolean constantMatch(Tuple tuple){
        for (int i =0;i<atom.getTerms().size();i++){
            Term term = atom.getTerms().get(i);
            Constant field = tuple.getFields().get(i);
            if (term instanceof Constant) {
                if (term instanceof StringConstant && field instanceof StringConstant){
                    if (((StringConstant) term).getValue() != ((StringConstant) field).getValue()){
                        return false;
                    }
                } else if (term instanceof IntegerConstant && field instanceof IntegerConstant ) {
                    if (((IntegerConstant) term).getValue() != ((IntegerConstant) field).getValue()){
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchOnePredicate(ComparisonAtom comp,Tuple tuple){
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
            return evaluateComparison(tupleTerm1,tupleTerm2,comp.getOp());
        }
    }

    private boolean evaluateComparison(Constant tupleTerm1,Constant tupleTerm2, ComparisonOperator cop){
        if (tupleTerm1.getClass()!=tupleTerm2.getClass()) return false;

        int comparisonResult;
        if (tupleTerm1.getClass().equals(IntegerConstant.class)) {
            comparisonResult = ((IntegerConstant) tupleTerm1).compareTo((IntegerConstant) tupleTerm2);
        } else if (tupleTerm1.getClass().equals(StringConstant.class)) {
            comparisonResult = ((StringConstant) tupleTerm1).compareTo((StringConstant) tupleTerm2);
        } else {
            throw new IllegalArgumentException("");
        }

        switch(cop) {
            case EQ:
                return comparisonResult==0;
            case NEQ:
                return comparisonResult!=0;
            case GT:
                return comparisonResult>0;
            case GEQ:
                return comparisonResult>=0;
            case LT:
                return comparisonResult<0;
            case LEQ:
                return comparisonResult<=0;
            default:
                throw new IllegalArgumentException("Unrecognized comparison operator");
        }
    }

    private boolean match(Tuple tuple){
        for (ComparisonAtom comp:comparisonAtomList){
            if (!matchOnePredicate(comp,tuple)){
                return false;
            }
        }
        return true;
    }

    @Override
    public void reset() {
        child.reset();
    }

    public RelationalAtom getAtom(){
        return this.atom;
    }
}
