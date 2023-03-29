package ed.inf.adbs.minibase.structures;

import ed.inf.adbs.minibase.base.*;

import java.io.IOException;
import java.util.*;

public class ProjectOperator extends Operator {

    private Set<Tuple> outputBuffer;
    private final Operator child;
    private final List<Variable> outputVars;
    private List<Integer> outputIndexes;
    private RelationalAtom atom;
    private String test;

    public ProjectOperator(Operator child, List<Variable> outputVars, RelationalAtom atom) {
        this.child = child;
        this.outputVars = outputVars;
        this.outputBuffer = new HashSet<>();
        this.atom = atom;
        outputIndexes();
        updateAtom();
    }

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

    private void updateAtom(){
        List<Term> terms = new ArrayList<>();

        for (Integer index:outputIndexes){
            if (!terms.contains(atom.getTerms().get(index))) terms.add(atom.getTerms().get(index));
        }

        atom = new RelationalAtom(atom.getName(),terms);
    }

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

    @Override
    public void reset() {
        outputBuffer = new HashSet<>();
        child.reset();
    }

    public RelationalAtom getAtom(){
        return this.atom;
    }
}
