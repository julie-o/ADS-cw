package ed.inf.adbs.minibase.structures;

import ed.inf.adbs.minibase.base.*;

import java.io.IOException;
import java.util.*;

public class SumOperator extends Operator {
    Operator child;
    SumAggregate agg;
    RelationalAtom atom;
    List<Variable> group;
    boolean block;
    List<Tuple> tuples;

    public SumOperator(Operator child, SumAggregate agg, RelationalAtom atom, List<Variable> group){
        this.child = child;
        this.agg = agg;
        this.atom = atom;
        this.group = group;
        this.block = false;
        this.tuples = new ArrayList<>();
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        if (block) return null;
        if (group.size()==0){
            List<Constant> sum = Collections.singletonList(new IntegerConstant(noGrouping()));
            block = true;
            return new Tuple(sum);
        } else {
            withGrouping();
            if (tuples.size()==0){
                return null;
            } else {
                Tuple tuple = tuples.get(0);
                tuples.remove(0);
                return tuple;
            }
        }
    }

    private void withGrouping() throws IOException {
        Map<List<Constant>,Integer> grouped = new HashMap<>();

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
                grouped.put(groupby,sumForRow(nextTuple));
            } else {
                grouped.put(groupby, oldValue+sumForRow(nextTuple));
            }

            nextTuple = child.getNextTuple();
        }

        for (List<Constant> key : grouped.keySet()){
            List<Constant> tupleTerms = new ArrayList<>(key);
            tupleTerms.add(new IntegerConstant(grouped.get(key)));
            tuples.add(new Tuple(tupleTerms));
        }
    }

    private int noGrouping() throws IOException {
        int sum = 0;

        Tuple nextTuple = child.getNextTuple();
        while (nextTuple != null) {
            sum = sum + sumForRow(nextTuple);
            nextTuple  = child.getNextTuple();
        }
        return sum;
    }

    private int sumForRow(Tuple tuple){
        int sumForRow = getValue(agg.getProductTerms().get(0),tuple);
        for (int i=1;i<agg.getProductTerms().size();i++){
            Term term = agg.getProductTerms().get(i);
            sumForRow = sumForRow * getValue(term,tuple);
        }
        return sumForRow;
    }

    public int getValue(Term term, Tuple tuple){
        if (term instanceof Variable) {
            int index = atom.getTerms().indexOf(term);
            if (index>0){
                if (!(tuple.getFields().get(index) instanceof IntegerConstant)) throw new IllegalArgumentException("Field is not an integer");
                IntegerConstant i = (IntegerConstant) tuple.getFields().get(index);
                return i.getValue();
            }
        } else if (term instanceof IntegerConstant){
            return ((IntegerConstant) term).getValue();
        }
        throw new IllegalArgumentException("Term not in tuple");
    }

    @Override
    public void reset() {
        child.reset();
    }

    @Override
    public RelationalAtom getAtom() {
        return atom;
    }
}
