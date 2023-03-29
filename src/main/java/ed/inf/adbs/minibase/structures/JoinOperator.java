package ed.inf.adbs.minibase.structures;

import ed.inf.adbs.minibase.base.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinOperator extends Operator {
    private Operator childLeft;
    private Operator childRight;
    private List<ComparisonAtom> comparisonAtomList;
    private ComparisonAtom compareAtom;
    private Variable compareVar;

    private int leftIndex;
    private int rightIndex;

    private Tuple inner;
    private Tuple outer;

    public JoinOperator(Operator childLeft, Operator childRight, List<ComparisonAtom> comparisonAtomList){
        this.childLeft = childLeft;
        this.childRight = childRight;
        this.comparisonAtomList = comparisonAtomList;
        parseComparisons(comparisonAtomList);
    }

    private void parseComparisons(List<ComparisonAtom> comparisonAtomList){
        for (ComparisonAtom comp:comparisonAtomList){
            if (childLeft.getAtom().getTerms().contains(comp.getTerm1())
                    && childRight.getAtom().getTerms().contains(comp.getTerm2())) {
                compareAtom = comp;
                leftIndex = childLeft.getAtom().getTerms().indexOf(comp.getTerm1());
                rightIndex = childRight.getAtom().getTerms().indexOf(comp.getTerm2());
            } else if (childLeft.getAtom().getTerms().contains(comp.getTerm2())
                    && childRight.getAtom().getTerms().contains(comp.getTerm1())) {
                compareAtom = comp;
                leftIndex = childLeft.getAtom().getTerms().indexOf(comp.getTerm2());
                rightIndex = childRight.getAtom().getTerms().indexOf(comp.getTerm1());
            }
        }

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

    private boolean match(Tuple outer, Tuple inner){
        if (compareAtom==null){
            if (compareVar==null){
                return true;
            }
        }
        return outer.getFields().get(leftIndex).equals(inner.getFields().get(rightIndex));
    }

    @Override
    public void reset() {
        childLeft.reset();
        childRight.reset();
    }

    public RelationalAtom getAtom(){
        List<Term> atomTerms = new ArrayList<>();
        atomTerms.addAll(childLeft.getAtom().getTerms());
        atomTerms.addAll(childRight.getAtom().getTerms());
        return new RelationalAtom(childLeft.getAtom().getName(), atomTerms);
    }

    public ComparisonAtom getCompareAtom() {
        return compareAtom;
    }

    public Variable getCompareVar() {
        return compareVar;
    }

    public void setChildRight(Operator childRight) {
        this.childRight = childRight;
    }

    public void setChildLeft(Operator childLeft) {
        this.childLeft = childLeft;
    }

    public void reloadIndex(){
        parseComparisons(comparisonAtomList);
    }
}
